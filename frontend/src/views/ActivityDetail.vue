<template>
  <div class="activity-detail">
    <el-page-header @back="goBack" content="活动详情" style="margin-bottom: 20px;">
      <template #content>
        <span style="font-size: 20px; font-weight: 600;">活动详情</span>
      </template>
    </el-page-header>

    <el-row :gutter="20" v-if="activity">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span style="font-weight: 600; font-size: 18px;">{{ activity.name }}</span>
            <el-tag
              :type="activity.status === 1 ? 'success' : 'info'"
              style="margin-left: 10px;"
            >
              {{ activity.status === 1 ? '已发布' : '已下架' }}
            </el-tag>
          </template>

          <div v-if="activity.coverImage" class="cover-image">
            <el-image :src="activity.coverImage" fit="cover" style="width: 100%; height: 300px;" />
          </div>

          <div class="info-section">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="活动时间">
                {{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="活动地点">
                {{ activity.location }}
              </el-descriptions-item>
              <el-descriptions-item label="活动名额">
                <span style="color: #409EFF; font-weight: 600;">{{ activity.registeredCount }}</span>
                / {{ activity.quota }} 人
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDateTime(activity.createTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="content-section">
            <h3 style="margin-bottom: 15px;">活动内容</h3>
            <div class="content-html" v-html="activity.content"></div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span style="font-weight: 600;">报名统计</span>
          </template>
          <div class="stats-item">
            <div class="stats-label">已报名人数</div>
            <div class="stats-value primary">{{ activity.registeredCount || 0 }}</div>
          </div>
          <el-divider />
          <div class="stats-item">
            <div class="stats-label">总名额</div>
            <div class="stats-value">{{ activity.quota || 0 }}</div>
          </div>
          <el-divider />
          <div class="stats-item">
            <div class="stats-label">剩余名额</div>
            <div class="stats-value success">
              {{ (activity.quota || 0) - (activity.registeredCount || 0) }}
            </div>
          </div>
          <el-divider />
          <div class="stats-item">
            <div class="stats-label">报名进度</div>
            <el-progress
              :percentage="activity.quota ? Math.round((activity.registeredCount / activity.quota) * 100) : 0"
              :status="activity.registeredCount >= activity.quota ? 'warning' : ''"
              style="margin-top: 10px;"
            />
          </div>
        </el-card>

        <el-card shadow="never" style="margin-top: 20px;">
          <template #header>
            <span style="font-weight: 600;">快捷操作</span>
          </template>
          <div style="display: flex; flex-direction: column; gap: 10px;">
            <el-button type="primary" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出报名名单
            </el-button>
            <el-button type="warning" @click="handleEdit">
              <el-icon><Edit /></el-icon>
              编辑活动
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 600;">报名名单</span>
          <div>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索姓名"
              style="width: 200px; margin-right: 10px;"
              clearable
              size="small"
              @keyup.enter="loadRegistrations"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" size="small" @click="loadRegistrations">
              搜索
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="registrationList" border stripe style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="读者关联" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.readerName" type="success" size="small">
              {{ row.readerName }}
            </el-tag>
            <span v-else style="color: #909399;">散客</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="报名时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已报名' : '已取消' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="danger"
              size="small"
              @click="handleCancelRegistration(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="regPagination.current"
        v-model:page-size="regPagination.size"
        :page-sizes="[10, 20, 50]"
        :total="regPagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRegistrations"
        @current-change="loadRegistrations"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑活动"
      width="900px"
      :close-on-click-modal="false"
      @closed="handleDialogClosed"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="活动名称" prop="name">
              <el-input v-model="editForm.name" placeholder="请输入活动名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="主图链接" prop="coverImage">
              <el-input v-model="editForm.coverImage" placeholder="请输入图片URL" />
              <div v-if="editForm.coverImage" style="margin-top: 10px;">
                <el-image :src="editForm.coverImage" style="width: 200px; height: 120px;" fit="cover" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="editForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%;"
                value-format="YYYY-MM-DD HH:mm:ss"
                :disabled-date="disableStartDate"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="editForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%;"
                value-format="YYYY-MM-DD HH:mm:ss"
                :disabled-date="disableEndDate"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动地点" prop="location">
              <el-input v-model="editForm.location" placeholder="请输入活动地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动名额" prop="quota">
              <el-input-number v-model="editForm.quota" :min="1" :max="10000" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="editForm.status">
                <el-radio :value="1">发布</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="活动内容" prop="content">
              <div class="rich-editor">
                <div class="editor-toolbar">
                  <el-button-group>
                    <el-button size="small" @click="execCommand('bold')"><b>B</b></el-button>
                    <el-button size="small" @click="execCommand('italic')"><i>I</i></el-button>
                    <el-button size="small" @click="execCommand('underline')"><u>U</u></el-button>
                    <el-button size="small" @click="execCommand('strikeThrough')"><s>S</s></el-button>
                  </el-button-group>
                  <el-button-group style="margin-left: 10px;">
                    <el-button size="small" @click="execCommand('justifyLeft')">左对齐</el-button>
                    <el-button size="small" @click="execCommand('justifyCenter')">居中</el-button>
                    <el-button size="small" @click="execCommand('justifyRight')">右对齐</el-button>
                  </el-button-group>
                  <el-button-group style="margin-left: 10px;">
                    <el-button size="small" @click="execCommand('insertUnorderedList')">无序列表</el-button>
                    <el-button size="small" @click="execCommand('insertOrderedList')">有序列表</el-button>
                  </el-button-group>
                  <el-select
                    v-model="fontSize"
                    size="small"
                    style="width: 100px; margin-left: 10px;"
                    @change="changeFontSize"
                    placeholder="字号"
                  >
                    <el-option label="小" value="2" />
                    <el-option label="中" value="3" />
                    <el-option label="大" value="5" />
                    <el-option label="特大" value="7" />
                  </el-select>
                  <el-color-picker
                    v-model="fontColor"
                    size="small"
                    style="margin-left: 10px;"
                    @change="changeFontColor"
                  />
                </div>
                <div
                  ref="editorRef"
                  class="editor-content"
                  contenteditable="true"
                  @input="onEditorInput"
                  @blur="onEditorBlur"
                />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, Edit } from '@element-plus/icons-vue'
import {
  getActivityById,
  getRegistrationList,
  cancelRegistration,
  exportRegistrations,
  updateActivity
} from '@/api/activity'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const activityId = route.params.id

const activity = ref(null)
const registrationList = ref([])
const searchKeyword = ref('')
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editorRef = ref(null)
const fontSize = ref('3')
const fontColor = ref('#303133')
const loading = ref(true)

const regPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const editForm = reactive({
  id: null,
  name: '',
  coverImage: '',
  content: '',
  startTime: '',
  endTime: '',
  location: '',
  quota: 50,
  status: 1
})

const editRules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  quota: [{ required: true, message: '请输入活动名额', trigger: 'blur' }]
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const goBack = () => {
  router.push('/activities')
}

const loadActivity = async () => {
  try {
    const res = await getActivityById(activityId)
    if (res.code === 200) {
      activity.value = res.data
    } else {
      activity.value = null
    }
  } catch (error) {
    console.error('加载活动详情失败', error)
    activity.value = null
  } finally {
    loading.value = false
  }
}

const loadRegistrations = async () => {
  try {
    const res = await getRegistrationList(activityId, {
      page: regPagination.current,
      size: regPagination.size,
      keyword: searchKeyword.value
    })
    if (res.code === 200) {
      registrationList.value = res.data.list
      regPagination.total = res.data.total
    } else {
      registrationList.value = []
      regPagination.total = 0
    }
  } catch (error) {
    console.error('加载报名名单失败', error)
    registrationList.value = []
    regPagination.total = 0
  }
}

const handleCancelRegistration = (row) => {
  ElMessageBox.confirm('确定要取消该用户的报名吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelRegistration(row.id)
      ElMessage.success('取消成功')
      loadActivity()
      loadRegistrations()
    } catch (error) {
      console.error('取消报名失败', error)
    }
  }).catch(() => {})
}

const handleExport = async () => {
  try {
    const response = await exportRegistrations(activityId)
    const blob = response.data
    let fileName = `${activity.value.name || '活动报名'}_${dayjs().format('YYYYMMDD_HHmmss')}.csv`
    const disposition = response.headers['content-disposition']
    if (disposition) {
      const matches = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (matches != null && matches[1]) {
        fileName = matches[1].replace(/['"]/g, '')
        fileName = decodeURIComponent(fileName)
      }
    }
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败')
  }
}

const handleEdit = () => {
  if (!activity.value) return
  Object.assign(editForm, {
    id: activity.value.id,
    name: activity.value.name,
    coverImage: activity.value.coverImage || '',
    content: activity.value.content || '',
    startTime: activity.value.startTime,
    endTime: activity.value.endTime,
    location: activity.value.location,
    quota: activity.value.quota,
    status: activity.value.status
  })
  editDialogVisible.value = true
  nextTick(() => {
    if (editorRef.value) {
      editorRef.value.innerHTML = activity.value.content || ''
    }
  })
}

const disableStartDate = (time) => {
  if (editForm.endTime) {
    const endTime = dayjs(editForm.endTime)
    return time.getTime() > endTime.valueOf()
  }
  return false
}

const disableEndDate = (time) => {
  if (editForm.startTime) {
    const startTime = dayjs(editForm.startTime)
    return time.getTime() < startTime.valueOf()
  }
  return false
}

const execCommand = (command, value = null) => {
  document.execCommand(command, false, value)
  editorRef.value?.focus()
}

const changeFontSize = (val) => {
  document.execCommand('fontSize', false, val)
  editorRef.value?.focus()
}

const changeFontColor = (val) => {
  document.execCommand('foreColor', false, val)
  editorRef.value?.focus()
}

const onEditorInput = () => {
  if (editorRef.value) {
    editForm.content = editorRef.value.innerHTML
  }
}

const onEditorBlur = () => {
  if (editorRef.value) {
    editForm.content = editorRef.value.innerHTML
  }
}

const handleSaveEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (!editForm.content || editForm.content.trim() === '') {
          ElMessage.warning('请输入活动内容')
          return
        }
        if (editForm.startTime && editForm.endTime && dayjs(editForm.startTime).isAfter(dayjs(editForm.endTime))) {
          ElMessage.warning('开始时间不能晚于结束时间')
          return
        }
        await updateActivity(editForm)
        ElMessage.success('保存成功')
        editDialogVisible.value = false
        loadActivity()
      } catch (error) {
        console.error('保存失败', error)
      }
    }
  })
}

const handleDialogClosed = () => {
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
  if (editorRef.value) {
    editorRef.value.innerHTML = ''
  }
}

onMounted(() => {
  loadActivity().finally(() => {
    loading.value = false
  })
  loadRegistrations()
})
</script>

<style scoped>
.activity-detail {
  padding: 0;
}

.cover-image {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}

.info-section {
  margin-bottom: 20px;
}

.content-section {
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.content-section h3 {
  font-size: 16px;
  color: #303133;
}

.content-html {
  line-height: 1.8;
  color: #606266;
}

.content-html :deep(p) {
  margin: 10px 0;
}

.content-html :deep(ul),
.content-html :deep(ol) {
  margin: 10px 0;
  padding-left: 24px;
}

.content-html :deep(li) {
  margin: 5px 0;
}

.stats-item {
  text-align: center;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stats-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stats-value.primary {
  color: #409EFF;
}

.stats-value.success {
  color: #67C23A;
}

.rich-editor {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  padding: 8px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  gap: 8px;
}

.editor-content {
  min-height: 200px;
  max-height: 400px;
  padding: 12px;
  overflow-y: auto;
  background-color: #fff;
}

.editor-content:focus {
  outline: none;
}

.editor-content:deep(p) {
  margin: 8px 0;
}

.editor-content:deep(ul),
.editor-content:deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}
</style>
