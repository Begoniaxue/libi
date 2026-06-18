<template>
  <div class="activity-detail">
    <el-page-header @back="goBack" content="活动详情" style="margin-bottom: 20px;">
      <template #content>
        <span style="font-size: 20px; font-weight: 600;">活动详情</span>
      </template>
    </el-page-header>

    <el-row :gutter="20">
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, Edit } from '@element-plus/icons-vue'
import {
  getActivityById,
  getRegistrationList,
  cancelRegistration,
  exportRegistrations
} from '@/api/activity'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const activityId = route.params.id

const activity = ref({})
const registrationList = ref([])
const searchKeyword = ref('')

const regPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

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
    activity.value = res.data
  } catch (error) {
    console.error('加载活动详情失败', error)
  }
}

const loadRegistrations = async () => {
  try {
    const res = await getRegistrationList(activityId, {
      page: regPagination.current,
      size: regPagination.size,
      keyword: searchKeyword.value
    })
    registrationList.value = res.data.list
    regPagination.total = res.data.total
  } catch (error) {
    console.error('加载报名名单失败', error)
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
  router.push('/activities')
}

onMounted(() => {
  loadActivity()
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
</style>
