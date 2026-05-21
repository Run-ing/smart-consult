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
  profileCompleted: boolean
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

export interface HealthProfileRequest {
  sex: 'MALE' | 'FEMALE'
  birthDate: string
  heightCm: number
  weightKg: number
  waistCm?: number | null
}

export interface HealthProfile {
  id: number
  userId: number
  sex: 'MALE' | 'FEMALE'
  birthDate: string
  age: number
  heightCm: number
  weightKg: number
  waistCm?: number | null
}

export async function saveHealthProfile(payload: HealthProfileRequest): Promise<HealthProfile> {
  const response = await http.post<ApiResult<HealthProfile>>('/user/profile', payload)
  return response.data.data!
}
