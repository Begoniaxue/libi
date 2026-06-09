import { beforeEach } from 'vitest'
import { config } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

config.global.plugins.push(ElementPlus)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  config.global.components[key] = component
}

const mockECharts = {
  init: vi.fn().mockReturnValue({
    setOption: vi.fn(),
    resize: vi.fn(),
    dispose: vi.fn(),
    on: vi.fn()
  })
}

vi.mock('echarts', () => mockECharts)

beforeEach(() => {
  vi.clearAllMocks()
})
