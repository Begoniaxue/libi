<template>
  <div class="reader-stats">
    <el-card shadow="never" class="filter-card">
      <div class="filter-bar">
        <div class="filter-item">
          <span class="filter-label">开始日期：</span>
          <el-date-picker
            v-model="startDate"
            type="date"
            placeholder="选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :clearable="false"
          />
        </div>
        <div class="filter-item">
          <span class="filter-label">结束日期：</span>
          <el-date-picker
            v-model="endDate"
            type="date"
            placeholder="选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :clearable="false"
          />
        </div>
        <el-button type="primary" @click="loadData">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button @click="resetFilter">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
        <div class="export-buttons">
          <el-button type="success" @click="handleExportExcel">
            <el-icon><Document /></el-icon>
            导出Excel
          </el-button>
          <el-button type="warning" @click="handleExportPdf">
            <el-icon><Picture /></el-icon>
            导出PDF
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#409EFF"><User /></el-icon>
            <div class="stat-info">
              <div class="stat-label">读者总数</div>
              <div class="stat-value">{{ stats.totalReaders || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#67C23A"><Avatar /></el-icon>
            <div class="stat-info">
              <div class="stat-label">活跃读者数</div>
              <div class="stat-value">{{ stats.activeReaders || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#E6A23C"><UserPlus /></el-icon>
            <div class="stat-info">
              <div class="stat-label">新增读者数</div>
              <div class="stat-value">{{ stats.newReaders || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#F56C6C"><Warning /></el-icon>
            <div class="stat-info">
              <div class="stat-label">有违规记录</div>
              <div class="stat-value">{{ stats.violationReaders || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600;">各年龄段读者分布</span>
          </template>
          <div ref="ageChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600;">各身份类型读者分布</span>
          </template>
          <div ref="identityChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600;">读者信用分布</span>
          </template>
          <div ref="creditChartRef" class="chart-container-bar"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600;">活跃读者TOP10</span>
          </template>
          <el-table :data="topActiveReaders" border stripe style="width: 100%;">
            <el-table-column type="index" label="排名" width="60" align="center">
              <template #default="{ $index }">
                <el-tag v-if="$index < 3" :type="getRankType($index)" effect="dark">
                  {{ $index + 1 }}
                </el-tag>
                <span v-else>{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="姓名" min-width="100" />
            <el-table-column prop="cardNo" label="借书证号" min-width="120" />
            <el-table-column prop="identityType" label="身份类型" min-width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.identityType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="borrowCount" label="借阅次数" min-width="100" align="center" />
            <el-table-column prop="lastBorrowDate" label="最近借阅" min-width="120" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600;">按身份类型的借阅偏好分析</span>
          </template>
          <el-table :data="borrowPreference" border stripe style="width: 100%;">
            <el-table-column prop="identityType" label="身份类型" min-width="100" fixed="left">
              <template #default="{ row }">
                <el-tag size="small" type="primary">{{ row.identityType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalBorrow" label="总借阅量" min-width="90" align="center" />
            <el-table-column prop="favoriteCategory" label="最喜爱类别" min-width="110">
              <template #default="{ row }">
                <el-tag size="small" type="success">{{ row.favoriteCategory }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="avgBorrowDays" label="平均借阅天数" min-width="120" align="center" />
            <el-table-column prop="returnRate" label="按时归还率" min-width="110" align="center">
              <template #default="{ row }">
                <el-progress 
                  :percentage="row.returnRate" 
                  :stroke-width="10"
                  :color="getProgressColor(row.returnRate)"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getReaderStatistics, exportReadersExcel, exportStatisticsPdf, downloadFile } from '@/api/statistics'

const ageChartRef = ref(null)
const identityChartRef = ref(null)
const creditChartRef = ref(null)

let ageChart = null
let identityChart = null
let creditChart = null

const startDate = ref(dayjs().subtract(30, 'day').format('YYYY-MM-DD'))
const endDate = ref(dayjs().format('YYYY-MM-DD'))

const stats = reactive({
  totalReaders: 0,
  activeReaders: 0,
  newReaders: 0,
  violationReaders: 0
})

const topActiveReaders = ref([])
const borrowPreference = ref([])

const ageDistribution = ref([])
const identityDistribution = ref([])
const creditDistribution = ref([])

const getRankType = (index) => {
  const types = ['danger', 'warning', 'success']
  return types[index] || 'info'
}

const getProgressColor = (percentage) => {
  if (percentage >= 95) return '#67C23A'
  if (percentage >= 85) return '#E6A23C'
  return '#F56C6C'
}

const initCharts = () => {
  if (ageChartRef.value) {
    ageChart = echarts.init(ageChartRef.value)
    ageChart.setOption(getPieOption('年龄段分布', ageDistribution.value, ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9b59b6']))
  }

  if (identityChartRef.value) {
    identityChart = echarts.init(identityChartRef.value)
    identityChart.setOption(getPieOption('身份类型分布', identityDistribution.value, ['#3498db', '#1abc9c', '#e74c3c', '#f39c12', '#2ecc71']))
  }

  if (creditChartRef.value) {
    creditChart = echarts.init(creditChartRef.value)
    creditChart.setOption(getBarOption())
  }
}

const getPieOption = (title, data, colors) => {
  return {
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
          }
        },
        data: data.map(item => ({
          name: item.name,
          value: item.value
        }))
      }
    ]
  }
}

const getBarOption = () => {
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: creditDistribution.value.map(item => item.name),
      axisLabel: {
        interval: 0,
        rotate: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '人数'
    },
    series: [
      {
        name: '读者数量',
        type: 'bar',
        barWidth: '50%',
        data: creditDistribution.value.map(item => ({
          value: item.value,
          itemStyle: {
            color: getCreditColor(item.name)
          }
        })),
        label: {
          show: true,
          position: 'top',
          fontWeight: 'bold'
        }
      }
    ]
  }
}

const getCreditColor = (level) => {
  const colorMap = {
    '优秀': '#67C23A',
    '良好': '#409EFF',
    '中等': '#E6A23C',
    '较差': '#F56C6C',
    '极差': '#909399'
  }
  return colorMap[level] || '#409EFF'
}

const resizeCharts = () => {
  ageChart?.resize()
  identityChart?.resize()
  creditChart?.resize()
}

const loadData = async () => {
  try {
    const res = await getReaderStatistics({
      startDate: startDate.value,
      endDate: endDate.value
    })
    const data = res.data

    Object.assign(stats, {
      totalReaders: data.totalReaders || 0,
      activeReaders: data.activeReaders || 0,
      newReaders: data.newReaders || 0,
      violationReaders: data.violationReaders || 0
    })

    ageDistribution.value = data.ageDistribution || []
    identityDistribution.value = data.identityDistribution || []
    creditDistribution.value = data.creditDistribution || []
    topActiveReaders.value = data.topActiveReaders || []
    borrowPreference.value = data.borrowPreference || []

    await nextTick()
    if (!ageChart) {
      initCharts()
    } else {
      ageChart.setOption(getPieOption('年龄段分布', ageDistribution.value, ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9b59b6']))
      identityChart.setOption(getPieOption('身份类型分布', identityDistribution.value, ['#3498db', '#1abc9c', '#e74c3c', '#f39c12', '#2ecc71']))
      creditChart.setOption(getBarOption())
    }
  } catch (error) {
    console.error('加载读者统计数据失败', error)
    ElMessage.error('加载数据失败')
  }
}

const resetFilter = () => {
  startDate.value = dayjs().subtract(30, 'day').format('YYYY-MM-DD')
  endDate.value = dayjs().format('YYYY-MM-DD')
  loadData()
}

const handleExportExcel = async () => {
  try {
    const res = await exportReadersExcel()
    const filename = `读者统计_${dayjs().format('YYYYMMDDHHmmss')}.xlsx`
    downloadFile(res, filename)
    ElMessage.success('Excel导出成功')
  } catch (error) {
    console.error('导出Excel失败', error)
    ElMessage.error('导出Excel失败')
  }
}

const handleExportPdf = async () => {
  try {
    const res = await exportStatisticsPdf('reader', {
      startDate: startDate.value,
      endDate: endDate.value
    })
    const filename = `读者统计报告_${dayjs().format('YYYYMMDDHHmmss')}.pdf`
    downloadFile(res, filename)
    ElMessage.success('PDF导出成功')
  } catch (error) {
    console.error('导出PDF失败', error)
    ElMessage.error('导出PDF失败')
  }
}

watch([startDate, endDate], () => {
  if (startDate.value && endDate.value && dayjs(startDate.value).isAfter(dayjs(endDate.value))) {
    ElMessage.warning('开始日期不能晚于结束日期')
  }
})

onMounted(() => {
  loadData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  ageChart?.dispose()
  identityChart?.dispose()
  creditChart?.dispose()
})
</script>

<style scoped>
.reader-stats {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 15px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.export-buttons {
  margin-left: auto;
  display: flex;
  gap: 10px;
}

.stats-cards {
  margin-bottom: 20px;
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

.charts-row {
  margin-bottom: 20px;
}

.charts-row .el-col {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.chart-container-bar {
  width: 100%;
  height: 350px;
}

@media screen and (max-width: 768px) {
  .reader-stats {
    padding: 10px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .export-buttons {
    margin-left: 0;
    justify-content: center;
  }

  .stat-value {
    font-size: 24px;
  }

  .chart-container,
  .chart-container-bar {
    height: 280px;
  }
}
</style>
