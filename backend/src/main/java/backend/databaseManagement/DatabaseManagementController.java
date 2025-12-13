package backend.databaseManagement;

import java.util.List;
import java.util.Map;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.security.AuthUser;

@RestController
@RequestMapping("/database")
public class DatabaseManagementController {

    @Autowired
    private DatabaseManagementService databaseManagementService;

    @PostMapping("/create-database")
    public ResponseEntity<?> createDatabase(@RequestBody CreateDatabaseDTO createDatabaseDTO,
            @AuthenticationPrincipal AuthUser authUser) {

        try {
            // Attempt to create the database
            CreateDatabaseDTO createdDatabase = databaseManagementService.createDatabase(createDatabaseDTO,
                    authUser.userId());
            return ResponseEntity.ok(createdDatabase);

        } catch (RuntimeException e) {
            // Return 400 Bad Request with the error message
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create-server")
    public ResponseEntity<?> createServer(@RequestBody CreateServerDTO createServerDTO,
            @AuthenticationPrincipal AuthUser authUser) {

        try {

            CreateServerDTO createdServer = databaseManagementService.createServer(createServerDTO, authUser.userId());
            return ResponseEntity.ok(createdServer);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error", e.getMessage()));
        }
    }

    @PostMapping("/create-mysql-container")
    public ResponseEntity<?> createMysqlContainer(@RequestBody int id) {
        try {
            InitiateDatabaseDTO initiateDatabaseDTO = databaseManagementService.initiateDatabase(id);
            return ResponseEntity.ok(initiateDatabaseDTO);
        } catch (RuntimeException | BadRequestException e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/get-user-servers")
    public ResponseEntity<?> getUserServers(@AuthenticationPrincipal AuthUser authUser) {
        List<CreateServerDTO> ans = databaseManagementService.getUserServers(authUser.userId());
        return ResponseEntity.ok(ans);
    }

    @GetMapping("/get-user-databases")
    public ResponseEntity<?> getUserDAtabases(@AuthenticationPrincipal AuthUser authUser) {

        SendDatabasesDTO sendDatabases = databaseManagementService.getUserDatabases(authUser.userId());
        return ResponseEntity.ok(sendDatabases);

    }
}
