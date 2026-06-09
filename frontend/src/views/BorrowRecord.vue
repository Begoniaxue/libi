<template>
  <div class="borrow-record">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span style="font-weight: 600;">借书操作</span>
          </template>
          <el-form :model="borrowForm" label-width="100px">
            <el-form-item label="借书证号">
              <el-input v-model="borrowForm.cardNo" placeholder="请输入借书证号" style="width: 250px;">
                <template #append>
                  <el-button type="primary" @click="queryReader">查询</el-button>
                </template>
              </el-input>
            </el-form-item>
            <div v-if="readerInfo" class="reader-info">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="姓名">{{ readerInfo.name }}</el-descriptions-item>
                <el-descriptions-item label="借书证号">{{ readerInfo.cardNo }}</el-descriptions-item>
                <el-descriptions-item label="已借数量">
                  <el-tag :type="borrowInfo.borrowingCount >= 5 ? 'danger' : 'success'">
                    {{ borrowInfo.borrowingCount }} / {{ borrowInfo.maxBorrowCount }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="逾期数量">
                  <el-tag :type="borrowInfo.overdueCount > 0 ? 'danger' : 'info'">
                    {{ borrowInfo.overdueCount }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
              <el-alert
                v-if="!borrowInfo.canBorrow"
                title="无法借书"
                :description="borrowInfo.borrowingCount >= 5 ? '已达到最大借阅数量' : '存在逾期未还图书'"
                type="error"
                :closable="false"
                style="margin-top: 10px;"
              />
            </div>
            <el-form-item label="选择图书" v-if="borrowInfo.canBorrow">
              <el-select
                v-model="borrowForm.bookId"
                placeholder="请选择要借阅的图书"
                style="width: 100%;"
                filterable
              >
                <el-option
                  v-for="book in availableBooks"
                  :key="book.id"
                  :label="`${book.name} - ${book.author} (剩余:${book.availableQuantity})`"
                  :value="book.id"
                  :disabled="book.availableQuantity <= 0"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="borrowInfo.canBorrow">
              <el-button type="primary" @click="handleBorrow" :disabled="!borrowForm.bookId">
                <el-icon><Plus /></el-icon>
                确认借书
              </el-button>
              <el-button @click="loadAvailableBooks">
                <el-icon><Refresh /></el-icon>
                刷新图书
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span style="font-weight: 600;">还书操作</span>
          </template>
          <el-alert
            title="温馨提示"
            description="请在下方借阅记录列表中点击「还书」按钮完成还书操作"
            type="info"
            :closable="false"
            show-icon
          />
          <div style="margin-top: 20px;">
            <el-button type="warning" @click="handleCheckOverdue">
              <el-icon><Warning /></el-icon>
              检查逾期图书
            </el-button>
            <el-button @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新记录
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px;">
      <div class="search-bar">
        <el-select v-model="filterStatus" placeholder="按状态筛选" style="width: 150px; margin-right: 10px;" clearable>
          <el-option label="借阅中" :value="1" />
          <el-option label="已归还" :value="2" />
          <el-option label="已逾期" :value="3" />
        </el-select>
        <el-button type="primary" @click="loadData">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
      </div>

      <el-table :data="tableData" border stripe style="width: 100%; margin-top: 20px;">
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column label="图书信息" min-width="180">
          <template #default="{ row }">
            <div v-if="row.book">
              <div style="font-weight: 600;">{{ row.book.name }}</div>
              <div style="color: #909399; font-size: 12px;">ISBN: {{ row.book.isbn }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="读者信息" width="150">
          <template #default="{ row }">
            <div v-if="row.reader">
              <div style="font-weight: 600;">{{ row.reader.name }}</div>
              <div style="color: #909399; font-size: 12px;">{{ row.reader.cardNo }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="borrowDate" label="借书日期" width="120" />
        <el-table-column prop="dueDate" label="应还日期" width="120" />
        <el-table-column prop="returnDate" label="实还日期" width="120">
          <template #default="{ row }">
            {{ row.returnDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 2" type="success">
              已归还
            </el-tag>
            <el-tag v-else-if="row.status === 3 || row.isOverdue === 1" type="danger" effect="dark">
              已逾期
            </el-tag>
            <el-tag v-else-if="row.status === 1" type="primary">
              借阅中
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1 || row.status === 3"
              type="success"
              size="small"
              @click="handleReturn(row)"
            >
              还书
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecordList, borrowBook, returnBook, checkOverdue, getReaderBorrowInfo } from '@/api/borrow'
import { getReaderByCardNo } from '@/api/reader'
import { getAvailableBooks } from '@/api/book'

const filterStatus = ref(null)
const tableData = ref([])
const availableBooks = ref([])
const readerInfo = ref(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const borrowForm = reactive({
  cardNo: '',
  bookId: null
})

const borrowInfo = reactive({
  borrowingCount: 0,
  overdueCount: 0,
  maxBorrowCount: 5,
  canBorrow: false
})

const loadData = async () => {
  try {
    const res = await getRecordList({
      page: pagination.current,
      size: pagination.size,
      status: filterStatus.value
    })
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载借阅记录失败', error)
  }
}

const loadAvailableBooks = async () => {
  try {
    const res = await getAvailableBooks()
    availableBooks.value = res.data
  } catch (error) {
    console.error('加载可借图书失败', error)
  }
}

const queryReader = async () => {
  if (!borrowForm.cardNo) {
    ElMessage.warning('请输入借书证号')
    return
  }
  try {
    const res = await getReaderByCardNo(borrowForm.cardNo)
    if (res.data) {
      readerInfo.value = res.data
      const infoRes = await getReaderBorrowInfo(res.data.id)
      Object.assign(borrowInfo, infoRes.data)
      if (borrowInfo.canBorrow) {
        loadAvailableBooks()
      }
    } else {
      ElMessage.warning('未找到该读者')
      readerInfo.value = null
    }
  } catch (error) {
    console.error('查询读者失败', error)
  }
}

const handleBorrow = async () => {
  if (!readerInfo.value || !borrowForm.bookId) {
    ElMessage.warning('请先选择读者和图书')
    return
  }
  ElMessageBox.confirm(`确认将图书借给 ${readerInfo.value.name} 吗？`, '借书确认', {
    confirmButtonText: '确认借书',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await borrowBook(borrowForm.bookId, readerInfo.value.id)
      ElMessage.success('借书成功')
      borrowForm.bookId = null
      queryReader()
      loadData()
      loadAvailableBooks()
    } catch (error) {
      console.error('借书失败', error)
    }
  }).catch(() => {})
}

const handleReturn = (row) => {
  const bookName = row.book ? row.book.name : ''
  const readerName = row.reader ? row.reader.name : ''
  ElMessageBox.confirm(`确认 ${readerName} 归还《${bookName}》吗？`, '还书确认', {
    confirmButtonText: '确认还书',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await returnBook(row.id)
      ElMessage.success('还书成功')
      loadData()
      if (readerInfo.value) {
        queryReader()
      }
      loadAvailableBooks()
    } catch (error) {
      console.error('还书失败', error)
    }
  }).catch(() => {})
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

loadData()
loadAvailableBooks()
</script>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
}

.reader-info {
  margin-bottom: 20px;
}
</style>
