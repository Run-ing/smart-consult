<template>
  <main class="login-shell">
    <section class="brand-panel">
      <div class="brand-mark">SC</div>
      <div>
        <p class="eyebrow">SMART CONSULT</p>
        <h1>健康管理平台</h1>
        <p class="brand-copy">用结构化数据和持续随访，建立个人健康风险画像。</p>
      </div>
      <div class="signal-grid" aria-label="平台能力">
        <span>慢病风险评估</span>
        <span>智能问卷</span>
        <span>健康档案</span>
        <span>随访管理</span>
      </div>
    </section>

    <section class="form-panel" aria-label="手机号登录">
      <div class="form-card">
        <p class="form-kicker">手机号验证码登录</p>
        <h2>欢迎回来</h2>
        <p class="form-note">未注册手机号将在首次登录时自动创建账号。</p>

        <form @submit.prevent="submitLogin" class="login-form">
          <label>
            <span>手机号</span>
            <input
              v-model.trim="phone"
              type="tel"
              inputmode="numeric"
              autocomplete="tel"
              maxlength="11"
              placeholder="请输入手机号"
            />
          </label>

          <label>
            <span>验证码</span>
            <div class="code-row">
              <input
                v-model.trim="smsCode"
                type="text"
                inputmode="numeric"
                maxlength="6"
                autocomplete="one-time-code"
                placeholder="6 位验证码"
              />
              <button type="button" class="secondary-button" :disabled="sendDisabled" @click="sendCode">
                {{ sendButtonText }}
              </button>
            </div>
          </label>

          <div v-if="mockCode" class="mock-code">
            <span>开发环境验证码</span>
            <strong>{{ mockCode }}</strong>
          </div>

          <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

          <button class="primary-button" type="submit" :disabled="loginLoading">
            {{ loginLoading ? '登录中...' : '登录' }}
          </button>
        </form>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getApiErrorMessage } from '../api/http'
import { loginWithSmsCode, requestSmsCode } from '../api/auth'
import { setAuth } from '../stores/auth'

const router = useRouter()
const phone = ref('')
const smsCode = ref('')
const mockCode = ref('')
const errorMessage = ref('')
const sendLoading = ref(false)
const loginLoading = ref(false)
const countdown = ref(0)
let countdownTimer: number | undefined

const phoneValid = computed(() => /^1[3-9]\d{9}$/.test(phone.value))
const sendDisabled = computed(() => sendLoading.value || countdown.value > 0 || !phoneValid.value)
const sendButtonText = computed(() => {
  if (sendLoading.value) return '发送中...'
  if (countdown.value > 0) return `${countdown.value}s`
  return '获取验证码'
})

async function sendCode() {
  if (!phoneValid.value) {
    errorMessage.value = '请输入正确的手机号'
    return
  }
  errorMessage.value = ''
  sendLoading.value = true
  try {
    const response = await requestSmsCode(phone.value)
    mockCode.value = response.mockCode || ''
    countdown.value = Math.min(response.expiresInSeconds, 60)
    countdownTimer = window.setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0 && countdownTimer) {
        window.clearInterval(countdownTimer)
        countdownTimer = undefined
      }
    }, 1000)
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error)
  } finally {
    sendLoading.value = false
  }
}

async function submitLogin() {
  if (!phoneValid.value) {
    errorMessage.value = '请输入正确的手机号'
    return
  }
  if (!/^\d{6}$/.test(smsCode.value)) {
    errorMessage.value = '请输入 6 位验证码'
    return
  }
  errorMessage.value = ''
  loginLoading.value = true
  try {
    const response = await loginWithSmsCode(phone.value, smsCode.value)
    setAuth(response.token, response.user)
    await router.push('/home')
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error)
  } finally {
    loginLoading.value = false
  }
}

onBeforeUnmount(() => {
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
  }
})
</script>
