import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import HotResources from '../HotResources.vue'
import * as statisticsApi from '@/api/statistics'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

vi.mock('@/api/statistics', () => ({
  getHotResourcesStatistics: vi.fn(),
  exportBooksExcel: vi.fn(),
  exportStatisticsPdf: vi.fn(),
  downloadFile: vi.fn()
}))

const mockHotData = {
  hotBooks: [
    {
      id: 1,
      name: 'Java编程思想',
      author: 'Bruce Eckel',
      categoryName: '计算机技术',
      borrowCount: 120,
      isbn: '978-7-111-21382-6'
    },
    {
      id: 2,
      name: '深入理解计算机系统',
      author: 'Randal E.Bryant',
      categoryName: '计算机技术',
      borrowCount: 98,
      isbn: '978-7-111-54493-7'
    },
    {
      id: 3,
      name: '算法导论',
      author: 'Thomas H.Cormen',
      categoryName: '计算机技术',
      borrowCount: 85,
      isbn: '978-7-111-40701-0'
    }
  ],
  coldBooks: [
    {
      id: 101,
      name: '古代汉语',
      author: '王力',
      categoryName: '语言学',
      borrowCount: 2,
      totalQuantity: 10,
      isbn: '978-7-101-00082-5'
    },
    {
      id: 102,
      name: '概率论与数理统计',
      author: '盛骤',
      categoryName: '数学',
      borrowCount: 1,
      totalQuantity: 15,
      isbn: '978-7-04-023896-9'
    }
  ],
  categoryStats: [
    { category: '计算机技术', borrowCount: 350 },
    { category: '文学小说', borrowCount: 220 },
    { category: '历史传记', borrowCount: 180 },
    { category: '经济管理', borrowCount: 150 },
    { category: '自然科学', borrowCount: 100 }
  ]
}

describe('HotResources.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    statisticsApi.getHotResourcesStatistics.mockResolvedValue({ data: mockHotData })
  })

  describe('组件渲染', () => {
    it('应该正确渲染筛选表单区域', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      expect(wrapper.find('.hot-resources').exists()).toBe(true)
      expect(wrapper.find('.filter-form').exists()).toBe(true)
      expect(wrapper.text()).toContain('开始日期')
      expect(wrapper.text()).toContain('结束日期')
      expect(wrapper.text()).toContain('TOP数量')
      expect(wrapper.text()).toContain('查询')
      expect(wrapper.text()).toContain('重置')
      expect(wrapper.text()).toContain('Excel导出')
      expect(wrapper.text()).toContain('PDF导出')
    })

    it('应该正确渲染热门和冷门图书表格区域', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      expect(wrapper.text()).toContain('高频借阅图书 TOP10')
      expect(wrapper.text()).toContain('冷门图书清单')
      expect(wrapper.text()).toContain('热门分类资源分布')

      const tables = wrapper.findAll('.el-table')
      expect(tables.length).toBe(2)
    })

    it('应该正确渲染图表区域和切换按钮', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      expect(wrapper.find('.chart-container').exists()).toBe(true)
      expect(wrapper.text()).toContain('柱状图')
      expect(wrapper.text()).toContain('饼图')
    })
  })

  describe('getRankClass 排名样式函数', () => {
    it('应该为前三名返回正确的样式类名', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      const { getRankClass } = wrapper.vm

      expect(getRankClass(0)).toBe('rank-first')
      expect(getRankClass(1)).toBe('rank-second')
      expect(getRankClass(2)).toBe('rank-third')
      expect(getRankClass(3)).toBe('')
      expect(getRankClass(10)).toBe('')
    })
  })

  describe('loadData 数据加载', () => {
    it('应该在组件挂载时调用 loadData 并正确解析数据', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      expect(statisticsApi.getHotResourcesStatistics).toHaveBeenCalledTimes(1)
      expect(statisticsApi.getHotResourcesStatistics).toHaveBeenCalledWith(expect.objectContaining({
        startDate: expect.any(String),
        endDate: expect.any(String),
        topN: 10
      }))

      expect(wrapper.vm.hotBooks.length).toBe(3)
      expect(wrapper.vm.coldBooks.length).toBe(2)
      expect(wrapper.vm.categoryStats.length).toBe(5)
    })

    it('应该正确解析 categoryName 字段（核心修复验证）', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      const firstHotBook = wrapper.vm.hotBooks[0]
      expect(firstHotBook.categoryName).toBe('计算机技术')
      expect(firstHotBook.name).toBe('Java编程思想')
      expect(firstHotBook.author).toBe('Bruce Eckel')
      expect(firstHotBook.borrowCount).toBe(120)

      const firstColdBook = wrapper.vm.coldBooks[0]
      expect(firstColdBook.categoryName).toBe('语言学')
      expect(firstColdBook.totalQuantity).toBe(10)
    })

    it('应该在API返回空数据时正确处理', async () => {
      statisticsApi.getHotResourcesStatistics.mockResolvedValue({ data: {} })

      const wrapper = mount(HotResources)
      await flushPromises()

      expect(wrapper.vm.hotBooks).toEqual([])
      expect(wrapper.vm.coldBooks).toEqual([])
      expect(wrapper.vm.categoryStats).toEqual([])
    })

    it('应该验证开始日期不能晚于结束日期', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      wrapper.vm.filterForm.startDate = '2026-06-30'
      wrapper.vm.filterForm.endDate = '2026-06-01'

      vi.clearAllMocks()
      await wrapper.vm.loadData()

      expect(statisticsApi.getHotResourcesStatistics).not.toHaveBeenCalled()
    })

    it('应该在API调用失败时显示错误消息', async () => {
      const mockError = new Error('Network error')
      statisticsApi.getHotResourcesStatistics.mockRejectedValue(mockError)
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const wrapper = mount(HotResources)
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('加载统计数据失败', mockError)
      expect(wrapper.vm.loading).toBe(false)

      consoleErrorSpy.mockRestore()
    })

    it('应该正确传递topN参数', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      wrapper.vm.filterForm.topN = 20
      await wrapper.vm.loadData()

      expect(statisticsApi.getHotResourcesStatistics).toHaveBeenLastCalledWith(expect.objectContaining({
        topN: 20
      }))

      wrapper.vm.filterForm.topN = 50
      await wrapper.vm.loadData()

      expect(statisticsApi.getHotResourcesStatistics).toHaveBeenLastCalledWith(expect.objectContaining({
        topN: 50
      }))
    })
  })

  describe('resetFilter 筛选重置', () => {
    it('应该重置所有筛选条件到默认值', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      wrapper.vm.filterForm.startDate = '2026-01-01'
      wrapper.vm.filterForm.endDate = '2026-01-31'
      wrapper.vm.filterForm.topN = 50

      await wrapper.vm.resetFilter()

      expect(wrapper.vm.filterForm.startDate).toBe(dayjs().subtract(30, 'day').format('YYYY-MM-DD'))
      expect(wrapper.vm.filterForm.endDate).toBe(dayjs().format('YYYY-MM-DD'))
      expect(wrapper.vm.filterForm.topN).toBe(10)
      expect(statisticsApi.getHotResourcesStatistics).toHaveBeenCalled()
    })
  })

  describe('initChart 图表初始化', () => {
    it('应该初始化柱状图并设置正确的配置', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(echarts.init).toHaveBeenCalled()
      expect(wrapper.vm.chartInstance.setOption).toHaveBeenCalled()

      const setOptionCall = wrapper.vm.chartInstance.setOption.mock.calls[0][0]
      expect(setOptionCall.xAxis.type).toBe('category')
      expect(setOptionCall.yAxis.type).toBe('value')
      expect(setOptionCall.series[0].type).toBe('bar')
      expect(setOptionCall.series[0].data.length).toBe(5)
      expect(setOptionCall.series[0].data[0]).toBe(350)
      expect(setOptionCall.xAxis.data[0]).toBe('计算机技术')
    })

    it('应该在切换为饼图时重新初始化图表', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()
      await wrapper.vm.$nextTick()

      wrapper.vm.chartType = 'pie'
      await wrapper.vm.$nextTick()

      const setOptionCall = wrapper.vm.chartInstance.setOption.mock.calls[1][0]
      expect(setOptionCall.series[0].type).toBe('pie')
      expect(setOptionCall.series[0].data.length).toBe(5)
      expect(setOptionCall.series[0].data[0].name).toBe('计算机技术')
      expect(setOptionCall.series[0].data[0].value).toBe(350)
    })

    it('柱状图应该设置正确的颜色和样式', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()
      await wrapper.vm.$nextTick()

      const setOptionCall = wrapper.vm.chartInstance.setOption.mock.calls[0][0]
      expect(setOptionCall.series[0].itemStyle.borderRadius).toEqual([4, 4, 0, 0])
      expect(setOptionCall.series[0].label.show).toBe(true)
      expect(setOptionCall.series[0].label.position).toBe('top')
    })

    it('饼图应该设置正确的环形样式', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()
      await wrapper.vm.$nextTick()

      wrapper.vm.chartType = 'pie'
      await wrapper.vm.$nextTick()

      const setOptionCall = wrapper.vm.chartInstance.setOption.mock.calls[1][0]
      expect(setOptionCall.series[0].radius).toEqual(['40%', '70%'])
      expect(setOptionCall.series[0].itemStyle.borderRadius).toBe(10)
    })

    it('在没有图表引用时不初始化图表', async () => {
      const wrapper = mount(HotResources, {
        attachTo: document.body
      })
      wrapper.vm.chartRef = null

      await flushPromises()
      wrapper.vm.initChart()

      expect(echarts.init).not.toHaveBeenCalled()
    })
  })

  describe('handleResize 响应式处理', () => {
    it('应该在窗口大小改变时调用图表resize', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()
      await wrapper.vm.$nextTick()

      window.dispatchEvent(new Event('resize'))

      expect(wrapper.vm.chartInstance.resize).toHaveBeenCalled()
    })
  })

  describe('导出功能', () => {
    it('handleExportExcel 应该调用导出API并下载文件', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/vnd.ms-excel' })
      statisticsApi.exportBooksExcel.mockResolvedValue({ data: mockBlob })

      const wrapper = mount(HotResources)
      await flushPromises()

      expect(wrapper.vm.exportingExcel).toBe(false)
      const exportPromise = wrapper.vm.handleExportExcel()
      expect(wrapper.vm.exportingExcel).toBe(true)

      await exportPromise

      expect(statisticsApi.exportBooksExcel).toHaveBeenCalled()
      expect(statisticsApi.downloadFile).toHaveBeenCalled()
      expect(wrapper.vm.exportingExcel).toBe(false)
    })

    it('handleExportPdf 应该调用导出API并传递正确参数', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/pdf' })
      statisticsApi.exportStatisticsPdf.mockResolvedValue({ data: mockBlob })

      const wrapper = mount(HotResources)
      await flushPromises()

      wrapper.vm.filterForm.startDate = '2026-06-01'
      wrapper.vm.filterForm.endDate = '2026-06-30'
      wrapper.vm.filterForm.topN = 20

      await wrapper.vm.handleExportPdf()

      expect(statisticsApi.exportStatisticsPdf).toHaveBeenCalledWith('hot-resources', expect.objectContaining({
        startDate: '2026-06-01',
        endDate: '2026-06-30',
        topN: 20
      }))
      expect(statisticsApi.downloadFile).toHaveBeenCalled()
    })
  })

  describe('categoryName 字段修复验证（核心修复）', () => {
    it('热门图书表格数据应该包含 categoryName 字段而非 category', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      wrapper.vm.hotBooks.forEach(book => {
        expect(book).toHaveProperty('categoryName')
        expect(book.categoryName).toBeDefined()
        expect(book.category).toBeUndefined()
      })
    })

    it('冷门图书表格数据应该包含 categoryName 字段而非 category', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      wrapper.vm.coldBooks.forEach(book => {
        expect(book).toHaveProperty('categoryName')
        expect(book.categoryName).toBeDefined()
        expect(book.category).toBeUndefined()
      })
    })

    it('热门图书第一本的分类应该正确显示为"计算机技术"', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      const firstBook = wrapper.vm.hotBooks[0]
      expect(firstBook.name).toBe('Java编程思想')
      expect(firstBook.categoryName).toBe('计算机技术')
      expect(firstBook.borrowCount).toBe(120)
    })

    it('分类统计数据应该使用 category 字段', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      const firstCategory = wrapper.vm.categoryStats[0]
      expect(firstCategory.category).toBe('计算机技术')
      expect(firstCategory.borrowCount).toBe(350)
    })

    it('图表数据应该正确使用 categoryStats 的 category 和 borrowCount', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()
      await wrapper.vm.$nextTick()

      const setOptionCall = wrapper.vm.chartInstance.setOption.mock.calls[0][0]

      expect(setOptionCall.xAxis.data).toEqual([
        '计算机技术',
        '文学小说',
        '历史传记',
        '经济管理',
        '自然科学'
      ])
      expect(setOptionCall.series[0].data).toEqual([350, 220, 180, 150, 100])
    })
  })

  describe('数据展示验证', () => {
    it('热门图书表格应该显示正确的排名样式', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      expect(wrapper.vm.getRankClass(0)).toBe('rank-first')
      expect(wrapper.vm.getRankClass(1)).toBe('rank-second')
      expect(wrapper.vm.getRankClass(2)).toBe('rank-third')
    })

    it('冷门图书应该显示库存数量', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      const firstColdBook = wrapper.vm.coldBooks[0]
      expect(firstColdBook.totalQuantity).toBe(10)
      expect(firstColdBook.borrowCount).toBe(2)
    })

    it('TOP数量应该支持10、20、50三种选项', async () => {
      const wrapper = mount(HotResources)
      await flushPromises()

      const validTopN = [10, 20, 50]
      expect(validTopN).toContain(wrapper.vm.filterForm.topN)

      for (const n of validTopN) {
        wrapper.vm.filterForm.topN = n
        expect(wrapper.vm.filterForm.topN).toBe(n)
      }
    })
  })
})
