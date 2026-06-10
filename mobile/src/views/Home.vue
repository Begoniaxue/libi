<template>
  <div class="home-page">
    <van-nav-bar title="图书馆" fixed placeholder>
      <template #right>
        <van-icon name="search" size="20" @click="$router.push('/books')" />
      </template>
    </van-nav-bar>

    <div class="user-card" v-if="readerInfo">
      <div class="user-info">
        <div class="avatar">{{ readerInfo.name.charAt(0) }}</div>
        <div class="user-detail">
          <h3>{{ readerInfo.name }}</h3>
          <p>证号：{{ readerInfo.cardNo }}</p>
        </div>
      </div>
      <div class="credit-score">
        <span class="score-label">信用分</span>
        <span class="score-value">{{ readerInfo.creditScore }}</span>
      </div>
    </div>

    <div class="stats-card" v-if="readerInfoData">
      <div class="stat-item">
        <div class="stat-value">{{ readerInfoData.borrowInfo.borrowingCount }}</div>
        <div class="stat-label">在借图书</div>
      </div>
      <div class="stat-item">
        <div class="stat-value text-warning">{{ readerInfoData.borrowInfo.overdueCount }}</div>
        <div class="stat-label">逾期</div>
      </div>
      <div class="stat-item">
        <div class="stat-value text-danger">¥{{ readerInfoData.unpaidAmount }}</div>
        <div class="stat-label">待缴费用</div>
      </div>
    </div>

    <div class="quick-actions">
      <div class="action-grid">
        <div class="action-item" @click="$router.push('/borrowing')">
          <div class="action-icon" style="background: #e8f3ff;">📖</div>
          <span>在借图书</span>
        </div>
        <div class="action-item" @click="$router.push('/borrow-records')">
          <div class="action-icon" style="background: #e8ffea;">📋</div>
          <span>借阅记录</span>
        </div>
        <div class="action-item" @click="$router.push('/overdue')">
          <div class="action-icon" style="background: #fff3e8;">⚠️</div>
          <span>逾期记录</span>
        </div>
        <div class="action-item" @click="$router.push('/fee-records')">
          <div class="action-icon" style="background: #ffe8e8;">💰</div>
          <span>罚款明细</span>
        </div>
        <div class="action-item" @click="$router.push('/books')">
          <div class="action-icon" style="background: #f0e8ff;">🔍</div>
          <span>馆藏查询</span>
        </div>
        <div class="action-item" @click="$router.push('/renew-logs')">
          <div class="action-icon" style="background: #e8fff8;">🔄</div>
          <span>续借记录</span>
        </div>
        <div class="action-item" @click="$router.push('/profile')">
          <div class="action-icon" style="background: #fff8e8;">👤</div>
          <span>个人中心</span>
        </div>
      </div>
    </div>

    <div class="notice-card">
      <div class="notice-title">📢 公告</div>
      <div class="notice-content">
        <p>• 每人最多可借阅5本图书，借期30天</p>
        <p>• 每本图书可续借1次，续借期30天</p>
        <p>• 逾期图书将产生罚款，请及时归还</p>
      </div>
    </div>

    <van-tabbar v-model="active" route>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="search" to="/books">查询</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReaderInfo } from '@/api'
import { showLoadingToast, closeToast } from 'vant'

const active = ref(0)
const readerInfo = ref(null)
const readerInfoData = ref(null)

const loadData = async () => {
  const stored = localStorage.getItem('readerInfo')
  if (stored) {
    readerInfo.value = JSON.parse(stored)
    
    showLoadingToast({
      message: '加载中...',
      forbidClick: true
    })
    
    try {
      const res = await getReaderInfo(readerInfo.value.id)
      readerInfoData.value = res.data
    } catch (error) {
      console.error('加载数据失败:', error)
    } finally {
      closeToast()
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 50px;
}

.user-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin: 12px;
  padding: 20px;
  border-radius: 16px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
}

.user-detail h3 {
  font-size: 18px;
  margin-bottom: 4px;
}

.user-detail p {
  font-size: 12px;
  opacity: 0.8;
}

.credit-score {
  text-align: center;
}

.score-label {
  display: block;
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 4px;
}

.score-value {
  font-size: 24px;
  font-weight: 600;
}

.stats-card {
  background: #fff;
  margin: 12px;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #969799;
}

.quick-actions {
  background: #fff;
  margin: 12px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #323233;
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.notice-card {
  background: #fff;
  margin: 12px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #323233;
}

.notice-content p {
  font-size: 13px;
  color: #646566;
  line-height: 1.8;
  margin: 0;
}
</style>
