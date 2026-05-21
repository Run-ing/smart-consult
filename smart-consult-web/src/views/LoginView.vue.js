import { computed, onBeforeUnmount, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getApiErrorMessage } from '../api/http';
import { loginWithSmsCode, requestSmsCode } from '../api/auth';
import { setAuth } from '../stores/auth';
const router = useRouter();
const phone = ref('');
const smsCode = ref('');
const mockCode = ref('');
const errorMessage = ref('');
const sendLoading = ref(false);
const loginLoading = ref(false);
const countdown = ref(0);
let countdownTimer;
const phoneValid = computed(() => /^1[3-9]\d{9}$/.test(phone.value));
const sendDisabled = computed(() => sendLoading.value || countdown.value > 0 || !phoneValid.value);
const sendButtonText = computed(() => {
    if (sendLoading.value)
        return '发送中...';
    if (countdown.value > 0)
        return `${countdown.value}s`;
    return '获取验证码';
});
async function sendCode() {
    if (!phoneValid.value) {
        errorMessage.value = '请输入正确的手机号';
        return;
    }
    errorMessage.value = '';
    sendLoading.value = true;
    try {
        const response = await requestSmsCode(phone.value);
        mockCode.value = response.mockCode || '';
        countdown.value = Math.min(response.expiresInSeconds, 60);
        countdownTimer = window.setInterval(() => {
            countdown.value -= 1;
            if (countdown.value <= 0 && countdownTimer) {
                window.clearInterval(countdownTimer);
                countdownTimer = undefined;
            }
        }, 1000);
    }
    catch (error) {
        errorMessage.value = getApiErrorMessage(error);
    }
    finally {
        sendLoading.value = false;
    }
}
async function submitLogin() {
    if (!phoneValid.value) {
        errorMessage.value = '请输入正确的手机号';
        return;
    }
    if (!/^\d{6}$/.test(smsCode.value)) {
        errorMessage.value = '请输入 6 位验证码';
        return;
    }
    errorMessage.value = '';
    loginLoading.value = true;
    try {
        const response = await loginWithSmsCode(phone.value, smsCode.value);
        setAuth(response.token, response.user);
        await router.push(response.user.profileCompleted ? '/home' : '/profile-setup');
    }
    catch (error) {
        errorMessage.value = getApiErrorMessage(error);
    }
    finally {
        loginLoading.value = false;
    }
}
onBeforeUnmount(() => {
    if (countdownTimer) {
        window.clearInterval(countdownTimer);
    }
});
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
__VLS_asFunctionalElement(__VLS_intrinsicElements.main, __VLS_intrinsicElements.main)({
    ...{ class: "login-shell" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "brand-panel" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "brand-mark" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "eyebrow" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "brand-copy" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "signal-grid" },
    'aria-label': "平台能力",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "form-panel" },
    'aria-label': "手机号登录",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "form-card" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "form-kicker" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "form-note" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.form, __VLS_intrinsicElements.form)({
    ...{ onSubmit: (__VLS_ctx.submitLogin) },
    ...{ class: "login-form" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "tel",
    inputmode: "numeric",
    autocomplete: "tel",
    maxlength: "11",
    placeholder: "请输入手机号",
});
(__VLS_ctx.phone);
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "code-row" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    value: (__VLS_ctx.smsCode),
    type: "text",
    inputmode: "numeric",
    maxlength: "6",
    autocomplete: "one-time-code",
    placeholder: "6 位验证码",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (__VLS_ctx.sendCode) },
    type: "button",
    ...{ class: "secondary-button" },
    disabled: (__VLS_ctx.sendDisabled),
});
(__VLS_ctx.sendButtonText);
if (__VLS_ctx.mockCode) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "mock-code" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
    (__VLS_ctx.mockCode);
}
if (__VLS_ctx.errorMessage) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "error-message" },
    });
    (__VLS_ctx.errorMessage);
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ class: "primary-button" },
    type: "submit",
    disabled: (__VLS_ctx.loginLoading),
});
(__VLS_ctx.loginLoading ? '登录中...' : '登录');
/** @type {__VLS_StyleScopedClasses['login-shell']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-mark']} */ ;
/** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-copy']} */ ;
/** @type {__VLS_StyleScopedClasses['signal-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['form-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['form-card']} */ ;
/** @type {__VLS_StyleScopedClasses['form-kicker']} */ ;
/** @type {__VLS_StyleScopedClasses['form-note']} */ ;
/** @type {__VLS_StyleScopedClasses['login-form']} */ ;
/** @type {__VLS_StyleScopedClasses['code-row']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary-button']} */ ;
/** @type {__VLS_StyleScopedClasses['mock-code']} */ ;
/** @type {__VLS_StyleScopedClasses['error-message']} */ ;
/** @type {__VLS_StyleScopedClasses['primary-button']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            phone: phone,
            smsCode: smsCode,
            mockCode: mockCode,
            errorMessage: errorMessage,
            loginLoading: loginLoading,
            sendDisabled: sendDisabled,
            sendButtonText: sendButtonText,
            sendCode: sendCode,
            submitLogin: submitLogin,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
