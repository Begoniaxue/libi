<template>
  <el-container class="app-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon size="32" color="#409EFF"><Reading /></el-icon>
        <span class="title">图书馆管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409EFF"
        class="menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/books">
          <el-icon><Reading /></el-icon>
          <span>图书管理</span>
        </el-menu-item>
        <el-menu-item index="/readers">
          <el-icon><User /></el-icon>
          <span>读者管理</span>
        </el-menu-item>
        <el-menu-item index="/borrow">
          <el-icon><DocumentCopy /></el-icon>
          <span>借阅管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="page-title">{{ pageTitle }}</span>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const titles = {
    '/dashboard': '数据概览',
    '/books': '图书管理',
    '/readers': '读者管理',
    '/borrow': '借阅管理'
  }
  return titles[route.path] || '图书馆管理系统'
})
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.sidebar {
  background-color: #001529;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  padding: 20px;
  color: #fff;
  gap: 10px;
  border-bottom: 1px solid #1f3a57;
}

.logo .title {
  font-size: 18px;
  font-weight: bold;
}

.menu {
  border-right: none;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.main-content {
  background-color: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}
</style>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
