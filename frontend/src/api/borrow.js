import request from '@/utils/request'

export function getRecordList(params) {
  return request({
    url: '/borrow/records',
    method: 'get',
    params
  })
}

export function getReaderBorrowInfo(readerId) {
  return request({
    url: `/borrow/reader-info/${readerId}`,
    method: 'get'
  })
}

export function borrowBook(bookId, readerId) {
  return request({
    url: '/borrow/borrow',
    method: 'post',
    params: { bookId, readerId }
  })
}

export function returnBook(recordId) {
  return request({
    url: `/borrow/return/${recordId}`,
    method: 'post'
  })
}

export function checkOverdue() {
  return request({
    url: '/borrow/check-overdue',
    method: 'post'
  })
}

export function getStatistics() {
  return request({
    url: '/statistics',
    method: 'get'
  })
}
