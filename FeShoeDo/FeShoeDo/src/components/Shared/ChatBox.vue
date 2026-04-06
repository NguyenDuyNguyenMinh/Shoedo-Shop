<script setup>
import { ref, nextTick } from 'vue'
import { apiClient } from '@/services/api.js'

// Hàm chuyển đổi Markdown sang HTML (Link và Xuống dòng)
const formatBotMessage = (text) => {
  if (!text) return ''
  
  // 1. Biến cú pháp [Tên hiển thị](Link) thành thẻ <a> màu xanh, in đậm, mở tab mới
  let htmlText = text.replace(
    /\[([^\]]+)\]\((https?:\/\/[^\s\)]+)\)/g, 
    '<a href="$2" target="_blank" style="color: #007bff; text-decoration: underline; font-weight: 600;">$1</a>'
  )
  
  // 2. Chuyển cả các ký tự xuống dòng (\n) thành thẻ <br> để đoạn chat nhìn thoáng hơn
  htmlText = htmlText.replace(/\n/g, '<br>')
  
  return htmlText
}

const isOpen = ref(false)
const userInput = ref('')
const messages = ref([
  { role: 'bot', text: 'Chào bạn! Mình là trợ lý ảo của Shoedo-Shop. Mình có thể giúp gì cho bạn hôm nay?' }
])
const isLoading = ref(false)
const chatBodyRef = ref(null)

// Hàm cuộn xuống cuối khung chat
const scrollToBottom = async () => {
  await nextTick()
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!userInput.value.trim()) return

  // Thêm tin nhắn của user vào giao diện
  const textToSend = userInput.value
  messages.value.push({ role: 'user', text: textToSend })
  userInput.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    // Gọi API sang Spring Boot
    const response = await apiClient.post('/chat/send', {
      text: textToSend
    })
    
    // Tạm thời hiển thị raw data trả về để xem cấu trúc Coze gửi lại
    const botReply = typeof response.data === 'string' ? response.data : JSON.stringify(response.data)
    
    messages.value.push({ role: 'bot', text: botReply })
  } catch (error) {
    console.error('Lỗi khi chat với bot:', error)
    messages.value.push({ role: 'bot', text: 'Xin lỗi, hệ thống chat đang gặp sự cố. Bạn thử lại sau nhé!' })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

</script>

<template>
  <div class="chat-wrapper">
    <Transition name="fade-slide" mode="out-in"> 
      
      <div v-if="isOpen" key="window" class="chat-window">
        <div class="chat-header">
          <div class="chat-title">
            <i class="bi bi-robot"></i> Shoedo Assistant
          </div>
          <button class="close-btn" @click="isOpen = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
          </button>
        </div>

        <div class="chat-body" ref="chatBodyRef">
          <div v-for="(msg, index) in messages" :key="index" :class="['message-row', msg.role]">
            <div class="message-bubble" v-html="formatBotMessage(msg.text)"></div>
          </div>
          <div v-if="isLoading" class="message-row bot">
            <div class="message-bubble typing">Đang gõ...</div>
          </div>
        </div>

        <div class="chat-footer">
          <input 
            v-model="userInput" 
            @keyup.enter="sendMessage" 
            type="text" 
            placeholder="Nhập câu hỏi của bạn..." 
          />
          <button class="send-btn" @click="sendMessage" :disabled="isLoading">Gửi</button>
        </div>
      </div>

      <button v-else key="button" class="chat-toggle-btn" @click="isOpen = true">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path></svg>
      </button>

    </Transition>
  </div>
</template>

<style scoped>
.chat-wrapper {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Nút mở chat */
.chat-toggle-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: black; /* [UPDATE] Đổi sang màu black */
  color: white;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4); /* [UPDATE] Đổi bóng đổ sang màu đen */
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}
.chat-toggle-btn:hover {
  transform: scale(1.1);
}

/* Cửa sổ chat */
.chat-window {
  width: 350px;
  height: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 5px 25px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #eee;
}

.chat-header {
  background: black; /* [UPDATE] Đổi sang màu black */
  color: white;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chat-title {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.close-btn {
  background: transparent;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0;
  display: flex;
}

.chat-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f9f9f9;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* [ADD] Custom Scrollbar for chat-body - Đồng bộ màu black */
.chat-body::-webkit-scrollbar {
  width: 6px; /* Độ rộng thanh cuộn */
}
.chat-body::-webkit-scrollbar-track {
  background: white; /* Nền thanh cuộn màu trắng */
}
.chat-body::-webkit-scrollbar-thumb {
  background: black; /* Cục cuộn màu đen */
  border-radius: 3px;
}
.chat-body::-webkit-scrollbar-thumb:hover {
  background: #333; /* Chuyển màu xám đen khi di chuột vào */
}
/* For Firefox */
.chat-body {
  scrollbar-width: thin;
  scrollbar-color: black white; /* Màu cục cuộn (đen) - Màu nền (trắng) */
}

.message-row {
  display: flex;
  width: 100%;
}
.message-row.user {
  justify-content: flex-end;
}
.message-row.bot {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.4;
  word-wrap: break-word;
}
.message-row.user .message-bubble {
  background: black; /* [UPDATE] Tin nhắn user cũng màu black cho đồng bộ */
  color: white;
  border-bottom-right-radius: 4px;
}
.message-row.bot .message-bubble {
  background: #e0e0e0;
  color: #333;
  border-bottom-left-radius: 4px;
}
.typing {
  font-style: italic;
  color: #666;
}

.chat-footer {
  padding: 12px;
  background: white; /* [UPDATE] Footer cũng chuyển sang nền black */
  border-top: 1px solid #eee; /* [UPDATE] Viền tối hơn */
  display: flex;
  gap: 8px;
}
.chat-footer input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #444; /* [UPDATE] Viền input tối hơn */
  border-radius: 20px;
  outline: none;
  font-size: 14px;
  background: #eee; /* [UPDATE] Nền input tối */
  color: black; /* [UPDATE] Text input trắng */
}
.chat-footer input:focus {
  border-color: #ddd; /* [UPDATE] Viền input sáng lên khi focus */
}
.send-btn {
  background: black; /* [UPDATE] Nút gửi chuyển sang màu sáng trên nền tối */
  color: white; /* [UPDATE] Chữ nút gửi màu đen */
  border: none;
  padding: 0 16px;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 600;
}
.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* [ADD] Transition classes for fade-slide effect */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease; /* Thời gian chuyển cảnh: 0.3 giây */
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0; /* Mờ dần */
  transform: translateY(20px); /* Dịch chuyển lên/xuống 20px */
}

/* responsive */
/* Màn hình Tablet nhỏ & Điện thoại (Dưới 480px) */
@media (max-width: 480px) {
  .chat-wrapper {
    bottom: 15px; /* Thu gọn khoảng cách viền để tiết kiệm không gian */
    right: 15px;
    z-index: 99999; /* Đảm bảo luôn nằm trên cùng ở màn hình nhỏ */
  }

  /* Khi mở chat trên điện thoại, nó sẽ phủ gần hết màn hình */
  .chat-window {
    width: calc(100vw - 30px); /* Rộng bằng toàn bộ màn hình trừ đi lề 15px mỗi bên */
    height: calc(100vh - 100px); /* Cao bằng màn hình trừ đi khoảng trống phía trên/dưới */
    max-height: 600px; /* Vẫn giữ một giới hạn chiều cao tối đa */
    bottom: 80px; /* Nâng lên một chút để không đè vào nút mở chat */
    position: fixed; 
    right: 15px;
  }

  /* Thu nhỏ nút bấm một chút cho đỡ thô */
  .chat-toggle-btn {
    width: 50px;
    height: 50px;
  }
  
  .chat-toggle-btn svg {
    width: 24px;
    height: 24px;
  }
  
  /* Giảm size chữ để hiển thị được nhiều nội dung hơn trên điện thoại */
  .message-bubble {
    font-size: 13px;
    padding: 8px 12px;
  }
  
  .chat-footer input {
    font-size: 13px;
    padding: 8px 10px;
  }
  
  .send-btn {
    padding: 0 12px;
    font-size: 13px;
  }
}

/* Điện thoại màn hình rất nhỏ (Ví dụ: iPhone SE - Dưới 360px) */
@media (max-width: 360px) {
  .chat-window {
    width: calc(100vw - 20px);
    right: 10px;
  }
  .chat-wrapper {
    right: 10px;
    bottom: 10px;
  }
}

</style>