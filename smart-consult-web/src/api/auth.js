import { http } from './http';
export async function requestSmsCode(phone) {
    const response = await http.post('/auth/sms-code', { phone });
    return response.data.data;
}
export async function loginWithSmsCode(phone, smsCode) {
    const response = await http.post('/auth/login', { phone, smsCode });
    return response.data.data;
}
export async function fetchCurrentUser() {
    const response = await http.get('/auth/me');
    return response.data.data;
}
export async function saveHealthProfile(payload) {
    const response = await http.post('/user/profile', payload);
    return response.data.data;
}
