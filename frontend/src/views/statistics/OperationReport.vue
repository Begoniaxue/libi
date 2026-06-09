<template>
  <div class="operation-report">
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20" align="middle">
        <el-col :xs="24" :sm="12" :md="6">
          <el-form-item label="报告类型" label-width="80px">
            <el-radio-group v-model="reportType">
              <el-radio-button value="monthly">月度报告</el-radio-button>
              <el-radio-button value="yearly">年度报告</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-form-item label="年份" label-width="50px">
            <el-date-picker
              v-model="selectedYear"
              type="year"
              placeholder="选择年份"
              value-format="YYYY"
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" v-if="reportType === 'monthly'">
          <el-form-item label="月份" label-width="50px">
            <el-date-picker
              v-model="selectedMonth"
              type="month"
              placeholder="选择月份"
              value-format="MM"
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="action-buttons">
            <el-button type="primary" @click="generateReport" :loading="generating">
              <el-icon><Document /></el-icon>
              生成报告
            </el-button>
            <el-button type="success" @click="handleExportExcel" :loading="exportingExcel" :disabled="!reportData">
              <el-icon><Download /></el-icon>
              导出Excel
            </el-button>
            <el-button type="warning" @click="handleExportPdf" :loading="exportingPdf" :disabled="!reportData">
              <el-icon><Picture /></el-icon>
              导出PDF
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <div v-if="reportData" class="report-content">
      <el-card shadow="hover" class="report-header-card">
        <div class="report-header">
          <div class="report-title">
            <h2>{{ reportData.title }}</h2>
            <span class="generate-time">生成时间：{{ reportData.generateTime }}</span>
          </div>
        </div>
      </el-card>

      <el-collapse v-model="activeSections" class="report-collapse">
        <el-collapse-item name="overview">
          <template #title>
            <div class="collapse-title">
              <el-icon><DataAnalysis /></el-icon>
              <span>运营概览</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :xs="12" :sm="12" :md="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-item">
                  <div class="stat-icon icon-blue">
                    <el-icon size="26"><Reading /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">馆藏总量</div>
                    <div class="stat-value">{{ reportData.overview.totalBooks || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-item">
                  <div class="stat-icon icon-green">
                    <el-icon size="26"><Plus /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">新增图书</div>
                    <div class="stat-value">{{ reportData.overview.newBooks || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-item">
                  <div class="stat-icon icon-purple">
                    <el-icon size="26"><User /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">读者总数</div>
                    <div class="stat-value">{{ reportData.overview.totalReaders || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-item">
                  <div class="stat-icon icon-orange">
                    <el-icon size="26"><UserPlus /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">新增读者</div>
                    <div class="stat-value">{{ reportData.overview.newReaders || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-item">
                  <div class="stat-icon icon-cyan">
                    <el-icon size="26"><Tickets /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">总借阅量</div>
                    <div class="stat-value">{{ reportData.overview.totalBorrow || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-item">
                  <div class="stat-icon icon-teal">
                    <el-icon size="26"><CircleCheck /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">归还率</div>
                    <div class="stat-value">{{ formatPercent(reportData.overview.returnRate) }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-item">
                  <div class="stat-icon icon-red">
                    <el-icon size="26"><Warning /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">逾期率</div>
                    <div class="stat-value">{{ formatPercent(reportData.overview.overdueRate) }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="12" :md="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-item">
                  <div class="stat-icon icon-pink">
                    <el-icon size="26"><Wallet /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">总收入</div>
                    <div class="stat-value">¥{{ reportData.overview.totalIncome || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-collapse-item>

        <el-collapse-item name="collection">
          <template #title>
            <div class="collapse-title">
              <el-icon><Collection /></el-icon>
              <span>馆藏数据分析</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :xs="24" :lg="12">
              <el-card shadow="hover">
                <template #header>
                  <span style="font-weight: 600;">分类分布</span>
                </template>
                <div ref="categoryPieRef" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :xs="24" :lg="12">
              <el-card shadow="hover">
                <template #header>
                  <span style="font-weight: 600;">新增趋势</span>
                </template>
                <div ref="collectionTrendRef" class="chart-container"></div>
              </el-card>
            </el-col>
          </el-row>
        </el-collapse-item>

        <el-collapse-item name="borrow">
          <template #title>
            <div class="collapse-title">
              <el-icon><Tickets /></el-icon>
              <span>借阅数据分析</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :xs="24" :lg="12">
              <el-card shadow="hover">
                <template #header>
                  <span style="font-weight: 600;">借阅趋势</span>
                </template>
                <div ref="borrowTrendRef" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :xs="24" :lg="12">
              <el-card shadow="hover">
                <template #header>
                  <span style="font-weight: 600;">借阅率分析</span>
                </template>
                <div ref="borrowRateRef" class="chart-container"></div>
              </el-card>
            </el-col>
          </el-row>
        </el-collapse-item>

        <el-collapse-item name="reader">
          <template #title>
            <div class="collapse-title">
              <el-icon><User /></el-icon>
              <span>读者数据分析</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :xs="24" :lg="12">
              <el-card shadow="hover">
                <template #header>
                  <span style="font-weight: 600;">活跃度分布</span>
                </template>
                <div ref="activityRef" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :xs="24" :lg="12">
              <el-card shadow="hover">
                <template #header>
                  <span style="font-weight: 600;">借阅偏好</span>
                </template>
                <div ref="preferenceRef" class="chart-container"></div>
              </el-card>
            </el-col>
          </el-row>
        </el-collapse-item>

        <el-collapse-item name="hot">
          <template #title>
            <div class="collapse-title">
              <el-icon><TrendCharts /></el-icon>
              <span>热门资源分析</span>
            </div>
          </template>
          <el-card shadow="hover">
            <template #header>
              <span style="font-weight: 600;">热门图书TOP10</span>
            </template>
            <el-table :data="reportData.hotResources.topBooks || []" border stripe style="width: 100%;">
              <el-table-column type="index" label="排名" width="70" align="center">
                <template #default="{ $index }">
                  <el-tag v-if="$index < 3" :type="getRankType($index)" effect="dark">
                    {{ $index + 1 }}
                  </el-tag>
                  <span v-else>{{ $index + 1 }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="书名" min-width="200" />
              <el-table-column prop="author" label="作者" width="120" />
              <el-table-column prop="categoryName" label="分类" width="120" />
              <el-table-column prop="borrowCount" label="借阅次数" width="100" align="center" />
            </el-table>
          </el-card>
        </el-collapse-item>

        <el-collapse-item name="fee">
          <template #title>
            <div class="collapse-title">
              <el-icon><Wallet /></el-icon>
              <span>费用数据分析</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :xs="24" :lg="12">
              <el-card shadow="hover">
                <template #header>
                  <span style="font-weight: 600;">收入构成</span>
                </template>
                <div ref="incomePieRef" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :xs="24" :lg="12">
              <el-card shadow="hover">
                <template #header>
                  <span style="font-weight: 600;">费用明细</span>
                </template>
                <el-table :data="reportData.fee.breakdown || []" border stripe style="width: 100%;">
                  <el-table-column prop="type" label="费用类型" min-width="150">
                    <template #default="{ row }">
                      <el-tag :type="getFeeTypeColor(row.type)" size="small">{{ row.type }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="amount" label="金额(元)" width="120" align="center" />
                  <el-table-column prop="count" label="笔数" width="100" align="center" />
                  <el-table-column prop="ratio" label="占比" width="120">
                    <template #default="{ row }">
                      <el-progress :percentage="row.ratio || 0" :stroke-width="10" />
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </el-collapse-item>

        <el-collapse-item name="suggestions">
          <template #title>
            <div class="collapse-title">
              <el-icon><Lightbulb /></el-icon>
              <span>运营分析建议</span>
            </div>
          </template>
          <el-card shadow="hover">
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in reportData.suggestions"
                :key="index"
                :type="getSuggestionType(item.priority)"
                :icon="getSuggestionIcon(item.priority)"
              >
                <el-card shadow="hover" class="suggestion-card">
                  <div class="suggestion-header">
                    <el-tag :type="getSuggestionType(item.priority)" effect="dark">
                      {{ getPriorityText(item.priority) }}
                    </el-tag>
                    <span class="suggestion-category">{{ item.category }}</span>
                  </div>
                  <h4 class="suggestion-title">{{ item.title }}</h4>
                  <p class="suggestion-content">{{ item.content }}</p>
                  <div class="suggestion-action">
                    <span class="suggestion-expected">预期效果：{{ item.expectedEffect }}</span>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-collapse-item>
      </el-collapse>
    </div>

    <el-empty v-else-if="!generating" description="请选择报告类型和时间，点击生成报告按钮" style="margin-top: 100px;" />
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import {
  generateMonthlyReport,
  generateYearlyReport,
  downloadMonthlyReport,
  downloadYearlyReport,
  downloadFile
} from '@/api/statistics'

const reportType = ref('monthly')
const selectedYear = ref(dayjs().format('YYYY'))
const selectedMonth = ref(dayjs().format('MM'))
const generating = ref(false)
const exportingExcel = ref(false)
const exportingPdf = ref(false)
const reportData = ref(null)
const activeSections = ref(['overview', 'collection', 'borrow', 'reader', 'hot', 'fee', 'suggestions'])

const categoryPieRef = ref(null)
const collectionTrendRef = ref(null)
const borrowTrendRef = ref(null)
const borrowRateRef = ref(null)
const activityRef = ref(null)
const preferenceRef = ref(null)
const incomePieRef = ref(null)

let categoryPieChart = null
let collectionTrendChart = null
let borrowTrendChart = null
let borrowRateChart = null
let activityChart = null
let preferenceChart = null
let incomePieChart = null

const chartColors = {
  category: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#48b4bd'],
  trend: ['#409EFF', '#67C23A'],
  borrowRate: ['#E6A23C', '#F56C6C', '#909399'],
  activity: ['#67C23A', '#409EFF', '#E6A23C', '#F56C6C', '#909399'],
  preference: ['#9b59b6', '#3498db', '#1abc9c', '#f39c12', '#e74c3c'],
  income: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
}

const formatPercent = (value) => {
  if (value === null || value === undefined) return '0.00%'
  return (Number(value) * 100).toFixed(2) + '%'
}

const getRankType = (index) => {
  const types = ['danger', 'warning', 'success']
  return types[index] || 'info'
}

const getFeeTypeColor = (type) => {
  const colorMap = {
    '押金': 'primary',
    '滞纳金': 'danger',
    '工本费': 'success',
    '会员费': 'warning',
    '其他': 'info'
  }
  return colorMap[type] || 'info'
}

const getSuggestionType = (priority) => {
  const typeMap = { high: 'danger', medium: 'warning', low: 'info' }
  return typeMap[priority] || 'info'
}

const getSuggestionIcon = (priority) => {
  const iconMap = { high: 'Warning', medium: 'InfoFilled', low: 'ChatDotRound' }
  return iconMap[priority] || 'ChatDotRound'
}

const getPriorityText = (priority) => {
  const textMap = { high: '高优先级', medium: '中优先级', low: '低优先级' }
  return textMap[priority] || '建议'
}

const initCharts = () => {
  if (categoryPieRef.value && !categoryPieChart) {
    categoryPieChart = echarts.init(categoryPieRef.value)
  }
  if (collectionTrendRef.value && !collectionTrendChart) {
    collectionTrendChart = echarts.init(collectionTrendRef.value)
  }
  if (borrowTrendRef.value && !borrowTrendChart) {
    borrowTrendChart = echarts.init(borrowTrendRef.value)
  }
  if (borrowRateRef.value && !borrowRateChart) {
    borrowRateChart = echarts.init(borrowRateRef.value)
  }
  if (activityRef.value && !activityChart) {
    activityChart = echarts.init(activityRef.value)
  }
  if (preferenceRef.value && !preferenceChart) {
    preferenceChart = echarts.init(preferenceRef.value)
  }
  if (incomePieRef.value && !incomePieChart) {
    incomePieChart = echarts.init(incomePieRef.value)
  }
}

const getPieOption = (data, colors, title) => ({
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
  series: [{
    name: title,
    type: 'pie',
    radius: ['40%', '70%'],
    center: ['65%', '50%'],
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
      label: { show: true, fontSize: 16, fontWeight: 'bold' }
    },
    data: data
  }]
})

const getLineOption = (xData, series, colors, title) => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderColor: '#e4e7ed',
    borderWidth: 1,
    textStyle: { color: '#303133' }
  },
  legend: { data: series.map(s => s.name), top: 0 },
  grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: xData,
    axisLine: { lineStyle: { color: '#dcdfe6' } },
    axisLabel: { color: '#606266' }
  },
  yAxis: {
    type: 'value',
    axisLine: { lineStyle: { color: '#dcdfe6' } },
    axisLabel: { color: '#606266' },
    splitLine: { lineStyle: { color: '#f0f2f5' } }
  },
  series: series.map((s, i) => ({
    name: s.name,
    type: 'line',
    smooth: true,
    data: s.data,
    lineStyle: { width: 3, color: colors[i] },
    itemStyle: { color: colors[i] },
    areaStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: colors[i] + '4D' },
        { offset: 1, color: colors[i] + '0D' }
      ])
    }
  }))
})

const getBarOption = (xData, yData, colors, title) => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: xData,
    axisLabel: { interval: 0, rotate: 0 }
  },
  yAxis: { type: 'value' },
  series: [{
    name: title,
    type: 'bar',
    barWidth: '50%',
    data: yData.map((v, i) => ({
      value: v,
      itemStyle: { color: colors[i % colors.length] }
    })),
    label: { show: true, position: 'top', fontWeight: 'bold' }
  }]
})

const updateCharts = () => {
  if (!reportData.value) return

  const data = reportData.value

  if (categoryPieChart && data.collection?.categoryDistribution) {
    const pieData = data.collection.categoryDistribution.map(item => ({
      name: item.name,
      value: item.count
    }))
    categoryPieChart.setOption(getPieOption(pieData, chartColors.category, '分类分布'))
  }

  if (collectionTrendChart && data.collection?.newTrend) {
    const trend = data.collection.newTrend
    collectionTrendChart.setOption(getLineOption(
      trend.xData,
      [{ name: '新增图书', data: trend.yData }],
      ['#409EFF'],
      '新增趋势'
    ))
  }

  if (borrowTrendChart && data.borrow?.trend) {
    const trend = data.borrow.trend
    borrowTrendChart.setOption(getLineOption(
      trend.xData,
      [
        { name: '借阅量', data: trend.borrowData },
        { name: '归还量', data: trend.returnData }
      ],
      chartColors.trend,
      '借阅趋势'
    ))
  }

  if (borrowRateChart && data.borrow?.rateAnalysis) {
    const rate = data.borrow.rateAnalysis
    borrowRateChart.setOption(getBarOption(
      rate.categories,
      rate.rates,
      chartColors.borrowRate,
      '借阅率'
    ))
  }

  if (activityChart && data.reader?.activityDistribution) {
    const pieData = data.reader.activityDistribution.map(item => ({
      name: item.name,
      value: item.count
    }))
    activityChart.setOption(getPieOption(pieData, chartColors.activity, '活跃度分布'))
  }

  if (preferenceChart && data.reader?.borrowPreference) {
    const pref = data.reader.borrowPreference
    preferenceChart.setOption(getBarOption(
      pref.categories,
      pref.counts,
      chartColors.preference,
      '借阅偏好'
    ))
  }

  if (incomePieChart && data.fee?.incomeBreakdown) {
    const pieData = data.fee.incomeBreakdown.map(item => ({
      name: item.type,
      value: item.amount
    }))
    incomePieChart.setOption(getPieOption(pieData, chartColors.income, '收入构成'))
  }
}

const generateReport = async () => {
  if (!selectedYear.value) {
    ElMessage.warning('请选择年份')
    return
  }
  if (reportType.value === 'monthly' && !selectedMonth.value) {
    ElMessage.warning('请选择月份')
    return
  }

  generating.value = true
  try {
    let res
    if (reportType.value === 'monthly') {
      res = await generateMonthlyReport(selectedYear.value, selectedMonth.value)
    } else {
      res = await generateYearlyReport(selectedYear.value)
    }
    reportData.value = res.data

    await nextTick()
    initCharts()
    await nextTick()
    updateCharts()

    ElMessage.success('报告生成成功')
  } catch (error) {
    console.error('生成报告失败', error)
    ElMessage.error('生成报告失败')
  } finally {
    generating.value = false
  }
}

const handleExportExcel = async () => {
  exportingExcel.value = true
  try {
    let res
    let filename
    if (reportType.value === 'monthly') {
      res = await downloadMonthlyReport(selectedYear.value, selectedMonth.value, 'excel')
      filename = `月度运营报告_${selectedYear.value}${selectedMonth.value}.xlsx`
    } else {
      res = await downloadYearlyReport(selectedYear.value, 'excel')
      filename = `年度运营报告_${selectedYear.value}.xlsx`
    }
    downloadFile(res, filename)
    ElMessage.success('Excel导出成功')
  } catch (error) {
    console.error('Excel导出失败', error)
    ElMessage.error('Excel导出失败')
  } finally {
    exportingExcel.value = false
  }
}

const handleExportPdf = async () => {
  exportingPdf.value = true
  try {
    let res
    let filename
    if (reportType.value === 'monthly') {
      res = await downloadMonthlyReport(selectedYear.value, selectedMonth.value, 'pdf')
      filename = `月度运营报告_${selectedYear.value}${selectedMonth.value}.pdf`
    } else {
      res = await downloadYearlyReport(selectedYear.value, 'pdf')
      filename = `年度运营报告_${selectedYear.value}.pdf`
    }
    downloadFile(res, filename)
    ElMessage.success('PDF导出成功')
  } catch (error) {
    console.error('PDF导出失败', error)
    ElMessage.error('PDF导出失败')
  } finally {
    exportingPdf.value = false
  }
}

const handleResize = () => {
  categoryPieChart?.resize()
  collectionTrendChart?.resize()
  borrowTrendChart?.resize()
  borrowRateChart?.resize()
  activityChart?.resize()
  preferenceChart?.resize()
  incomePieChart?.resize()
}

watch(activeSections, async () => {
  await nextTick()
  handleResize()
}, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  categoryPieChart?.dispose()
  collectionTrendChart?.dispose()
  borrowTrendChart?.dispose()
  borrowRateChart?.dispose()
  activityChart?.dispose()
  preferenceChart?.dispose()
  incomePieChart?.dispose()
})
</script>

<style scoped>
.operation-report {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.report-content {
  margin-top: 20px;
}

.report-header-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.report-header {
  text-align: center;
  padding: 10px 0;
}

.report-title h2 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.generate-time {
  color: #909399;
  font-size: 14px;
}

.report-collapse {
  border: none;
}

.collapse-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.stat-card {
  border-radius: 8px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 52px;
  height: 52px;
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

.icon-orange {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.icon-cyan {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.icon-teal {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.icon-red {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.icon-pink {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.suggestion-card {
  margin-bottom: 10px;
}

.suggestion-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.suggestion-category {
  color: #909399;
  font-size: 13px;
}

.suggestion-title {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.suggestion-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 10px;
}

.suggestion-expected {
  color: #67C23A;
  font-size: 13px;
}

@media (max-width: 768px) {
  .operation-report {
    padding: 10px;
  }

  .action-buttons {
    justify-content: flex-start;
    margin-top: 10px;
  }

  .stat-value {
    font-size: 22px;
  }

  .chart-container {
    height: 280px;
  }

  .report-title h2 {
    font-size: 20px;
  }
}
</style>
