<template>
  <div class="borrowing-page">
    <van-nav-bar title="在借图书" left-arrow @click-left="$router.back()" fixed placeholder />

    <div class="page-content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div class="book-item" v-for="record in list" :key="record.id">
            <div class="book-cover">📖</div>
            <div class="book-info">
              <h3 class="book-name">{{ record.book?.name }}</h3>
              <p class="book-author">{{ record.book?.author }}</p>
              <p class="book-meta">
                <span>借阅日期：{{ record.borrowDate }}</span>
              </p>
              <p class="book-meta">
                <span class="due-date" :class="{ overdue: isOverdue(record) }">
                  应还日期：{{ record.dueDate }}
                </span>
              </p>
              <p class="book-meta" v-if="record.renewCount > 0">
                <span class="text-success">已续借 {{ record.renewCount }} 次</span>
              </p>
              <div class="book-status">
                <van-tag :type="isOverdue(record) ? 'danger' : 'success'" size="medium">
                  {{ isOverdue(record) ? '已逾期' : '借阅中' }}
                </van-tag>
                <van-button
                  size="small"
                  type="primary"
                  :disabled="!canRenew(record)"
                  @click="handleRenew(record)"
                >
                  {{ record.renewCount > 0 ? '已续借' : '续借' }}
                </van-button>
              </div>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>

      <van-empty v-if="!loading && list.length === 0" description="暂无在借图书" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBorrowingBooks, renewBook } from '@/api'
import { showToast, showConfirmDialog } from 'vant'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = 10
const readerId = ref(null)

onMounted(() => {
  const stored = localStorage.getItem('readerInfo')
  if (stored) {
    readerId.value = JSON.parse(stored).id
  }
})

const isOverdue = (record) => {
  return record.isOverdue === 1
}

const canRenew = (record) => {
  return record.renewCount === 0 && record.isOverdue === 0 && record.status === 1
}

const handleRenew = async (record) => {
  try {
    await showConfirmDialog({
      title: '确认续借',
      message: `确定要续借《${record.book?.name}》吗？续借后将延长30天借阅期。`
    })
    
    const res = await renewBook(record.id)
    if (res.code === 200) {
      showToast('续借成功')
      onRefresh()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('续借失败:', error)
    }
  }
}

const onLoad = async () => {
  if (!readerId.value) return
  
  try {
    const res = await getBorrowingBooks(readerId.value, {
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
.borrowing-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.page-content {
  padding: 12px;
}

.book-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.book-cover {
  width: 60px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  flex-shrink: 0;
}

.book-info {
  flex: 1;
  min-width: 0;
}

.book-name {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 13px;
  color: #969799;
  margin-bottom: 8px;
}

.book-meta {
  font-size: 12px;
  color: #646566;
  margin: 3px 0;
}

.due-date.overdue {
  color: #ee0a24;
  font-weight: 500;
}

.book-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}
</style>
