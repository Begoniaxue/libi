import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as activityApi from '../activity'
import request from '@/utils/request'

vi.mock('@/utils/request', () => ({
  default: vi.fn()
}))

const mockActivity = {
  id: 1,
  name: '测试活动',
  coverImage: 'https://example.com/image.jpg',
  content: '<p>活动内容</p>',
  startTime: '2026-07-15 14:00:00',
  endTime: '2026-07-15 17:00:00',
  location: '图书馆一楼',
  quota: 100,
  registeredCount: 50,
  status: 1,
  createTime: '2026-06-01 10:00:00'
}

const mockRegistration = {
  id: 1,
  activityId: 1,
  name: '张三',
  phone: '13800138000',
  email: 'zhangsan@example.com',
  status: 1,
  createTime: '2026-06-10 10:00:00'
}

describe('activity.js API 封装测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getActivityList 获取活动列表', () => {
    it('应该调用 request 并传递正确的 GET 参数', async () => {
      const mockParams = { page: 1, size: 10, keyword: '测试' }
      const mockResponse = { code: 200, data: { list: [mockActivity], total: 1 } }
      request.mockResolvedValue(mockResponse)

      const result = await activityApi.getActivityList(mockParams)

      expect(request).toHaveBeenCalledTimes(1)
      expect(request).toHaveBeenCalledWith({
        url: '/activities',
        method: 'get',
        params: mockParams
      })
      expect(result).toEqual(mockResponse)
    })

    it('应该正确处理空参数', async () => {
      const mockResponse = { code: 200, data: { list: [], total: 0 } }
      request.mockResolvedValue(mockResponse)

      await activityApi.getActivityList()

      expect(request).toHaveBeenCalledWith({
        url: '/activities',
        method: 'get',
        params: undefined
      })
    })
  })

  describe('getActivityById 获取单个活动', () => {
    it('应该调用 request 并传递正确的活动 ID', async () => {
      const mockResponse = { code: 200, data: mockActivity }
      request.mockResolvedValue(mockResponse)

      const result = await activityApi.getActivityById(1)

      expect(request).toHaveBeenCalledWith({
        url: '/activities/1',
        method: 'get'
      })
      expect(result).toEqual(mockResponse)
    })

    it('应该正确处理不同类型的 ID 参数', async () => {
      request.mockResolvedValue({ code: 200, data: mockActivity })

      await activityApi.getActivityById('100')

      expect(request).toHaveBeenCalledWith({
        url: '/activities/100',
        method: 'get'
      })
    })
  })

  describe('addActivity 新增活动', () => {
    it('应该调用 POST 请求并传递活动数据', async () => {
      const activityData = {
        name: '新活动',
        coverImage: 'https://example.com/new.jpg',
        content: '<p>新内容</p>',
        startTime: '2026-08-01 10:00:00',
        endTime: '2026-08-01 12:00:00',
        location: '新地点',
        quota: 50,
        status: 1
      }
      const mockResponse = { code: 200, data: { id: 2, ...activityData } }
      request.mockResolvedValue(mockResponse)

      const result = await activityApi.addActivity(activityData)

      expect(request).toHaveBeenCalledWith({
        url: '/activities',
        method: 'post',
        data: activityData
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('updateActivity 更新活动', () => {
    it('应该调用 PUT 请求并传递完整活动数据', async () => {
      const updateData = { id: 1, name: '更新后的名称', status: 0 }
      const mockResponse = { code: 200, data: { ...mockActivity, ...updateData } }
      request.mockResolvedValue(mockResponse)

      const result = await activityApi.updateActivity(updateData)

      expect(request).toHaveBeenCalledWith({
        url: '/activities',
        method: 'put',
        data: updateData
      })
      expect(result).toEqual(mockResponse)
    })

    it('应该支持仅更新状态字段（上下架功能）', async () => {
      const statusUpdate = { id: 1, status: 0 }
      request.mockResolvedValue({ code: 200, data: { ...mockActivity, status: 0 } })

      await activityApi.updateActivity(statusUpdate)

      expect(request).toHaveBeenCalledWith({
        url: '/activities',
        method: 'put',
        data: statusUpdate
      })
    })
  })

  describe('deleteActivity 删除活动', () => {
    it('应该调用 DELETE 请求并传递正确的活动 ID', async () => {
      const mockResponse = { code: 200, data: null }
      request.mockResolvedValue(mockResponse)

      const result = await activityApi.deleteActivity(1)

      expect(request).toHaveBeenCalledWith({
        url: '/activities/1',
        method: 'delete'
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('getRegistrationList 获取报名列表', () => {
    it('应该调用正确的接口并传递活动 ID 和分页参数', async () => {
      const params = { page: 1, size: 10, keyword: '张' }
      const mockResponse = { code: 200, data: { list: [mockRegistration], total: 1 } }
      request.mockResolvedValue(mockResponse)

      const result = await activityApi.getRegistrationList(1, params)

      expect(request).toHaveBeenCalledWith({
        url: '/activities/1/registrations',
        method: 'get',
        params
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('registerActivity 用户报名', () => {
    it('应该调用 POST 请求并传递报名数据', async () => {
      const registrationData = {
        activityId: 1,
        name: '李四',
        phone: '13900139000',
        email: 'lisi@example.com'
      }
      const mockResponse = { code: 200, data: { id: 2, ...registrationData } }
      request.mockResolvedValue(mockResponse)

      const result = await activityApi.registerActivity(registrationData)

      expect(request).toHaveBeenCalledWith({
        url: '/activities/register',
        method: 'post',
        data: registrationData
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('cancelRegistration 取消报名', () => {
    it('应该调用 POST 请求并传递报名记录 ID', async () => {
      const mockResponse = { code: 200, data: null }
      request.mockResolvedValue(mockResponse)

      const result = await activityApi.cancelRegistration(1)

      expect(request).toHaveBeenCalledWith({
        url: '/activities/registrations/1/cancel',
        method: 'post'
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('exportRegistrations 导出报名名单', () => {
    it('应该设置 responseType 为 blob', async () => {
      const mockBlob = new Blob(['test'], { type: 'text/csv' })
      const mockResponse = { data: mockBlob }
      request.mockResolvedValue(mockResponse)

      const result = await activityApi.exportRegistrations(1)

      expect(request).toHaveBeenCalledWith({
        url: '/activities/1/registrations/export',
        method: 'get',
        responseType: 'blob'
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('getActivityStatistics 获取活动统计', () => {
    it('应该调用正确的统计接口', async () => {
      const mockResponse = {
        code: 200,
        data: {
          totalActivities: 10,
          publishedActivities: 6,
          upcomingActivities: 3
        }
      }
      request.mockResolvedValue(mockResponse)

      const result = await activityApi.getActivityStatistics()

      expect(request).toHaveBeenCalledWith({
        url: '/activities/statistics/summary',
        method: 'get'
      })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('API 错误处理', () => {
    it('应该正确传递并抛出请求错误', async () => {
      const mockError = new Error('Network Error')
      request.mockRejectedValue(mockError)

      await expect(activityApi.getActivityList()).rejects.toThrow('Network Error')
    })

    it('应该正确处理 404 错误', async () => {
      const mockError = new Error('Request failed with status code 404')
      request.mockRejectedValue(mockError)

      await expect(activityApi.getActivityById(9999)).rejects.toThrow('404')
    })
  })
})
