import request from '@/utils/request'

export function getBookList(params) {
  return request({
    url: '/books',
    method: 'get',
    params
  })
}

export function getBookById(id) {
  return request({
    url: `/books/${id}`,
    method: 'get'
  })
}

export function addBook(data) {
  return request({
    url: '/books',
    method: 'post',
    data
  })
}

export function updateBook(data) {
  return request({
    url: '/books',
    method: 'put',
    data
  })
}

export function deleteBook(id) {
  return request({
    url: `/books/${id}`,
    method: 'delete'
  })
}

export function getAvailableBooks() {
  return request({
    url: '/books/available',
    method: 'get'
  })
}
