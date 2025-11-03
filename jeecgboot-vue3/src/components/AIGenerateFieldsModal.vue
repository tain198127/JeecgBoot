<template>
  <a-modal
    v-model:open="visible"
    :title="title"
    :width="500"
    @ok="handleOk"
    @cancel="handleCancel"
    okText="生成"
    cancelText="取消"
  >
    <a-form :model="form" layout="vertical" :rules="rules" ref="formRef">
      <a-form-item label="元数据中文名称" field="cnName" required>
        <a-input
          v-model:value="form.cnName"
          placeholder="请输入元数据中文名称"
          @input="handleInput"
        />
      </a-form-item>
      <a-form-item label="所属业务域" field="businessDomain" required>
        <a-select
          v-model:value="form.businessDomain"
          placeholder="请选择业务域"
        >
          <a-select-option v-for="domain in businessDomains" :key="domain" :value="domain">
            {{ domain }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="生成结果预览">
        <a-card title="元数据英文名称" size="small" :bordered="false">
          {{ previewResult.metadataEnName || '未生成' }}
        </a-card>
        <a-card title="Java代码中的英文名称" size="small" :bordered="false" style="margin-top: 10px">
          {{ previewResult.javaFieldName || '未生成' }}
        </a-card>
        <a-card title="数据库表中的字段名称" size="small" :bordered="false" style="margin-top: 10px">
          {{ previewResult.dbFieldName || '未生成' }}
        </a-card>
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="dialog-footer">
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" @click="handleGenerate" :loading="generating">
          生成字段
        </a-button>
        <a-button type="primary" @click="handleOk" :disabled="!isGenerated">
          确认并填充
        </a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { message } from '@/utils/message'
import { generateMetadataFields } from '@/api/system/metadata.api'
import type { FormInstance } from 'ant-design-vue'

// Props
interface Props {
  visible: boolean
  title?: string
  businessDomains?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  title: 'AI生成元数据字段',
  businessDomains: () => ['用户管理', '订单管理', '商品管理', '财务管理', '系统管理']
})

// Emits
const emit = defineEmits<{
  'update:visible': [visible: boolean]
  'generate': [result: { metadataEnName: string; javaFieldName: string; dbFieldName: string }]
  'cancel': []
}>()

// Form reference
const formRef = ref<FormInstance | null>(null)

// Form data
const form = reactive({
  cnName: '',
  businessDomain: ''
})

// Form rules
const rules = {
  cnName: [
    { required: true, message: '请输入元数据中文名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  businessDomain: [
    { required: true, message: '请选择所属业务域', trigger: 'change' }
  ]
}

// Loading state
const generating = ref(false)

// Preview result
const previewResult = reactive({
  metadataEnName: '',
  javaFieldName: '',
  dbFieldName: ''
})

// Check if generated
const isGenerated = computed(() => {
  return !!(previewResult.metadataEnName && previewResult.javaFieldName && previewResult.dbFieldName)
})

// Handle input change
const handleInput = () => {
  // Clear preview when input changes
  if (isGenerated.value) {
    previewResult.metadataEnName = ''
    previewResult.javaFieldName = ''
    previewResult.dbFieldName = ''
  }
}

// Handle generate fields
const handleGenerate = async () => {
  if (!formRef.value) return

  // Validate form
  const valid = await formRef.value.validate()
  if (!valid) return

  generating.value = true
  try {
    const res = await generateMetadataFields(form.cnName, form.businessDomain)
    if (res.code === 200) {
      previewResult.metadataEnName = res.data.metadataEnName
      previewResult.javaFieldName = res.data.javaFieldName
      previewResult.dbFieldName = res.data.dbFieldName
      message.success('生成成功')
    } else {
      message.error(res.message || '生成失败')
    }
  } catch (error) {
    message.error('生成失败：' + error)
  } finally {
    generating.value = false
  }
}

// Handle ok
const handleOk = () => {
  if (!isGenerated.value) {
    message.warning('请先生成字段')
    return
  }

  emit('generate', {
    metadataEnName: previewResult.metadataEnName,
    javaFieldName: previewResult.javaFieldName,
    dbFieldName: previewResult.dbFieldName
  })
  handleCancel()
}

// Handle cancel
const handleCancel = () => {
  emit('update:visible', false)
  emit('cancel')
  resetForm()
}

// Reset form
const resetForm = () => {
  form.cnName = ''
  form.businessDomain = ''
  previewResult.metadataEnName = ''
  previewResult.javaFieldName = ''
  previewResult.dbFieldName = ''
  formRef.value?.resetFields()
}

// Watch visible prop
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      // Reset form when modal opens
      resetForm()
    }
  }
)
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>