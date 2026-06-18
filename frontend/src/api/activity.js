import request from '@/utils/request'

export function getActivityList(params) {
  return request({
    url: '/activities',
    method: 'get',
    params
  })
}

export function getActivityById(id) {
  return request({
    url: `/activities/${id}`,
    method: 'get'
  })
}

export function addActivity(data) {
  return request({
    url: '/activities',
    method: 'post',
    data
  })
}

export function updateActivity(data) {
  return request({
    url: '/activities',
    method: 'put',
    data
  })
}

export function deleteActivity(id) {
  return request({
    url: `/activities/${id}`,
    method: 'delete'
  })
}

export function getRegistrationList(activityId, params) {
  return request({
    url: `/activities/${activityId}/registrations`,
    method: 'get',
    params
  })
}

export function registerActivity(data) {
  return request({
    url: '/activities/register',
    method: 'post',
    data
  })
}

export function cancelRegistration(id) {
  return request({
    url: `/activities/registrations/${id}/cancel`,
    method: 'post'
  })
}

export function exportRegistrations(activityId) {
  return request({
    url: `/activities/${activityId}/registrations/export`,
    method: 'get',
    responseType: 'blob'
  })
}

export function getActivityStatistics() {
  return request({
    url: '/activities/statistics/summary',
    method: 'get'
  })
}
