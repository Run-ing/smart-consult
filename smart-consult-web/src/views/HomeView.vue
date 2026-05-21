<template>
  <main class="home-shell">
    <header class="home-header">
      <div>
        <p class="eyebrow">HEALTH WORKSPACE</p>
        <h1>个人健康管理</h1>
      </div>
      <button class="secondary-button" type="button" @click="logout">退出登录</button>
    </header>

    <section class="profile-strip">
      <div>
        <span>用户 ID</span>
        <strong>{{ user?.id || '-' }}</strong>
      </div>
      <div>
        <span>手机号</span>
        <strong>{{ user?.phone || '-' }}</strong>
      </div>
      <div>
        <span>昵称</span>
        <strong>{{ user?.nickname || '-' }}</strong>
      </div>
      <div>
        <span>最后登录</span>
        <strong>{{ formattedLoginTime }}</strong>
      </div>
    </section>

    <section class="workspace-grid">
      <article>
        <p>风险评估</p>
        <strong>待开始</strong>
        <span>慢病风险问卷将从这里进入。</span>
      </article>
      <article>
        <p>健康档案</p>
        <strong>已就绪</strong>
        <span>基础账号信息已完成初始化。</span>
      </article>
      <article>
        <p>随访任务</p>
        <strong>0</strong>
        <span>后续可接入提醒和随访计划。</span>
      </article>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchCurrentUser, type UserProfile } from '../api/auth'
import { clearAuth, getStoredUser, setAuth, getToken } from '../stores/auth'

const router = useRouter()
const user = ref<UserProfile | null>(getStoredUser())

const formattedLoginTime = computed(() => {
  if (!user.value?.lastLoginTime) {
    return '-'
  }
  return new Date(user.value.lastLoginTime).toLocaleString('zh-CN', { hour12: false })
})

onMounted(async () => {
  if (!getToken()) {
    await router.push('/login')
    return
  }
  try {
    const currentUser = await fetchCurrentUser()
    user.value = currentUser
    setAuth(getToken(), currentUser)
  } catch {
    clearAuth()
    await router.push('/login')
  }
})

async function logout() {
  clearAuth()
  await router.push('/login')
}
</script>
