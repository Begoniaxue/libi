<template>
  <div class="overdue-page">
    <van-nav-bar title="逾期记录" left-arrow @click-left="$router.back()" fixed placeholder />

    <div class="page-content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div class="record-item" v-for="record in list" :key="record.id">
            <div class="record-header">
              <div class="danger-badge">!</div>
              <h3 class="book-name">{{ record.book?.name }}</h3>
            </div>
            <div class="record-info">
              <p><span>作者：</span>{{ record.book?.author }}</p>
              <p><span>借阅日期：</span>{{ record.borrowDate }}</p>
              <p><span>应还日期：</span>{{ record.dueDate }}</p>
              <p class="text-danger"><span>逾期天数：</span>{{ record.overdueDays }} 天</p>
              <p v-if="record.fineAmount > 0" class="text-warning">
                <span>罚款金额：</span>¥{{ record.fineAmount }}
              </p>
              <p v-if="record.paidAmount > 0" class="text-success">
                <span>已缴金额：</span>¥{{ record.paidAmount }}
              </p>
              <p v-if="record.fineAmount > record.paidAmount" class="text-danger">
                <span>待缴金额：</span>¥{{ (record.fineAmount - record.paidAmount).toFixed(2) }}
              </p>
            </div>
            <div class="record-footer">
              <van-tag type="danger">请尽快归还并缴纳罚款</van-tag>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>

      <van-empty v-if="!loading && list.length === 0" description="暂无逾期记录，继续保持！" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getOverdueRecords } from '@/api'
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
    const res = await getOverdueRecords(readerInfo.value.id, {
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
.overdue-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.page-content {
  padding: 12px;
}

.record-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(238, 10, 36, 0.1);
  border-left: 4px solid #ee0a24;
}

.record-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  gap: 10px;
}

.danger-badge {
  width: 24px;
  height: 24px;
  background: #ee0a24;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
  flex-shrink: 0;
}

.book-name {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin: 0;
}

.record-info p {
  font-size: 13px;
  color: #646566;
  margin: 6px 0;
}

.record-info span {
  color: #969799;
}

.record-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebedf0;
}
</style>
