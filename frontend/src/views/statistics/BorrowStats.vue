<template>
  <div class="borrow-stats">
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <div class="filter-left">
          <span class="filter-label">时间范围：</span>
          <el-radio-group v-model="quickFilter" @change="handleQuickFilter">
            <el-radio-button value="today">今日</el-radio-button>
            <el-radio-button value="week">本周</el-radio-button>
            <el-radio-button value="month">本月</el-radio-button>
            <el-radio-button value="year">本年</el-radio-button>
            <el-radio-button value="custom">自定义</el-radio-button>
          </el-radio-group>
          <el-date-picker
            v-model="startDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            style="width: 140px; margin-left: 10px;"
            :disabled="quickFilter !== 'custom'"
            @change="handleDateChange"
          />
          <span class="date-separator">至</span>
          <el-date-picker
            v-model="endDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 140px;"
            :disabled="quickFilter !== 'custom'"
            @change="handleDateChange"
          />
          <el-button type="primary" @click="loadData" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
        <div class="filter-right">
          <el-button type="success" @click="handleExportExcel" :loading="exportingExcel">
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
          <el-button type="warning" @click="handleExportPdf" :loading="exportingPdf">
            <el-icon><Document /></el-icon>
            导出PDF
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon icon-blue">
              <el-icon size="28"><Reading /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">总借阅量</div>
              <div class="stat-value">{{ stats.totalBorrow || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon icon-green">
              <el-icon size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">总归还量</div>
              <div class="stat-value">{{ stats.totalReturn || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon icon-purple">
              <el-icon size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">归还率</div>
              <div class="stat-value">{{ formatPercent(stats.returnRate) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon icon-red">
              <el-icon size="28"><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">逾期数</div>
              <div class="stat-value">{{ stats.overdueCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon icon-orange">
              <el-icon size="28"><Timer /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">逾期率</div>
              <div class="stat-value">{{ formatPercent(stats.overdueRate) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon icon-cyan">
              <el-icon size="28"><Refresh /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">续借数</div>
              <div class="stat-value">{{ stats.renewCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon icon-pink">
              <el-icon size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">续借率</div>
              <div class="stat-value">{{ formatPercent(stats.renewRate) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon icon-teal">
              <el-icon size="28"><Tickets /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">预约成功率</div>
              <div class="stat-value">{{ formatPercent(stats.reservationSuccessRate) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="24" :md="16" :lg="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span style="font-weight: 600;">借阅趋势</span>
              <el-radio-group v-model="trendType" size="small" @change="loadTrendChart">
                <el-radio-button value="day">日</el-radio-button>
                <el-radio-button value="week">周</el-radio-button>
                <el-radio-button value="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600;">时段借阅统计</span>
          </template>
          <div class="period-stats">
            <div class="period-item">
              <div class="period-label">今日借阅</div>
              <div class="period-value">{{ periodStats.todayBorrow || 0 }}</div>
            </div>
            <el-divider />
            <div class="period-item">
              <div class="period-label">本周借阅</div>
              <div class="period-value">{{ periodStats.weekBorrow || 0 }}</div>
            </div>
            <el-divider />
            <div class="period-item">
              <div class="period-label">本月借阅</div>
              <div class="period-value">{{ periodStats.monthBorrow || 0 }}</div>
            </div>
            <el-divider />
            <div class="period-item">
              <div class="period-label">本年借阅</div>
              <div class="period-value">{{ periodStats.yearBorrow || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, Document, Reading, CircleCheck, Warning, RefreshRight, List, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getBorrowStatistics, exportBorrowExcel, exportStatisticsPdf, downloadFile } from '@/api/statistics'

const loading = ref(false)
const exportingExcel = ref(false)
const exportingPdf = ref(false)
const quickFilter = ref('month')
const startDate = ref('')
const endDate = ref('')
const trendType = ref('day')
const trendChartRef = ref(null)
let trendChart = null

const stats = reactive({
  totalBorrow: 0,
  totalReturn: 0,
  returnRate: 0,
  overdueCount: 0,
  overdueRate: 0,
  renewCount: 0,
  renewRate: 0,
  reservationSuccessRate: 0
})

const periodStats = reactive({
  todayBorrow: 0,
  weekBorrow: 0,
  monthBorrow: 0,
  yearBorrow: 0
})

const dailyBorrows = ref([])
const dailyReturns = ref([])
const weeklyBorrows = ref([])
const monthlyBorrows = ref([])

const formatPercent = (value) => {
  if (value === null || value === undefined) return '0.00%'
  return Number(value).toFixed(2) + '%'
}

const handleQuickFilter = (value) => {
  if (value === 'custom') {
    startDate.value = ''
    endDate.value = ''
    return
  }
  const today = dayjs()
  switch (value) {
    case 'today':
      startDate.value = today.format('YYYY-MM-DD')
      endDate.value = today.format('YYYY-MM-DD')
      break
    case 'week':
      startDate.value = today.startOf('week').format('YYYY-MM-DD')
      endDate.value = today.endOf('week').format('YYYY-MM-DD')
      break
    case 'month':
      startDate.value = today.startOf('month').format('YYYY-MM-DD')
      endDate.value = today.endOf('month').format('YYYY-MM-DD')
      break
    case 'year':
      startDate.value = today.startOf('year').format('YYYY-MM-DD')
      endDate.value = today.endOf('year').format('YYYY-MM-DD')
      break
  }
}

const handleDateChange = () => {
  if (startDate.value && endDate.value) {
    quickFilter.value = 'custom'
  }
}

const resetFilter = () => {
  quickFilter.value = 'month'
  handleQuickFilter('month')
  loadData()
}

const loadData = async () => {
  if (!startDate.value || !endDate.value) {
    ElMessage.warning('请选择时间范围')
    return
  }
  if (dayjs(startDate.value).isAfter(dayjs(endDate.value))) {
    ElMessage.warning('开始日期不能大于结束日期')
    return
  }
  loading.value = true
  try {
    const res = await getBorrowStatistics({
      startDate: startDate.value,
      endDate: endDate.value
    })
    if (res.data) {
      Object.assign(stats, res.data)
      if (res.data.periodStats) {
        Object.assign(periodStats, res.data.periodStats)
      }
      dailyBorrows.value = res.data.dailyBorrows || []
      dailyReturns.value = res.data.dailyReturns || []
      weeklyBorrows.value = res.data.weeklyBorrows || []
      monthlyBorrows.value = res.data.monthlyBorrows || []
      loadTrendChart()
    }
  } catch (error) {
    console.error('加载借阅统计数据失败', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  trendChart?.resize()
}

const loadTrendChart = () => {
  if (!trendChart) {
    nextTick(() => {
      initTrendChart()
      renderTrendChart()
    })
  } else {
    renderTrendChart()
  }
}

const renderTrendChart = () => {
  if (!trendChart) return
  const { xData, borrowData, returnData } = generateTrendData()
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: {
        color: '#303133'
      }
    },
    legend: {
      data: ['借阅量', '归还量'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData,
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      axisLabel: {
        color: '#606266'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      axisLabel: {
        color: '#606266'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f2f5'
        }
      }
    },
    series: [
      {
        name: '借阅量',
        type: 'line',
        smooth: true,
        data: borrowData,
        lineStyle: {
          width: 3,
          color: '#409EFF'
        },
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      },
      {
        name: '归还量',
        type: 'line',
        smooth: true,
        data: returnData,
        lineStyle: {
          width: 3,
          color: '#67C23A'
        },
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        }
      }
    ]
  }
  trendChart.setOption(option)
}

const generateTrendData = () => {
  const xData = []
  const borrowData = []
  const returnData = []
  const start = dayjs(startDate.value)
  const end = dayjs(endDate.value)
  
  const borrowMap = new Map()
  const returnMap = new Map()
  
  if (trendType.value === 'day') {
    dailyBorrows.value.forEach(item => {
      const date = dayjs(item.date).format('YYYY-MM-DD')
      borrowMap.set(date, item.count || 0)
    })
    dailyReturns.value.forEach(item => {
      const date = dayjs(item.date).format('YYYY-MM-DD')
      returnMap.set(date, item.count || 0)
    })
    
    let current = start.clone()
    while (current.isBefore(end) || current.isSame(end, 'day')) {
      const dateKey = current.format('YYYY-MM-DD')
      xData.push(current.format('MM-DD'))
      borrowData.push(borrowMap.get(dateKey) || 0)
      returnData.push(returnMap.get(dateKey) || 0)
      current = current.add(1, 'day')
    }
  } else if (trendType.value === 'week') {
    weeklyBorrows.value.forEach(item => {
      borrowMap.set(item.week, item.count || 0)
    })
    
    let current = start.clone().startOf('week')
    const endWeek = end.clone().endOf('week')
    while (current.isBefore(endWeek) || current.isSame(endWeek, 'week')) {
      const weekKey = current.format('YYYY-MM-DD')
      xData.push(current.format('MM-DD'))
      borrowData.push(borrowMap.get(weekKey) || 0)
      returnData.push(0)
      current = current.add(1, 'week')
    }
  } else if (trendType.value === 'month') {
    monthlyBorrows.value.forEach(item => {
      borrowMap.set(item.month, item.count || 0)
    })
    
    let current = start.clone().startOf('month')
    const endMonth = end.clone().endOf('month')
    while (current.isBefore(endMonth) || current.isSame(endMonth, 'month')) {
      const monthKey = current.format('YYYY-MM')
      xData.push(current.format('YYYY-MM'))
      borrowData.push(borrowMap.get(monthKey) || 0)
      returnData.push(0)
      current = current.add(1, 'month')
    }
  }
  return { xData, borrowData, returnData }
}

const handleExportExcel = async () => {
  if (!startDate.value || !endDate.value) {
    ElMessage.warning('请选择时间范围')
    return
  }
  exportingExcel.value = true
  try {
    const res = await exportBorrowExcel({
      startDate: startDate.value,
      endDate: endDate.value
    })
    const filename = `借阅统计_${startDate.value}_${endDate.value}.xlsx`
    downloadFile(res, filename)
    ElMessage.success('Excel导出成功')
  } catch (error) {
    console.error('导出Excel失败', error)
    ElMessage.error('导出Excel失败')
  } finally {
    exportingExcel.value = false
  }
}

const handleExportPdf = async () => {
  if (!startDate.value || !endDate.value) {
    ElMessage.warning('请选择时间范围')
    return
  }
  exportingPdf.value = true
  try {
    const res = await exportStatisticsPdf('borrow', {
      startDate: startDate.value,
      endDate: endDate.value
    })
    const filename = `借阅统计_${startDate.value}_${endDate.value}.pdf`
    downloadFile(res, filename)
    ElMessage.success('PDF导出成功')
  } catch (error) {
    console.error('导出PDF失败', error)
    ElMessage.error('导出PDF失败')
  } finally {
    exportingPdf.value = false
  }
}

onMounted(() => {
  handleQuickFilter(quickFilter.value)
  nextTick(() => {
    initTrendChart()
    loadData()
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
})
</script>

<style scoped>
.borrow-stats {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.filter-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.filter-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-label {
  font-weight: 500;
  color: #606266;
}

.date-separator {
  color: #909399;
  margin: 0 5px;
}

.filter-right {
  display: flex;
  gap: 10px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.icon-blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.icon-green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.icon-purple {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}

.icon-red {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.icon-orange {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.icon-cyan {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.icon-pink {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
}

.icon-teal {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 380px;
}

.period-stats {
  padding: 10px 0;
}

.period-item {
  text-align: center;
  padding: 15px 0;
}

.period-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.period-value {
  font-size: 32px;
  font-weight: 700;
  color: #409EFF;
}

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-right {
    width: 100%;
    justify-content: flex-start;
  }
  
  .stat-value {
    font-size: 22px;
  }
  
  .period-value {
    font-size: 26px;
  }
  
  .chart-container {
    height: 280px;
  }
}

@media (max-width: 480px) {
  .borrow-stats {
    padding: 10px;
  }
  
  .filter-left {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
  }
  
  .stat-value {
    font-size: 20px;
  }
}
</style>
