import request from '@/utils/request'

export const loginByCardNo = (cardNo) => {
  return request({
    url: `/mobile/reader/${cardNo}`,
    method: 'get'
  })
}

export const getReaderInfo = (readerId) => {
  return request({
    url: `/mobile/reader/${readerId}/info`,
    method: 'get'
  })
}

export const getBorrowRecords = (readerId, params) => {
  return request({
    url: `/mobile/reader/${readerId}/borrow-records`,
    method: 'get',
    params
  })
}

export const getBorrowingBooks = (readerId, params) => {
  return request({
    url: `/mobile/reader/${readerId}/borrowing`,
    method: 'get',
    params
  })
}

export const getOverdueRecords = (readerId, params) => {
  return request({
    url: `/mobile/reader/${readerId}/overdue`,
    method: 'get',
    params
  })
}

export const getFeeRecords = (readerId, params) => {
  return request({
    url: `/mobile/reader/${readerId}/fee-records`,
    method: 'get',
    params
  })
}

export const getRenewLogs = (readerId, params) => {
  return request({
    url: `/mobile/reader/${readerId}/renew-logs`,
    method: 'get',
    params
  })
}

export const searchBooks = (params) => {
  return request({
    url: '/mobile/books/search',
    method: 'get',
    params
  })
}

export const getBookDetail = (id) => {
  return request({
    url: `/mobile/books/${id}`,
    method: 'get'
  })
}

export const getCategories = () => {
  return request({
    url: '/mobile/books/categories',
    method: 'get'
  })
}

export const renewBook = (recordId) => {
  return request({
    url: `/mobile/borrow/renew/${recordId}`,
    method: 'post'
  })
}

export const getBorrowRecordDetail = (recordId) => {
  return request({
    url: `/mobile/borrow/record/${recordId}`,
    method: 'get'
  })
}
