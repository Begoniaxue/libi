<template>
  <div class="hot-resources">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="filterForm.startDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            :clearable="false"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="filterForm.endDate"
            type="date"
            placeholder="选择结束日期"
            value-format="YYYY-MM-DD"
            :clearable="false"
          />
        </el-form-item>
        <el-form-item label="TOP数量">
          <el-select v-model="filterForm.topN" style="width: 120px;">
            <el-option label="TOP 10" :value="10" />
            <el-option label="TOP 20" :value="20" />
            <el-option label="TOP 50" :value="50" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
        <el-form-item class="export-buttons">
          <el-button type="success" @click="handleExportExcel" :loading="exportingExcel">
            <el-icon><Download /></el-icon>
            Excel导出
          </el-button>
          <el-button type="warning" @click="handleExportPdf" :loading="exportingPdf">
            <el-icon><Document /></el-icon>
            PDF导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span style="font-weight: 600;">
                <el-icon style="vertical-align: middle; margin-right: 5px;"><TrendCharts /></el-icon>
                高频借阅图书 TOP{{ filterForm.topN }}
              </span>
            </div>
          </template>
          <el-table :data="hotBooks" border stripe style="width: 100%;" max-height="500">
            <el-table-column label="排名" width="80" align="center">
              <template #default="{ $index }">
                <div :class="getRankClass($index)">
                  <span v-if="$index < 3" class="rank-badge">{{ $index + 1 }}</span>
                  <span v-else>{{ $index + 1 }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="图书名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
            <el-table-column prop="categoryName" label="分类" width="100" show-overflow-tooltip />
            <el-table-column prop="borrowCount" label="借阅次数" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="primary" effect="dark">{{ row.borrowCount }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="hotBooks.length === 0" description="暂无数据" style="padding: 40px 0;" />
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span style="font-weight: 600;">
                <el-icon style="vertical-align: middle; margin-right: 5px;"><ColdDrink /></el-icon>
                冷门图书清单
              </span>
            </div>
          </template>
          <el-table :data="coldBooks" border stripe style="width: 100%;" max-height="500">
            <el-table-column prop="name" label="图书名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
            <el-table-column prop="categoryName" label="分类" width="100" show-overflow-tooltip />
            <el-table-column prop="borrowCount" label="借阅次数" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="info" effect="plain">{{ row.borrowCount || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalQuantity" label="库存" width="80" align="center" />
          </el-table>
          <el-empty v-if="coldBooks.length === 0" description="暂无数据" style="padding: 40px 0;" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span style="font-weight: 600;">
                <el-icon style="vertical-align: middle; margin-right: 5px;"><Histogram /></el-icon>
                热门分类资源分布
              </span>
              <el-button-group>
                <el-button size="small" :type="chartType === 'bar' ? 'primary' : ''" @click="chartType = 'bar'">
                  柱状图
                </el-button>
                <el-button size="small" :type="chartType === 'pie' ? 'primary' : ''" @click="chartType = 'pie'">
                  饼图
                </el-button>
              </el-button-group>
            </div>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, Document, TrendCharts, ColdDrink, Histogram } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import {
  getHotResourcesStatistics,
  exportBooksExcel,
  exportStatisticsPdf,
  downloadFile
} from '@/api/statistics'

const loading = ref(false)
const exportingExcel = ref(false)
const exportingPdf = ref(false)
const chartRef = ref(null)
let chartInstance = null

const chartType = ref('bar')

const filterForm = reactive({
  startDate: dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
  endDate: dayjs().format('YYYY-MM-DD'),
  topN: 10
})

const hotBooks = ref([])
const coldBooks = ref([])
const categoryStats = ref([])

const getRankClass = (index) => {
  if (index === 0) return 'rank-first'
  if (index === 1) return 'rank-second'
  if (index === 2) return 'rank-third'
  return ''
}

const loadData = async () => {
  if (dayjs(filterForm.startDate).isAfter(dayjs(filterForm.endDate))) {
    ElMessage.warning('开始日期不能晚于结束日期')
    return
  }

  loading.value = true
  try {
    const res = await getHotResourcesStatistics({
      startDate: filterForm.startDate,
      endDate: filterForm.endDate,
      topN: filterForm.topN
    })
    const data = res.data || {}
    hotBooks.value = data.hotBooks || []
    coldBooks.value = data.coldBooks || []
    categoryStats.value = data.categoryStats || []

    await nextTick()
    initChart()
  } catch (error) {
    console.error('加载统计数据失败', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterForm.startDate = dayjs().subtract(30, 'day').format('YYYY-MM-DD')
  filterForm.endDate = dayjs().format('YYYY-MM-DD')
  filterForm.topN = 10
  loadData()
}

const initChart = () => {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const categories = categoryStats.value.map(item => item.category)
  const values = categoryStats.value.map(item => item.borrowCount)

  const colors = [
    '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
    '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#48b4bd'
  ]

  let option = {}

  if (chartType.value === 'bar') {
    option = {
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
        data: categories,
        axisLabel: {
          interval: 0,
          rotate: 30
        }
      },
      yAxis: {
        type: 'value',
        name: '借阅次数'
      },
      series: [
        {
          name: '借阅次数',
          type: 'bar',
          data: values,
          itemStyle: {
            color: function(params) {
              return colors[params.dataIndex % colors.length]
            },
            borderRadius: [4, 4, 0, 0]
          },
          barWidth: '50%',
          label: {
            show: true,
            position: 'top'
          }
        }
      ]
    }
  } else {
    option = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}次 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '分类借阅',
          type: 'pie',
          radius: ['40%', '70%'],
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
          data: categoryStats.value.map((item, index) => ({
            value: item.borrowCount,
            name: item.category,
            itemStyle: {
              color: colors[index % colors.length]
            }
          }))
        }
      ]
    }
  }

  chartInstance.setOption(option, true)
}

const handleExportExcel = async () => {
  exportingExcel.value = true
  try {
    const res = await exportBooksExcel()
    const filename = `热门资源统计_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`
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
    const res = await exportStatisticsPdf('hot-resources', {
      startDate: filterForm.startDate,
      endDate: filterForm.endDate,
      topN: filterForm.topN
    })
    const filename = `热门资源统计_${dayjs().format('YYYYMMDD_HHmmss')}.pdf`
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
  if (chartInstance) {
    chartInstance.resize()
  }
}

watch(chartType, () => {
  initChart()
})

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.hot-resources {
  padding: 20px;
}

.filter-card {
  margin-bottom: 0;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.export-buttons {
  margin-left: auto !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.rank-first .rank-badge {
  display: inline-block;
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffd700, #ffb800);
  color: #fff;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
}

.rank-second .rank-badge {
  display: inline-block;
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #c0c0c0, #a8a8a8);
  color: #fff;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(192, 192, 192, 0.4);
}

.rank-third .rank-badge {
  display: inline-block;
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #cd7f32, #b87333);
  color: #fff;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.4);
}

:deep(.el-table tbody tr) {
  transition: background-color 0.3s;
}

:deep(.el-table tbody tr:hover) {
  background-color: #f5f7fa !important;
}

@media (max-width: 768px) {
  .hot-resources {
    padding: 10px;
  }

  .filter-form {
    flex-direction: column;
    align-items: stretch;
  }

  .export-buttons {
    margin-left: 0 !important;
    justify-content: center;
  }

  .chart-container {
    height: 300px;
  }
}
</style>
