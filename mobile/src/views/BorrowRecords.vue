<template>
  <div class="records-page">
    <van-nav-bar title="借阅记录" left-arrow @click-left="$router.back()" fixed placeholder />

    <van-tabs v-model:active="activeTab" sticky offset-top="46px">
      <van-tab title="全部" name="all" />
      <van-tab title="借阅中" name="borrowing" />
      <van-tab title="已归还" name="returned" />
      <van-tab title="已逾期" name="overdue" />
    </van-tabs>

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
              <h3 class="book-name">{{ record.book?.name }}</h3>
              <van-tag :type="getStatusType(record.status)" size="small">
                {{ getStatusText(record.status) }}
              </van-tag>
            </div>
            <div class="record-info">
              <p><span>作者：</span>{{ record.book?.author }}</p>
              <p><span>ISBN：</span>{{ record.book?.isbn }}</p>
              <p><span>借阅日期：</span>{{ record.borrowDate }}</p>
              <p><span>应还日期：</span>{{ record.dueDate }}</p>
              <p v-if="record.returnDate"><span>归还日期：</span>{{ record.returnDate }}</p>
              <p v-if="record.renewCount > 0" class="text-success">
                <span>已续借 {{ record.renewCount }} 次</span>
              </p>
              <p v-if="record.isOverdue === 1" class="text-danger">
                <span>逾期 {{ record.overdueDays }} 天</span>
              </p>
              <p v-if="record.fineAmount > 0" class="text-warning">
                <span>罚款金额：¥{{ record.fineAmount }}</span>
              </p>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>

      <van-empty v-if="!loading && list.length === 0" description="暂无借阅记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { getBorrowRecords } from '@/api'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = 10
const readerId = ref(null)
const activeTab = ref('all')

onMounted(() => {
  const stored = localStorage.getItem('readerInfo')
  if (stored) {
    readerId.value = JSON.parse(stored).id
  }
})

watch(activeTab, () => {
  onRefresh()
})

const getStatusType = (status) => {
  const map = { 1: 'primary', 2: 'success', 3: 'danger', 4: 'warning' }
  return map[status] || 'default'
}

const getStatusText = (status) => {
  const map = { 1: '借阅中', 2: '已归还', 3: '已逾期', 4: '已赔偿' }
  return map[status] || '未知'
}

const getStatusParam = () => {
  const map = { borrowing: 1, returned: 2, overdue: 3 }
  return map[activeTab.value] || null
}

const onLoad = async () => {
  if (!readerId.value) return
  
  try {
    const res = await getBorrowRecords(readerId.value, {
      page: page.value,
      size: pageSize,
      status: getStatusParam()
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
.records-page {
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.book-name {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin: 0;
  flex: 1;
  margin-right: 12px;
}

.record-info p {
  font-size: 13px;
  color: #646566;
  margin: 6px 0;
}

.record-info span {
  color: #969799;
}
</style>
