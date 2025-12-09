package backend.databaseManagement;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import backend.entities.User;
import backend.entities.UserDatabase;
import backend.user.UserRepository;


@Service
public class DatabaseManagementService {
    private final UserDatabaseRepository userDatabaseRepository;
    private final UserRepository userRepository;   

    public DatabaseManagementService(UserDatabaseRepository userDatabaseRepository, UserRepository userRepository) {
        this.userDatabaseRepository = userDatabaseRepository;
        this.userRepository = userRepository;
    }

    public int createDatabase(CreateDatabaseDTO createDatabaseDTO, int ownerId) throws RuntimeException {

        if (createDatabaseDTO == null) {
            throw new RuntimeException("DTO cannot be null");
        }

        User owner = userRepository.findById(ownerId);
        if (owner == null) {
            throw new RuntimeException("Owner not found");
        }

        if (createDatabaseDTO.getDatabaseName() == null || createDatabaseDTO.getDatabasePassword() == null) {
            throw new RuntimeException("Missing required fields");
        }

        UserDatabase newDatabase = new UserDatabase();
        newDatabase.setName(createDatabaseDTO.getDatabaseName());
        newDatabase.setOwner(owner);
        newDatabase.setPassword(createDatabaseDTO.getDatabasePassword());
        newDatabase.setDdl(createDatabaseDTO.getDdl());
        newDatabase.setServer(null);

        userDatabaseRepository.save(newDatabase);
        return newDatabase.getId();
    }

    public InitiateDatabaseDTO initiateDatabase(int id) throws BadRequestException {

        if (id <= 0) {
            throw new BadRequestException("Invalid database ID");
        }

        UserDatabase database = userDatabaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Database not found"));

        InitiateDatabaseDTO dto = new InitiateDatabaseDTO();
        dto.setDatabaseName(database.getName());
        dto.setPassword(database.getPassword());
        dto.setDdl(database.getDdl());
        dto.setContainerId(id);
        dto.setWsUrl("ws://localhost:8080/agent-ws");
        dto.setContainerName("mysql" + id);
        return dto;
    }



    
}
