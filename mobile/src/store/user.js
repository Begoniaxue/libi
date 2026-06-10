import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginByCardNo, getReaderInfo } from '@/api'

export const useUserStore = defineStore('user', () => {
  const readerInfo = ref(null)
  const readerInfoData = ref(null)
  const isLoggedIn = computed(() => !!readerInfo.value)

  const initFromStorage = () => {
    const stored = localStorage.getItem('readerInfo')
    if (stored) {
      readerInfo.value = JSON.parse(stored)
    }
  }

  const login = async (cardNo) => {
    const res = await loginByCardNo(cardNo)
    if (res.data) {
      readerInfo.value = res.data
      localStorage.setItem('readerInfo', JSON.stringify(res.data))
      await loadReaderInfoData()
      return res.data
    }
    return null
  }

  const loadReaderInfoData = async () => {
    if (readerInfo.value) {
      const res = await getReaderInfo(readerInfo.value.id)
      readerInfoData.value = res.data
      return res.data
    }
    return null
  }

  const refreshReaderInfo = () => {
    const stored = localStorage.getItem('readerInfo')
    if (stored) {
      readerInfo.value = JSON.parse(stored)
    }
  }

  const logout = () => {
    readerInfo.value = null
    readerInfoData.value = null
    localStorage.removeItem('readerInfo')
  }

  return {
    readerInfo,
    readerInfoData,
    isLoggedIn,
    initFromStorage,
    login,
    loadReaderInfoData,
    refreshReaderInfo,
    logout
  }
})
