import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import FeeStats from '../FeeStats.vue'
import * as statisticsApi from '@/api/statistics'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

vi.mock('@/api/statistics', () => ({
  getFeeStatistics: vi.fn(),
  exportFeeExcel: vi.fn(),
  exportStatisticsPdf: vi.fn(),
  downloadFile: vi.fn()
}))

const mockFeeData = {
  summary: {
    overdueFineTotal: 150.5,
    compensationTotal: 200.0,
    totalIncome: 350.5,
    unpaidAmount: 80.0,
    unpaidCount: 3,
    overdueFineCount: 5,
    compensationCount: 2,
    paidAmount: 270.5,
    paidCount: 4,
    feeTypeDistribution: [
      { name: '逾期罚款', value: 150.5 },
      { name: '赔偿费用', value: 200.0 }
    ]
  },
  feeDetails: {
    list: [
      {
        id: 1,
        reader: { name: '张三', cardNo: 'R001' },
        book: { name: 'Java编程思想', isbn: '978-7-111-21382-6' },
        feeType: 1,
        amount: 50.0,
        status: 1,
        generateDate: '2026-06-01',
        payDate: '2026-06-05',
        remark: '逾期5天'
      },
      {
        id: 2,
        reader: { name: '李四', cardNo: 'R002' },
        book: { name: '深入理解计算机系统', isbn: '978-7-111-54493-7' },
        feeType: 2,
        amount: 100.0,
        status: 0,
        generateDate: '2026-06-10',
        payDate: null,
        remark: '图书损坏赔偿'
      }
    ],
    total: 2
  },
  unpaidFees: {
    list: [
      {
        id: 2,
        reader: { name: '李四', cardNo: 'R002' },
        book: { name: '深入理解计算机系统', isbn: '978-7-111-54493-7' },
        feeType: 2,
        amount: 100.0,
        generateDate: '2026-06-10',
        overdueDays: 5,
        remark: '图书损坏赔偿'
      }
    ],
    total: 1
  }
}

describe('FeeStats.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    statisticsApi.getFeeStatistics.mockResolvedValue({ data: mockFeeData })
  })

  describe('组件渲染', () => {
    it('应该正确渲染组件标题和筛选区域', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      expect(wrapper.find('.fee-stats').exists()).toBe(true)
      expect(wrapper.find('.filter-bar').exists()).toBe(true)
      expect(wrapper.text()).toContain('时间段：')
      expect(wrapper.text()).toContain('查询')
      expect(wrapper.text()).toContain('重置')
      expect(wrapper.text()).toContain('导出Excel')
      expect(wrapper.text()).toContain('导出PDF')
    })

    it('应该正确渲染统计卡片', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      const statCards = wrapper.findAll('.stat-card')
      expect(statCards.length).toBe(5)

      expect(wrapper.text()).toContain('逾期罚款总额')
      expect(wrapper.text()).toContain('赔偿费用总额')
      expect(wrapper.text()).toContain('总收入')
      expect(wrapper.text()).toContain('欠费未结清金额')
      expect(wrapper.text()).toContain('欠费未结清单数')
    })

    it('应该正确渲染图表和概览区域', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      expect(wrapper.text()).toContain('费用类型分布')
      expect(wrapper.text()).toContain('费用概览')
      expect(wrapper.find('.pie-chart').exists()).toBe(true)
      expect(wrapper.find('.stat-detail').exists()).toBe(true)
    })

    it('应该正确渲染费用明细和欠费列表表格', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      expect(wrapper.text()).toContain('费用明细列表')
      expect(wrapper.text()).toContain('欠费未结清列表')

      const tables = wrapper.findAll('.el-table')
      expect(tables.length).toBe(2)
    })
  })

  describe('formatAmount 函数', () => {
    it('应该正确格式化金额，保留两位小数', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      const { formatAmount } = wrapper.vm

      expect(formatAmount(100)).toBe('100.00')
      expect(formatAmount(150.5)).toBe('150.50')
      expect(formatAmount(0)).toBe('0.00')
      expect(formatAmount(null)).toBe('0.00')
      expect(formatAmount(undefined)).toBe('0.00')
      expect(formatAmount('50.5')).toBe('50.50')
    })
  })

  describe('loadData 数据加载', () => {
    it('应该在组件挂载时调用 loadData 并正确解析数据', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      expect(statisticsApi.getFeeStatistics).toHaveBeenCalledTimes(1)
      expect(statisticsApi.getFeeStatistics).toHaveBeenCalledWith(expect.objectContaining({
        startDate: expect.any(String),
        endDate: expect.any(String),
        detailPage: 1,
        detailSize: 10,
        unpaidPage: 1,
        unpaidSize: 10
      }))

      expect(wrapper.vm.feeData.overdueFineTotal).toBe(150.5)
      expect(wrapper.vm.feeData.compensationTotal).toBe(200.0)
      expect(wrapper.vm.feeData.totalIncome).toBe(350.5)
      expect(wrapper.vm.feeData.unpaidAmount).toBe(80.0)
      expect(wrapper.vm.feeData.unpaidCount).toBe(3)

      expect(wrapper.vm.feeDetailList.length).toBe(2)
      expect(wrapper.vm.detailPagination.total).toBe(2)

      expect(wrapper.vm.unpaidList.length).toBe(1)
      expect(wrapper.vm.unpaidPagination.total).toBe(1)
    })

    it('应该正确展示格式化后的金额数据', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      const html = wrapper.html()
      expect(html).toContain('¥150.50')
      expect(html).toContain('¥200.00')
      expect(html).toContain('¥350.50')
      expect(html).toContain('¥80.00')
    })

    it('应该在API返回空数据时正确处理', async () => {
      statisticsApi.getFeeStatistics.mockResolvedValue({ data: {} })

      const wrapper = mount(FeeStats)
      await flushPromises()

      expect(wrapper.vm.feeData).toEqual({})
      expect(wrapper.vm.feeDetailList).toEqual([])
      expect(wrapper.vm.unpaidList).toEqual([])
      expect(wrapper.vm.detailPagination.total).toBe(0)
      expect(wrapper.vm.unpaidPagination.total).toBe(0)
    })

    it('应该在API调用失败时显示错误消息', async () => {
      const mockError = new Error('Network error')
      statisticsApi.getFeeStatistics.mockRejectedValue(mockError)
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const wrapper = mount(FeeStats)
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('加载费用统计数据失败', mockError)
      expect(wrapper.vm.loading).toBe(false)

      consoleErrorSpy.mockRestore()
    })

    it('应该正确传递分页参数', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      wrapper.vm.detailPagination.current = 2
      wrapper.vm.detailPagination.size = 20
      wrapper.vm.unpaidPagination.current = 3
      wrapper.vm.unpaidPagination.size = 50

      await wrapper.vm.loadData()

      expect(statisticsApi.getFeeStatistics).toHaveBeenLastCalledWith(expect.objectContaining({
        detailPage: 2,
        detailSize: 20,
        unpaidPage: 3,
        unpaidSize: 50
      }))
    })
  })

  describe('resetFilter 筛选重置', () => {
    it('应该重置日期范围和分页到默认值', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      wrapper.vm.startDate = '2026-01-01'
      wrapper.vm.endDate = '2026-01-31'
      wrapper.vm.detailPagination.current = 5
      wrapper.vm.unpaidPagination.current = 5

      await wrapper.vm.resetFilter()

      expect(wrapper.vm.startDate).toBe(dayjs().subtract(30, 'day').format('YYYY-MM-DD'))
      expect(wrapper.vm.endDate).toBe(dayjs().format('YYYY-MM-DD'))
      expect(wrapper.vm.detailPagination.current).toBe(1)
      expect(wrapper.vm.unpaidPagination.current).toBe(1)
      expect(statisticsApi.getFeeStatistics).toHaveBeenCalled()
    })
  })

  describe('initPieChart 图表初始化', () => {
    it('应该初始化饼图并设置正确的配置', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      await wrapper.vm.$nextTick()

      expect(echarts.init).toHaveBeenCalled()
      expect(wrapper.vm.pieChart.setOption).toHaveBeenCalled()

      const setOptionCall = wrapper.vm.pieChart.setOption.mock.calls[0][0]
      expect(setOptionCall.series[0].type).toBe('pie')
      expect(setOptionCall.series[0].data.length).toBe(2)
      expect(setOptionCall.series[0].data[0].name).toBe('逾期罚款')
      expect(setOptionCall.series[0].data[0].value).toBe(150.5)
      expect(setOptionCall.series[0].data[1].name).toBe('赔偿费用')
      expect(setOptionCall.series[0].data[1].value).toBe(200.0)
    })

    it('应该设置正确的饼图颜色', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      await wrapper.vm.$nextTick()

      const setOptionCall = wrapper.vm.pieChart.setOption.mock.calls[0][0]
      expect(setOptionCall.series[0].data[0].itemStyle.color).toBe('#F56C6C')
      expect(setOptionCall.series[0].data[1].itemStyle.color).toBe('#E6A23C')
    })

    it('在没有图表引用时不初始化图表', async () => {
      const wrapper = mount(FeeStats, {
        attachTo: document.body
      })
      wrapper.vm.pieChartRef = null

      await flushPromises()
      wrapper.vm.initPieChart()

      expect(echarts.init).not.toHaveBeenCalled()
    })
  })

  describe('handleResize 响应式处理', () => {
    it('应该在窗口大小改变时调用图表resize', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()
      await wrapper.vm.$nextTick()

      window.dispatchEvent(new Event('resize'))

      expect(wrapper.vm.pieChart.resize).toHaveBeenCalled()
    })
  })

  describe('导出功能', () => {
    it('handleExportExcel 应该调用导出API并下载文件', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/vnd.ms-excel' })
      statisticsApi.exportFeeExcel.mockResolvedValue({ data: mockBlob })

      const wrapper = mount(FeeStats)
      await flushPromises()

      await wrapper.vm.handleExportExcel()

      expect(statisticsApi.exportFeeExcel).toHaveBeenCalledWith(expect.objectContaining({
        startDate: wrapper.vm.startDate,
        endDate: wrapper.vm.endDate
      }))
      expect(statisticsApi.downloadFile).toHaveBeenCalled()
    })

    it('handleExportPdf 应该调用导出API并下载文件', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/pdf' })
      statisticsApi.exportStatisticsPdf.mockResolvedValue({ data: mockBlob })

      const wrapper = mount(FeeStats)
      await flushPromises()

      await wrapper.vm.handleExportPdf()

      expect(statisticsApi.exportStatisticsPdf).toHaveBeenCalledWith('fee', expect.objectContaining({
        startDate: wrapper.vm.startDate,
        endDate: wrapper.vm.endDate
      }))
      expect(statisticsApi.downloadFile).toHaveBeenCalled()
    })
  })

  describe('数据展示验证', () => {
    it('费用明细表格应该正确显示categoryName之外的字段', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      const firstRow = wrapper.vm.feeDetailList[0]
      expect(firstRow.reader.name).toBe('张三')
      expect(firstRow.book.name).toBe('Java编程思想')
      expect(firstRow.feeType).toBe(1)
      expect(firstRow.amount).toBe(50.0)
      expect(firstRow.status).toBe(1)
    })

    it('欠费列表应该正确显示逾期天数', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      const firstUnpaid = wrapper.vm.unpaidList[0]
      expect(firstUnpaid.overdueDays).toBe(5)
      expect(firstUnpaid.amount).toBe(100.0)
    })

    it('费用概览应该显示正确的统计数据', async () => {
      const wrapper = mount(FeeStats)
      await flushPromises()

      const html = wrapper.html()
      expect(html).toContain('逾期罚款笔数')
      expect(html).toContain('赔偿费用笔数')
      expect(html).toContain('已结清金额')
      expect(html).toContain('已结清单数')
      expect(html).toContain('¥270.50')
    })
  })
})
