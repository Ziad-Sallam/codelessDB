package backend.databaseManagement;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.security.AuthUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/database")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DatabaseManagementController {
    @Autowired
    private DatabaseManagementService databaseManagementService;

    @PostMapping("/create")
    public ResponseEntity<?> createDatabase(@RequestBody CreateDatabaseDTO createDatabaseDTO, @AuthenticationPrincipal AuthUser authUser) {

        int databaseId = databaseManagementService.createDatabase(createDatabaseDTO, authUser.userId());
        return ResponseEntity.ok(databaseId);
    }

    @PostMapping("/create-mysql-container")
    public ResponseEntity<?> createMysqlContainer(@RequestBody int id) {

        InitiateDatabaseDTO initiateDatabaseDTO = databaseManagementService.initiateDatabase(id);
        return ResponseEntity.ok(initiateDatabaseDTO);
    }
}
