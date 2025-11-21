<template>
  <div class="app-container">
    <el-card title="PDF文档管理" class="box-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-button type="primary" @click="handleUpload">上传PDF文件</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="success" @click="handleParseSelected">解析选中文档</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="warning" @click="handleRefresh">刷新文档列表</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="danger" @click="handleDeleteSelected">删除选中文档</el-button>
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

      <!-- PDF文档列表 -->
      <el-table
        :data="documentList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="文档ID" width="180"></el-table-column>
        <el-table-column prop="fileName" label="文件名" width="200"></el-table-column>
        <el-table-column prop="filePath" label="文件路径" width="300"></el-table-column>
        <el-table-column prop="fileSize" label="文件大小" width="120"></el-table-column>
        <el-table-column prop="fileType" label="文件类型" width="120"></el-table-column>
        <el-table-column prop="title" label="文档标题" width="200"></el-table-column>
        <el-table-column prop="pageCount" label="页数" width="100"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="handleParseDocument(scope.row.id)"
              :disabled="scope.row.status === 'parsed'"
            >
              解析文档
            </el-button>
            <el-button
              size="mini"
              type="info"
              @click="handleViewDocument(scope.row.id)"
            >
              查看文档
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

      <!-- 文档解析结果对话框 -->
      <el-dialog title="PDF文档解析结果" :visible.sync="parseResultDialogVisible" width="800px">
        <el-form :model="parseResult" label-width="120px">
          <el-form-item label="PDF文档ID">
            <span>{{ parseResult.pdfId }}</span>
          </el-form-item>
          <el-form-item label="文档标题">
            <span>{{ parseResult.title }}</span>
          </el-form-item>
          <el-form-item label="文档页数">
            <span>{{ parseResult.pageCount }}</span>
          </el-form-item>
          <el-form-item label="文档大小">
            <span>{{ parseResult.fileSize }}</span>
          </el-form-item>
          <el-form-item label="文档内容">
            <el-input
              :value="parseResult.content"
              type="textarea"
              :rows="15"
              readonly
            ></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="parseResultDialogVisible = false">关闭</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { uploadPdf, getPdfDocumentList, parsePdfContent, deletePdfDocument, deleteBatchPdfDocuments } from '@/api/pdfindicator/pdfDocument'

export default {
  name: 'PdfDocument',
  data() {
    return {
      loading: false,
      documentList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      selectedDocumentIds: [],
      uploadDialogVisible: false,
      parseResultDialogVisible: false,
      fileList: [],
      parseResult: {}
    }
  },
  mounted() {
    this.loadDocumentList()
  },
  methods: {
    // 加载文档列表
    loadDocumentList() {
      this.loading = true
      getPdfDocumentList(this.currentPage, this.pageSize).then(response => {
        this.documentList = response.data.records
        this.total = response.data.total
        this.loading = false
      }).catch(error => {
        this.loading = false
        this.$message.error('加载文档列表失败: ' + error.message)
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
        this.loadDocumentList()
      }).catch(error => {
        this.$message.error('PDF文件上传失败: ' + error.message)
      })
    },
    // 关闭上传对话框
    handleUploadDialogClose() {
      this.fileList = []
      this.$refs.upload.clearFiles()
    },
    // 解析文档
    handleParseDocument(pdfId) {
      this.$confirm('确定要解析该PDF文档吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        parsePdfContent(pdfId).then(response => {
          this.$message.success('PDF文档解析成功')
          this.parseResult = response.data
          this.parseResultDialogVisible = true
          this.loadDocumentList()
        }).catch(error => {
          this.$message.error('PDF文档解析失败: ' + error.message)
        })
      }).catch(() => {
        this.$message.info('已取消解析文档')
      })
    },
    // 解析选中文档
    handleParseSelected() {
      if (this.selectedDocumentIds.length === 0) {
        this.$message.warning('请选择要解析的PDF文档')
        return
      }
      
      this.$confirm('确定要解析选中的PDF文档吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // TODO: 实现批量解析文档的逻辑
        this.$message.success('批量解析成功')
        this.selectedDocumentIds = []
        this.loadDocumentList()
      }).catch(() => {
        this.$message.info('已取消批量解析')
      })
    },
    // 查看文档
    handleViewDocument(pdfId) {
      // TODO: 实现查看文档的逻辑
      this.$message.info('查看文档功能待实现')
    },
    // 刷新文档列表
    handleRefresh() {
      this.loadDocumentList()
    },
    // 删除选中文档
    handleDeleteSelected() {
      if (this.selectedDocumentIds.length === 0) {
        this.$message.warning('请选择要删除的PDF文档')
        return
      }
      
      this.$confirm('确定要删除选中的PDF文档吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteBatchPdfDocuments(this.selectedDocumentIds).then(response => {
          this.$message.success('删除成功')
          this.selectedDocumentIds = []
          this.loadDocumentList()
        }).catch(error => {
          this.$message.error('删除失败: ' + error.message)
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },
    // 选择文档
    handleSelectionChange(selection) {
      this.selectedDocumentIds = selection.map(item => item.id)
    },
    // 分页大小改变
    handleSizeChange(val) {
      this.pageSize = val
      this.loadDocumentList()
    },
    // 当前页改变
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadDocumentList()
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
