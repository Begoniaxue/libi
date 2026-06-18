<template>
  <div class="activity-list">
    <el-card shadow="never">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="请输入活动名称搜索"
          style="width: 300px; margin-right: 10px;"
          clearable
          @keyup.enter="loadData"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadData">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button type="success" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          发布活动
        </el-button>
      </div>

      <el-table :data="tableData" border stripe style="width: 100%; margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="主图" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              style="width: 80px; height: 60px;"
              fit="cover"
              :preview-src-list="[row.coverImage]"
            />
            <span v-else style="color: #909399;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="活动名称" min-width="180" />
        <el-table-column label="活动时间" width="200">
          <template #default="{ row }">
            <div style="font-size: 12px;">
              <div>开始：{{ formatDateTime(row.startTime) }}</div>
              <div>结束：{{ formatDateTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="150" show-overflow-tooltip />
        <el-table-column label="报名情况" width="120">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.round((row.registeredCount / row.quota) * 100)"
              :status="row.registeredCount >= row.quota ? 'warning' : ''"
            />
            <div style="font-size: 12px; color: #606266; text-align: center; margin-top: 4px;">
              {{ row.registeredCount }} / {{ row.quota }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已发布' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleDetail(row)">
              详情
            </el-button>
            <el-button type="warning" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handleToggleStatus(row, 1)"
            >
              上架
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="info"
              size="small"
              @click="handleToggleStatus(row, 0)"
            >
              下架
            </el-button>
            <el-button
              type="danger"
              size="small"
              :disabled="row.status === 1"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="活动名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入活动名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="主图链接" prop="coverImage">
              <el-input v-model="form.coverImage" placeholder="请输入图片URL" />
              <div v-if="form.coverImage" style="margin-top: 10px;">
                <el-image :src="form.coverImage" style="width: 200px; height: 120px;" fit="cover" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
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
                v-model="form.endTime"
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
              <el-input v-model="form.location" placeholder="请输入活动地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动名额" prop="quota">
              <el-input-number v-model="form.quota" :min="1" :max="10000" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getActivityList, addActivity, updateActivity, deleteActivity } from '@/api/activity'
import dayjs from 'dayjs'

const router = useRouter()
const keyword = ref('')
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const editorRef = ref(null)
const isEdit = ref(false)
const fontSize = ref('3')
const fontColor = ref('#303133')

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
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

const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  quota: [{ required: true, message: '请输入活动名额', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑活动' : '发布活动')

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

const loadData = async () => {
  try {
    const res = await getActivityList({
      page: pagination.current,
      size: pagination.size,
      keyword: keyword.value
    })
    if (res.code === 200) {
      tableData.value = res.data.list
      pagination.total = res.data.total
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载活动列表失败', error)
    tableData.value = []
    pagination.total = 0
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
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
  dialogVisible.value = true
  nextTick(() => {
    if (editorRef.value) {
      editorRef.value.innerHTML = ''
    }
  })
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
  nextTick(() => {
    if (editorRef.value) {
      editorRef.value.innerHTML = row.content || ''
    }
  })
}

const disableStartDate = (time) => {
  if (form.endTime) {
    const endTime = dayjs(form.endTime)
    return time.getTime() > endTime.valueOf()
  }
  return false
}

const disableEndDate = (time) => {
  if (form.startTime) {
    const startTime = dayjs(form.startTime)
    return time.getTime() < startTime.valueOf()
  }
  return false
}

const handleDetail = (row) => {
  router.push(`/activities/${row.id}`)
}

const handleToggleStatus = (row, targetStatus) => {
  const actionName = targetStatus === 1 ? '上架' : '下架'
  ElMessageBox.confirm(`确定要${actionName}该活动吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateActivity({
        id: row.id,
        status: targetStatus
      })
      ElMessage.success(`${actionName}成功`)
      loadData()
    } catch (error) {
      console.error(`${actionName}失败`, error)
    }
  }).catch(() => {})
}

const handleDelete = (row) => {
  if (row.status === 1) {
    ElMessage.warning('已上架的活动不允许删除，请先下架')
    return
  }
  ElMessageBox.confirm('确定要删除该活动吗？删除后数据不可恢复！', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteActivity(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败', error)
    }
  }).catch(() => {})
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
    form.content = editorRef.value.innerHTML
  }
}

const onEditorBlur = () => {
  if (editorRef.value) {
    form.content = editorRef.value.innerHTML
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (!form.content || form.content.trim() === '') {
          ElMessage.warning('请输入活动内容')
          return
        }
        if (form.startTime && form.endTime && dayjs(form.startTime).isAfter(dayjs(form.endTime))) {
          ElMessage.warning('开始时间不能晚于结束时间')
          return
        }
        if (isEdit.value) {
          await updateActivity(form)
          ElMessage.success('更新成功')
        } else {
          await addActivity(form)
          ElMessage.success('发布成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('提交失败', error)
      }
    }
  })
}

loadData()
</script>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
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
