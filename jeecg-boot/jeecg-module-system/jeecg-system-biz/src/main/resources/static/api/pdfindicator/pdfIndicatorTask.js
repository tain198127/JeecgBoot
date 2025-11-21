import request from '@/utils/request'

// 上传PDF文件
export function uploadPdf(data) {
  return request({
    url: '/pdfindicator/pdfDocument/upload',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取PDF文档列表
export function getPdfDocumentList() {
  return request({
    url: '/pdfindicator/pdfDocument/list',
    method: 'get'
  })
}

// 创建PDF指标抽取任务
export function createTask(data) {
  return request({
    url: '/pdfindicator/pdfIndicatorTask/createTask',
    method: 'post',
    data: data
  })
}

// 执行PDF指标抽取任务
export function executeTask(taskId) {
  return request({
    url: '/pdfindicator/pdfIndicatorTask/executeTask',
    method: 'post',
    params: {
      taskId: taskId
    }
  })
}

// 查询任务状态
export function queryTaskStatus(taskId) {
  return request({
    url: '/pdfindicator/pdfIndicatorTask/queryTaskStatus',
    method: 'get',
    params: {
      taskId: taskId
    }
  })
}

// 查询任务结果
export function queryTaskResult(taskId) {
  return request({
    url: '/pdfindicator/pdfIndicatorTask/queryTaskResult',
    method: 'get',
    params: {
      taskId: taskId
    }
  })
}

// 获取任务列表
export function getTaskList(page, size) {
  return request({
    url: '/pdfindicator/pdfIndicatorTask/list',
    method: 'get',
    params: {
      pageNo: page,
      pageSize: size
    }
  })
}

// 删除任务
export function deleteTask(id) {
  return request({
    url: '/pdfindicator/pdfIndicatorTask/delete',
    method: 'delete',
    params: {
      id: id
    }
  })
}

// 批量删除任务
export function deleteBatch(ids) {
  return request({
    url: '/pdfindicator/pdfIndicatorTask/deleteBatch',
    method: 'delete',
    params: {
      ids: ids
    }
  })
}
