package poly.edu.controller;

import poly.edu.dto.UserMessageDTO;
import poly.edu.service.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

	@RestController
	@RequestMapping("/api/chat")
public class ChatBotController {

		@Autowired
	    private ChatService chatService;
		
	    @Value("${coze.api.token}")
	    private String apiToken;
	    
	    @Value("${coze.bot.id}")
	    private String botId;

	    @PostMapping("/send")
	    public ResponseEntity<String> sendMessage(@RequestBody UserMessageDTO message) {

	    	String responseFromCoze = chatService.chatWithGroq(message.getText());
	        return ResponseEntity.ok(responseFromCoze);
	}
}
