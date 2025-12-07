package backend.databaseManagement;

import java.util.ArrayList;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import backend.entities.*;
import backend.user.UserRepository;


@Service
public class DatabaseManagementService {
    private final UserDatabaseRepository userDatabaseRepository;
    private final UserRepository userRepository;
    private final ServerRepository serverRepository;   

    public DatabaseManagementService(
        UserDatabaseRepository userDatabaseRepository, 
        UserRepository userRepository,
        ServerRepository serverRepository
    ) {
        this.userDatabaseRepository = userDatabaseRepository;
        this.userRepository = userRepository;
        this.serverRepository = serverRepository;

    }

    public CreateServerDTO createServer(CreateServerDTO createServerDTO, int ownerId) throws RuntimeException{
        if (createServerDTO == null) throw new RuntimeException("DTO cannot be null");
        

        User owner = userRepository.findById(ownerId);
        if (owner == null) throw new RuntimeException("Owner not found");
        
        if (createServerDTO.getServerName() == null) throw new RuntimeException("Server name Not Found !");
        Server newServer = new Server();
        newServer.setName(createServerDTO.getServerName());
        newServer.setOwner(owner);

        serverRepository.save(newServer);
        owner.getServers().add(newServer);

        userRepository.save(owner);
        createServerDTO.setServerId(newServer.getId());

        return createServerDTO;

    }

    public CreateDatabaseDTO createDatabase(CreateDatabaseDTO dto, int ownerId) {

        if (dto == null) throw new RuntimeException("DTO cannot be null");

        User owner = userRepository.findById(ownerId);
                
        if (dto.getDatabaseName() == null || dto.getDatabasePassword() == null) {
            throw new RuntimeException("Missing required fields");
        }

        Server server;
        Integer serverId = dto.getServerId();
        if (serverId != null) {
            
            server = serverRepository.findById(dto.getServerId())
                    .orElseThrow(() -> new RuntimeException("Server not found"));
            dto.setServerName(server.getName());
            
            boolean hasAccess = owner.getServers().stream()
                                .anyMatch(s -> s.getId() == server.getId());

            if (!hasAccess) throw new RuntimeException("Unauthorized Access");

        } else {
            if (dto.getServerName() == null)
                throw new RuntimeException("serverName is required when serverId is null");

            CreateServerDTO serverDTO = new CreateServerDTO();
            serverDTO.setServerName(dto.getServerName());
            serverDTO = createServer(serverDTO, ownerId);

            server = serverRepository.findById(serverDTO.getServerId())
                    .orElseThrow(() -> new RuntimeException("Created server not found"));

            dto.setServerId(server.getId());
        }

        UserDatabase db = new UserDatabase();
        db.setName(dto.getDatabaseName());
        db.setPassword(dto.getDatabasePassword());
        db.setOwner(owner);
        db.setDdl(dto.getDdl());
        db.setServer(server);
        owner.getAccessibleDatabases().add(db);

        userDatabaseRepository.save(db);
        dto.setDatabaseId(db.getId());

        return dto;
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
