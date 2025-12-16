package backend.databaseManagement;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CreateDatabaseDTO createdDatabase =
                databaseManagementService.createDatabase(
                        createDatabaseDTO,
                        authUser.userId()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(createdDatabase);
    }

    @GetMapping("/get-user-databases")
    public ResponseEntity<SendDatabasesDTO> getUserDatabases(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(
                databaseManagementService.getUserDatabases(authUser.userId())
        );
    }

    @PostMapping("/create-mysql-container")
    public ResponseEntity<InitiateDatabaseDTO> createMysqlContainer(
            @RequestBody int id
    ) {
        return ResponseEntity.ok(
                databaseManagementService.initiateDatabase(id)
        );
    }


    @PostMapping("/create-server")
    public ResponseEntity<CreateServerDTO> createServer(
            @RequestBody CreateServerDTO createServerDTO,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CreateServerDTO createdServer =
                databaseManagementService.createServer(
                        createServerDTO,
                        authUser.userId()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(createdServer);
    }

    @GetMapping("/get-user-servers")
    public ResponseEntity<List<CreateServerDTO>> getUserServers(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(
                databaseManagementService.getUserServers(authUser.userId())
        );
    }
}
