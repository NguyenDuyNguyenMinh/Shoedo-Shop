package poly.edu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatService {

    @Value("${coze.api.token}")
    private String apiToken;

    @Value("${coze.bot.id}")
    private String botId;

    public String callCozeApi(String userMessage) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        
        // --- BƯỚC 1: GỬI TIN NHẮN CHO COZE ---
        String chatUrl = "https://api.coze.com/v3/chat";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        Map<String, Object> body = new HashMap<>();
        body.put("bot_id", botId);
        body.put("user_id", "shoedo_user_123");
        body.put("stream", false);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", userMessage);
        message.put("content_type", "text");
        messages.add(message);
        body.put("additional_messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            // Nhận phản hồi "in_progress"
            ResponseEntity<String> response = restTemplate.postForEntity(chatUrl, entity, String.class);
            JsonNode rootNode = mapper.readTree(response.getBody());
            
            // Lấy mã phiên chat để chuẩn bị "đòi" kết quả
            String chatId = rootNode.path("data").path("id").asText();
            String conversationId = rootNode.path("data").path("conversation_id").asText();

            // --- BƯỚC 2: CHỜ BOT SUY NGHĨ (POLLING) ---
            String retrieveUrl = "https://api.coze.com/v3/chat/retrieve?chat_id=" + chatId + "&conversation_id=" + conversationId;
            HttpEntity<String> getEntity = new HttpEntity<>(headers); 
            
            boolean isCompleted = false;
            int maxRetries = 15; // Đợi tối đa 15 giây để bot gõ xong
            int attempts = 0;
            
            while (!isCompleted && attempts < maxRetries) {
                Thread.sleep(1000); // Nghỉ 1 giây rồi hỏi lại xem xong chưa
                attempts++;
                
                ResponseEntity<String> retrieveResponse = restTemplate.exchange(retrieveUrl, HttpMethod.GET, getEntity, String.class);
                JsonNode retrieveNode = mapper.readTree(retrieveResponse.getBody());
                String status = retrieveNode.path("data").path("status").asText();
                
                if ("completed".equals(status)) {
                    isCompleted = true; // Bot đã gõ xong!
                } else if ("failed".equals(status) || "canceled".equals(status)) {
                    return "Xin lỗi, hệ thống Shoedo Assistant đang bận. Bạn thử lại nhé!";
                }
            }

            // --- BƯỚC 3: LẤY CÂU TRẢ LỜI CHÍNH THỨC ---
            if (isCompleted) {
                String msgListUrl = "https://api.coze.com/v3/chat/message/list?chat_id=" + chatId + "&conversation_id=" + conversationId;
                ResponseEntity<String> msgResponse = restTemplate.exchange(msgListUrl, HttpMethod.GET, getEntity, String.class);
                JsonNode msgNode = mapper.readTree(msgResponse.getBody());
                
                JsonNode dataArray = msgNode.path("data");
                for (JsonNode msg : dataArray) {
                    // Tìm đúng đoạn tin nhắn có type là "answer" (câu trả lời của bot)
                    if ("answer".equals(msg.path("type").asText())) {
                        return msg.path("content").asText();
                    }
                }
            }
            return "Bot đã nhận được tin nhắn nhưng chưa kịp trả lời.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi kết nối tới trợ lý ảo.";
        }
    }
}