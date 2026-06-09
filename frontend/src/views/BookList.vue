<template>
  <div class="book-list">
    <el-card shadow="never">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="请输入书名/作者/ISBN搜索"
          style="width: 300px; margin-right: 10px;"
          clearable
          @keyup.enter="loadData"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadData">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button type="success" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增图书
        </el-button>
      </div>

      <el-table :data="tableData" border stripe style="width: 100%; margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="isbn" label="ISBN" width="150" />
        <el-table-column prop="name" label="书名" min-width="180" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="publisher" label="出版社" width="140" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="location" label="位置" width="100" />
        <el-table-column label="库存" width="120">
          <template #default="{ row }">
            {{ row.availableQuantity }} / {{ row.totalQuantity }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="form.isbn" placeholder="请输入ISBN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="书名" prop="name">
              <el-input v-model="form.name" placeholder="请输入书名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="form.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="form.category" placeholder="请输入分类" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存放位置" prop="location">
              <el-input v-model="form.location" placeholder="请输入存放位置" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总数量" prop="totalQuantity">
              <el-input-number v-model="form.totalQuantity" :min="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">上架</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="3"
                placeholder="请输入图书简介"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookList, addBook, updateBook, deleteBook } from '@/api/book'

const keyword = ref('')
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  isbn: '',
  name: '',
  author: '',
  publisher: '',
  category: '',
  location: '',
  totalQuantity: 1,
  status: 1,
  description: ''
})

const rules = {
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  name: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  totalQuantity: [{ required: true, message: '请输入总数量', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑图书' : '新增图书')

const loadData = async () => {
  try {
    const res = await getBookList({
      page: pagination.current,
      size: pagination.size,
      keyword: keyword.value
    })
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载图书列表失败', error)
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    isbn: '',
    name: '',
    author: '',
    publisher: '',
    category: '',
    location: '',
    totalQuantity: 1,
    status: 1,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该图书吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteBook(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败', error)
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateBook(form)
          ElMessage.success('更新成功')
        } else {
          await addBook(form)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('提交失败', error)
      }
    }
  })
}

loadData()
</script>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
}
</style>
