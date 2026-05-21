import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchCurrentUser, saveHealthProfile } from '../api/auth';
import { getApiErrorMessage } from '../api/http';
import { clearAuth, getToken, setAuth } from '../stores/auth';
const router = useRouter();
const today = new Date().toISOString().slice(0, 10);
const submitLoading = ref(false);
const errorMessage = ref('');
const form = reactive({
    sex: 'MALE',
    birthDate: '',
    heightCm: 0,
    weightKg: 0,
    waistCm: null
});
const computedAge = computed(() => {
    if (!form.birthDate)
        return 0;
    const birthDate = new Date(form.birthDate);
    const now = new Date();
    let age = now.getFullYear() - birthDate.getFullYear();
    const birthdayPassed = now.getMonth() > birthDate.getMonth() ||
        (now.getMonth() === birthDate.getMonth() && now.getDate() >= birthDate.getDate());
    if (!birthdayPassed)
        age -= 1;
    return age > 0 ? age : 0;
});
const bmiPreview = computed(() => {
    if (!form.heightCm || !form.weightKg)
        return '-';
    const meters = form.heightCm / 100;
    return (form.weightKg / (meters * meters)).toFixed(1);
});
async function submitProfile() {
    if (!form.birthDate) {
        errorMessage.value = '请选择出生日期';
        return;
    }
    if (!form.heightCm || form.heightCm < 50 || form.heightCm > 250) {
        errorMessage.value = '请输入合理的身高';
        return;
    }
    if (!form.weightKg || form.weightKg < 10 || form.weightKg > 300) {
        errorMessage.value = '请输入合理的体重';
        return;
    }
    if (form.waistCm && (form.waistCm < 30 || form.waistCm > 250)) {
        errorMessage.value = '请输入合理的腰围，或留空';
        return;
    }
    errorMessage.value = '';
    submitLoading.value = true;
    try {
        await saveHealthProfile({
            ...form,
            waistCm: form.waistCm || null
        });
        const currentUser = await fetchCurrentUser();
        setAuth(getToken(), currentUser);
        await router.push('/home');
    }
    catch (error) {
        errorMessage.value = getApiErrorMessage(error);
    }
    finally {
        submitLoading.value = false;
    }
}
async function logout() {
    clearAuth();
    await router.push('/login');
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
__VLS_asFunctionalElement(__VLS_intrinsicElements.main, __VLS_intrinsicElements.main)({
    ...{ class: "profile-setup-shell" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "profile-setup-header" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "eyebrow" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (__VLS_ctx.logout) },
    ...{ class: "secondary-button" },
    type: "button",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.form, __VLS_intrinsicElements.form)({
    ...{ onSubmit: (__VLS_ctx.submitProfile) },
    ...{ class: "profile-setup-form" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.fieldset, __VLS_intrinsicElements.fieldset)({
    ...{ class: "segmented-field" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.legend, __VLS_intrinsicElements.legend)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({
    ...{ class: ({ active: __VLS_ctx.form.sex === 'MALE' }) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "radio",
    value: "MALE",
});
(__VLS_ctx.form.sex);
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({
    ...{ class: ({ active: __VLS_ctx.form.sex === 'FEMALE' }) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "radio",
    value: "FEMALE",
});
(__VLS_ctx.form.sex);
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "profile-form-grid" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "date",
    max: (__VLS_ctx.today),
});
(__VLS_ctx.form.birthDate);
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "number",
    min: "50",
    max: "250",
    step: "0.1",
    placeholder: "如 175",
});
(__VLS_ctx.form.heightCm);
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "number",
    min: "10",
    max: "300",
    step: "0.1",
    placeholder: "如 70",
});
(__VLS_ctx.form.weightKg);
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "number",
    min: "30",
    max: "250",
    step: "0.1",
    placeholder: "可选，不清楚可留空",
});
(__VLS_ctx.form.waistCm);
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "profile-summary" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
(__VLS_ctx.computedAge || '-');
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
(__VLS_ctx.bmiPreview);
if (__VLS_ctx.errorMessage) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
        ...{ class: "error-message" },
    });
    (__VLS_ctx.errorMessage);
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ class: "primary-button" },
    type: "submit",
    disabled: (__VLS_ctx.submitLoading),
});
(__VLS_ctx.submitLoading ? '保存中...' : '保存并进入平台');
/** @type {__VLS_StyleScopedClasses['profile-setup-shell']} */ ;
/** @type {__VLS_StyleScopedClasses['profile-setup-header']} */ ;
/** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary-button']} */ ;
/** @type {__VLS_StyleScopedClasses['profile-setup-form']} */ ;
/** @type {__VLS_StyleScopedClasses['segmented-field']} */ ;
/** @type {__VLS_StyleScopedClasses['profile-form-grid']} */ ;
/** @type {__VLS_StyleScopedClasses['profile-summary']} */ ;
/** @type {__VLS_StyleScopedClasses['error-message']} */ ;
/** @type {__VLS_StyleScopedClasses['primary-button']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            today: today,
            submitLoading: submitLoading,
            errorMessage: errorMessage,
            form: form,
            computedAge: computedAge,
            bmiPreview: bmiPreview,
            submitProfile: submitProfile,
            logout: logout,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
