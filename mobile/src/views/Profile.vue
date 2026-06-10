<template>
  <div class="profile-page">
    <van-nav-bar title="个人中心" left-arrow @click-left="$router.back()" fixed placeholder />

    <div class="user-header" v-if="readerInfo">
      <div class="avatar">{{ readerInfo.name.charAt(0) }}</div>
      <div class="user-info">
        <h2>{{ readerInfo.name }}</h2>
        <p>证号：{{ readerInfo.cardNo }}</p>
        <p>身份：{{ readerInfo.identityType }}</p>
      </div>
    </div>

    <div class="credit-card" v-if="readerInfo">
      <div class="credit-item">
        <div class="credit-value">{{ readerInfo.creditScore }}</div>
        <div class="credit-label">信用分</div>
      </div>
      <div class="credit-item">
        <div class="credit-value">{{ readerInfo.violationCount }}</div>
        <div class="credit-label">违规次数</div>
      </div>
      <div class="credit-item">
        <div class="credit-value" :class="readerInfo.status === 1 ? 'text-success' : 'text-danger'">
          {{ readerInfo.status === 1 ? '正常' : '注销' }}
        </div>
        <div class="credit-label">账号状态</div>
      </div>
    </div>

    <van-cell-group inset class="info-group">
      <van-cell title="性别" :value="readerInfo?.gender === 1 ? '男' : readerInfo?.gender === 2 ? '女' : '未知'" />
      <van-cell title="手机号" :value="readerInfo?.phone" />
      <van-cell title="邮箱" :value="readerInfo?.email" />
      <van-cell title="身份证号" :value="readerInfo?.idCard" />
      <van-cell title="出生日期" :value="readerInfo?.birthday" />
      <van-cell title="联系地址" :value="readerInfo?.address" />
      <van-cell title="注册时间" :value="readerInfo?.createTime" />
    </van-cell-group>

    <van-cell-group inset class="menu-group">
      <van-cell title="在借图书" is-link @click="$router.push('/borrowing')">
        <template #icon>📖</template>
      </van-cell>
      <van-cell title="借阅记录" is-link @click="$router.push('/borrow-records')">
        <template #icon>📋</template>
      </van-cell>
      <van-cell title="逾期记录" is-link @click="$router.push('/overdue')">
        <template #icon>⚠️</template>
      </van-cell>
      <van-cell title="罚款明细" is-link @click="$router.push('/fee-records')">
        <template #icon>💰</template>
      </van-cell>
      <van-cell title="续借记录" is-link @click="$router.push('/renew-logs')">
        <template #icon>🔄</template>
      </van-cell>
    </van-cell-group>

    <div class="logout-btn">
      <van-button block type="danger" @click="logout">退出登录</van-button>
    </div>

    <van-tabbar v-model="active" route>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="search" to="/books">查询</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { useUserStore } from '@/store/user'
import { storeToRefs } from 'pinia'

const router = useRouter()
const userStore = useUserStore()
const { readerInfo } = storeToRefs(userStore)
const active = ref(2)

watch(() => userStore.isLoggedIn, (loggedIn) => {
  if (!loggedIn) {
    router.push('/login')
  }
}, { immediate: true })

const logout = () => {
  showConfirmDialog({
    title: '提示',
    message: '确定要退出登录吗？'
  }).then(() => {
    userStore.logout()
    showToast('已退出登录')
    setTimeout(() => {
      router.push('/login')
    }, 500)
  }).catch(() => {})
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 50px;
}

.user-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
}

.user-info h2 {
  font-size: 20px;
  margin-bottom: 6px;
}

.user-info p {
  font-size: 13px;
  opacity: 0.85;
  margin: 2px 0;
}

.credit-card {
  background: #fff;
  margin: -20px 12px 12px;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
}

.credit-item {
  text-align: center;
}

.credit-value {
  font-size: 24px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 4px;
}

.credit-label {
  font-size: 12px;
  color: #969799;
}

.info-group {
  margin: 12px;
}

.menu-group {
  margin: 12px;
}

.logout-btn {
  padding: 16px 12px;
}
</style>
