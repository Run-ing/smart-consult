<template>
  <main class="chat-shell">
    <aside class="chat-sidebar">
      <div>
        <p class="eyebrow">HEALTH ADVISOR</p>
        <h1>智能健康顾问</h1>
        <p class="advisor-note">基于你的建档信息，持续整理健康问题、风险线索和下一步建议。</p>
      </div>

      <section class="member-panel">
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

      <button class="secondary-button" type="button" @click="logout">退出登录</button>
    </aside>

    <section class="chat-workspace" aria-label="健康顾问对话">
      <header class="chat-header">
        <div>
          <p class="eyebrow">CONSULTATION</p>
          <h2>健康问答</h2>
        </div>
        <span class="advisor-status">在线</span>
      </header>

      <div ref="messageListRef" class="message-list">
        <article
          v-for="message in messages"
          :key="message.id"
          class="message-row"
          :class="message.role"
        >
          <div class="message-bubble">
            <span class="message-author">{{ message.role === 'assistant' ? '健康顾问' : '我' }}</span>
            <p>{{ message.content }}</p>
          </div>
        </article>
      </div>

      <form class="chat-composer" @submit.prevent="sendMessage">
        <input
          v-model.trim="draft"
          type="text"
          autocomplete="off"
          placeholder="描述你的症状、体检指标或健康问题"
        />
        <button class="primary-button" type="submit" :disabled="!draft || replying">
          {{ replying ? '思考中...' : '发送' }}
        </button>
      </form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchCurrentUser, type UserProfile } from '../api/auth'
import { clearAuth, getStoredUser, getToken, setAuth } from '../stores/auth'

interface ChatMessage {
  id: number
  role: 'assistant' | 'user'
  content: string
}

const router = useRouter()
const user = ref<UserProfile | null>(getStoredUser())
const draft = ref('')
const replying = ref(false)
const messageListRef = ref<HTMLElement | null>(null)
const messages = ref<ChatMessage[]>([
  {
    id: 1,
    role: 'assistant',
    content: '你好，我是你的智能健康顾问。你可以告诉我最近的不适、体检指标，或想重点管理的健康目标。'
  }
])

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
    if (!currentUser.profileCompleted) {
      await router.push('/profile-setup')
    }
  } catch {
    clearAuth()
    await router.push('/login')
  }
})

async function sendMessage() {
  const content = draft.value
  if (!content || replying.value) {
    return
  }

  messages.value.push({
    id: Date.now(),
    role: 'user',
    content
  })
  draft.value = ''
  replying.value = true
  await scrollToBottom()

  window.setTimeout(async () => {
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: `我已经记录你的问题：“${content}”。当前版本先展示对话流程，接入真实 LLM 接口后会结合你的健康档案给出更具体的分析和建议。`
    })
    replying.value = false
    await scrollToBottom()
  }, 500)
}

async function scrollToBottom() {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

async function logout() {
  clearAuth()
  await router.push('/login')
}
</script>
