import { http, type ApiResult } from './http'

export interface SmsCodeResponse {
  phone: string
  mockCode?: string
  expiresInSeconds: number
}

export interface UserProfile {
  id: number
  phone: string
  nickname: string
  avatarUrl?: string | null
  lastLoginTime?: string | null
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresInSeconds: number
  user: UserProfile
  registered: boolean
}

export async function requestSmsCode(phone: string): Promise<SmsCodeResponse> {
  const response = await http.post<ApiResult<SmsCodeResponse>>('/auth/sms-code', { phone })
  return response.data.data!
}

export async function loginWithSmsCode(phone: string, smsCode: string): Promise<LoginResponse> {
  const response = await http.post<ApiResult<LoginResponse>>('/auth/login', { phone, smsCode })
  return response.data.data!
}

export async function fetchCurrentUser(): Promise<UserProfile> {
  const response = await http.get<ApiResult<UserProfile>>('/auth/me')
  return response.data.data!
}
