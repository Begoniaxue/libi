import request from '@/utils/request'

export function getDashboard() {
  return request({
    url: '/statistics/dashboard',
    method: 'get'
  })
}

export function getCollectionStatistics(params) {
  return request({
    url: '/statistics/collection',
    method: 'get',
    params
  })
}

export function getBorrowStatistics(params) {
  return request({
    url: '/statistics/borrow',
    method: 'get',
    params
  })
}

export function getReaderStatistics(params) {
  return request({
    url: '/statistics/reader',
    method: 'get',
    params
  })
}

export function getHotResourcesStatistics(params) {
  return request({
    url: '/statistics/hot-resources',
    method: 'get',
    params
  })
}

export function getFeeStatistics(params) {
  return request({
    url: '/statistics/fee',
    method: 'get',
    params
  })
}

export function getAllStatistics(params) {
  return request({
    url: '/statistics/all',
    method: 'get',
    params
  })
}

export function generateMonthlyReport(year, month) {
  return request({
    url: '/statistics/report/monthly',
    method: 'get',
    params: { year, month }
  })
}

export function generateYearlyReport(year) {
  return request({
    url: '/statistics/report/yearly',
    method: 'get',
    params: { year }
  })
}

export function downloadMonthlyReport(year, month, format) {
  return request({
    url: '/statistics/report/monthly/download',
    method: 'get',
    params: { year, month, format },
    responseType: 'blob'
  })
}

export function downloadYearlyReport(year, format) {
  return request({
    url: '/statistics/report/yearly/download',
    method: 'get',
    params: { year, format },
    responseType: 'blob'
  })
}

export function exportBooksExcel() {
  return request({
    url: '/statistics/export/books/excel',
    method: 'get',
    responseType: 'blob'
  })
}

export function exportBorrowExcel(params) {
  return request({
    url: '/statistics/export/borrow/excel',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function exportReadersExcel() {
  return request({
    url: '/statistics/export/readers/excel',
    method: 'get',
    responseType: 'blob'
  })
}

export function exportFeeExcel(params) {
  return request({
    url: '/statistics/export/fee/excel',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function exportStatisticsPdf(reportType, params) {
  return request({
    url: `/statistics/export/${reportType}/pdf`,
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function downloadFile(blob, filename) {
  const url = window.URL.createObjectURL(new Blob([blob]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
