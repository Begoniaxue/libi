<template>
  <div class="login-container">
    <div class="login-header">
      <div class="logo">📚</div>
      <h1>图书馆移动端</h1>
      <p class="subtitle">欢迎使用图书馆读者服务</p>
    </div>

    <div class="login-form">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="cardNo"
            name="cardNo"
            label="借书证号"
            placeholder="请输入借书证号"
            :rules="[{ required: true, message: '请输入借书证号' }]"
          />
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit">
            登录
          </van-button>
        </div>
        <div class="tips">
          <p>演示账号：R2024001、R2024002、R2024003</p>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { loginByCardNo } from '@/api'

const router = useRouter()
const cardNo = ref('')

const onSubmit = async () => {
  try {
    showLoadingToast({
      message: '登录中...',
      forbidClick: true
    })
    
    const res = await loginByCardNo(cardNo.value)
    if (res.data) {
      localStorage.setItem('readerInfo', JSON.stringify(res.data))
      showToast('登录成功')
      setTimeout(() => {
        router.push('/home')
      }, 500)
    } else {
      showToast('借书证号不存在')
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    closeToast()
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 20px 0;
}

.login-header {
  text-align: center;
  color: #fff;
  margin-bottom: 40px;
}

.logo {
  font-size: 60px;
  margin-bottom: 16px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  opacity: 0.8;
}

.login-form {
  background: #fff;
  border-radius: 16px;
  padding: 24px 0;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.tips {
  text-align: center;
  color: #969799;
  font-size: 12px;
  margin-top: 16px;
}
</style>
