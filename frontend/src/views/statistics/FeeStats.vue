<template>
  <div class="fee-stats">
    <el-card shadow="never" style="margin-bottom: 20px;">
      <div class="filter-bar">
        <div class="filter-items">
          <span class="filter-label">时间段：</span>
          <el-date-picker
            v-model="startDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            style="width: 180px; margin-right: 10px;"
          />
          <span style="margin-right: 10px;">至</span>
          <el-date-picker
            v-model="endDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 180px; margin-right: 20px;"
          />
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
        <div class="export-buttons">
          <el-button type="success" @click="handleExportExcel">
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
          <el-button type="warning" @click="handleExportPdf">
            <el-icon><Document /></el-icon>
            导出PDF
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#F56C6C"><Warning /></el-icon>
            <div class="stat-info">
              <div class="stat-label">逾期罚款总额</div>
              <div class="stat-value danger">¥{{ formatAmount(feeData.overdueFineTotal) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#E6A23C"><Money /></el-icon>
            <div class="stat-info">
              <div class="stat-label">赔偿费用总额</div>
              <div class="stat-value warning">¥{{ formatAmount(feeData.compensationTotal) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#67C23A"><Wallet /></el-icon>
            <div class="stat-info">
              <div class="stat-label">总收入</div>
              <div class="stat-value success">¥{{ formatAmount(feeData.totalIncome) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#909399"><Tickets /></el-icon>
            <div class="stat-info">
              <div class="stat-label">欠费未结清金额</div>
              <div class="stat-value">¥{{ formatAmount(feeData.unpaidAmount) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#409EFF"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-label">欠费未结清单数</div>
              <div class="stat-value primary">{{ feeData.unpaidCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600;">费用类型分布</span>
          </template>
          <div ref="pieChartRef" class="pie-chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600;">费用概览</span>
          </template>
          <div class="stat-detail">
            <div class="detail-item">
              <span class="detail-label">逾期罚款笔数</span>
              <span class="detail-value danger">{{ feeData.overdueFineCount || 0 }}</span>
            </div>
            <el-divider />
            <div class="detail-item">
              <span class="detail-label">赔偿费用笔数</span>
              <span class="detail-value warning">{{ feeData.compensationCount || 0 }}</span>
            </div>
            <el-divider />
            <div class="detail-item">
              <span class="detail-label">已结清金额</span>
              <span class="detail-value success">¥{{ formatAmount(feeData.paidAmount) }}</span>
            </div>
            <el-divider />
            <div class="detail-item">
              <span class="detail-label">已结清单数</span>
              <span class="detail-value primary">{{ feeData.paidCount || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span style="font-weight: 600;">费用明细列表</span>
      </template>
      <el-table :data="feeDetailList" border stripe style="width: 100%;" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="读者信息" min-width="150">
          <template #default="{ row }">
            <div v-if="row.reader">
              <div style="font-weight: 600;">{{ row.reader.name }}</div>
              <div style="color: #909399; font-size: 12px;">{{ row.reader.cardNo }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="图书信息" min-width="180">
          <template #default="{ row }">
            <div v-if="row.book">
              <div style="font-weight: 600;">{{ row.book.name }}</div>
              <div style="color: #909399; font-size: 12px;">ISBN: {{ row.book.isbn }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="feeType" label="费用类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.feeType === 1 ? 'danger' : 'warning'">
              {{ row.feeType === 1 ? '逾期罚款' : '赔偿费用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="120">
          <template #default="{ row }">
            <span style="font-weight: 600;">¥{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已结清' : '未结清' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="generateDate" label="生成日期" width="120" />
        <el-table-column prop="payDate" label="支付日期" width="120">
          <template #default="{ row }">
            {{ row.payDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      </el-table>
      <el-pagination
        v-model:current-page="detailPagination.current"
        v-model:page-size="detailPagination.size"
        :page-sizes="[10, 20, 50]"
        :total="detailPagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span style="font-weight: 600;">欠费未结清列表</span>
      </template>
      <el-table :data="unpaidList" border stripe style="width: 100%;" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="读者信息" min-width="150">
          <template #default="{ row }">
            <div v-if="row.reader">
              <div style="font-weight: 600;">{{ row.reader.name }}</div>
              <div style="color: #909399; font-size: 12px;">{{ row.reader.cardNo }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="图书信息" min-width="180">
          <template #default="{ row }">
            <div v-if="row.book">
              <div style="font-weight: 600;">{{ row.book.name }}</div>
              <div style="color: #909399; font-size: 12px;">ISBN: {{ row.book.isbn }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="feeType" label="费用类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.feeType === 1 ? 'danger' : 'warning'">
              {{ row.feeType === 1 ? '逾期罚款' : '赔偿费用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="欠费金额(元)" width="140">
          <template #default="{ row }">
            <span style="font-weight: 600; color: #F56C6C;">¥{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="generateDate" label="生成日期" width="120" />
        <el-table-column prop="overdueDays" label="逾期天数" width="100">
          <template #default="{ row }">
            <span v-if="row.overdueDays" style="color: #F56C6C;">{{ row.overdueDays }}天</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      </el-table>
      <el-pagination
        v-model:current-page="unpaidPagination.current"
        v-model:page-size="unpaidPagination.size"
        :page-sizes="[10, 20, 50]"
        :total="unpaidPagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, Document, Warning, Money, Wallet, Tickets } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getFeeStatistics, exportFeeExcel, exportStatisticsPdf, downloadFile } from '@/api/statistics'

const loading = ref(false)
const pieChartRef = ref(null)
let pieChart = null

const startDate = ref(dayjs().subtract(30, 'day').format('YYYY-MM-DD'))
const endDate = ref(dayjs().format('YYYY-MM-DD'))

const feeData = ref({})
const feeDetailList = ref([])
const unpaidList = ref([])

const detailPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const unpaidPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getFeeStatistics({
      startDate: startDate.value,
      endDate: endDate.value,
      detailPage: detailPagination.current,
      detailSize: detailPagination.size,
      unpaidPage: unpaidPagination.current,
      unpaidSize: unpaidPagination.size
    })
    const data = res.data || {}
    feeData.value = data.summary || {}
    feeDetailList.value = data.feeDetails?.list || []
    detailPagination.total = data.feeDetails?.total || 0
    unpaidList.value = data.unpaidFees?.list || []
    unpaidPagination.total = data.unpaidFees?.total || 0
    nextTick(() => {
      initPieChart()
    })
  } catch (error) {
    console.error('加载费用统计数据失败', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  startDate.value = dayjs().subtract(30, 'day').format('YYYY-MM-DD')
  endDate.value = dayjs().format('YYYY-MM-DD')
  detailPagination.current = 1
  unpaidPagination.current = 1
  loadData()
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }
  const feeTypeDistribution = feeData.value.feeTypeDistribution || []
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}元 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [
      {
        name: '费用类型',
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
          formatter: '{b}\n{c}元'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: feeTypeDistribution.map(item => ({
          name: item.name,
          value: item.value,
          itemStyle: {
            color: item.name === '逾期罚款' ? '#F56C6C' : '#E6A23C'
          }
        }))
      }
    ]
  }
  pieChart.setOption(option)
}

const handleResize = () => {
  pieChart?.resize()
}

const handleExportExcel = async () => {
  try {
    const res = await exportFeeExcel({
      startDate: startDate.value,
      endDate: endDate.value
    })
    const filename = `费用统计_${dayjs().format('YYYYMMDDHHmmss')}.xlsx`
    downloadFile(res.data, filename)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出Excel失败', error)
    ElMessage.error('导出失败')
  }
}

const handleExportPdf = async () => {
  try {
    const res = await exportStatisticsPdf('fee', {
      startDate: startDate.value,
      endDate: endDate.value
    })
    const filename = `费用统计_${dayjs().format('YYYYMMDDHHmmss')}.pdf`
    downloadFile(res.data, filename)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出PDF失败', error)
    ElMessage.error('导出失败')
  }
}

watch([startDate, endDate], () => {
  detailPagination.current = 1
  unpaidPagination.current = 1
})

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.filter-items {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-label {
  font-weight: 600;
  color: #606266;
}

.export-buttons {
  display: flex;
  gap: 10px;
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
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-value.primary {
  color: #409EFF;
}

.stat-value.success {
  color: #67C23A;
}

.stat-value.warning {
  color: #E6A23C;
}

.stat-value.danger {
  color: #F56C6C;
}

.pie-chart {
  width: 100%;
  height: 350px;
}

.stat-detail {
  padding: 10px 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
}

.detail-label {
  color: #606266;
  font-size: 14px;
}

.detail-value {
  font-weight: bold;
  font-size: 18px;
  color: #303133;
}

.detail-value.primary {
  color: #409EFF;
}

.detail-value.success {
  color: #67C23A;
}

.detail-value.warning {
  color: #E6A23C;
}

.detail-value.danger {
  color: #F56C6C;
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .export-buttons {
    width: 100%;
    justify-content: flex-start;
  }
  
  .stat-value {
    font-size: 22px;
  }
}
</style>
