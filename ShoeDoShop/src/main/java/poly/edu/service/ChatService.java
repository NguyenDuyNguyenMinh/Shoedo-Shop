package poly.edu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import poly.edu.controller.BotDataController; 

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Autowired
    private BotDataController botDataController;

    public String chatWithGroq(String userMessage) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        String groqUrl = "https://api.groq.com/openai/v1/chat/completions";

        try {
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("model", "llama-3.1-8b-instant");

            ArrayNode messages = mapper.createArrayNode();
            

            ObjectNode systemMsg = mapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", "Bạn là trợ lý ảo của Shoedo-Shop. Khi có kết quả từ công cụ tìm kiếm, BẮT BUỘC trình bày MỖI sản phẩm thành 3 dòng. TUYỆT ĐỐI KHÔNG in link trần dạng 'Xem chi tiết: http...'. BẮT BUỘC PHẢI nhúng link vào chữ bằng chuẩn Markdown đúng như mẫu sau:\n* **Tên sản phẩm**\nGiá: Giá bán\n[Xem chi tiết](Link chi tiết)");

            ObjectNode userMsg = mapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            requestBody.set("messages", messages);

            ArrayNode tools = mapper.createArrayNode();
            ObjectNode tool = mapper.createObjectNode();
            tool.put("type", "function");
            
            ObjectNode function = mapper.createObjectNode();
            function.put("name", "search_shoes");
            function.put("description", "Công cụ tìm kiếm sản phẩm giày. BẮT BUỘC phải truyền tham số có tên khóa là 'keyword'.");
            
            ObjectNode parameters = mapper.createObjectNode();
            parameters.put("type", "object");
            ObjectNode properties = mapper.createObjectNode();

            ObjectNode keywordProp = mapper.createObjectNode();
            keywordProp.put("type", "string");

            keywordProp.put("description", "Từ khóa tìm kiếm giày. Có thể là tên giày (sneaker, boot, sandal), màu sắc, hoặc CÁC TÍNH NĂNG trong mô tả (ví dụ: chống nước, da thật, thể thao, vintage, êm chân). BẮT BUỘC điền chuỗi rỗng \"\" nếu khách không nhắc thông tin cụ thể.");
            properties.set("keyword", keywordProp);

            parameters.set("properties", properties);
            
            ArrayNode required = mapper.createArrayNode();
            required.add("keyword");
            parameters.set("required", required);
            function.set("parameters", parameters);
            tool.set("function", function);
            tools.add(tool);

            requestBody.set("tools", tools);
            requestBody.put("tool_choice", "auto");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);
            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(groqUrl, entity, String.class);
            JsonNode responseNode = mapper.readTree(response.getBody());
            JsonNode responseMessage = responseNode.path("choices").get(0).path("message");

            if (responseMessage.has("tool_calls")) {
                JsonNode toolCall = responseMessage.path("tool_calls").get(0);
                String functionName = toolCall.path("function").path("name").asText();
                String argumentsStr = toolCall.path("function").path("arguments").asText();
                JsonNode arguments = mapper.readTree(argumentsStr);
                

                String keyword = arguments.has("keyword") ? arguments.path("keyword").asText() : "";

                System.out.println(">>> AI yêu cầu tìm giày với từ khóa: '" + keyword + "'");

                Map<String, Object> dbResultMap = botDataController.searchProductForBot(keyword).getBody();
                

                Object dataObj = dbResultMap.get("data");
                List<Map<String, Object>> productList = new ArrayList<>();
                if (dataObj instanceof List<?>) {
                    for (Object item : (List<?>) dataObj) {
                        if (item instanceof Map<?, ?>) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> mapItem = (Map<String, Object>) item;
                            productList.add(mapItem);
                        }
                    }
                }
                
                List<String> formattedList = new ArrayList<>();
                if (!productList.isEmpty()) {
                    for (Map<String, Object> item : productList) {
                        String markdownItem = "- [" + item.get("ten_san_pham") + "](" + item.get("link_chi_tiet") + ") - " + item.get("gia_ban");
                        formattedList.add(markdownItem);
                    }
                }

                dbResultMap.put("data", productList); 

                dbResultMap.put("message", "Tìm thấy " + productList.size() + " sản phẩm. YÊU CẦU TỐI THƯỢNG: Trình bày TẤT CẢ sản phẩm theo đúng mẫu 3 dòng sau, KHÔNG ĐƯỢC để lộ đường link http ra ngoài văn bản:\n* **[Tên sản phẩm]**\nGiá: [gia_ban]\n[Xem chi tiết]([link_chi_tiet])\n\nVÍ DỤ CHUẨN BẠN BẮT BUỘC PHẢI BẮT CHƯỚC:\n* **Giày Da Nam Shoedo**\nGiá: 1,500,000 VNĐ\n[Xem chi tiết](http://localhost:5173/customer/detail-product/22)");
                String dbResultJson = mapper.writeValueAsString(dbResultMap);

                messages.add(responseMessage); 

                ObjectNode toolMessage = mapper.createObjectNode();
                toolMessage.put("role", "tool");
                toolMessage.put("tool_call_id", toolCall.path("id").asText());
                toolMessage.put("name", functionName);
                toolMessage.put("content", dbResultJson);
                messages.add(toolMessage);

                ObjectNode finalRequestBody = mapper.createObjectNode();
                finalRequestBody.put("model", "llama-3.1-8b-instant");
                finalRequestBody.set("messages", messages);

                HttpEntity<String> secondEntity = new HttpEntity<>(mapper.writeValueAsString(finalRequestBody), headers);
                ResponseEntity<String> finalResponse = restTemplate.postForEntity(groqUrl, secondEntity, String.class);
                JsonNode finalNode = mapper.readTree(finalResponse.getBody());

                return finalNode.path("choices").get(0).path("message").path("content").asText();
            } else {
                return responseMessage.path("content").asText();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Hệ thống đang bận. Bạn vui lòng thử lại sau!";
        }
    }
}