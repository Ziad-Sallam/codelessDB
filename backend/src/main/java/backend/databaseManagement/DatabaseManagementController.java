package backend.databaseManagement;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        @AuthenticationPrincipal AuthUser authUser) throws BadRequestException {

        CreateDatabaseDTO databaseId = databaseManagementService.createDatabase(createDatabaseDTO, authUser.userId());
        return ResponseEntity.ok(databaseId);
    }

    @PostMapping("/create-server")
    public ResponseEntity<?> createServer(@RequestBody CreateServerDTO createServerDTO,
        @AuthenticationPrincipal AuthUser authUser) throws BadRequestException {

        CreateServerDTO databaseId = databaseManagementService.createServer(createServerDTO, authUser.userId());
        return ResponseEntity.ok(databaseId);
    }

    @PostMapping("/create-mysql-container")
    public ResponseEntity<?> createMysqlContainer(@RequestBody int id) {
        try {
            InitiateDatabaseDTO initiateDatabaseDTO = databaseManagementService.initiateDatabase(id);
            return ResponseEntity.ok(initiateDatabaseDTO);
        } catch (RuntimeException | BadRequestException e) {
            return ResponseEntity.internalServerError().build(); // <-- ALWAYS RETURNS 500
        }

    }
}
