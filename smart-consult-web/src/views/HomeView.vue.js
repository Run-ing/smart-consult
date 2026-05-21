import { computed, nextTick, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { fetchCurrentUser } from '../api/auth';
import { clearAuth, getStoredUser, getToken, setAuth } from '../stores/auth';
const router = useRouter();
const user = ref(getStoredUser());
const draft = ref('');
const replying = ref(false);
const messageListRef = ref(null);
const messages = ref([
    {
        id: 1,
        role: 'assistant',
        content: '你好，我是你的智能健康顾问。你可以告诉我最近的不适、体检指标，或想重点管理的健康目标。'
    }
]);
const formattedLoginTime = computed(() => {
    if (!user.value?.lastLoginTime) {
        return '-';
    }
    return new Date(user.value.lastLoginTime).toLocaleString('zh-CN', { hour12: false });
});
onMounted(async () => {
    if (!getToken()) {
        await router.push('/login');
        return;
    }
    try {
        const currentUser = await fetchCurrentUser();
        user.value = currentUser;
        setAuth(getToken(), currentUser);
        if (!currentUser.profileCompleted) {
            await router.push('/profile-setup');
        }
    }
    catch {
        clearAuth();
        await router.push('/login');
    }
});
async function sendMessage() {
    const content = draft.value;
    if (!content || replying.value) {
        return;
    }
    messages.value.push({
        id: Date.now(),
        role: 'user',
        content
    });
    draft.value = '';
    replying.value = true;
    await scrollToBottom();
    window.setTimeout(async () => {
        messages.value.push({
            id: Date.now() + 1,
            role: 'assistant',
            content: `我已经记录你的问题：“${content}”。当前版本先展示对话流程，接入真实 LLM 接口后会结合你的健康档案给出更具体的分析和建议。`
        });
        replying.value = false;
        await scrollToBottom();
    }, 500);
}
async function scrollToBottom() {
    await nextTick();
    if (messageListRef.value) {
        messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
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
    ...{ class: "chat-shell" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.aside, __VLS_intrinsicElements.aside)({
    ...{ class: "chat-sidebar" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "eyebrow" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "advisor-note" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "member-panel" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
(__VLS_ctx.user?.phone || '-');
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
(__VLS_ctx.user?.nickname || '-');
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
(__VLS_ctx.formattedLoginTime);
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (__VLS_ctx.logout) },
    ...{ class: "secondary-button" },
    type: "button",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "chat-workspace" },
    'aria-label': "健康顾问对话",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.header, __VLS_intrinsicElements.header)({
    ...{ class: "chat-header" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({
    ...{ class: "eyebrow" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "advisor-status" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ref: "messageListRef",
    ...{ class: "message-list" },
});
/** @type {typeof __VLS_ctx.messageListRef} */ ;
for (const [message] of __VLS_getVForSourceType((__VLS_ctx.messages))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.article, __VLS_intrinsicElements.article)({
        key: (message.id),
        ...{ class: "message-row" },
        ...{ class: (message.role) },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "message-bubble" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "message-author" },
    });
    (message.role === 'assistant' ? '健康顾问' : '我');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
    (message.content);
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.form, __VLS_intrinsicElements.form)({
    ...{ onSubmit: (__VLS_ctx.sendMessage) },
    ...{ class: "chat-composer" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    value: (__VLS_ctx.draft),
    type: "text",
    autocomplete: "off",
    placeholder: "描述你的症状、体检指标或健康问题",
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ class: "primary-button" },
    type: "submit",
    disabled: (!__VLS_ctx.draft || __VLS_ctx.replying),
});
(__VLS_ctx.replying ? '思考中...' : '发送');
/** @type {__VLS_StyleScopedClasses['chat-shell']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-sidebar']} */ ;
/** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
/** @type {__VLS_StyleScopedClasses['advisor-note']} */ ;
/** @type {__VLS_StyleScopedClasses['member-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary-button']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-workspace']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-header']} */ ;
/** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
/** @type {__VLS_StyleScopedClasses['advisor-status']} */ ;
/** @type {__VLS_StyleScopedClasses['message-list']} */ ;
/** @type {__VLS_StyleScopedClasses['message-row']} */ ;
/** @type {__VLS_StyleScopedClasses['message-bubble']} */ ;
/** @type {__VLS_StyleScopedClasses['message-author']} */ ;
/** @type {__VLS_StyleScopedClasses['chat-composer']} */ ;
/** @type {__VLS_StyleScopedClasses['primary-button']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            user: user,
            draft: draft,
            replying: replying,
            messageListRef: messageListRef,
            messages: messages,
            formattedLoginTime: formattedLoginTime,
            sendMessage: sendMessage,
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
