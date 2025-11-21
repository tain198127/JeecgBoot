import request from '@/utils/request'

// 上传PDF文件
export function uploadPdf(data) {
  return request({
    url: '/system/pdfDocument/upload',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取PDF文档列表
export function getPdfDocumentList(page, limit) {
  return request({
    url: '/system/pdfDocument/list',
    method: 'get',
    params: {
      page: page,
      limit: limit
    }
  })
}

// 根据ID获取PDF文档
export function getPdfDocumentById(id) {
  return request({
    url: '/system/pdfDocument/getById',
    method: 'get',
    params: {
      id: id
    }
  })
}

// 解析PDF文档内容
export function parsePdfContent(pdfId) {
  return request({
    url: '/system/pdfDocument/parse',
    method: 'post',
    params: {
      pdfId: pdfId
    }
  })
}

// 抽取PDF文档指标
export function extractPdfIndicators(pdfId) {
  return request({
    url: '/system/pdfDocument/extract',
    method: 'post',
    params: {
      pdfId: pdfId
    }
  })
}

// 智能校验PDF文档指标
export function validatePdfIndicators(pdfId) {
  return request({
    url: '/system/pdfDocument/validate',
    method: 'post',
    params: {
      pdfId: pdfId
    }
  })
}

// 更新PDF文档
export function updatePdfDocument(data) {
  return request({
    url: '/system/pdfDocument/update',
    method: 'put',
    data: data
  })
}

// 删除PDF文档
export function deletePdfDocument(id) {
  return request({
    url: '/system/pdfDocument/delete',
    method: 'delete',
    params: {
      id: id
    }
  })
}

// 批量删除PDF文档
export function deleteBatchPdfDocuments(ids) {
  return request({
    url: '/system/pdfDocument/deleteBatch',
    method: 'delete',
    data: ids
  })
}

// 导出PDF文档列表
export function exportPdfDocumentList(query) {
  return request({
    url: '/system/pdfDocument/exportXls',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 导入PDF文档列表
export function importPdfDocumentList(data) {
  return request({
    url: '/system/pdfDocument/importExcel',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
