import request from '@/utils/request'

export function getReaderList(params) {
  return request({
    url: '/readers',
    method: 'get',
    params
  })
}

export function getReaderById(id) {
  return request({
    url: `/readers/${id}`,
    method: 'get'
  })
}

export function getReaderByCardNo(cardNo) {
  return request({
    url: `/readers/cardNo/${cardNo}`,
    method: 'get'
  })
}

export function addReader(data) {
  return request({
    url: '/readers',
    method: 'post',
    data
  })
}

export function updateReader(data) {
  return request({
    url: '/readers',
    method: 'put',
    data
  })
}

export function deleteReader(id) {
  return request({
    url: `/readers/${id}`,
    method: 'delete'
  })
}

export function generateCardNo() {
  return request({
    url: '/readers/generate-card-no',
    method: 'get'
  })
}
