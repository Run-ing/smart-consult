import { http, type ApiResult } from './http'

export interface AgentChatResponse {
  message: string
}

export async function sendAgentMessage(message: string): Promise<AgentChatResponse> {
  const response = await http.post<ApiResult<AgentChatResponse>>('/agent/chat', { message }, { timeout: 60000 })
  return response.data.data!
}

export async function startAgentConversation(): Promise<AgentChatResponse> {
  const response = await http.post<ApiResult<AgentChatResponse>>('/agent/chat', {
    message: '用户已进入慢病风险评估对话页面，请读取当前登录用户基础资料并获取第一道需要询问的题目。'
  }, { timeout: 60000 })
  return response.data.data!
}
