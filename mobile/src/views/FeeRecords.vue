<template>
  <div class="fee-page">
    <van-nav-bar title="罚款明细" left-arrow @click-left="$router.back()" fixed placeholder />

    <div class="sticky-wrapper">
      <div class="summary-card" v-if="summaryData">
        <div class="summary-item">
          <div class="summary-value">¥{{ summaryData?.totalAmount }}</div>
          <div class="summary-label">总罚款</div>
        </div>
        <div class="summary-item">
          <div class="summary-value text-success">¥{{ summaryData?.paidAmount }}</div>
          <div class="summary-label">已缴纳</div>
        </div>
        <div class="summary-item">
          <div class="summary-value text-danger">¥{{ summaryData?.unpaidAmount }}</div>
          <div class="summary-label">待缴纳</div>
        </div>
      </div>

      <van-tabs v-model:active="activeTab">
        <van-tab title="全部" name="all" />
        <van-tab title="未缴纳" name="unpaid" />
        <van-tab title="已缴纳" name="paid" />
      </van-tabs>
    </div>

    <div class="page-content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div class="fee-item" v-for="record in list" :key="record?.id">
            <div class="fee-header">
              <div class="fee-type">
                <span class="type-icon">{{ getTypeIcon(record?.feeType) }}</span>
                <span class="type-text">{{ record?.feeType || '-' }}</span>
              </div>
              <div class="fee-amount" :class="record?.isPaid === 1 ? 'text-success' : 'text-danger'">
                {{ record?.isPaid === 1 ? '+' : '-' }}¥{{ record?.amount || '0.00' }}
              </div>
            </div>
            <div class="fee-info">
              <p v-if="record?.borrowRecord?.book?.name">
                <span>图书：</span>{{ record.borrowRecord.book.name }}
              </p>
              <p><span>金额：</span>¥{{ record?.amount || '0.00' }}</p>
              <p><span>已缴：</span>¥{{ record?.paidAmount || '0.00' }}</p>
              <p v-if="record?.remark"><span>备注：</span>{{ record.remark }}</p>
              <p><span>时间：</span>{{ record?.createTime || '-' }}</p>
            </div>
            <div class="fee-status">
              <van-tag :type="record?.isPaid === 1 ? 'success' : 'danger'" size="small">
                {{ record?.isPaid === 1 ? '已缴纳' : '未缴纳' }}
              </van-tag>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>

      <van-empty v-if="!loading && list.length === 0" description="暂无罚款记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getFeeRecords } from '@/api'
import { useUserStore } from '@/store/user'
import { storeToRefs } from 'pinia'

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = 10
const activeTab = ref('all')
const summaryData = ref(null)

const userStore = useUserStore()
const { readerInfo } = storeToRefs(userStore)

watch(() => userStore.readerInfo?.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    summaryData.value = null
    onRefresh()
  }
})

watch(activeTab, () => {
  onRefresh()
})

const getTypeIcon = (type) => {
  const map = { '逾期罚款': '⏰', '赔偿': '📚', '其他': '📝' }
  return map[type] || '📝'
}

const getIsPaidParam = () => {
  const map = { unpaid: 0, paid: 1 }
  return map[activeTab.value] || null
}

const calculateSummary = () => {
  let totalAmount = 0
  let paidAmount = 0
  list.value.forEach(item => {
    totalAmount += Number(item?.amount) || 0
    paidAmount += Number(item?.paidAmount) || 0
  })
  summaryData.value = {
    totalAmount: totalAmount.toFixed(2),
    paidAmount: paidAmount.toFixed(2),
    unpaidAmount: (totalAmount - paidAmount).toFixed(2)
  }
}

const onLoad = async () => {
  if (!readerInfo.value?.id) return
  
  try {
    const res = await getFeeRecords(readerInfo.value.id, {
      page: page.value,
      size: pageSize,
      isPaid: getIsPaidParam()
    })
    
    const newList = res?.data?.list || []
    if (page.value === 1) {
      list.value = newList
    } else {
      list.value = [...list.value, ...newList]
    }
    calculateSummary()
    
    if (newList.length < pageSize) {
      finished.value = true
    } else {
      page.value++
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    calculateSummary()
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
.fee-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.sticky-wrapper {
  position: sticky;
  top: 46px;
  z-index: 100;
  background: #f7f8fa;
}

.sticky-wrapper :deep(.van-tabs) {
  background: #f7f8fa;
}

.sticky-wrapper :deep(.van-tabs__wrap) {
  border-bottom: none;
}

.summary-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin: 12px;
  padding: 16px;
  border-radius: 12px;
  display: flex;
  justify-content: space-around;
  color: #fff;
}

.summary-item {
  text-align: center;
}

.summary-value {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.summary-label {
  font-size: 12px;
  opacity: 0.85;
}

.page-content {
  padding: 0 12px 12px;
}

.page-content :deep(.van-pull-refresh) {
  padding-top: 0;
  margin-top: 0;
}

.page-content :deep(.van-list) {
  padding-top: 0;
  margin-top: 0;
}

.fee-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.fee-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.fee-type {
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-icon {
  font-size: 20px;
}

.type-text {
  font-size: 15px;
  font-weight: 500;
  color: #323233;
}

.fee-amount {
  font-size: 20px;
  font-weight: 600;
}

.fee-info p {
  font-size: 13px;
  color: #646566;
  margin: 5px 0;
}

.fee-info span {
  color: #969799;
}

.fee-status {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebedf0;
}
</style>
