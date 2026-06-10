<template>
  <div class="search-page">
    <van-nav-bar title="馆藏查询" fixed placeholder>
      <template #right>
        <van-icon name="filter-o" size="20" @click="showFilter = true" />
      </template>
    </van-nav-bar>

    <div class="search-bar">
      <van-search
        v-model="keyword"
        placeholder="搜索书名、作者、ISBN..."
        show-action
        shape="round"
        @search="onSearch"
        @clear="onSearch"
      >
        <template #action>
          <div @click="onSearch">搜索</div>
        </template>
      </van-search>
    </div>

    <div class="filter-tags" v-if="categories.length > 0">
      <van-scroll-view scroll-x>
        <div class="tag-list">
          <span
            class="tag"
            :class="{ active: selectedCategory === '' }"
            @click="selectCategory('')"
          >
            全部
          </span>
          <span
            v-for="cat in categories"
            :key="cat"
            class="tag"
            :class="{ active: selectedCategory === cat }"
            @click="selectCategory(cat)"
          >
            {{ cat }}
          </span>
        </div>
      </van-scroll-view>
    </div>

    <div class="page-content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div
            class="book-item"
            v-for="book in list"
            :key="book.id"
            @click="$router.push(`/books/${book.id}`)"
          >
            <div class="book-cover">📖</div>
            <div class="book-info">
              <h3 class="book-name">{{ book.name }}</h3>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-meta">
                <span>{{ book.category }}</span>
                <span>·</span>
                <span>{{ book.publisher }}</span>
              </p>
              <p class="book-location">
                <van-icon name="location-o" />
                {{ book.location }}
              </p>
              <div class="book-stock">
                <span class="stock-info">
                  可借 <b :class="book.availableQuantity > 0 ? 'text-success' : 'text-danger'">
                    {{ book.availableQuantity }}
                  </b> / {{ book.totalQuantity }} 册
                </span>
                <van-tag
                  :type="book.availableQuantity > 0 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ book.availableQuantity > 0 ? '可借' : '已借完' }}
                </van-tag>
              </div>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>

      <van-empty v-if="!loading && list.length === 0" description="暂无符合条件的图书" />
    </div>

    <van-popup v-model:show="showFilter" position="right" :style="{ width: '80%', height: '100%' }">
      <div class="filter-panel">
        <div class="filter-header">
          <span class="filter-title">高级筛选</span>
          <van-icon name="close" size="20" @click="showFilter = false" />
        </div>
        
        <van-cell-group inset>
          <van-field
            v-model="filterForm.name"
            label="书名"
            placeholder="请输入书名"
            clearable
          />
          <van-field
            v-model="filterForm.author"
            label="作者"
            placeholder="请输入作者"
            clearable
          />
          <van-field
            v-model="filterForm.isbn"
            label="ISBN"
            placeholder="请输入ISBN"
            clearable
          />
          <van-field
            v-model="filterForm.publisher"
            label="出版社"
            placeholder="请输入出版社"
            clearable
          />
          <van-field
            v-model="filterForm.category"
            label="分类"
            placeholder="请输入分类"
            clearable
          />
        </van-cell-group>

        <div class="filter-actions">
          <van-button type="default" block @click="resetFilter">重置</van-button>
          <van-button type="primary" block @click="applyFilter">确定</van-button>
        </div>
      </div>
    </van-popup>

    <van-tabbar v-model="active" route>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="search" to="/books">查询</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { searchBooks, getCategories } from '@/api'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = 10
const keyword = ref('')
const categories = ref([])
const selectedCategory = ref('')
const showFilter = ref(false)
const active = ref(1)

const filterForm = reactive({
  name: '',
  author: '',
  isbn: '',
  category: '',
  publisher: ''
})

onMounted(() => {
  loadCategories()
})

const loadCategories = async () => {
  try {
    const res = await getCategories()
    if (res.data) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const selectCategory = (cat) => {
  selectedCategory.value = cat
  filterForm.category = cat
  onRefresh()
}

const onSearch = () => {
  onRefresh()
}

const resetFilter = () => {
  filterForm.name = ''
  filterForm.author = ''
  filterForm.isbn = ''
  filterForm.category = ''
  filterForm.publisher = ''
  selectedCategory.value = ''
}

const applyFilter = () => {
  showFilter.value = false
  selectedCategory.value = filterForm.category || ''
  onRefresh()
}

const onLoad = async () => {
  try {
    const params = {
      page: page.value,
      size: pageSize,
      keyword: keyword.value || undefined,
      name: filterForm.name || undefined,
      author: filterForm.author || undefined,
      isbn: filterForm.isbn || undefined,
      category: filterForm.category || undefined,
      publisher: filterForm.publisher || undefined
    }
    
    const res = await searchBooks(params)
    
    if (res.data) {
      const newList = res.data.list || []
      if (page.value === 1) {
        list.value = newList
      } else {
        list.value = [...list.value, ...newList]
      }
      
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
.search-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 50px;
}

.search-bar {
  padding: 12px;
  background: #fff;
}

.filter-tags {
  background: #fff;
  padding: 0 12px 12px;
  border-bottom: 1px solid #ebedf0;
}

.tag-list {
  display: flex;
  gap: 8px;
  padding: 4px 0;
}

.tag {
  flex-shrink: 0;
  padding: 6px 14px;
  background: #f2f3f5;
  border-radius: 16px;
  font-size: 13px;
  color: #646566;
  transition: all 0.2s;
}

.tag.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
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
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 13px;
  color: #969799;
  margin: 0 0 6px 0;
}

.book-meta {
  font-size: 12px;
  color: #969799;
  margin: 3px 0;
}

.book-meta span {
  margin-right: 4px;
}

.book-location {
  font-size: 12px;
  color: #646566;
  margin: 6px 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.book-stock {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.stock-info {
  font-size: 13px;
  color: #646566;
}

.stock-info b {
  font-size: 15px;
}

.filter-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f7f8fa;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #ebedf0;
}

.filter-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
}

.filter-actions {
  padding: 16px;
  display: flex;
  gap: 12px;
  margin-top: auto;
}
</style>
