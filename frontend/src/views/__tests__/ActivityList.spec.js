import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import ActivityList from '../ActivityList.vue'
import * as activityApi from '@/api/activity'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import ElementPlus from 'element-plus'

vi.mock('@/api/activity', () => ({
  getActivityList: vi.fn(),
  addActivity: vi.fn(),
  updateActivity: vi.fn(),
  deleteActivity: vi.fn()
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

const mockActivities = [
  {
    id: 1,
    name: '读书分享会',
    coverImage: 'https://example.com/1.jpg',
    content: '<p>活动内容1</p>',
    startTime: '2026-07-15 14:00:00',
    endTime: '2026-07-15 17:00:00',
    location: '图书馆一楼',
    quota: 100,
    registeredCount: 50,
    status: 1,
    createTime: '2026-06-01 10:00:00'
  },
  {
    id: 2,
    name: '作家讲座',
    coverImage: 'https://example.com/2.jpg',
    content: '<p>活动内容2</p>',
    startTime: '2026-08-01 10:00:00',
    endTime: '2026-08-01 12:00:00',
    location: '图书馆二楼',
    quota: 50,
    registeredCount: 20,
    status: 0,
    createTime: '2026-06-05 10:00:00'
  }
]

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/activities', component: { template: '<div>List</div>' } },
    { path: '/activities/:id', component: { template: '<div>Detail</div>' } }
  ]
})

const createWrapper = () => {
  return mount(ActivityList, {
    global: {
      plugins: [router, ElementPlus],
      stubs: {
        'el-image': { template: '<div class="el-image"><img :src="src" /></div>', props: ['src', 'previewSrcList'] }
      }
    }
  })
}

describe('ActivityList.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    activityApi.getActivityList.mockResolvedValue({
      code: 200,
      data: { list: mockActivities, total: 2, pages: 1, current: 1, size: 10 }
    })
  })

  describe('组件初始化与渲染', () => {
    it('应该在组件挂载时调用 loadData 加载活动列表', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      expect(activityApi.getActivityList).toHaveBeenCalledTimes(1)
      expect(activityApi.getActivityList).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        keyword: ''
      })
      expect(wrapper.vm.tableData.length).toBe(2)
      expect(wrapper.vm.pagination.total).toBe(2)
    })

    it('应该正确渲染搜索栏和发布按钮', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      expect(wrapper.find('.search-bar').exists()).toBe(true)
      expect(wrapper.find('input[placeholder="请输入活动名称搜索"]').exists()).toBe(true)
      expect(wrapper.text()).toContain('搜索')
      expect(wrapper.text()).toContain('发布活动')
    })

    it('应该正确渲染表格列', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      const headers = wrapper.findAll('.el-table__header th')
      const headerTexts = headers.map(h => h.text().trim())

      expect(headerTexts).toContain('ID')
      expect(headerTexts).toContain('主图')
      expect(headerTexts).toContain('活动名称')
      expect(headerTexts).toContain('活动时间')
      expect(headerTexts).toContain('地点')
      expect(headerTexts).toContain('报名情况')
      expect(headerTexts).toContain('状态')
      expect(headerTexts).toContain('操作')
    })

    it('应该正确渲染活动数据行', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      expect(wrapper.text()).toContain('读书分享会')
      expect(wrapper.text()).toContain('作家讲座')
      expect(wrapper.text()).toContain('图书馆一楼')
      expect(wrapper.text()).toContain('图书馆二楼')
    })

    it('应该正确渲染分页组件', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      expect(wrapper.find('.el-pagination').exists()).toBe(true)
    })
  })

  describe('formatDateTime 日期格式化函数', () => {
    it('应该正确格式化日期时间为 YYYY-MM-DD HH:mm', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      const result = wrapper.vm.formatDateTime('2026-07-15 14:30:00')
      expect(result).toBe('2026-07-15 14:30')
    })

    it('应该在传入空值时返回空字符串', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      expect(wrapper.vm.formatDateTime(null)).toBe('')
      expect(wrapper.vm.formatDateTime(undefined)).toBe('')
      expect(wrapper.vm.formatDateTime('')).toBe('')
    })
  })

  describe('状态标签渲染', () => {
    it('已发布活动应该显示绿色成功标签', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      const rows = wrapper.findAll('.el-table__row')
      const firstRowTags = rows[0].findAll('.el-tag')
      expect(firstRowTags[0].text()).toContain('已发布')
      expect(firstRowTags[0].classes()).toContain('el-tag--success')
    })

    it('已下架活动应该显示蓝色信息标签', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      const rows = wrapper.findAll('.el-table__row')
      const secondRowTags = rows[1].findAll('.el-tag')
      expect(secondRowTags[0].text()).toContain('已下架')
      expect(secondRowTags[0].classes()).toContain('el-tag--info')
    })
  })

  describe('上下架快捷功能', () => {
    it('已发布活动应该显示「下架」按钮，隐藏「上架」按钮', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      const rows = wrapper.findAll('.el-table__row')
      const firstRowButtons = rows[0].findAll('.el-button')

      const buttonTexts = firstRowButtons.map(b => b.text().trim())
      expect(buttonTexts).toContain('下架')
      expect(buttonTexts).not.toContain('上架')
    })

    it('已下架活动应该显示「上架」按钮，隐藏「下架」按钮', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      const rows = wrapper.findAll('.el-table__row')
      const secondRowButtons = rows[1].findAll('.el-button')

      const buttonTexts = secondRowButtons.map(b => b.text().trim())
      expect(buttonTexts).toContain('上架')
      expect(buttonTexts).not.toContain('下架')
    })

    it('点击下架按钮应该弹出确认框', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      ElMessageBox.confirm.mockResolvedValue(true)
      activityApi.updateActivity.mockResolvedValue({ code: 200, data: {} })

      const rows = wrapper.findAll('.el-table__row')
      const offShelfBtn = rows[0].findAll('.el-button').find(b => b.text().trim() === '下架')

      await offShelfBtn.trigger('click')
      await flushPromises()

      expect(ElMessageBox.confirm).toHaveBeenCalledWith(
        '确定要下架该活动吗？',
        '提示',
        expect.objectContaining({ type: 'warning' })
      )
    })

    it('点击上架按钮应该弹出确认框', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      ElMessageBox.confirm.mockResolvedValue(true)
      activityApi.updateActivity.mockResolvedValue({ code: 200, data: {} })

      const rows = wrapper.findAll('.el-table__row')
      const onShelfBtn = rows[1].findAll('.el-button').find(b => b.text().trim() === '上架')

      await onShelfBtn.trigger('click')
      await flushPromises()

      expect(ElMessageBox.confirm).toHaveBeenCalledWith(
        '确定要上架该活动吗？',
        '提示',
        expect.objectContaining({ type: 'warning' })
      )
    })

    it('确认下架后应该调用 updateActivity 并只传递 id 和 status', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      ElMessageBox.confirm.mockResolvedValue(true)
      activityApi.updateActivity.mockResolvedValue({ code: 200, data: {} })

      await wrapper.vm.handleToggleStatus(mockActivities[0], 0)
      await flushPromises()

      expect(activityApi.updateActivity).toHaveBeenCalledWith({
        id: 1,
        status: 0
      })
    })

    it('上下架成功后应该显示成功提示并重新加载列表', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      ElMessageBox.confirm.mockResolvedValue(true)
      activityApi.updateActivity.mockResolvedValue({ code: 200, data: {} })
      activityApi.getActivityList.mockResolvedValue({
        code: 200,
        data: { list: [{ ...mockActivities[0], status: 0 }], total: 1 }
      })

      await wrapper.vm.handleToggleStatus(mockActivities[0], 0)
      await flushPromises()

      expect(ElMessage.success).toHaveBeenCalledWith('下架成功')
      expect(activityApi.getActivityList).toHaveBeenCalled()
    })

    it('取消上下架确认后不应该调用 API', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      ElMessageBox.confirm.mockRejectedValue(new Error('Cancel'))

      await wrapper.vm.handleToggleStatus(mockActivities[0], 0)
      await flushPromises()

      expect(activityApi.updateActivity).not.toHaveBeenCalled()
      expect(activityApi.getActivityList).not.toHaveBeenCalled()
    })
  })

  describe('删除功能（已上架禁止删除）', () => {
    it('已上架活动的删除按钮应该被禁用', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      const rows = wrapper.findAll('.el-table__row')
      const firstRowButtons = rows[0].findAll('.el-button')
      const deleteBtn = firstRowButtons.find(b => b.text().trim() === '删除')

      expect(deleteBtn.attributes('disabled')).toBeDefined()
    })

    it('已下架活动的删除按钮应该可用', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      const rows = wrapper.findAll('.el-table__row')
      const secondRowButtons = rows[1].findAll('.el-button')
      const deleteBtn = secondRowButtons.find(b => b.text().trim() === '删除')

      expect(deleteBtn.attributes('disabled')).toBeUndefined()
    })

    it('handleDelete 在活动已上架时应该直接返回并显示警告', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.handleDelete({ id: 1, status: 1, name: '测试活动' })

      expect(ElMessage.warning).toHaveBeenCalledWith('已上架的活动不允许删除，请先下架')
      expect(activityApi.deleteActivity).not.toHaveBeenCalled()
    })

    it('handleDelete 在活动已下架时应该显示确认对话框', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      ElMessageBox.confirm.mockResolvedValue(true)
      activityApi.deleteActivity.mockResolvedValue({ code: 200, data: null })

      await wrapper.vm.handleDelete({ id: 2, status: 0, name: '测试活动' })
      await flushPromises()

      expect(ElMessageBox.confirm).toHaveBeenCalled()
      expect(activityApi.deleteActivity).toHaveBeenCalledWith(2)
    })

    it('删除确认提示应该包含"删除后数据不可恢复"警告', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      ElMessageBox.confirm.mockResolvedValue(true)
      activityApi.deleteActivity.mockResolvedValue({ code: 200, data: null })

      await wrapper.vm.handleDelete({ id: 2, status: 0, name: '测试活动' })

      const callArgs = ElMessageBox.confirm.mock.calls[0]
      expect(callArgs[0]).toContain('删除后数据不可恢复')
    })

    it('删除成功后应该重新加载列表', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      ElMessageBox.confirm.mockResolvedValue(true)
      activityApi.deleteActivity.mockResolvedValue({ code: 200, data: null })
      activityApi.getActivityList.mockResolvedValue({
        code: 200,
        data: { list: [], total: 0 }
      })

      await wrapper.vm.handleDelete({ id: 2, status: 0 })
      await flushPromises()

      expect(ElMessage.success).toHaveBeenCalledWith('删除成功')
      expect(activityApi.getActivityList).toHaveBeenCalled()
    })

    it('取消删除确认后不应该调用 API', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      ElMessageBox.confirm.mockRejectedValue(new Error('Cancel'))

      await wrapper.vm.handleDelete({ id: 2, status: 0 })
      await flushPromises()

      expect(activityApi.deleteActivity).not.toHaveBeenCalled()
    })
  })

  describe('时间控件选择范围限制', () => {
    it('disableStartDate 在结束时间存在时，应该禁用结束时间之后的日期', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.form.endTime = '2026-07-15 17:00:00'

      const endTime = dayjs('2026-07-15 17:00:00')

      const beforeEndTime = new Date('2026-07-14').getTime()
      const atEndTime = endTime.valueOf()
      const afterEndTime = new Date('2026-07-16').getTime()

      expect(wrapper.vm.disableStartDate({ getTime: () => beforeEndTime })).toBe(false)
      expect(wrapper.vm.disableStartDate({ getTime: () => atEndTime })).toBe(false)
      expect(wrapper.vm.disableStartDate({ getTime: () => afterEndTime })).toBe(true)
    })

    it('disableEndDate 在开始时间存在时，应该禁用开始时间之前的日期', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.form.startTime = '2026-07-15 14:00:00'

      const startTime = dayjs('2026-07-15 14:00:00')

      const beforeStartTime = new Date('2026-07-14').getTime()
      const atStartTime = startTime.valueOf()
      const afterStartTime = new Date('2026-07-16').getTime()

      expect(wrapper.vm.disableEndDate({ getTime: () => beforeStartTime })).toBe(true)
      expect(wrapper.vm.disableEndDate({ getTime: () => atStartTime })).toBe(false)
      expect(wrapper.vm.disableEndDate({ getTime: () => afterStartTime })).toBe(false)
    })

    it('disableStartDate 在结束时间不存在时，应该不限制任何日期', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.form.endTime = ''

      expect(wrapper.vm.disableStartDate({ getTime: () => Date.now() })).toBe(false)
      expect(wrapper.vm.disableStartDate({ getTime: () => Date.now() + 86400000 })).toBe(false)
    })

    it('disableEndDate 在开始时间不存在时，应该不限制任何日期', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.form.startTime = ''

      expect(wrapper.vm.disableEndDate({ getTime: () => Date.now() })).toBe(false)
      expect(wrapper.vm.disableEndDate({ getTime: () => Date.now() - 86400000 })).toBe(false)
    })
  })

  describe('搜索功能', () => {
    it('调用 loadData 应该传递正确的搜索参数', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      activityApi.getActivityList.mockResolvedValue({
        code: 200,
        data: { list: [mockActivities[0]], total: 1 }
      })

      wrapper.vm.keyword = '读书'
      await wrapper.vm.loadData()

      expect(activityApi.getActivityList).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        keyword: '读书'
      })
    })

    it('清空搜索框后应该搜索全部', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      activityApi.getActivityList.mockResolvedValue({
        code: 200,
        data: { list: mockActivities, total: 2 }
      })

      wrapper.vm.keyword = ''
      await wrapper.vm.loadData()

      expect(activityApi.getActivityList).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        keyword: ''
      })
    })
  })

  describe('分页功能', () => {
    it('切换分页大小应该传递正确参数', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      activityApi.getActivityList.mockResolvedValue({
        code: 200,
        data: { list: mockActivities, total: 2 }
      })

      wrapper.vm.pagination.size = 20
      await wrapper.vm.loadData()

      expect(activityApi.getActivityList).toHaveBeenCalledWith({
        page: 1,
        size: 20,
        keyword: ''
      })
    })

    it('切换页码应该传递正确参数', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      vi.clearAllMocks()
      activityApi.getActivityList.mockResolvedValue({
        code: 200,
        data: { list: [], total: 2 }
      })

      wrapper.vm.pagination.current = 2
      await wrapper.vm.loadData()

      expect(activityApi.getActivityList).toHaveBeenCalledWith({
        page: 2,
        size: 10,
        keyword: ''
      })
    })
  })

  describe('handleAdd 新增活动', () => {
    it('应该设置 isEdit 为 false 并重置表单', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.form.name = '测试'
      wrapper.vm.form.id = 999

      await wrapper.vm.handleAdd()

      expect(wrapper.vm.isEdit).toBe(false)
      expect(wrapper.vm.dialogTitle).toBe('发布活动')
      expect(wrapper.vm.form.id).toBeNull()
      expect(wrapper.vm.form.name).toBe('')
      expect(wrapper.vm.dialogVisible).toBe(true)
    })
  })

  describe('handleEdit 编辑活动', () => {
    it('应该设置 isEdit 为 true 并回填数据', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      await wrapper.vm.handleEdit(mockActivities[0])

      expect(wrapper.vm.isEdit).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('编辑活动')
      expect(wrapper.vm.form.id).toBe(1)
      expect(wrapper.vm.form.name).toBe('读书分享会')
      expect(wrapper.vm.dialogVisible).toBe(true)
    })
  })

  describe('handleDetail 跳转详情', () => {
    it('应该跳转到活动详情页', async () => {
      const wrapper = createWrapper()
      await flushPromises()
      await router.push('/activities')

      const pushSpy = vi.spyOn(router, 'push')

      await wrapper.vm.handleDetail({ id: 1 })

      expect(pushSpy).toHaveBeenCalledWith('/activities/1')

      pushSpy.mockRestore()
    })
  })

  describe('handleSubmit 表单提交验证', () => {
    beforeEach(() => {
      ElMessage.warning.mockClear()
    })

    it('应该在活动内容为空时显示警告', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.formRef = { validate: vi.fn(callback => callback(true)) }
      wrapper.vm.form.content = ''
      wrapper.vm.form.name = '测试'
      wrapper.vm.form.startTime = '2026-07-15 14:00:00'
      wrapper.vm.form.endTime = '2026-07-15 17:00:00'
      wrapper.vm.form.location = '图书馆'
      wrapper.vm.form.quota = 50

      await wrapper.vm.handleSubmit()
      await flushPromises()

      expect(ElMessage.warning).toHaveBeenCalledWith('请输入活动内容')
      expect(activityApi.addActivity).not.toHaveBeenCalled()
      expect(activityApi.updateActivity).not.toHaveBeenCalled()
    })

    it('应该在开始时间晚于结束时间时显示警告', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.formRef = { validate: vi.fn(callback => callback(true)) }
      wrapper.vm.form.content = '<p>内容</p>'
      wrapper.vm.form.name = '测试'
      wrapper.vm.form.startTime = '2026-07-16 14:00:00'
      wrapper.vm.form.endTime = '2026-07-15 17:00:00'
      wrapper.vm.form.location = '图书馆'
      wrapper.vm.form.quota = 50

      await wrapper.vm.handleSubmit()
      await flushPromises()

      expect(ElMessage.warning).toHaveBeenCalledWith('开始时间不能晚于结束时间')
      expect(activityApi.addActivity).not.toHaveBeenCalled()
    })

    it('formRef 为 null 时不应该报错', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.formRef = null

      expect(() => wrapper.vm.handleSubmit()).not.toThrow()
    })

    it('表单验证失败时不应该调用 API', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.formRef = { validate: vi.fn(callback => callback(false)) }
      wrapper.vm.form.content = '<p>内容</p>'

      await wrapper.vm.handleSubmit()
      await flushPromises()

      expect(activityApi.addActivity).not.toHaveBeenCalled()
      expect(activityApi.updateActivity).not.toHaveBeenCalled()
    })
  })

  describe('handleSubmit 表单提交 - 成功场景', () => {
    beforeEach(() => {
      vi.clearAllMocks()
    })

    it('新增活动验证通过后应该调用 addActivity', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.formRef = { validate: vi.fn(callback => callback(true)) }
      wrapper.vm.isEdit = false
      wrapper.vm.form.id = null
      wrapper.vm.form.name = '新活动'
      wrapper.vm.form.content = '<p>内容</p>'
      wrapper.vm.form.startTime = '2026-07-15 14:00:00'
      wrapper.vm.form.endTime = '2026-07-15 17:00:00'
      wrapper.vm.form.location = '图书馆'
      wrapper.vm.form.quota = 50
      wrapper.vm.form.status = 1

      activityApi.addActivity.mockResolvedValue({ code: 200, data: { id: 3 } })
      activityApi.getActivityList.mockResolvedValue({
        code: 200, data: { list: [], total: 3 }
      })

      await wrapper.vm.handleSubmit()
      await flushPromises()

      expect(activityApi.addActivity).toHaveBeenCalled()
      expect(ElMessage.success).toHaveBeenCalledWith('发布成功')
      expect(wrapper.vm.dialogVisible).toBe(false)
      expect(activityApi.getActivityList).toHaveBeenCalled()
    })

    it('编辑活动验证通过后应该调用 updateActivity', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.formRef = { validate: vi.fn(callback => callback(true)) }
      wrapper.vm.isEdit = true
      wrapper.vm.form.id = 1
      wrapper.vm.form.name = '更新后的活动'
      wrapper.vm.form.content = '<p>内容</p>'
      wrapper.vm.form.startTime = '2026-07-15 14:00:00'
      wrapper.vm.form.endTime = '2026-07-15 17:00:00'
      wrapper.vm.form.location = '图书馆'
      wrapper.vm.form.quota = 50
      wrapper.vm.form.status = 1

      activityApi.updateActivity.mockResolvedValue({ code: 200, data: {} })
      activityApi.getActivityList.mockResolvedValue({
        code: 200, data: { list: [], total: 2 }
      })

      await wrapper.vm.handleSubmit()
      await flushPromises()

      expect(activityApi.updateActivity).toHaveBeenCalled()
      expect(ElMessage.success).toHaveBeenCalledWith('更新成功')
      expect(wrapper.vm.dialogVisible).toBe(false)
      expect(activityApi.getActivityList).toHaveBeenCalled()
    })
  })

  describe('富文本编辑器', () => {
    it('onEditorInput 应该同步内容到 form.content', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.editorRef = { innerHTML: '<p>测试内容</p>' }
      wrapper.vm.onEditorInput()

      expect(wrapper.vm.form.content).toBe('<p>测试内容</p>')
    })

    it('onEditorBlur 应该同步内容到 form.content', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.editorRef = { innerHTML: '<p>失焦时的内容</p>' }
      wrapper.vm.onEditorBlur()

      expect(wrapper.vm.form.content).toBe('<p>失焦时的内容</p>')
    })
  })

  describe('API 错误处理', () => {
    it('加载活动列表失败时应该打印错误日志', async () => {
      const mockError = new Error('Network Error')
      activityApi.getActivityList.mockRejectedValue(mockError)
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const wrapper = createWrapper()
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('加载活动列表失败', mockError)

      consoleErrorSpy.mockRestore()
    })

    it('删除活动失败时应该打印错误日志', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      const mockError = new Error('Delete Error')
      ElMessageBox.confirm.mockResolvedValue(true)
      activityApi.deleteActivity.mockRejectedValue(mockError)
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      await wrapper.vm.handleDelete({ id: 2, status: 0 })
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('删除失败', mockError)

      consoleErrorSpy.mockRestore()
    })

    it('上下架失败时应该打印错误日志', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      const mockError = new Error('Update Error')
      ElMessageBox.confirm.mockResolvedValue(true)
      activityApi.updateActivity.mockRejectedValue(mockError)
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      await wrapper.vm.handleToggleStatus({ id: 1 }, 0)
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('下架失败', mockError)

      consoleErrorSpy.mockRestore()
    })
  })

  describe('报名进度显示', () => {
    it('应该正确计算报名百分比', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      const activity = wrapper.vm.tableData[0]
      const expectedPercentage = Math.round((activity.registeredCount / activity.quota) * 100)
      expect(expectedPercentage).toBe(50)
    })

    it('应该在名额已满时计算正确的百分比', async () => {
      activityApi.getActivityList.mockResolvedValue({
        code: 200,
        data: {
          list: [{ ...mockActivities[0], registeredCount: 100, quota: 100 }],
          total: 1
        }
      })

      const wrapper = createWrapper()
      await flushPromises()

      const activity = wrapper.vm.tableData[0]
      const percentage = Math.round((activity.registeredCount / activity.quota) * 100)
      expect(percentage).toBe(100)
    })
  })

  describe('dialogTitle 计算属性', () => {
    it('isEdit 为 true 时应该返回「编辑活动」', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.isEdit = true
      expect(wrapper.vm.dialogTitle).toBe('编辑活动')
    })

    it('isEdit 为 false 时应该返回「发布活动」', async () => {
      const wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.isEdit = false
      expect(wrapper.vm.dialogTitle).toBe('发布活动')
    })
  })
})
