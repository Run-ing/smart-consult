import { http } from './http';
export async function sendAgentMessage(message) {
    const response = await http.post('/agent/chat', { message }, { timeout: 60000 });
    return response.data.data;
}
export async function startAgentConversation() {
    const response = await http.post('/agent/chat', {
        message: '用户已进入慢病风险评估对话页面，请读取当前登录用户基础资料并获取第一道需要询问的题目。'
    }, { timeout: 60000 });
    return response.data.data;
}
