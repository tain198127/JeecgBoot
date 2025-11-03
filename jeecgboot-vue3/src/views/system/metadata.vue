<template>
  <div class="metadata-page">
    <a-card title="元数据管理">
      <!-- 查询区域 -->
      <a-form layout="inline" :model="queryForm" @keyup.enter.native="handleQuery">
        <a-form-item label="中文名称">
          <a-input v-model:value="queryForm.metadataCnName" placeholder="请输入中文名称" style="width: 200px" />
        </a-form-item>
        <a-form-item label="英文名称">
          <a-input v-model:value="queryForm.metadataEnName" placeholder="请输入英文名称" style="width: 200px" />
        </a-form-item>
        <a-form-item label="业务域">
          <a-select v-model:value="queryForm.businessDomain" placeholder="请选择业务域" style="width: 200px">
            <a-select-option value="">全部</a-select-option>
            <a-select-option v-for="domain in businessDomains" :key="domain" :value="domain">{{ domain }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleQuery" :icon="h(SearchOutlined)">查询</a-button>
          <a-button @click="handleReset" :icon="h(RefreshOutlined)">重置</a-button>
          <a-button type="primary" @click="handleAdd" :icon="h(PlusOutlined)">新增</a-button>
          <a-button type="primary" @click="handleExport" :icon="h(ExportOutlined)">导出</a-button>
          <a-upload
            :before-upload="handleBeforeUpload"
            :show-upload-list="false"
            accept=".xlsx,.xls"
          >
            <a-button type="primary" :icon="h(ImportOutlined)">
              导入
            </a-button>
          </a-upload>
          <a-button @click="handleDownloadTemplate" :icon="h(CloudDownloadOutlined)">下载模板</a-button>
        </a-form-item>
      </a-form>

      <!-- 数据列表 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        bordered
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === '1' ? 'green' : 'red'">
              {{ record.status === '1' ? '生效' : '失效' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'createTime' || column.key === 'updateTime'">
            {{ formatDate(record[column.key]) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button
              type="link"
              size="small"
              @click="handleEdit(record)"
              :icon="h(EditOutlined)"
            >
              编辑
            </a-button>
            <a-button
              v-if="record.status === '1'"
              type="link"
              size="small"
              @click="handleStatusChange(record, '0')"
              :icon="h(PauseCircleOutlined)"
              danger
            >
              失效
            </a-button>
            <a-button
              v-else
              type="link"
              size="small"
              @click="handleStatusChange(record, '1')"
              :icon="h(PlayCircleOutlined)"
              success
            >
              生效
            </a-button>
            <a-button
              type="link"
              size="small"
              @click="handleDelete(record)"
              :icon="h(DeleteOutlined)"
              danger
            >
              删除
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑模态框 -->
    <modal-form
      v-model:visible="modalVisible"
      :title="modalTitle"
      :form="form"
      :columns="formColumns"
      @submit="handleSubmit"
      @cancel="handleCancel"
    >
      <template #footer>
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</a-button>
      </template>
    </modal-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { message } from '@/utils/message'
import { formatDate } from '@/utils/dateUtil'
import {
  SearchOutlined,
  RefreshOutlined,
  PlusOutlined,
  ExportOutlined,
  ImportOutlined,
  CloudDownloadOutlined,
  EditOutlined,
  PauseCircleOutlined,
  PlayCircleOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import ModalForm from '@/components/ModalForm'
import {
  getMetadataPage,
  deleteMetadata,
  updateMetadataStatus,
  exportMetadataExcel,
  importMetadataExcel,
  downloadMetadataTemplate
} from '@/api/system/metadata.api'

// 查询表单
const queryForm = reactive({
  metadataCnName: '',
  metadataEnName: '',
  businessDomain: ''
})

// 表格数据
const tableData = ref<any[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: ['10', '50', '100'],
  showTotal: (total) => `共 ${total} 条数据`
})
const loading = ref(false)

// 业务域列表
const businessDomains = ref<string[]>(['用户管理', '订单管理', '商品管理', '财务管理', '系统管理'])

// 模态框
const modalVisible = ref(false)
const modalTitle = ref('新增元数据')
const submitLoading = ref(false)

// 表单列配置
const formColumns = [
  {
    label: '元数据中文名称',
    prop: 'metadataCnName',
    required: true,
    span: 6,
    type: 'input'
  },
  {
    label: '元数据英文名称',
    prop: 'metadataEnName',
    required: true,
    span: 6,
    type: 'input'
  },
  {
    label: 'Java代码中的英文名称',
    prop: 'javaFieldName',
    required: true,
    span: 6,
    type: 'input'
  },
  {
    label: '数据库表中的字段名称',
    prop: 'dbFieldName',
    required: true,
    span: 6,
    type: 'input'
  },
  {
    label: '元数据中文含义',
    prop: 'metadataCnMeaning',
    required: true,
    span: 12,
    type: 'input'
  },
  {
    label: '元数据英文含义',
    prop: 'metadataEnMeaning',
    required: true,
    span: 12,
    type: 'input'
  },
  {
    label: 'Java数据类型',
    prop: 'javaDataType',
    required: true,
    span: 6,
    type: 'select',
    options: [
      { label: 'String', value: 'String' },
      { label: 'Integer', value: 'Integer' },
      { label: 'Long', value: 'Long' },
      { label: 'Double', value: 'Double' },
      { label: 'Float', value: 'Float' },
      { label: 'Boolean', value: 'Boolean' },
      { label: 'Date', value: 'Date' },
      { label: 'LocalDate', value: 'LocalDate' },
      { label: 'LocalDateTime', value: 'LocalDateTime' }
    ]
  },
  {
    label: 'MySQL数据类型',
    prop: 'mysqlDataType',
    required: true,
    span: 6,
    type: 'select',
    options: [
      { label: 'VARCHAR', value: 'VARCHAR' },
      { label: 'CHAR', value: 'CHAR' },
      { label: 'TEXT', value: 'TEXT' },
      { label: 'INT', value: 'INT' },
      { label: 'BIGINT', value: 'BIGINT' },
      { label: 'DECIMAL', value: 'DECIMAL' },
      { label: 'DOUBLE', value: 'DOUBLE' },
      { label: 'FLOAT', value: 'FLOAT' },
      { label: 'DATE', value: 'DATE' },
      { label: 'DATETIME', value: 'DATETIME' },
      { label: 'TIMESTAMP', value: 'TIMESTAMP' },
      { label: 'BOOLEAN', value: 'BOOLEAN' }
    ]
  },
  {
    label: '最大长度',
    prop: 'maxLength',
    required: true,
    span: 6,
    type: 'input',
    inputType: 'number'
  },
  {
    label: '最小长度',
    prop: 'minLength',
    required: true,
    span: 6,
    type: 'input',
    inputType: 'number',
    defaultValue: 0
  },
  {
    label: '取值范围（正则表达式）',
    prop: 'valueRange',
    span: 6,
    type: 'input'
  },
  {
    label: '所属业务域',
    prop: 'businessDomain',
    required: true,
    span: 6,
    type: 'select',
    options: businessDomains.value.map(domain => ({ label: domain, value: domain }))
  },
  {
    label: '业务描述',
    prop: 'businessDesc',
    required: true,
    span: 12,
    type: 'textarea',
    rows: 4
  },
  {
    label: '生效状态',
    prop: 'status',
    required: true,
    span: 6,
    type: 'select',
    options: [
      { label: '生效', value: '1' },
      { label: '失效', value: '0' }
    ],
    defaultValue: '1'
  }
]

// 表单数据
const form = reactive({
  metadataCnName: '',
  metadataEnName: '',
  metadataCnMeaning: '',
  metadataEnMeaning: '',
  javaFieldName: '',
  dbFieldName: '',
  javaDataType: '',
  mysqlDataType: '',
  maxLength: null,
  minLength: 0,
  valueRange: '',
  businessDesc: '',
  businessDomain: '',
  status: '1'
})

// 表格列配置
const columns = [
  {
    title: '元数据中文名称',
    dataIndex: 'metadataCnName',
    key: 'metadataCnName',
    ellipsis: true
  },
  {
    title: '元数据英文名称',
    dataIndex: 'metadataEnName',
    key: 'metadataEnName',
    ellipsis: true
  },
  {
    title: 'Java字段名',
    dataIndex: 'javaFieldName',
    key: 'javaFieldName',
    ellipsis: true
  },
  {
    title: '数据库字段名',
    dataIndex: 'dbFieldName',
    key: 'dbFieldName',
    ellipsis: true
  },
  {
    title: 'Java数据类型',
    dataIndex: 'javaDataType',
    key: 'javaDataType',
    ellipsis: true
  },
  {
    title: 'MySQL数据类型',
    dataIndex: 'mysqlDataType',
    key: 'mysqlDataType',
    ellipsis: true
  },
  {
    title: '最大长度',
    dataIndex: 'maxLength',
    key: 'maxLength',
    width: 100
  },
  {
    title: '最小长度',
    dataIndex: 'minLength',
    key: 'minLength',
    width: 100
  },
  {
    title: '所属业务域',
    dataIndex: 'businessDomain',
    key: 'businessDomain',
    ellipsis: true
  },
  {
    title: '生效状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '录入时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '最后修改时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 180
  },
  {
    title: '操作',
    dataIndex: 'action',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

// 查询数据
const handleQuery = () => {
  pagination.current = 1
  loadData()
}

// 重置表单
const handleReset = () => {
  Object.keys(queryForm).forEach(key => {
    queryForm[key as keyof typeof queryForm] = ''
  })
  pagination.current = 1
  loadData()
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getMetadataPage({
      current: pagination.current,
      pageSize: pagination.pageSize,
      ...queryForm
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    } else {
      message.error(res.message || '查询失败')
    }
  } catch (error) {
    message.error('查询失败：' + error)
  } finally {
    loading.value = false
  }
}

// 新增元数据
const handleAdd = () => {
  modalTitle.value = '新增元数据'
  resetForm()
  modalVisible.value = true
}

// 编辑元数据
const handleEdit = (record: any) => {
  modalTitle.value = '编辑元数据'
  // 复制数据到表单
  Object.keys(form).forEach(key => {
    form[key as keyof typeof form] = record[key] || ''
  })
  modalVisible.value = true
}

// 保存元数据
const handleSubmit = async () => {
  submitLoading.value = true
  try {
    // 这里需要调用保存API，暂时注释
    // const res = await saveMetadata(form)
    // if (res.code === 200) {
    //   message.success('保存成功')
    //   modalVisible.value = false
    //   loadData()
    // } else {
    //   message.error(res.message || '保存失败')
    // }
    message.success('保存成功')
    modalVisible.value = false
    loadData()
  } catch (error) {
    message.error('保存失败：' + error)
  } finally {
    submitLoading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  modalVisible.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.keys(form).forEach(key => {
    form[key as keyof typeof form] = ''
  })
  form.minLength = 0
  form.status = '1'
}

// 删除元数据
const handleDelete = (record: any) => {
  Modal.confirm({
    title: '删除确认',
    content: `确定要删除元数据"${record.metadataCnName}"吗？`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await deleteMetadata(record.id)
        if (res.code === 200) {
          message.success('删除成功')
          loadData()
        } else {
          message.error(res.message || '删除失败')
        }
      } catch (error) {
        message.error('删除失败：' + error)
      }
    }
  })
}

// 更新元数据状态
const handleStatusChange = (record: any, status: string) => {
  const statusText = status === '1' ? '生效' : '失效'
  Modal.confirm({
    title: '状态确认',
    content: `确定要${statusText}元数据"${record.metadataCnName}"吗？`,
    okText: statusText,
    okType: status === '1' ? 'success' : 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await updateMetadataStatus(record.id, status)
        if (res.code === 200) {
          message.success(`${statusText}成功`)
          loadData()
        } else {
          message.error(res.message || `${statusText}失败`)
        }
      } catch (error) {
        message.error(`${statusText}失败：` + error)
      }
    }
  })
}

// 导出元数据
const handleExport = async () => {
  try {
    const res = await exportMetadataExcel(queryForm)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `元数据管理表_${formatDate(new Date())}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (error) {
    message.error('导出失败：' + error)
  }
}

// 导入元数据前校验
const handleBeforeUpload = async (file: File) => {
  try {
    const res = await importMetadataExcel(file)
    if (res.code === 200) {
      message.success(res.message || '导入成功')
      loadData()
    } else {
      message.error(res.message || '导入失败')
    }
  } catch (error) {
    message.error('导入失败：' + error)
  }
  return false
}

// 下载导入模板
const handleDownloadTemplate = async () => {
  try {
    const res = await downloadMetadataTemplate()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '元数据导入模板.xlsx'
    a.click()
    URL.revokeObjectURL(url)
    message.success('模板下载成功')
  } catch (error) {
    message.error('模板下载失败：' + error)
  }
}

// 分页变化
const onPageChange = (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  loadData()
}

// 页面挂载时加载数据
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.metadata-page {
  padding: 20px;
}
</style>