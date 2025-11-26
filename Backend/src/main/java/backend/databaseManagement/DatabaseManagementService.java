package backend.databaseManagement;

import org.springframework.stereotype.Service;
import backend.entities.*;
import backend.user.UserRepository;


@Service
public class DatabaseManagementService {
    private final UserDatabaseRepository userDatabaseRepository;
    private final UserRepository userRepository;   

    public DatabaseManagementService(UserDatabaseRepository userDatabaseRepository, UserRepository userRepository) {
        this.userDatabaseRepository = userDatabaseRepository;
        this.userRepository = userRepository;
    }

    public int createDatabase(CreateDatabaseDTO createDatabaseDTO, int ownerId) {
        UserDatabase newDatabase = new UserDatabase();
        User owner = userRepository.findById(ownerId);
        newDatabase.setName(createDatabaseDTO.getDatabaseName());
        newDatabase.setOwner(owner);
        newDatabase.setPassword(createDatabaseDTO.getDatabasePassword());  
        newDatabase.setServer(null); // Set server later as needed     
        newDatabase.setDdl(createDatabaseDTO.getDdl());

        userDatabaseRepository.save(newDatabase);
        return newDatabase.getId();
    }

    public InitiateDatabaseDTO initiateDatabase(int id) {

        UserDatabase database = userDatabaseRepository.findById(id).orElseThrow(() -> new RuntimeException("Database not found"));
        System.out.println("Initiating database: " + database.getName());
        InitiateDatabaseDTO dto = new InitiateDatabaseDTO();
        dto.setDatabaseName(database.getName());
        dto.setPassword(database.getPassword());
        dto.setDdl(database.getDdl());
        dto.setContainerId(id);
        dto.setWsUrl("ws://localhost:8080/agent-ws"); // Example WebSocket URL
        dto.setContainerName("mysql" + id);
        return dto;
    }
    
}
