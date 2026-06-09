<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#409EFF"><Reading /></el-icon>
            <div class="stat-info">
              <div class="stat-label">图书总数</div>
              <div class="stat-value">{{ stats.totalBooks || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#67C23A"><Collection /></el-icon>
            <div class="stat-info">
              <div class="stat-label">可借数量</div>
              <div class="stat-value">{{ stats.availableQuantity || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#E6A23C"><User /></el-icon>
            <div class="stat-info">
              <div class="stat-label">读者总数</div>
              <div class="stat-value">{{ stats.totalReaders || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-item">
            <el-icon size="40" color="#F56C6C"><DocumentCopy /></el-icon>
            <div class="stat-info">
              <div class="stat-label">借阅中</div>
              <div class="stat-value">{{ stats.borrowingCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600;">借阅规则</span>
          </template>
          <el-steps direction="vertical" :active="4" finish-status="success">
            <el-step title="读者注册" description="读者信息录入系统，生成借书证号" />
            <el-step title="图书上架" description="图书信息录入，设置可借数量" />
            <el-step title="借阅图书" description="每人最多同时借阅5本，超期不可借书" />
            <el-step title="归还图书" description="按时归还，逾期自动标记" />
          </el-steps>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600;">借阅统计</span>
          </template>
          <div class="stat-detail">
            <div class="detail-item">
              <span class="detail-label">总借阅记录</span>
              <span class="detail-value">{{ stats.totalRecords || 0 }}</span>
            </div>
            <el-divider />
            <div class="detail-item">
              <span class="detail-label">当前借阅中</span>
              <span class="detail-value primary">{{ stats.borrowingCount || 0 }}</span>
            </div>
            <el-divider />
            <div class="detail-item">
              <span class="detail-label">逾期未还</span>
              <span class="detail-value danger">{{ stats.overdueCount || 0 }}</span>
            </div>
            <el-divider />
            <div class="detail-item">
              <span class="detail-label">活跃读者</span>
              <span class="detail-value success">{{ stats.activeReaders || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-weight: 600;">快捷操作</span>
              <el-button type="primary" @click="loadData">
                <el-icon><Refresh /></el-icon>
                刷新数据
              </el-button>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" size="large" @click="$router.push('/books')">
              <el-icon size="20"><Plus /></el-icon>
              图书管理
            </el-button>
            <el-button type="success" size="large" @click="$router.push('/readers')">
              <el-icon size="20"><UserPlus /></el-icon>
              读者管理
            </el-button>
            <el-button type="warning" size="large" @click="$router.push('/borrow')">
              <el-icon size="20"><Tickets /></el-icon>
              借阅管理
            </el-button>
            <el-button type="danger" size="large" @click="handleCheckOverdue">
              <el-icon size="20"><Warning /></el-icon>
              检查逾期
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStatistics, checkOverdue } from '@/api/borrow'

const stats = ref({})

const loadData = async () => {
  try {
    const res = await getStatistics()
    stats.value = res.data
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const handleCheckOverdue = async () => {
  try {
    await checkOverdue()
    ElMessage.success('逾期检查完成')
    loadData()
  } catch (error) {
    console.error('检查逾期失败', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.stat-card {
  border-radius: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-detail {
  padding: 10px 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
}

.detail-label {
  color: #606266;
  font-size: 14px;
}

.detail-value {
  font-weight: bold;
  font-size: 18px;
  color: #303133;
}

.detail-value.primary {
  color: #409EFF;
}

.detail-value.danger {
  color: #F56C6C;
}

.detail-value.success {
  color: #67C23A;
}

.quick-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  padding: 20px 0;
}

.quick-actions .el-button {
  min-width: 160px;
  height: 50px;
}
</style>
