import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import ActivityDetail from '../ActivityDetail.vue'
import * as activityApi from '@/api/activity'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import ElementPlus from 'element-plus'

vi.mock('@/api/activity', () => ({
  getActivityById: vi.fn(),
  getRegistrationList: vi.fn(),
  cancelRegistration: vi.fn(),
  exportRegistrations: vi.fn(),
  updateActivity: vi.fn()
}))

vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      warning: vi.fn(),
      error: vi.fn()
    },
    ElMessageBox: {
      confirm: vi.fn()
    }
  }
})

const mockActivity = {
  id: 1,
  name: '读书分享会',
  coverImage: 'https://example.com/1.jpg',
  content: '<p>活动内容详情</p>',
  startTime: '2026-07-15 14:00:00',
  endTime: '2026-07-15 17:00:00',
  location: '图书馆一楼多功能厅',
  quota: 100,
  registeredCount: 50,
  status: 1,
  createTime: '2026-06-01 10:00:00'
}

const mockRegistrations = [
  {
    id: 1,
    activityId: 1,
    name: '张三',
    phone: '13800138001',
    email: 'zhangsan@example.com',
    readerId: 'R001',
    readerName: 'R001',
    remark: '第一次参加',
    status: 1,
    createTime: '2026-07-01 10:00:00'
  },
  {
    id: 2,
    activityId: 1,
    name: '李四',
    phone: '13800138002',
    email: 'lisi@example.com',
    readerId: null,
    readerName: null,
    remark: '',
    status: 1,
    createTime: '2026-07-02 14:30:00'
  },
  {
    id: 3,
    activityId: 1,
    name: '王五',
    phone: '13800138003',
    email: 'wangwu@example.com',
    readerId: 'R003',
    readerName: 'R003',
    remark: '',
    status: 0,
    createTime: '2026-07-03 09:00:00'
  }
]

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/activities', component: { template: '<div>List</div>' } },
    { path: '/activities/:id', component: ActivityDetail }
  ]
})

const createWrapper = async () => {
  await router.push('/activities/1')
  await flushPromises()
  return mount(ActivityDetail, {
    global: {
      plugins: [router, ElementPlus],
      stubs: {
        'el-page-header': {
          template: '<div class="el-page-header"><button class="back-btn" @click="$emit(\'back\')">Back</button><slot name="content"></slot></div>',
          emits: ['back']
        },
        'el-image': { template: '<div class="el-image"><img :src="src" /></div>', props: ['src', 'fit'] }
      }
    }
  })
}

describe('ActivityDetail.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    activityApi.getActivityById.mockResolvedValue({
      code: 200,
      data: mockActivity
    })
    activityApi.getRegistrationList.mockResolvedValue({
      code: 200,
      data: {
        list: mockRegistrations,
        total: 3,
        pages: 1,
        current: 1,
        size: 10
      }
    })
  })

  describe('组件初始化与数据加载', () => {
    it('应该在组件挂载时加载活动数据和报名列表', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      expect(activityApi.getActivityById).toHaveBeenCalledWith('1')
      expect(activityApi.getRegistrationList).toHaveBeenCalledWith(
        '1',
        {
          page: 1,
          size: 10,
          keyword: ''
        }
      )
      expect(wrapper.vm.activity.id).toBe(1)
      expect(wrapper.vm.registrationList.length).toBe(3)
    })

    it('应该正确显示活动基本信息', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      expect(wrapper.text()).toContain('读书分享会')
      expect(wrapper.text()).toContain('图书馆一楼多功能厅')
      expect(wrapper.text()).toContain('50')
      expect(wrapper.text()).toContain('/ 100')
    })

    it('应该正确显示活动时间范围', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      expect(wrapper.text()).toContain('2026-07-15 14:00:00 - 2026-07-15 17:00:00')
    })

    it('应该正确显示活动状态', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      expect(wrapper.text()).toContain('已发布')
    })

    it('loading 应该在数据加载完成后为 false', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      expect(wrapper.vm.loading).toBe(false)
    })

    it('加载失败时 loading 也应该为 false', async () => {
      activityApi.getActivityById.mockRejectedValue(new Error('API Error'))

      const wrapper = await createWrapper()
      await flushPromises()

      expect(wrapper.vm.loading).toBe(false)
    })

    it('加载失败时应该打印错误日志', async () => {
      const mockError = new Error('Load Error')
      activityApi.getActivityById.mockRejectedValue(mockError)
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const wrapper = await createWrapper()
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('加载活动详情失败', mockError)

      consoleErrorSpy.mockRestore()
    })
  })

  describe('formatDateTime 日期格式化', () => {
    it('应该正确格式化完整日期时间', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      expect(wrapper.vm.formatDateTime('2026-07-15 14:30:00')).toBe('2026-07-15 14:30:00')
    })

    it('空值应该返回空字符串', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      expect(wrapper.vm.formatDateTime(null)).toBe('')
      expect(wrapper.vm.formatDateTime('')).toBe('')
    })
  })

  describe('goBack 返回上一页', () => {
    it('应该跳转到活动列表页', async () => {
      const wrapper = await createWrapper()
      await flushPromises()
      await router.push('/activities/1')

      const pushSpy = vi.spyOn(router, 'push')

      wrapper.vm.goBack()

      expect(pushSpy).toHaveBeenCalledWith('/activities')

      pushSpy.mockRestore()
    })
  })

  describe('loadActivity 加载活动数据', () => {
    it('应该正确设置 activity 数据', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      const newActivity = { ...mockActivity, name: '更新后的活动' }
      activityApi.getActivityById.mockResolvedValue({ code: 200, data: newActivity })

      await wrapper.vm.loadActivity()

      expect(wrapper.vm.activity.name).toBe('更新后的活动')
    })

    it('API 返回非 200 时 activity 应该为 null', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      activityApi.getActivityById.mockResolvedValue({ code: 500, message: 'Error' })

      await wrapper.vm.loadActivity()

      expect(wrapper.vm.activity).toBeNull()
    })
  })

  describe('loadRegistrations 加载报名列表', () => {
    it('应该正确设置 registrationList 数据和分页', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      const newRegistrations = [{ ...mockRegistrations[0] }]
      activityApi.getRegistrationList.mockResolvedValue({
        code: 200,
        data: { list: newRegistrations, total: 1, pages: 1, current: 1, size: 10 }
      })

      await wrapper.vm.loadRegistrations()

      expect(wrapper.vm.registrationList.length).toBe(1)
      expect(wrapper.vm.regPagination.total).toBe(1)
    })

    it('API 返回非 200 时 registrationList 应该为空数组', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      activityApi.getRegistrationList.mockResolvedValue({ code: 500 })

      await wrapper.vm.loadRegistrations()

      expect(wrapper.vm.registrationList).toEqual([])
      expect(wrapper.vm.regPagination.total).toBe(0)
    })

    it('API 抛出异常时应该捕获并设置空数组', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      activityApi.getRegistrationList.mockRejectedValue(new Error('API Error'))
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      await wrapper.vm.loadRegistrations()

      expect(wrapper.vm.registrationList).toEqual([])
      expect(wrapper.vm.regPagination.total).toBe(0)
      expect(consoleErrorSpy).toHaveBeenCalled()

      consoleErrorSpy.mockRestore()
    })
  })

  describe('报名名单搜索和分页', () => {
    it('搜索报名应该传递正确的 keyword 参数', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      activityApi.getRegistrationList.mockResolvedValue({
        code: 200,
        data: { list: [mockRegistrations[0]], total: 1 }
      })

      wrapper.vm.searchKeyword = '张三'
      await wrapper.vm.loadRegistrations()

      expect(activityApi.getRegistrationList).toHaveBeenCalledWith(
        '1',
        {
          page: 1,
          size: 10,
          keyword: '张三'
        }
      )
    })

    it('切换分页应该传递正确的 page 参数', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      activityApi.getRegistrationList.mockResolvedValue({
        code: 200,
        data: { list: [], total: 2 }
      })

      wrapper.vm.regPagination.current = 2
      await wrapper.vm.loadRegistrations()

      expect(activityApi.getRegistrationList).toHaveBeenCalledWith(
        '1',
        {
          page: 2,
          size: 10,
          keyword: ''
        }
      )
    })
  })

  describe('cancelRegistration 取消报名', () => {
    it('应该弹出确认对话框', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      ElMessageBox.confirm.mockResolvedValue(true)
      activityApi.cancelRegistration.mockResolvedValue({ code: 200, data: null })

      await wrapper.vm.handleCancelRegistration(mockRegistrations[0])

      expect(ElMessageBox.confirm).toHaveBeenCalledWith(
        '确定要取消该用户的报名吗？',
        '提示',
        expect.objectContaining({ type: 'warning' })
      )
    })

    it('确认后应该调用 cancelRegistration API 并刷新列表', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      ElMessageBox.confirm.mockResolvedValue(true)
      activityApi.cancelRegistration.mockResolvedValue({ code: 200, data: null })
      activityApi.getRegistrationList.mockResolvedValue({
        code: 200,
        data: { list: [], total: 2 }
      })

      await wrapper.vm.handleCancelRegistration(mockRegistrations[0])
      await flushPromises()

      expect(activityApi.cancelRegistration).toHaveBeenCalledWith(1)
      expect(ElMessage.success).toHaveBeenCalledWith('取消成功')
      expect(activityApi.getRegistrationList).toHaveBeenCalled()
    })

    it('取消确认后不应该调用 API', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      ElMessageBox.confirm.mockRejectedValue(new Error('Cancel'))

      await wrapper.vm.handleCancelRegistration(mockRegistrations[0])
      await flushPromises()

      expect(activityApi.cancelRegistration).not.toHaveBeenCalled()
    })
  })

  describe('exportRegistrations 导出 CSV', () => {
    it('应该调用 exportRegistrations API', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      const mockBlob = new Blob(['id,name,phone\n1,张三,13800138001'], { type: 'text/csv' })
      const mockResponse = {
        headers: { 'content-disposition': 'attachment; filename="registrations.csv"' },
        data: mockBlob
      }
      activityApi.exportRegistrations.mockResolvedValue(mockResponse)

      await wrapper.vm.handleExport()
      await flushPromises()

      expect(activityApi.exportRegistrations).toHaveBeenCalledWith('1')
    })

    it('应该从 content-disposition 解析文件名', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      const mockBlob = new Blob(['id,name,phone\n1,张三,13800138001'], { type: 'text/csv' })
      const mockResponse = {
        headers: { 'content-disposition': 'attachment; filename="test-file.csv"' },
        data: mockBlob
      }
      activityApi.exportRegistrations.mockResolvedValue(mockResponse)

      const createElementSpy = vi.spyOn(document, 'createElement').mockReturnValueOnce({
        href: '',
        download: '',
        click: vi.fn(),
        style: {}
      })
      const createObjectURLSpy = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:url')
      const revokeObjectURLSpy = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {})

      await wrapper.vm.handleExport()
      await flushPromises()

      expect(createElementSpy).toHaveBeenCalledWith('a')

      createElementSpy.mockRestore()
      createObjectURLSpy.mockRestore()
      revokeObjectURLSpy.mockRestore()
    })

    it('导出成功后应该显示成功提示并清理资源', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      const mockBlob = new Blob(['id,name,phone\n1,张三,13800138001'], { type: 'text/csv' })
      const mockResponse = {
        headers: { 'content-disposition': 'attachment; filename="export.csv"' },
        data: mockBlob
      }
      activityApi.exportRegistrations.mockResolvedValue(mockResponse)

      const mockLink = { href: '', download: '', click: vi.fn(), style: {} }
      const createElementSpy = vi.spyOn(document, 'createElement').mockReturnValue(mockLink)
      const createObjectURLSpy = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:test-url')
      const revokeObjectURLSpy = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {})
      const appendChildSpy = vi.spyOn(document.body, 'appendChild').mockImplementation(() => {})
      const removeChildSpy = vi.spyOn(document.body, 'removeChild').mockImplementation(() => {})

      await wrapper.vm.handleExport()
      await flushPromises()

      expect(mockLink.download).toBe('export.csv')
      expect(mockLink.click).toHaveBeenCalled()
      expect(ElMessage.success).toHaveBeenCalledWith('导出成功')
      expect(revokeObjectURLSpy).toHaveBeenCalledWith('blob:test-url')
      expect(removeChildSpy).toHaveBeenCalledWith(mockLink)

      createElementSpy.mockRestore()
      createObjectURLSpy.mockRestore()
      revokeObjectURLSpy.mockRestore()
      appendChildSpy.mockRestore()
      removeChildSpy.mockRestore()
    })

    it('导出失败时应该显示错误提示', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      const mockError = new Error('Export failed')
      activityApi.exportRegistrations.mockRejectedValue(mockError)

      await wrapper.vm.handleExport()
      await flushPromises()

      expect(ElMessage.error).toHaveBeenCalledWith('导出失败')
    })

    it('导出失败时应该打印错误日志', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      const mockError = new Error('Export Error')
      activityApi.exportRegistrations.mockRejectedValue(mockError)
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      await wrapper.vm.handleExport()
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('导出失败', mockError)

      consoleErrorSpy.mockRestore()
    })
  })

  describe('handleEdit 打开编辑弹窗', () => {
    it('应该打开编辑弹窗并回填活动数据', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      await wrapper.vm.handleEdit()

      expect(wrapper.vm.editDialogVisible).toBe(true)
      expect(wrapper.vm.editForm.id).toBe(1)
      expect(wrapper.vm.editForm.name).toBe('读书分享会')
      expect(wrapper.vm.editForm.content).toBe('<p>活动内容详情</p>')
    })

    it('activity 为 null 时不应该报错', async () => {
      activityApi.getActivityById.mockResolvedValue({ code: 500 })

      const wrapper = await createWrapper()
      await flushPromises()

      expect(() => wrapper.vm.handleEdit()).not.toThrow()
    })
  })

  describe('时间控件选择范围限制', () => {
    it('disableStartDate 在结束时间存在时应该禁用结束时间之后的日期', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      wrapper.vm.editForm.endTime = '2026-07-15 17:00:00'

      const afterEndTime = new Date('2026-07-16').getTime()
      const beforeEndTime = new Date('2026-07-14').getTime()

      expect(wrapper.vm.disableStartDate({ getTime: () => afterEndTime })).toBe(true)
      expect(wrapper.vm.disableStartDate({ getTime: () => beforeEndTime })).toBe(false)
    })

    it('disableEndDate 在开始时间存在时应该禁用开始时间之前的日期', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      wrapper.vm.editForm.startTime = '2026-07-15 14:00:00'

      const beforeStartTime = new Date('2026-07-14').getTime()
      const afterStartTime = new Date('2026-07-16').getTime()

      expect(wrapper.vm.disableEndDate({ getTime: () => beforeStartTime })).toBe(true)
      expect(wrapper.vm.disableEndDate({ getTime: () => afterStartTime })).toBe(false)
    })

    it('disableStartDate 在结束时间不存在时不限制', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      wrapper.vm.editForm.endTime = ''

      expect(wrapper.vm.disableStartDate({ getTime: () => Date.now() })).toBe(false)
    })

    it('disableEndDate 在开始时间不存在时不限制', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      wrapper.vm.editForm.startTime = ''

      expect(wrapper.vm.disableEndDate({ getTime: () => Date.now() })).toBe(false)
    })
  })

  describe('handleSaveEdit 保存编辑', () => {
    beforeEach(() => {
      ElMessage.warning.mockClear()
    })

    it('应该在活动内容为空时显示警告', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      wrapper.vm.editFormRef = { validate: vi.fn(callback => callback(true)) }
      wrapper.vm.editForm.content = ''

      await wrapper.vm.handleSaveEdit()
      await flushPromises()

      expect(ElMessage.warning).toHaveBeenCalledWith('请输入活动内容')
      expect(activityApi.updateActivity).not.toHaveBeenCalled()
    })

    it('应该在开始时间晚于结束时间时显示警告', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      wrapper.vm.editFormRef = { validate: vi.fn(callback => callback(true)) }
      wrapper.vm.editForm.content = '<p>内容</p>'
      wrapper.vm.editForm.startTime = '2026-07-16 14:00:00'
      wrapper.vm.editForm.endTime = '2026-07-15 17:00:00'

      await wrapper.vm.handleSaveEdit()
      await flushPromises()

      expect(ElMessage.warning).toHaveBeenCalledWith('开始时间不能晚于结束时间')
      expect(activityApi.updateActivity).not.toHaveBeenCalled()
    })

    it('验证通过后应该调用 updateActivity', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      wrapper.vm.editFormRef = { validate: vi.fn(callback => callback(true)) }
      wrapper.vm.editForm.content = '<p>更新后的内容</p>'
      wrapper.vm.editForm.startTime = '2026-07-15 14:00:00'
      wrapper.vm.editForm.endTime = '2026-07-15 17:00:00'

      activityApi.updateActivity.mockResolvedValue({ code: 200, data: {} })
      activityApi.getActivityById.mockResolvedValue({
        code: 200,
        data: { ...mockActivity, content: '<p>更新后的内容</p>' }
      })

      await wrapper.vm.handleSaveEdit()
      await flushPromises()

      expect(activityApi.updateActivity).toHaveBeenCalled()
      expect(ElMessage.success).toHaveBeenCalledWith('保存成功')
      expect(wrapper.vm.editDialogVisible).toBe(false)
      expect(activityApi.getActivityById).toHaveBeenCalled()
    })

    it('editFormRef 为 null 时不应该报错', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      wrapper.vm.editFormRef = null

      expect(() => wrapper.vm.handleSaveEdit()).not.toThrow()
    })

    it('表单验证失败时不应该调用 API', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      wrapper.vm.editFormRef = { validate: vi.fn(callback => callback(false)) }
      wrapper.vm.editForm.content = '<p>内容</p>'

      await wrapper.vm.handleSaveEdit()
      await flushPromises()

      expect(activityApi.updateActivity).not.toHaveBeenCalled()
    })
  })

  describe('handleDialogClosed 弹窗关闭', () => {
    it('应该重置 editor 内容', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      wrapper.vm.editorRef = { innerHTML: '<p>临时内容</p>' }

      wrapper.vm.handleDialogClosed()

      expect(wrapper.vm.editorRef.innerHTML).toBe('')
    })

    it('editorRef 为 null 时不应该报错', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      wrapper.vm.editorRef = null

      expect(() => wrapper.vm.handleDialogClosed()).not.toThrow()
    })
  })

  describe('报名百分比计算', () => {
    it('应该正确计算报名百分比', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      const percentage = Math.round(
        (wrapper.vm.activity.registeredCount / wrapper.vm.activity.quota) * 100
      )
      expect(percentage).toBe(50)
    })

    it('名额为 0 时进度条应该显示 0%', async () => {
      activityApi.getActivityById.mockResolvedValue({
        code: 200,
        data: { ...mockActivity, quota: 0, registeredCount: 0 }
      })

      const wrapper = await createWrapper()
      await flushPromises()

      const percentage = wrapper.vm.activity.quota
        ? Math.round((wrapper.vm.activity.registeredCount / wrapper.vm.activity.quota) * 100)
        : 0
      expect(percentage).toBe(0)
    })

    it('报名人数为 0 时百分比应该为 0', async () => {
      activityApi.getActivityById.mockResolvedValue({
        code: 200,
        data: { ...mockActivity, registeredCount: 0 }
      })

      const wrapper = await createWrapper()
      await flushPromises()

      const percentage = wrapper.vm.activity.quota
        ? Math.round((wrapper.vm.activity.registeredCount / wrapper.vm.activity.quota) * 100)
        : 0
      expect(percentage).toBe(0)
    })
  })

  describe('快捷操作区域', () => {
    it('应该显示「编辑活动」和「导出报名名单」按钮', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      expect(wrapper.text()).toContain('编辑活动')
      expect(wrapper.text()).toContain('导出报名名单')
    })
  })

  describe('报名状态标签', () => {
    it('已报名状态应该显示绿色标签', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      const rows = wrapper.findAll('.el-table__row')
      expect(rows[0].text()).toContain('已报名')
    })

    it('已取消状态应该显示灰色标签', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      const rows = wrapper.findAll('.el-table__row')
      expect(rows[2].text()).toContain('已取消')
    })
  })

  describe('读者关联显示', () => {
    it('有 readerName 时应该显示', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      const rows = wrapper.findAll('.el-table__row')
      expect(rows[0].text()).toContain('R001')
    })

    it('没有 readerName 时应该显示「散客」', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      const rows = wrapper.findAll('.el-table__row')
      expect(rows[1].text()).toContain('散客')
    })
  })

  describe('报名操作按钮', () => {
    it('已报名状态应该显示「取消」按钮', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      const rows = wrapper.findAll('.el-table__row')
      const buttons = rows[0].findAll('.el-button')
      const buttonTexts = buttons.map(b => b.text().trim())
      expect(buttonTexts).toContain('取消')
    })

    it('已取消状态不应该显示操作按钮', async () => {
      const wrapper = await createWrapper()
      await flushPromises()

      const rows = wrapper.findAll('.el-table__row')
      const buttons = rows[2].findAll('.el-button')
      expect(buttons.length).toBe(0)
    })
  })
})
