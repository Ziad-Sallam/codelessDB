package backend.agent.HTTPHandler;

import java.nio.file.Paths;
import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import backend.agent.WebSocketHandler.*;
import backend.security.AuthUser;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Path;


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

    @GetMapping("/create-container")
    public ResponseEntity<Resource> downloadFile1() throws IOException {
        Path path = Paths.get("backend/uploads/agent/dist/create_container.exe").toAbsolutePath();
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/communicate")
    public ResponseEntity<Resource> downloadFile2() throws IOException {
        Path path = Paths.get("backend/uploads/agent/dist/communicate.exe").toAbsolutePath();
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/local-ip")
    public String getLocalIp() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            if (ni.isLoopback() || !ni.isUp()) continue;

            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
                    return "Local Network IP: " + addr.getHostAddress();
                }
            }
        }
        return "Local IP not found";
    }

}
