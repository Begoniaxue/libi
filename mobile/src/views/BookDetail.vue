<template>
  <div class="detail-page">
    <van-nav-bar title="图书详情" left-arrow @click-left="$router.back()" fixed placeholder />

    <div v-if="book" class="book-detail">
      <div class="book-header">
        <div class="book-cover">📖</div>
        <div class="book-basic">
          <h1 class="book-name">{{ book.name }}</h1>
          <p class="book-author">{{ book.author }}</p>
          <p class="book-isbn">ISBN：{{ book.isbn }}</p>
          <div class="book-tags">
            <van-tag type="primary" size="medium">{{ book.category }}</van-tag>
            <van-tag size="medium">{{ book.publisher }}</van-tag>
          </div>
        </div>
      </div>

      <div class="stock-card">
        <div class="stock-item">
          <div class="stock-value" :class="book.availableQuantity > 0 ? 'text-success' : 'text-danger'">
            {{ book.availableQuantity }}
          </div>
          <div class="stock-label">可借数量</div>
        </div>
        <div class="stock-item">
          <div class="stock-value">{{ book.totalQuantity }}</div>
          <div class="stock-label">总馆藏</div>
        </div>
        <div class="stock-item">
          <div class="stock-value">{{ book.totalQuantity - book.availableQuantity }}</div>
          <div class="stock-label">已借出</div>
        </div>
      </div>

      <div class="info-card">
        <div class="card-title">馆藏信息</div>
        <van-cell-group inset>
          <van-cell title="存放位置" :value="book.location" />
          <van-cell title="出版社" :value="book.publisher" />
          <van-cell title="出版日期" :value="book.publishDate" />
          <van-cell title="状态" :value="book.status === 1 ? '在架' : '下架'" />
        </van-cell-group>
      </div>

      <div class="info-card" v-if="book.description">
        <div class="card-title">内容简介</div>
        <div class="description">
          <p>{{ book.description }}</p>
        </div>
      </div>

      <div class="bottom-bar">
        <div class="status-text">
          <van-icon
            :name="book.availableQuantity > 0 ? 'checked' : 'close'"
            :color="book.availableQuantity > 0 ? '#07c160' : '#ee0a24'"
          />
          <span :class="book.availableQuantity > 0 ? 'text-success' : 'text-danger'">
            {{ book.availableQuantity > 0 ? '馆藏充足，可借阅' : '暂无馆藏可借' }}
          </span>
        </div>
        <van-button
          type="primary"
          round
          :disabled="book.availableQuantity === 0"
          @click="showBorrowTip"
        >
          {{ book.availableQuantity > 0 ? '前往借阅' : '暂无馆藏' }}
        </van-button>
      </div>
    </div>

    <van-empty v-if="!loading && !book" description="图书不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getBookDetail } from '@/api'
import { showToast, showLoadingToast, closeToast } from 'vant'

const route = useRoute()
const book = ref(null)
const loading = ref(false)

const loadData = async () => {
  showLoadingToast({
    message: '加载中...',
    forbidClick: true
  })
  
  try {
    const res = await getBookDetail(route.params.id)
    if (res.data) {
      book.value = res.data
    }
  } catch (error) {
    console.error('加载图书详情失败:', error)
  } finally {
    closeToast()
  }
}

const showBorrowTip = () => {
  showToast('请携带借书证到图书馆借阅处办理借阅手续')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 80px;
}

.book-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px 20px;
  display: flex;
  gap: 20px;
  color: #fff;
}

.book-cover {
  width: 100px;
  height: 130px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 50px;
  flex-shrink: 0;
}

.book-basic {
  flex: 1;
  min-width: 0;
}

.book-name {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 8px 0;
  line-height: 1.3;
}

.book-author {
  font-size: 14px;
  opacity: 0.9;
  margin: 0 0 6px 0;
}

.book-isbn {
  font-size: 13px;
  opacity: 0.8;
  margin: 0 0 12px 0;
}

.book-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.stock-card {
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

.stock-item {
  text-align: center;
}

.stock-value {
  font-size: 28px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 4px;
}

.stock-label {
  font-size: 13px;
  color: #969799;
}

.info-card {
  background: #fff;
  margin: 12px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 12px;
}

.description {
  font-size: 14px;
  color: #646566;
  line-height: 1.8;
}

.description p {
  margin: 0;
  text-indent: 2em;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.status-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}
</style>
