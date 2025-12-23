package backend.databaseManagement;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.databaseManagement.dto.CreateDatabaseDTO;
import backend.databaseManagement.dto.CreateServerDTO;
import backend.databaseManagement.dto.InitiateDatabaseDTO;
import backend.databaseManagement.dto.PasswordRequest;
import backend.databaseManagement.dto.SendDatabasesDTO;
import backend.databaseManagement.dto.AddDatabaseToUser;
import backend.security.AuthUser;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/database")
@RequiredArgsConstructor
public class DatabaseManagementController {

    private final DatabaseManagementService databaseManagementService;

    @PostMapping("/create-database")
    public ResponseEntity<CreateDatabaseDTO> createDatabase(
            @RequestBody CreateDatabaseDTO createDatabaseDTO,
            @AuthenticationPrincipal AuthUser authUser) {
        CreateDatabaseDTO createdDatabase = databaseManagementService.createDatabase(
                createDatabaseDTO,
                authUser.userId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdDatabase);
    }

    @GetMapping("/check-database-password")
    public ResponseEntity<Boolean> checkDatabasePassword(
            @RequestBody PasswordRequest passwordRequest) {
        return ResponseEntity.ok(
                databaseManagementService.checkDatabasePassword(passwordRequest.getDatabaseId(),
                        passwordRequest.getPassword()));
    }

    @GetMapping("/get-user-databases")
    public ResponseEntity<SendDatabasesDTO> getUserDatabases(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(
                databaseManagementService.getUserDatabases(authUser.userId()));
    }

    @PostMapping("/create-mysql-container")
    public ResponseEntity<InitiateDatabaseDTO> createMysqlContainer(
            @RequestBody int id) {
        return ResponseEntity.ok(
                databaseManagementService.initiateDatabase(id));
    }

    @PostMapping("/create-server")
    public ResponseEntity<CreateServerDTO> createServer(
            @RequestBody CreateServerDTO createServerDTO,
            @AuthenticationPrincipal AuthUser authUser) {
        CreateServerDTO createdServer = databaseManagementService.createServer(
                createServerDTO,
                authUser.userId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdServer);
    }

    @GetMapping("/get-user-servers")
    public ResponseEntity<List<CreateServerDTO>> getUserServers(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(
                databaseManagementService.getUserServers(authUser.userId()));
    }

    @PostMapping("/add-database-to-user")
    public ResponseEntity<AddDatabaseToUser> addDatabaseToUser(
            @RequestBody AddDatabaseToUser addDatabaseToUser,
            @AuthenticationPrincipal AuthUser authUser) {
        databaseManagementService.addDatabaseToUser(
                addDatabaseToUser.getDatabaseId(),
                addDatabaseToUser.getUserId(),
                authUser.userId(),
                addDatabaseToUser.getRole());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/remove-database-from-user")
    public ResponseEntity<Void> removeDatabaseFromUser(
            @RequestBody AddDatabaseToUser addDatabaseToUser,
            @AuthenticationPrincipal AuthUser authUser) {

        databaseManagementService.removeDatabaseFromUser(
                addDatabaseToUser.getDatabaseId(),
                addDatabaseToUser.getUserId(),
                authUser.userId());
        return ResponseEntity.noContent().build();
    }

}
