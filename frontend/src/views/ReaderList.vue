<template>
  <div class="reader-list">
    <el-card shadow="never">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="请输入姓名/借书证号/手机号搜索"
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
          新增读者
        </el-button>
      </div>

      <el-table :data="tableData" border stripe style="width: 100%; margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="cardNo" label="借书证号" width="140" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '正常' : '注销' }}
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
            <el-form-item label="借书证号" prop="cardNo">
              <el-input v-model="form.cardNo" placeholder="请输入借书证号">
                <template #append>
                  <el-button @click="generateCardNo">生成</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">注销</el-radio>
              </el-radio-group>
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
import { getReaderList, addReader, updateReader, deleteReader, generateCardNo as genCardNo } from '@/api/reader'

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
  cardNo: '',
  name: '',
  gender: 1,
  phone: '',
  email: '',
  idCard: '',
  address: '',
  status: 1
})

const rules = {
  cardNo: [{ required: true, message: '请输入借书证号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑读者' : '新增读者')

const loadData = async () => {
  try {
    const res = await getReaderList({
      page: pagination.current,
      size: pagination.size,
      keyword: keyword.value
    })
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    console.error('加载读者列表失败', error)
  }
}

const generateCardNo = async () => {
  try {
    const res = await genCardNo()
    form.cardNo = res.data
  } catch (error) {
    console.error('生成借书证号失败', error)
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    cardNo: '',
    name: '',
    gender: 1,
    phone: '',
    email: '',
    idCard: '',
    address: '',
    status: 1
  })
  generateCardNo()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该读者吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteReader(row.id)
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
          await updateReader(form)
          ElMessage.success('更新成功')
        } else {
          await addReader(form)
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
