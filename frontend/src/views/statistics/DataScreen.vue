<template>
  <div class="data-screen">
    <div class="bg-particles"></div>
    <div class="bg-grid"></div>
    
    <header class="screen-header">
      <div class="header-decoration left"></div>
      <div class="header-content">
        <h1 class="main-title">
          <span class="title-icon">📚</span>
          图书馆运营数据大屏
          <span class="title-icon">📊</span>
        </h1>
        <div class="current-time">
          <el-icon size="18"><Timer /></el-icon>
          <span>{{ currentTime }}</span>
        </div>
      </div>
      <div class="header-decoration right"></div>
    </header>

    <section class="metrics-section">
      <div class="metric-card" v-for="(metric, index) in metrics" :key="index">
        <div class="metric-icon" :class="'icon-' + index">
          <el-icon :size="32"><component :is="metric.icon" /></el-icon>
        </div>
        <div class="metric-info">
          <div class="metric-label">{{ metric.label }}</div>
          <div class="metric-value">
            <span class="number-flip" :data-value="metric.value">
              {{ metric.displayValue }}
            </span>
            <span class="metric-unit" v-if="metric.unit">{{ metric.unit }}</span>
          </div>
        </div>
        <div class="metric-glow" :class="'glow-' + index"></div>
      </div>
    </section>

    <main class="main-content">
      <aside class="left-panel">
        <div class="chart-card">
          <div class="chart-title">
            <span class="title-bar"></span>
            各分类图书占比
          </div>
          <div ref="categoryChartRef" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div class="chart-title">
            <span class="title-bar"></span>
            各年龄段读者分布
          </div>
          <div ref="ageChartRef" class="chart-container"></div>
        </div>
      </aside>

      <section class="center-panel">
        <div class="chart-card large">
          <div class="chart-title">
            <span class="title-bar"></span>
            近30天借阅趋势
            <span class="legend-group">
              <span class="legend-item borrow"><i></i>借阅</span>
              <span class="legend-item return"><i></i>归还</span>
            </span>
          </div>
          <div ref="trendChartRef" class="chart-container large"></div>
        </div>
        <div class="chart-card large">
          <div class="chart-title">
            <span class="title-bar"></span>
            热门分类借阅排行
          </div>
          <div ref="hotCategoryChartRef" class="chart-container large"></div>
        </div>
      </section>

      <aside class="right-panel">
        <div class="chart-card">
          <div class="chart-title">
            <span class="title-bar"></span>
            热门图书 TOP10
          </div>
          <div class="ranking-container">
            <div class="ranking-list" ref="rankingListRef">
              <div 
                v-for="(book, index) in hotBooks" 
                :key="book.id" 
                class="ranking-item"
              >
                <div class="rank-num" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
                <div class="book-info">
                  <div class="book-name">{{ book.name }}</div>
                  <div class="book-meta">
                    <span class="author">{{ book.author }}</span>
                    <span class="category">{{ book.categoryName }}</span>
                  </div>
                </div>
                <div class="borrow-count">
                  <el-icon size="14" color="#409EFF"><TrendCharts /></el-icon>
                  {{ book.borrowCount }}
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="chart-card">
          <div class="chart-title">
            <span class="title-bar"></span>
            实时借阅动态
          </div>
          <div class="activity-container">
            <div class="activity-list" ref="activityListRef">
              <div 
                v-for="(activity, index) in realtimeActivities" 
                :key="activity.id || index"
                class="activity-item"
              >
                <div class="activity-avatar">
                  {{ activity.readerName?.charAt(0) || '读' }}
                </div>
                <div class="activity-content">
                  <div class="activity-text">
                    <span class="reader-name">{{ activity.readerName }}</span>
                    <span class="action">借阅了</span>
                    <span class="book-name">{{ activity.bookName }}</span>
                  </div>
                  <div class="activity-time">{{ activity.time }}</div>
                </div>
                <div class="activity-status" :class="activity.status">
                  {{ activity.statusText }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { 
  Reading, User, DocumentCopy, CircleCheck, Warning, Tickets, Timer, TrendCharts 
} from '@element-plus/icons-vue'
import { getAllStatistics } from '@/api/statistics'

const currentTime = ref('')
const categoryChartRef = ref(null)
const ageChartRef = ref(null)
const trendChartRef = ref(null)
const hotCategoryChartRef = ref(null)
const rankingListRef = ref(null)
const activityListRef = ref(null)

let categoryChart = null
let ageChart = null
let trendChart = null
let hotCategoryChart = null
let timeTimer = null
let rankingTimer = null
let activityTimer = null
let dataTimer = null

const metrics = reactive([
  { label: '馆藏总量', value: 0, displayValue: '0', icon: Reading, unit: '册' },
  { label: '读者总数', value: 0, displayValue: '0', icon: User, unit: '人' },
  { label: '今日借阅', value: 0, displayValue: '0', icon: DocumentCopy, unit: '册' },
  { label: '今日归还', value: 0, displayValue: '0', icon: CircleCheck, unit: '册' },
  { label: '逾期数量', value: 0, displayValue: '0', icon: Warning, unit: '册' },
  { label: '本月收入', value: 0, displayValue: '0', icon: Tickets, unit: '元' }
])

const hotBooks = ref([])
const realtimeActivities = ref([])

const chartColors = {
  primary: '#00d4ff',
  secondary: '#0066ff',
  success: '#00ff88',
  warning: '#ffaa00',
  danger: '#ff4757',
  purple: '#a855f7',
  pink: '#ec4899',
  cyan: '#06b6d4'
}

const darkTheme = {
  backgroundColor: 'transparent',
  textStyle: {
    color: '#b8c5d6'
  },
  title: {
    textStyle: {
      color: '#fff'
    }
  },
  legend: {
    textStyle: {
      color: '#b8c5d6'
    }
  },
  tooltip: {
    backgroundColor: 'rgba(0, 20, 40, 0.9)',
    borderColor: '#00d4ff',
    borderWidth: 1,
    textStyle: {
      color: '#fff'
    }
  },
  axisLine: {
    lineStyle: {
      color: '#1e3a5f'
    }
  },
  splitLine: {
    lineStyle: {
      color: 'rgba(30, 58, 95, 0.5)'
    }
  },
  axisLabel: {
    color: '#6b8cae'
  }
}

const updateTime = () => {
  currentTime.value = dayjs().format('YYYY年MM月DD日 HH:mm:ss')
}

const animateNumber = (target, duration = 1500) => {
  const start = target.displayValue === '0' ? 0 : parseInt(target.displayValue.replace(/,/g, ''))
  const end = target.value
  const startTime = performance.now()
  
  const update = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeProgress = 1 - Math.pow(1 - progress, 3)
    const currentValue = Math.floor(start + (end - start) * easeProgress)
    target.displayValue = currentValue.toLocaleString()
    
    if (progress < 1) {
      requestAnimationFrame(update)
    }
  }
  
  requestAnimationFrame(update)
}

const initCategoryChart = () => {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  
  const option = {
    ...darkTheme,
    tooltip: {
      ...darkTheme.tooltip,
      trigger: 'item',
      formatter: '{b}: {c}册 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: {
        color: '#b8c5d6',
        fontSize: 12
      },
      itemWidth: 10,
      itemHeight: 10
    },
    color: [
      '#00d4ff', '#0066ff', '#a855f7', '#ec4899', '#06b6d4',
      '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#0ea5e9'
    ],
    series: [{
      name: '图书分类',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#0a1628',
        borderWidth: 3
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold',
          color: '#fff'
        },
        itemStyle: {
          shadowBlur: 20,
          shadowColor: 'rgba(0, 212, 255, 0.5)'
        }
      },
      labelLine: {
        show: false
      },
      data: []
    }]
  }
  
  categoryChart.setOption(option)
}

const initAgeChart = () => {
  if (!ageChartRef.value) return
  ageChart = echarts.init(ageChartRef.value)
  
  const option = {
    ...darkTheme,
    tooltip: {
      ...darkTheme.tooltip,
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: {
        color: '#b8c5d6',
        fontSize: 12
      },
      itemWidth: 10,
      itemHeight: 10
    },
    color: ['#00d4ff', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'],
    series: [{
      name: '年龄段',
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['35%', '50%'],
      roseType: 'radius',
      itemStyle: {
        borderRadius: 6,
        borderColor: '#0a1628',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold',
          color: '#fff'
        },
        itemStyle: {
          shadowBlur: 20,
          shadowColor: 'rgba(0, 212, 255, 0.5)'
        }
      },
      labelLine: {
        show: false
      },
      data: []
    }]
  }
  
  ageChart.setOption(option)
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  
  const option = {
    ...darkTheme,
    tooltip: {
      ...darkTheme.tooltip,
      trigger: 'axis'
    },
    legend: {
      show: false
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: [],
      axisLine: {
        lineStyle: { color: '#1e3a5f' }
      },
      axisLabel: {
        color: '#6b8cae',
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: { color: '#1e3a5f' }
      },
      axisLabel: {
        color: '#6b8cae',
        fontSize: 11
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(30, 58, 95, 0.3)',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '借阅',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: [],
        lineStyle: {
          width: 3,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#0066ff' },
            { offset: 1, color: '#00d4ff' }
          ])
        },
        itemStyle: {
          color: '#00d4ff',
          borderColor: '#0a1628',
          borderWidth: 2
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 212, 255, 0.4)' },
            { offset: 1, color: 'rgba(0, 212, 255, 0.05)' }
          ])
        }
      },
      {
        name: '归还',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: [],
        lineStyle: {
          width: 3,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#10b981' },
            { offset: 1, color: '#00ff88' }
          ])
        },
        itemStyle: {
          color: '#00ff88',
          borderColor: '#0a1628',
          borderWidth: 2
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 255, 136, 0.3)' },
            { offset: 1, color: 'rgba(0, 255, 136, 0.05)' }
          ])
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

const initHotCategoryChart = () => {
  if (!hotCategoryChartRef.value) return
  hotCategoryChart = echarts.init(hotCategoryChartRef.value)
  
  const option = {
    ...darkTheme,
    tooltip: {
      ...darkTheme.tooltip,
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: [],
      axisLine: {
        lineStyle: { color: '#1e3a5f' }
      },
      axisLabel: {
        color: '#6b8cae',
        fontSize: 11,
        rotate: 20
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: { color: '#1e3a5f' }
      },
      axisLabel: {
        color: '#6b8cae',
        fontSize: 11
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(30, 58, 95, 0.3)',
          type: 'dashed'
        }
      }
    },
    series: [{
      type: 'bar',
      data: [],
      barWidth: '50%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#00d4ff' },
          { offset: 1, color: '#0066ff' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#00ff88' },
            { offset: 1, color: '#10b981' }
          ]),
          shadowBlur: 15,
          shadowColor: 'rgba(0, 212, 255, 0.5)'
        }
      }
    }]
  }
  
  hotCategoryChart.setOption(option)
}

const updateCharts = (data) => {
  if (categoryChart && data?.categoryStats) {
    const categoryData = data.categoryStats.map(item => ({
      value: item.count || item.value || 0,
      name: item.name
    }))
    categoryChart.setOption({
      series: [{ data: categoryData }]
    })
  }
  
  if (ageChart && data?.ageStats) {
    const ageData = data.ageStats.map(item => ({
      value: item.count || item.value || 0,
      name: item.name
    }))
    ageChart.setOption({
      series: [{ data: ageData }]
    })
  }
  
  if (trendChart && data?.trendStats) {
    const xData = data.trendStats.dates || []
    const borrowData = data.trendStats.borrowData || []
    const returnData = data.trendStats.returnData || []
    
    trendChart.setOption({
      xAxis: { data: xData },
      series: [
        { data: borrowData },
        { data: returnData }
      ]
    })
  }
  
  if (hotCategoryChart && data?.hotCategoryStats) {
    const categoryNames = data.hotCategoryStats.map(item => item.name)
    const categoryValues = data.hotCategoryStats.map(item => item.count || item.value || 0)
    
    hotCategoryChart.setOption({
      xAxis: { data: categoryNames },
      series: [{ data: categoryValues }]
    })
  }
}

const loadData = async () => {
  try {
    const res = await getAllStatistics()
    const data = res.data || {}
    
    if (data.dashboard) {
      const metricValues = [
        data.dashboard.totalBooks || 0,
        data.dashboard.totalReaders || 0,
        data.dashboard.todayBorrow || 0,
        data.dashboard.todayReturn || 0,
        data.dashboard.overdueCount || 0,
        data.dashboard.monthlyIncome || 0
      ]
      
      metrics.forEach((metric, index) => {
        metric.value = metricValues[index]
        animateNumber(metric)
      })
    }
    
    if (data.hotBooks?.list) {
      hotBooks.value = data.hotBooks.list.slice(0, 10)
    } else {
      hotBooks.value = generateMockHotBooks()
    }
    
    if (data.realtimeActivities) {
      realtimeActivities.value = data.realtimeActivities
    } else {
      realtimeActivities.value = generateMockActivities()
    }
    
    updateCharts(data)
  } catch (error) {
    console.error('加载统计数据失败', error)
    loadMockData()
  }
}

const loadMockData = () => {
  const mockMetrics = [12580, 3680, 156, 142, 23, 8560]
  metrics.forEach((metric, index) => {
    metric.value = mockMetrics[index]
    animateNumber(metric)
  })
  
  hotBooks.value = generateMockHotBooks()
  realtimeActivities.value = generateMockActivities()
  
  if (categoryChart) {
    categoryChart.setOption({
      series: [{
        data: [
          { value: 2580, name: '文学小说' },
          { value: 2100, name: '科技计算机' },
          { value: 1850, name: '历史传记' },
          { value: 1620, name: '经济管理' },
          { value: 1450, name: '教育学习' },
          { value: 1200, name: '艺术设计' },
          { value: 980, name: '生活休闲' },
          { value: 800, name: '其他' }
        ]
      }]
    })
  }
  
  if (ageChart) {
    ageChart.setOption({
      series: [{
        data: [
          { value: 820, name: '18岁以下' },
          { value: 1560, name: '18-25岁' },
          { value: 980, name: '26-40岁' },
          { value: 520, name: '41-60岁' },
          { value: 320, name: '60岁以上' }
        ]
      }]
    })
  }
  
  if (trendChart) {
    const dates = []
    const borrowData = []
    const returnData = []
    for (let i = 29; i >= 0; i--) {
      dates.push(dayjs().subtract(i, 'day').format('MM-DD'))
      borrowData.push(Math.floor(Math.random() * 80) + 100)
      returnData.push(Math.floor(Math.random() * 70) + 90)
    }
    trendChart.setOption({
      xAxis: { data: dates },
      series: [
        { data: borrowData },
        { data: returnData }
      ]
    })
  }
  
  if (hotCategoryChart) {
    hotCategoryChart.setOption({
      xAxis: {
        data: ['文学小说', '科技计算机', '历史传记', '经济管理', '教育学习', '艺术设计', '生活休闲', '哲学心理']
      },
      series: [{
        data: [1256, 1089, 956, 823, 756, 634, 512, 428]
      }]
    })
  }
}

const generateMockHotBooks = () => {
  const books = [
    { id: 1, name: '活着', author: '余华', categoryName: '文学', borrowCount: 328 },
    { id: 2, name: '三体', author: '刘慈欣', categoryName: '科幻', borrowCount: 295 },
    { id: 3, name: '百年孤独', author: '马尔克斯', categoryName: '文学', borrowCount: 267 },
    { id: 4, name: '人类简史', author: '赫拉利', categoryName: '历史', borrowCount: 245 },
    { id: 5, name: '红楼梦', author: '曹雪芹', categoryName: '古典', borrowCount: 223 },
    { id: 6, name: '西游记', author: '吴承恩', categoryName: '古典', borrowCount: 201 },
    { id: 7, name: '算法导论', author: 'Thomas', categoryName: '计算机', borrowCount: 189 },
    { id: 8, name: '经济学原理', author: '曼昆', categoryName: '经济', borrowCount: 176 },
    { id: 9, name: '心理学与生活', author: '津巴多', categoryName: '心理', borrowCount: 165 },
    { id: 10, name: '设计模式', author: 'GoF', categoryName: '计算机', borrowCount: 152 }
  ]
  return books
}

const generateMockActivities = () => {
  const names = ['张三', '李四', '王五', '赵六', '陈七', '刘八', '周九', '吴十', '郑十一', '孙十二']
  const bookNames = ['活着', '三体', '百年孤独', '人类简史', '红楼梦', '西游记', '围城', '平凡的世界', '小王子', '追风筝的人']
  const activities = []
  
  for (let i = 0; i < 20; i++) {
    activities.push({
      id: i,
      readerName: names[Math.floor(Math.random() * names.length)],
      bookName: bookNames[Math.floor(Math.random() * bookNames.length)],
      time: dayjs().subtract(Math.floor(Math.random() * 60), 'minute').format('HH:mm:ss'),
      status: Math.random() > 0.3 ? 'success' : 'warning',
      statusText: Math.random() > 0.3 ? '成功' : '预约'
    })
  }
  return activities.sort((a, b) => b.time.localeCompare(a.time))
}

const startRankingScroll = () => {
  if (rankingTimer) clearInterval(rankingTimer)
  
  rankingTimer = setInterval(() => {
    if (rankingListRef.value) {
      const container = rankingListRef.value
      const firstItem = container.querySelector('.ranking-item')
      if (firstItem) {
        const itemHeight = firstItem.offsetHeight
        container.style.transition = 'transform 0.5s ease'
        container.style.transform = `translateY(-${itemHeight}px)`
        
        setTimeout(() => {
          container.style.transition = 'none'
          container.appendChild(firstItem)
          container.style.transform = 'translateY(0)'
        }, 500)
      }
    }
  }, 3000)
}

const startActivityScroll = () => {
  if (activityTimer) clearInterval(activityTimer)
  
  activityTimer = setInterval(() => {
    if (activityListRef.value) {
      const container = activityListRef.value
      const firstItem = container.querySelector('.activity-item')
      if (firstItem) {
        const itemHeight = firstItem.offsetHeight
        container.style.transition = 'transform 0.5s ease'
        container.style.transform = `translateY(-${itemHeight}px)`
        
        setTimeout(() => {
          container.style.transition = 'none'
          container.appendChild(firstItem)
          container.style.transform = 'translateY(0)'
        }, 500)
      }
    }
  }, 2000)
}

const handleResize = () => {
  categoryChart?.resize()
  ageChart?.resize()
  trendChart?.resize()
  hotCategoryChart?.resize()
}

onMounted(() => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
  
  nextTick(() => {
    initCategoryChart()
    initAgeChart()
    initTrendChart()
    initHotCategoryChart()
    loadData()
    
    setTimeout(() => {
      startRankingScroll()
      startActivityScroll()
    }, 1000)
  })
  
  dataTimer = setInterval(loadData, 60000)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (timeTimer) clearInterval(timeTimer)
  if (rankingTimer) clearInterval(rankingTimer)
  if (activityTimer) clearInterval(activityTimer)
  if (dataTimer) clearInterval(dataTimer)
  
  window.removeEventListener('resize', handleResize)
  
  categoryChart?.dispose()
  ageChart?.dispose()
  trendChart?.dispose()
  hotCategoryChart?.dispose()
})
</script>

<style scoped>
.data-screen {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #020c1b 0%, #0a1628 50%, #0d2137 100%);
  position: fixed;
  top: 0;
  left: 0;
  overflow: hidden;
  color: #fff;
  font-family: 'Microsoft YaHei', -apple-system, sans-serif;
}

.bg-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle at 20% 30%, rgba(0, 212, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(168, 85, 247, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(0, 102, 255, 0.05) 0%, transparent 60%);
  pointer-events: none;
  animation: pulse 8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.bg-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(0, 212, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.screen-header {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 10;
}

.header-decoration {
  position: absolute;
  top: 50%;
  width: 25%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00d4ff, transparent);
  transform: translateY(-50%);
}

.header-decoration.left {
  left: 5%;
}

.header-decoration.right {
  right: 5%;
}

.header-content {
  text-align: center;
  padding: 0 40px;
}

.main-title {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(90deg, #00d4ff 0%, #fff 50%, #00d4ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: 4px;
  text-shadow: 0 0 30px rgba(0, 212, 255, 0.5);
  animation: titleGlow 3s ease-in-out infinite alternate;
}

@keyframes titleGlow {
  from { filter: drop-shadow(0 0 10px rgba(0, 212, 255, 0.5)); }
  to { filter: drop-shadow(0 0 25px rgba(0, 212, 255, 0.8)); }
}

.title-icon {
  margin: 0 15px;
  font-size: 28px;
}

.current-time {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
  font-size: 16px;
  color: #00d4ff;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
}

.metrics-section {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
  padding: 0 30px;
  margin-bottom: 20px;
  position: relative;
  z-index: 10;
}

.metric-card {
  background: linear-gradient(135deg, rgba(10, 22, 40, 0.9) 0%, rgba(13, 33, 55, 0.9) 100%);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.metric-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.1), transparent);
  transition: left 0.5s ease;
}

.metric-card:hover::before {
  left: 100%;
}

.metric-card:hover {
  border-color: rgba(0, 212, 255, 0.8);
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(0, 212, 255, 0.2);
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-0 {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.2) 0%, rgba(0, 102, 255, 0.2) 100%);
  color: #00d4ff;
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
}

.icon-1 {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(0, 255, 136, 0.2) 100%);
  color: #00ff88;
  box-shadow: 0 0 20px rgba(0, 255, 136, 0.3);
}

.icon-2 {
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.2) 0%, rgba(139, 92, 246, 0.2) 100%);
  color: #a855f7;
  box-shadow: 0 0 20px rgba(168, 85, 247, 0.3);
}

.icon-3 {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.2) 0%, rgba(244, 114, 182, 0.2) 100%);
  color: #ec4899;
  box-shadow: 0 0 20px rgba(236, 72, 153, 0.3);
}

.icon-4 {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.2) 0%, rgba(248, 113, 113, 0.2) 100%);
  color: #ef4444;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.3);
}

.icon-5 {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2) 0%, rgba(251, 191, 36, 0.2) 100%);
  color: #f59e0b;
  box-shadow: 0 0 20px rgba(245, 158, 11, 0.3);
}

.metric-info {
  flex: 1;
  min-width: 0;
}

.metric-label {
  font-size: 14px;
  color: #6b8cae;
  margin-bottom: 6px;
}

.metric-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.number-flip {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
}

.metric-unit {
  font-size: 14px;
  color: #6b8cae;
}

.metric-glow {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.3;
}

.glow-0 { background: #00d4ff; }
.glow-1 { background: #00ff88; }
.glow-2 { background: #a855f7; }
.glow-3 { background: #ec4899; }
.glow-4 { background: #ef4444; }
.glow-5 { background: #f59e0b; }

.main-content {
  display: grid;
  grid-template-columns: 1fr 1.5fr 1fr;
  gap: 20px;
  padding: 0 30px 20px;
  height: calc(100vh - 80px - 140px);
  position: relative;
  z-index: 10;
}

.left-panel,
.right-panel,
.center-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-card {
  background: linear-gradient(135deg, rgba(10, 22, 40, 0.9) 0%, rgba(13, 33, 55, 0.9) 100%);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.chart-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #00d4ff, transparent);
}

.chart-card.large {
  flex: 1;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-left: 12px;
  position: relative;
}

.title-bar {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, #00d4ff 0%, #0066ff 100%);
  border-radius: 2px;
  box-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
}

.legend-group {
  display: flex;
  gap: 20px;
  margin-left: auto;
  font-size: 13px;
  font-weight: normal;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #b8c5d6;
}

.legend-item i {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-item.borrow i {
  background: #00d4ff;
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.6);
}

.legend-item.return i {
  background: #00ff88;
  box-shadow: 0 0 8px rgba(0, 255, 136, 0.6);
}

.chart-container {
  flex: 1;
  min-height: 0;
}

.chart-container.large {
  height: 100%;
}

.ranking-container,
.activity-container {
  flex: 1;
  overflow: hidden;
  position: relative;
  mask-image: linear-gradient(180deg, transparent 0%, #000 10%, #000 90%, transparent 100%);
  -webkit-mask-image: linear-gradient(180deg, transparent 0%, #000 10%, #000 90%, transparent 100%);
}

.ranking-list,
.activity-list {
  display: flex;
  flex-direction: column;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
  transition: background 0.3s ease;
}

.ranking-item:hover {
  background: rgba(0, 212, 255, 0.05);
}

.rank-num {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
  background: rgba(107, 140, 174, 0.2);
  color: #6b8cae;
}

.rank-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
  color: #000;
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.5);
}

.rank-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #a0a0a0 100%);
  color: #000;
  box-shadow: 0 0 15px rgba(192, 192, 192, 0.4);
}

.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #b8860b 100%);
  color: #fff;
  box-shadow: 0 0 15px rgba(205, 127, 50, 0.4);
}

.book-info {
  flex: 1;
  min-width: 0;
}

.book-name {
  font-size: 14px;
  color: #fff;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
}

.book-meta .author {
  color: #6b8cae;
}

.book-meta .category {
  color: #00d4ff;
}

.borrow-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  color: #00d4ff;
  flex-shrink: 0;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
  transition: background 0.3s ease;
}

.activity-item:hover {
  background: rgba(0, 212, 255, 0.05);
}

.activity-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00d4ff 0%, #0066ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  font-size: 13px;
  color: #b8c5d6;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-text .reader-name {
  color: #00d4ff;
  font-weight: 500;
}

.activity-text .action {
  color: #6b8cae;
  margin: 0 4px;
}

.activity-text .book-name {
  color: #00ff88;
  font-weight: 500;
}

.activity-time {
  font-size: 11px;
  color: #6b8cae;
}

.activity-status {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

.activity-status.success {
  background: rgba(0, 255, 136, 0.15);
  color: #00ff88;
  border: 1px solid rgba(0, 255, 136, 0.3);
}

.activity-status.warning {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

@media (max-width: 1600px) {
  .main-title {
    font-size: 26px;
  }
  
  .metrics-section {
    gap: 15px;
  }
  
  .metric-card {
    padding: 15px;
  }
  
  .number-flip {
    font-size: 26px;
  }
  
  .metric-icon {
    width: 50px;
    height: 50px;
  }
}

@media (max-width: 1400px) {
  .metrics-section {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .main-content {
    grid-template-columns: 1fr 1fr;
  }
  
  .center-panel {
    grid-column: span 2;
    order: -1;
  }
}
</style>
