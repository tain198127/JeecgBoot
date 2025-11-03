import request from '@/utils/request'

// 分页查询元数据
export function getMetadataPage(query: any) {
  return request({
    url: '/system/metadata/page',
    method: 'get',
    params: query
  })
}

// 保存元数据
export function saveMetadata(data: any) {
  return request({
    url: '/system/metadata/save',
    method: 'post',
    data
  })
}

// 删除元数据
export function deleteMetadata(id: string) {
  return request({
    url: `/system/metadata/delete/${id}`,
    method: 'delete'
  })
}

// 更新元数据状态
export function updateMetadataStatus(id: string, status: string) {
  return request({
    url: `/system/metadata/updateStatus/${id}/${status}`,
    method: 'put'
  })
}

// 导出元数据
export function exportMetadataExcel(query: any) {
  return request({
    url: '/system/metadata/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 导入元数据
export function importMetadataExcel(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/system/metadata/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载导入模板
export function downloadMetadataTemplate() {
  return request({
    url: '/system/metadata/downloadTemplate',
    method: 'get',
    responseType: 'blob'
  })
}

// AI生成元数据字段
export function generateMetadataFields(cnName: string, businessDomain: string) {
  return request({
    url: '/system/metadata/generateFields',
    method: 'get',
    params: { cnName, businessDomain }
  })
}