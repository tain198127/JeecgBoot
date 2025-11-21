<template>
  <div class="app-container">
    <el-card title="PDF指标抽取任务管理" class="box-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-button type="primary" @click="handleUpload">上传PDF文件</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="success" @click="handleCreateTask">创建抽取任务</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="warning" @click="handleRefresh">刷新任务列表</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="danger" @click="handleDeleteSelected">删除选中任务</el-button>
        </el-col>
      </el-row>

      <!-- 文件上传对话框 -->
      <el-dialog title="上传PDF文件" :visible.sync="uploadDialogVisible" width="500px" @close="handleUploadDialogClose">
        <el-upload
          ref="upload"
          :auto-upload="false"
          :file-list="fileList"
          :on-change="handleFileChange"
          :before-upload="beforeUpload"
          accept=".pdf"
          drag
          multiple
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将PDF文件拖到此处，或<em>点击上传</em></div>
          <div class="el-upload__tip" slot="tip">只能上传PDF文件，且不超过10MB</div>
        </el-upload>
        <div slot="footer" class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitUpload">确定上传</el-button>
        </div>
      </el-dialog>

      <!-- 创建任务对话框 -->
      <el-dialog title="创建PDF指标抽取任务" :visible.sync="createTaskDialogVisible" width="500px">
        <el-form :model="createTaskForm" :rules="createTaskRules" ref="createTaskForm">
          <el-form-item label="PDF文档" prop="pdfId">
            <el-select v-model="createTaskForm.pdfId" placeholder="请选择PDF文档">
              <el-option
                v-for="pdf in pdfDocumentList"
                :key="pdf.id"
                :label="pdf.fileName"
                :value="pdf.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="指标类型" prop="indicatorTypes">
            <el-checkbox-group v-model="createTaskForm.indicatorTypes">
              <el-checkbox label="资产总额" name="type"></el-checkbox>
              <el-checkbox label="负债总额" name="type"></el-checkbox>
              <el-checkbox label="营业收入" name="type"></el-checkbox>
              <el-checkbox label="利润总额" name="type"></el-checkbox>
              <el-checkbox label="净利润" name="type"></el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="createTaskDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateTaskSubmit">确定创建</el-button>
        </div>
      </el-dialog>

      <!-- 任务列表 -->
      <el-table
        :data="taskList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="taskId" label="任务ID" width="180"></el-table-column>
        <el-table-column prop="pdfId" label="PDF文档ID" width="180"></el-table-column>
        <el-table-column prop="indicatorTypes" label="指标类型" width="200"></el-table-column>
        <el-table-column prop="taskStatus" label="任务状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getTagType(scope.row.taskStatus)">{{ scope.row.taskStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180"></el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180"></el-table-column>
        <el-table-column prop="taskResult" label="任务结果" width="200"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="handleExecuteTask(scope.row.taskId)"
              :disabled="scope.row.taskStatus === 'running' || scope.row.taskStatus === 'success'"
            >
              执行任务
            </el-button>
            <el-button
              size="mini"
              type="info"
              @click="handleViewTaskResult(scope.row.taskId)"
              :disabled="scope.row.taskStatus !== 'success'"
            >
              查看结果
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      ></el-pagination>

      <!-- 任务结果对话框 -->
      <el-dialog title="PDF指标抽取任务结果" :visible.sync="taskResultDialogVisible" width="800px">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="解析结果" name="parseResult">
            <el-form :model="taskResult" label-width="120px">
              <el-form-item label="PDF文档ID">
                <span>{{ taskResult.parseResult ? taskResult.parseResult.pdfId : '' }}</span>
              </el-form-item>
              <el-form-item label="文档标题">
                <span>{{ taskResult.parseResult ? taskResult.parseResult.title : '' }}</span>
              </el-form-item>
              <el-form-item label="文档页数">
                <span>{{ taskResult.parseResult ? taskResult.parseResult.pageCount : '' }}</span>
              </el-form-item>
              <el-form-item label="文档大小">
                <span>{{ taskResult.parseResult ? taskResult.parseResult.fileSize : '' }}</span>
              </el-form-item>
              <el-form-item label="文档内容">
                <el-input
                  :value="taskResult.parseResult ? taskResult.parseResult.content : ''"
                  type="textarea"
                  :rows="10"
                  readonly
                ></el-input>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="指标抽取结果" name="extractResult">
            <el-table :data="taskResult.extractResult ? taskResult.extractResult.indicators : []" style="width: 100%">
              <el-table-column prop="name" label="指标名称" width="150"></el-table-column>
              <el-table-column prop="value" label="指标值" width="150"></el-table-column>
              <el-table-column prop="type" label="指标类型" width="120"></el-table-column>
              <el-table-column prop="unit" label="单位" width="100"></el-table-column>
              <el-table-column prop="pageNumber" label="所在页码" width="120"></el-table-column>
              <el-table-column prop="confidence" label="置信度" width="120"></el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="智能校验结果" name="validateResult">
            <el-form :model="taskResult" label-width="120px">
              <el-form-item label="校验状态">
                <el-tag :type="taskResult.validateResult ? (taskResult.validateResult.isValid ? 'success' : 'danger') : ''">
                  {{ taskResult.validateResult ? (taskResult.validateResult.isValid ? '校验通过' : '校验不通过') : '' }}
                </el-tag>
              </el-form-item>
              <el-form-item label="校验结果">
                <el-input
                  :value="taskResult.validateResult ? taskResult.validateResult.result : ''"
                  type="textarea"
                  :rows="10"
                  readonly
                ></el-input>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
        <div slot="footer" class="dialog-footer">
          <el-button @click="taskResultDialogVisible = false">关闭</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { uploadPdf, getPdfDocumentList, createTask, executeTask, queryTaskStatus, queryTaskResult, getTaskList } from '@/api/pdfindicator/pdfIndicatorTask'

export default {
  name: 'PdfIndicatorTask',
  data() {
    return {
      loading: false,
      taskList: [],
      pdfDocumentList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      selectedTaskIds: [],
      uploadDialogVisible: false,
      createTaskDialogVisible: false,
      taskResultDialogVisible: false,
      fileList: [],
      createTaskForm: {
        pdfId: '',
        indicatorTypes: []
      },
      createTaskRules: {
        pdfId: [
          { required: true, message: '请选择PDF文档', trigger: 'change' }
        ],
        indicatorTypes: [
          { required: true, message: '请选择至少一个指标类型', trigger: 'change' }
        ]
      },
      taskResult: {},
      activeTab: 'parseResult'
    }
  },
  mounted() {
    this.loadTaskList()
    this.loadPdfDocumentList()
  },
  methods: {
    // 加载任务列表
    loadTaskList() {
      this.loading = true
      getTaskList(this.currentPage, this.pageSize).then(response => {
        this.taskList = response.data.records
        this.total = response.data.total
        this.loading = false
      }).catch(error => {
        this.loading = false
        this.$message.error('加载任务列表失败: ' + error.message)
      })
    },
    // 加载PDF文档列表
    loadPdfDocumentList() {
      getPdfDocumentList().then(response => {
        this.pdfDocumentList = response.data
      }).catch(error => {
        this.$message.error('加载PDF文档列表失败: ' + error.message)
      })
    },
    // 上传文件
    handleUpload() {
      this.uploadDialogVisible = true
      this.fileList = []
    },
    // 文件改变
    handleFileChange(file, fileList) {
      this.fileList = fileList
    },
    // 上传前校验
    beforeUpload(file) {
      const isPDF = file.type === 'application/pdf'
      if (!isPDF) {
        this.$message.error('只能上传PDF文件!')
        return false
      }
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isLt10M) {
        this.$message.error('PDF文件大小不能超过10MB!')
        return false
      }
      return true
    },
    // 提交上传
    handleSubmitUpload() {
      if (this.fileList.length === 0) {
        this.$message.warning('请选择要上传的PDF文件')
        return
      }
      
      const formData = new FormData()
      this.fileList.forEach(file => {
        formData.append('file', file.raw)
      })
      
      uploadPdf(formData).then(response => {
        this.$message.success('PDF文件上传成功')
        this.uploadDialogVisible = false
        this.loadPdfDocumentList()
      }).catch(error => {
        this.$message.error('PDF文件上传失败: ' + error.message)
      })
    },
    // 关闭上传对话框
    handleUploadDialogClose() {
      this.fileList = []
      this.$refs.upload.clearFiles()
    },
    // 创建任务
    handleCreateTask() {
      this.createTaskDialogVisible = true
      this.createTaskForm = {
        pdfId: '',
        indicatorTypes: []
      }
      this.$refs.createTaskForm.resetFields()
    },
    // 提交创建任务
    handleCreateTaskSubmit() {
      this.$refs.createTaskForm.validate(valid => {
        if (valid) {
          createTask(this.createTaskForm).then(response => {
            this.$message.success('PDF指标抽取任务创建成功')
            this.createTaskDialogVisible = false
            this.loadTaskList()
          }).catch(error => {
            this.$message.error('PDF指标抽取任务创建失败: ' + error.message)
          })
        }
      })
    },
    // 执行任务
    handleExecuteTask(taskId) {
      this.$confirm('确定要执行该任务吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        executeTask(taskId).then(response => {
          this.$message.success('PDF指标抽取任务执行成功')
          this.loadTaskList()
        }).catch(error => {
          this.$message.error('PDF指标抽取任务执行失败: ' + error.message)
        })
      }).catch(() => {
        this.$message.info('已取消执行任务')
      })
    },
    // 查看任务结果
    handleViewTaskResult(taskId) {
      queryTaskResult(taskId).then(response => {
        this.taskResult = response.data
        this.taskResultDialogVisible = true
      }).catch(error => {
        this.$message.error('查询任务结果失败: ' + error.message)
      })
    },
    // 刷新任务列表
    handleRefresh() {
      this.loadTaskList()
    },
    // 删除选中任务
    handleDeleteSelected() {
      if (this.selectedTaskIds.length === 0) {
        this.$message.warning('请选择要删除的任务')
        return
      }
      
      this.$confirm('确定要删除选中的任务吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // TODO: 实现删除选中任务的逻辑
        this.$message.success('删除成功')
        this.selectedTaskIds = []
        this.loadTaskList()
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },
    // 选择任务
    handleSelectionChange(selection) {
      this.selectedTaskIds = selection.map(item => item.id)
    },
    // 分页大小改变
    handleSizeChange(val) {
      this.pageSize = val
      this.loadTaskList()
    },
    // 当前页改变
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadTaskList()
    },
    // 获取标签类型
    getTagType(status) {
      switch (status) {
        case 'created':
          return 'info'
        case 'running':
          return 'warning'
        case 'success':
          return 'success'
        case 'failed':
          return 'danger'
        default:
          return ''
      }
    }
  }
}
</script>

<style scoped>
.box-card {
  margin-bottom: 20px;
}
.dialog-footer {
  text-align: right;
}
</style>
