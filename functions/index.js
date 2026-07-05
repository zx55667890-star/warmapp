const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onValueWritten } = require("firebase-functions/v2/database");
const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.database();

// ── Helper: send FCM notification to a user ──
async function sendNotification(uid, title, body, data) {
    if (!uid) return;
    const tokenSnap = await db.ref(`users/${uid}/fcmToken`).once("value");
    const token = tokenSnap.val();
    if (!token) return;
    const message = {
        token,
        notification: { title, body },
        data: data || {},
        android: { priority: "high" },
    };
    try {
        await admin.messaging().send(message);
    } catch (err) {
        if (err.code === "messaging/registration-token-not-registered") {
            await db.ref(`users/${uid}/fcmToken`).remove();
        }
    }
}

exports.sendNotificationOnNewMessage = onValueWritten(
    { ref: "/chatrooms/{chatroomId}/messages/{messageId}", region: "us-central1" },
    async (event) => {
        const { chatroomId, messageId } = event.params;
        if (!event.data.after.exists()) return;
        const msg = event.data.after.val();
        if (msg.senderId === "system" || msg.sender === "ai") return;
        const chatroomSnap = await db.ref(`chatrooms/${chatroomId}`).once("value");
        const chatroom = chatroomSnap.val() || {};
        const opponentId = chatroom.opponentId || "";
        const senderId = msg.senderId || "";
        if (!opponentId || opponentId === senderId) return;
        const text = msg.text || (msg.voiceUrl ? "[語音訊息]" : msg.videoUrl ? "[影片]" : msg.imageUrls ? "[圖片]" : "新訊息");
        const senderSnap = await db.ref(`users/${senderId}/nickname`).once("value");
        const senderName = senderSnap.val() || "對方";
        await sendNotification(opponentId, senderName, text, {
            chatroomId,
            myRole: chatroom.opponentRole || "user",
            expertId: chatroom.expertId || "",
            expertText: chatroom.expertText || "",
            expertDate: chatroom.expertDate || "",
        });
    }
);

exports.sendNotificationOnExpertAccept = onValueWritten(
    { ref: "/questions/{questionId}/status", region: "us-central1" },
    async (event) => {
        const { questionId } = event.params;
        const newStatus = event.data.after.val();
        if (newStatus !== "expert_accepted" && newStatus !== "taken") return;
        const questionSnap = await db.ref(`questions/${questionId}`).once("value");
        const question = questionSnap.val() || {};
        const authorId = question.authorId || "";
        if (!authorId) return;
        const expertId = question.expertId || "";
        let expertName = "專家";
        if (expertId) {
            const nameSnap = await db.ref(`users/${expertId}/nickname`).once("value");
            expertName = nameSnap.val() || "專家";
        }
        await sendNotification(authorId, "配對成功", `${expertName} 已接受您的問題`, {
            chatroomId: questionId,
            myRole: "user",
            expertId,
            expertText: question.text || "",
            expertDate: "",
        });
    }
);

// ── Reset Password ──
exports.resetPassword = onCall({ invoker: 'public' }, async (request) => {
    const data = request.data;
    const email = data.email;
    const newPassword = data.newPassword;
    const code = data.code;
    if (!email || !newPassword || !code) {
        throw new HttpsError('invalid-argument', '缺少必要參數');
    }
    try {
        const emailKey = "reset_" + email.replace(/\./g, ",").replace(/@/g, "~");
        const snap = await db.ref(`email_verification/${emailKey}/code`).once('value');
        const realCode = snap.val();
        if (!realCode || String(realCode) !== String(code)) {
            throw new HttpsError('permission-denied', '驗證碼錯誤或已過期');
        }
        let user;
        try {
            user = await admin.auth().getUserByEmail(email);
        } catch (notFound) {
            throw new HttpsError('not-found', '此 Email 尚未註冊');
        }
        await admin.auth().updateUser(user.uid, { password: newPassword });
        await db.ref(`email_verification/${emailKey}`).remove().catch(() => {});
        return { success: true };
    } catch (err) {
        if (err instanceof HttpsError) throw err;
        throw new HttpsError('internal', '密碼重設失敗，請稍後再試');
    }
});

// ── Send Verification Email ──
exports.sendVerificationEmail = onCall({ invoker: 'public' }, async (request) => {
    const data = request.data;
    const email = data.email;
    const code = data.code;
    const type = data.type;
    if (!email || !code || !type) {
        throw new HttpsError('invalid-argument', '缺少必要參數');
    }
    const nodemailer = require("nodemailer");
    const transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: { user: 'zx55667890@gmail.com', pass: 'xyog vyzl anrb npth' }
    });
    const subject = type === 'reset' ? '密碼重設驗證碼' : '驗證您的信箱';
    const text = type === 'reset' ? `您的密碼重設驗證碼是：${code}` : `您的驗證碼是：${code}`;
    try {
        await transporter.sendMail({ from: 'zx55667890@gmail.com', to: email, subject, text, headers: { 'Importance': 'high', 'X-Priority': '1' } });
        return { success: true };
    } catch (err) {
        console.error("Email send failed:", err.message, err.code);
        throw new HttpsError('internal', '寄信失敗，請稍後再試');
    }
});
