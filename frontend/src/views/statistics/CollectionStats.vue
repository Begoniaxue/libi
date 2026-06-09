<template>
  <div class="collection-stats">
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20" align="middle">
        <el-col :xs="24" :sm="12" :md="8">
          <el-form-item label="开始日期" label-width="80px">
            <el-date-picker
              v-model="startDate"
              type="date"
              placeholder="选择开始日期"
              value-format="YYYY-MM-DD"
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-form-item label="结束日期" label-width="80px">
            <el-date-picker
              v-model="endDate"
              type="date"
              placeholder="选择结束日期"
              value-format="YYYY-MM-DD"
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8">
          <div class="action-buttons">
            <el-button type="primary" @click="loadData">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="resetFilter">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
            <el-button type="success" @click="handleExportExcel">
              <el-icon><Download /></el-icon>
              Excel导出
            </el-button>
            <el-button type="warning" @click="handleExportPdf">
              <el-icon><Document /></el-icon>
              PDF导出
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#409EFF"><Reading /></el-icon>
            <div class="stat-info">
              <div class="stat-label">馆藏总量</div>
              <div class="stat-value">{{ stats.totalBooks || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#67C23A"><Collection /></el-icon>
            <div class="stat-info">
              <div class="stat-label">可借图书数</div>
              <div class="stat-value">{{ stats.availableBooks || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#E6A23C"><Tickets /></el-icon>
            <div class="stat-info">
              <div class="stat-label">馆藏总册数</div>
              <div class="stat-value">{{ stats.totalQuantity || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#F56C6C"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-label">可借册数</div>
              <div class="stat-value">{{ stats.availableQuantity || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#909399"><Plus /></el-icon>
            <div class="stat-info">
              <div class="stat-label">新增图书数</div>
              <div class="stat-value">{{ stats.newBooks || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#F56C6C"><Warning /></el-icon>
            <div class="stat-info">
              <div class="stat-label">呆滞图书数</div>
              <div class="stat-value">{{ stats.stagnantBooks || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600;">各分类图书占比</span>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600;">资源类型分布</span>
          </template>
          <div ref="typeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span style="font-weight: 600;">呆滞图书列表</span>
      </template>
      <el-table :data="stagnantBooks" border stripe style="width: 100%;" v-loading="loading">
        <el-table-column prop="id" label="图书ID" width="80" />
        <el-table-column prop="name" label="书名" min-width="180" />
        <el-table-column prop="isbn" label="ISBN" width="150" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="publisher" label="出版社" width="150" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="totalQuantity" label="总册数" width="100" />
        <el-table-column prop="availableQuantity" label="可借册数" width="100" />
        <el-table-column prop="lastBorrowDate" label="最后借阅日期" width="140">
          <template #default="{ row }">
            {{ row.lastBorrowDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="stagnantDays" label="呆滞天数" width="100">
          <template #default="{ row }">
            <el-tag type="danger">{{ row.stagnantDays || 0 }}天</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import {
  getCollectionStatistics,
  exportBooksExcel,
  exportStatisticsPdf,
  downloadFile
} from '@/api/statistics'

const startDate = ref(dayjs().subtract(30, 'day').format('YYYY-MM-DD'))
const endDate = ref(dayjs().format('YYYY-MM-DD'))
const loading = ref(false)
const stats = ref({})
const stagnantBooks = ref([])
const categoryChartRef = ref(null)
const typeChartRef = ref(null)
let categoryChart = null
let typeChart = null

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const categoryColors = [
  '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
  '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#48b4bd'
]

const typeColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']

const initCharts = () => {
  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
    categoryChart.setOption(getPieOption([], '图书分类', categoryColors))
  }
  if (typeChartRef.value) {
    typeChart = echarts.init(typeChartRef.value)
    typeChart.setOption(getPieOption([], '资源类型', typeColors))
  }
}

const getPieOption = (data, title, colors) => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    top: 'center'
  },
  color: colors,
  series: [
    {
      name: title,
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      data: data
    }
  ]
})

const loadData = async () => {
  if (startDate.value && endDate.value && dayjs(startDate.value).isAfter(dayjs(endDate.value))) {
    ElMessage.warning('开始日期不能晚于结束日期')
    return
  }
  loading.value = true
  try {
    const res = await getCollectionStatistics({
      startDate: startDate.value,
      endDate: endDate.value,
      page: pagination.current,
      size: pagination.size
    })
    stats.value = res.data || {}
    stagnantBooks.value = res.data?.stagnantBooks?.list || []
    pagination.total = res.data?.stagnantBooks?.total || 0

    await nextTick()
    updateCharts(res.data)
  } catch (error) {
    console.error('加载馆藏统计数据失败', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const updateCharts = (data) => {
  if (categoryChart && data?.categoryStats) {
    const categoryData = data.categoryStats.map(item => ({
      value: item.count,
      name: item.name
    }))
    categoryChart.setOption(getPieOption(categoryData, '图书分类', categoryColors))
  }
  if (typeChart && data?.typeStats) {
    const typeData = data.typeStats.map(item => ({
      value: item.count,
      name: item.name
    }))
    typeChart.setOption(getPieOption(typeData, '资源类型', typeColors))
  }
}

const resetFilter = () => {
  startDate.value = dayjs().subtract(30, 'day').format('YYYY-MM-DD')
  endDate.value = dayjs().format('YYYY-MM-DD')
  pagination.current = 1
  loadData()
}

const handleExportExcel = async () => {
  try {
    const res = await exportBooksExcel()
    const filename = `馆藏统计_${dayjs().format('YYYYMMDD')}.xlsx`
    downloadFile(res, filename)
    ElMessage.success('Excel导出成功')
  } catch (error) {
    console.error('Excel导出失败', error)
    ElMessage.error('导出失败')
  }
}

const handleExportPdf = async () => {
  try {
    const res = await exportStatisticsPdf('collection', {
      startDate: startDate.value,
      endDate: endDate.value
    })
    const filename = `馆藏统计报表_${dayjs().format('YYYYMMDD')}.pdf`
    downloadFile(res, filename)
    ElMessage.success('PDF导出成功')
  } catch (error) {
    console.error('PDF导出失败', error)
    ElMessage.error('导出失败')
  }
}

const handleResize = () => {
  categoryChart?.resize()
  typeChart?.resize()
}

onMounted(() => {
  initCharts()
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  categoryChart?.dispose()
  typeChart?.dispose()
})
</script>

<style scoped>
.filter-card {
  margin-bottom: 0;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.stat-card {
  border-radius: 8px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 350px;
}

@media (max-width: 768px) {
  .action-buttons {
    justify-content: flex-start;
    margin-top: 10px;
  }

  .stat-value {
    font-size: 24px;
  }

  .chart-container {
    height: 300px;
  }
}
</style>
