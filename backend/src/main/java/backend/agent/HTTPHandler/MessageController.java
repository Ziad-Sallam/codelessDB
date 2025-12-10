package backend.agent.HTTPHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import backend.agent.WebSocketHandler.*;
import backend.security.AuthUser;



@RestController
@RequestMapping("/agent")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<?> sendToUser(@RequestBody MessageDTO request,@AuthenticationPrincipal AuthUser user) {
        AgentMessageDTO message = new AgentMessageDTO("Server", request.getContent());
        try{
            ClientResponseDTO res =  messageService.runQuery(request.getDatabaseId(), message,user.userId());
            return ResponseEntity.ok(res);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        
    }

    @GetMapping("/is-database-online")
    public ResponseEntity<?> isDatabaseOnline( @RequestParam int databaseId, @AuthenticationPrincipal AuthUser user) {
        try {
            boolean x = messageService.databaseIsOnline(user.userId(), databaseId);
            return ResponseEntity.ok(x);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
