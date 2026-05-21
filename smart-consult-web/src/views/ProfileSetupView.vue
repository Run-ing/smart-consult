<template>
  <main class="profile-setup-shell">
    <section class="profile-setup-header">
      <div>
        <p class="eyebrow">HEALTH PROFILE</p>
        <h1>完善健康档案</h1>
        <p>首次使用前补全基础画像，用于后续 BMI、年龄门槛和中心型肥胖风险判断。</p>
      </div>
      <button class="secondary-button" type="button" @click="logout">退出登录</button>
    </section>

    <form class="profile-setup-form" @submit.prevent="submitProfile">
      <fieldset class="segmented-field">
        <legend>性别</legend>
        <label :class="{ active: form.sex === 'MALE' }">
          <input v-model="form.sex" type="radio" value="MALE" />
          <span>男性</span>
        </label>
        <label :class="{ active: form.sex === 'FEMALE' }">
          <input v-model="form.sex" type="radio" value="FEMALE" />
          <span>女性</span>
        </label>
      </fieldset>

      <div class="profile-form-grid">
        <label>
          <span>出生日期</span>
          <input v-model="form.birthDate" type="date" :max="today" />
        </label>

        <label>
          <span>身高 cm</span>
          <input v-model.number="form.heightCm" type="number" min="50" max="250" step="0.1" placeholder="如 175" />
        </label>

        <label>
          <span>体重 kg</span>
          <input v-model.number="form.weightKg" type="number" min="10" max="300" step="0.1" placeholder="如 70" />
        </label>

        <label>
          <span>腰围 cm</span>
          <input v-model.number="form.waistCm" type="number" min="30" max="250" step="0.1" placeholder="可选，不清楚可留空" />
        </label>
      </div>

      <div class="profile-summary">
        <div>
          <span>系统计算年龄</span>
          <strong>{{ computedAge || '-' }}</strong>
        </div>
        <div>
          <span>BMI 计算项</span>
          <strong>{{ bmiPreview }}</strong>
        </div>
      </div>

      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

      <button class="primary-button" type="submit" :disabled="submitLoading">
        {{ submitLoading ? '保存中...' : '保存并进入平台' }}
      </button>
    </form>
  </main>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchCurrentUser, saveHealthProfile, type HealthProfileRequest } from '../api/auth'
import { getApiErrorMessage } from '../api/http'
import { clearAuth, getToken, setAuth } from '../stores/auth'

const router = useRouter()
const today = new Date().toISOString().slice(0, 10)
const submitLoading = ref(false)
const errorMessage = ref('')

const form = reactive<HealthProfileRequest>({
  sex: 'MALE',
  birthDate: '',
  heightCm: 0,
  weightKg: 0,
  waistCm: null
})

const computedAge = computed(() => {
  if (!form.birthDate) return 0
  const birthDate = new Date(form.birthDate)
  const now = new Date()
  let age = now.getFullYear() - birthDate.getFullYear()
  const birthdayPassed =
    now.getMonth() > birthDate.getMonth() ||
    (now.getMonth() === birthDate.getMonth() && now.getDate() >= birthDate.getDate())
  if (!birthdayPassed) age -= 1
  return age > 0 ? age : 0
})

const bmiPreview = computed(() => {
  if (!form.heightCm || !form.weightKg) return '-'
  const meters = form.heightCm / 100
  return (form.weightKg / (meters * meters)).toFixed(1)
})

async function submitProfile() {
  if (!form.birthDate) {
    errorMessage.value = '请选择出生日期'
    return
  }
  if (!form.heightCm || form.heightCm < 50 || form.heightCm > 250) {
    errorMessage.value = '请输入合理的身高'
    return
  }
  if (!form.weightKg || form.weightKg < 10 || form.weightKg > 300) {
    errorMessage.value = '请输入合理的体重'
    return
  }
  if (form.waistCm && (form.waistCm < 30 || form.waistCm > 250)) {
    errorMessage.value = '请输入合理的腰围，或留空'
    return
  }

  errorMessage.value = ''
  submitLoading.value = true
  try {
    await saveHealthProfile({
      ...form,
      waistCm: form.waistCm || null
    })
    const currentUser = await fetchCurrentUser()
    setAuth(getToken(), currentUser)
    await router.push('/home')
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error)
  } finally {
    submitLoading.value = false
  }
}

async function logout() {
  clearAuth()
  await router.push('/login')
}
</script>
