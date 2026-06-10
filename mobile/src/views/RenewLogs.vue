<template>
  <div class="renew-logs-page">
    <van-nav-bar title="续借记录" left-arrow @click-left="$router.back()" fixed placeholder />

    <div class="page-content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div class="log-item" v-for="log in list" :key="log.id">
            <div class="log-icon">🔄</div>
            <div class="log-content">
              <h3 class="book-name">{{ log.book?.name }}</h3>
              <p class="log-meta">
                <span>续借天数：</span>{{ log.renewDays }} 天
              </p>
              <p class="log-meta">
                <span>原到期日：</span>{{ log.oldDueDate }}
              </p>
              <p class="log-meta text-success">
                <span>新到期日：</span>{{ log.newDueDate }}
              </p>
              <p class="log-meta">
                <span>操作人：</span>{{ log.operator }}
              </p>
              <p class="log-meta" v-if="log.remark">
                <span>备注：</span>{{ log.remark }}
              </p>
              <p class="log-time">{{ log.createTime }}</p>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>

      <van-empty v-if="!loading && list.length === 0" description="暂无续借记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getRenewLogs } from '@/api'
import { useUserStore } from '@/store/user'
import { storeToRefs } from 'pinia'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = 10

const userStore = useUserStore()
const { readerInfo } = storeToRefs(userStore)

watch(() => userStore.readerInfo?.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    onRefresh()
  }
})

const onLoad = async () => {
  if (!readerInfo.value?.id) return
  
  try {
    const res = await getRenewLogs(readerInfo.value.id, {
      page: page.value,
      size: pageSize
    })
    
    if (res.data) {
      const newList = res.data.list || []
      list.value = [...list.value, ...newList]
      
      if (newList.length < pageSize) {
        finished.value = true
      } else {
        page.value++
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const onRefresh = () => {
  refreshing.value = true
  page.value = 1
  list.value = []
  finished.value = false
  onLoad().finally(() => {
    refreshing.value = false
  })
}
</script>

<style scoped>
.renew-logs-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.page-content {
  padding: 12px;
}

.log-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.log-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.log-content {
  flex: 1;
  min-width: 0;
}

.book-name {
  font-size: 15px;
  font-weight: 600;
  color: #323233;
  margin: 0 0 8px 0;
}

.log-meta {
  font-size: 13px;
  color: #646566;
  margin: 4px 0;
}

.log-meta span {
  color: #969799;
}

.log-time {
  font-size: 12px;
  color: #c8c9cc;
  margin-top: 8px;
}
</style>
