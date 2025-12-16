package backend.databaseManagement;

import java.util.ArrayList;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import backend.agent.WebSocketHandler.OnlineUserTracker;
import backend.entities.Server;
import backend.entities.User;
import backend.entities.UserDatabase;
import backend.user.UserRepository;
import backend.user.exceptions.UserException.UserNotFoundException;
import backend.databaseManagement.exception.DatabaseException.DatabaseAlreadyExistsException;
import backend.databaseManagement.exception.DatabaseException.ServerAlreadyExistsException;
import backend.databaseManagement.exception.DatabaseException.ServerNotFoundException;
import backend.databaseManagement.exception.DatabaseException.DatabaseNotFoundException;
import backend.databaseManagement.exception.DatabaseException.MissingFieldException;
import org.springframework.security.access.AccessDeniedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatabaseManagementService {
    private final UserDatabaseRepository userDatabaseRepository;
    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final OnlineUserTracker tracker;

    public CreateServerDTO createServer(CreateServerDTO createServerDTO, int ownerId){
        if (createServerDTO == null)
            throw new MissingFieldException("DTO cannot be null");

        User owner = userRepository.findById(ownerId);
        if (owner == null)
            throw new UserNotFoundException("Owner not found");

        String serverName = createServerDTO.getServerName();
        if (serverName == null)
            throw new ServerNotFoundException("Server name not found!");

        boolean exists = owner.getServers().stream()
                .anyMatch(s -> s.getName().equals(serverName));
        if (exists)
            throw new ServerAlreadyExistsException("Server name already exists for this user!");

        Server newServer = new Server();
        newServer.setName(serverName);
        newServer.setOwner(owner);

        serverRepository.save(newServer);
        owner.getServers().add(newServer);
        userRepository.save(owner);

        createServerDTO.setServerId(newServer.getId());

        return createServerDTO;
    }

    public CreateDatabaseDTO createDatabase(CreateDatabaseDTO dto, int ownerId) {

        if (dto == null)
            throw new MissingFieldException("DTO cannot be null");

        User owner = userRepository.findById(ownerId);

        if (dto.getDatabaseName() == null || dto.getDatabasePassword() == null) {
            throw new MissingFieldException("Missing required fields");
        }

        Server server;
        Integer serverId = dto.getServerId();
        if (serverId != null) {

            server = serverRepository.findById(dto.getServerId())
                    .orElseThrow(() -> new ServerNotFoundException("Server not found"));
            dto.setServerName(server.getName());

            boolean hasAccess = owner.getServers().stream()
                    .anyMatch(s -> s.getId() == server.getId());

            if (!hasAccess)
                throw new AccessDeniedException("Unauthorized Access");

        } else {
            if (dto.getServerName() == null)
                throw new MissingFieldException("serverName is required when serverId is null");

            CreateServerDTO serverDTO = new CreateServerDTO();
            serverDTO.setServerName(dto.getServerName());
            serverDTO = createServer(serverDTO, ownerId);

            server = serverRepository.findById(serverDTO.getServerId())
                    .orElseThrow(() -> new ServerNotFoundException("Created server not found"));

            dto.setServerId(server.getId());
        }

        boolean exists = owner.getAccessibleDatabases().stream()
                .anyMatch(db -> db.getServer().getId() == server.getId()
                        && db.getName().equals(dto.getDatabaseName()));
        if (exists) {
            throw new DatabaseAlreadyExistsException("Database name already exists on this server for this user");
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

    public InitiateDatabaseDTO initiateDatabase(int id) {

        if (id <= 0) {
            throw new DatabaseNotFoundException("Invalid database ID");
        }

        UserDatabase database = userDatabaseRepository.findById(id)
                .orElseThrow(() -> new DatabaseNotFoundException("Database not found"));

        InitiateDatabaseDTO dto = new InitiateDatabaseDTO();
        dto.setDatabaseName(database.getName());
        dto.setPassword(database.getPassword());
        dto.setDdl(database.getDdl());
        dto.setContainerId(id);
        dto.setWsUrl("ws://localhost:8080/agent-ws");
        dto.setContainerName("mysql" + id);
        return dto;
    }

    public SendDatabasesDTO getUserDatabases(int userId) {
        User usr = userRepository.findById(userId);
        List<UserDatabase> dbs = usr.getAccessibleDatabases().stream().toList();
        SendDatabasesDTO ans = new SendDatabasesDTO();
        for (UserDatabase db : dbs) {
            Database temp = new Database();
            temp.setDatabaseId(db.getId());
            temp.setServerName(db.getServer().getName());
            temp.setDatabaseName(db.getName());
            temp.setDatabaseddl(db.getDdl());
            temp.setConnected(tracker.isOnline(Integer.toString(db.getId())));
            ans.getDatabases().add(temp);
        }
        return ans;

    }

    public List<CreateServerDTO> getUserServers(int userId) {
        User usr = userRepository.findById(userId);
        List<Server> servers = usr.getServers().stream().toList();
        List<CreateServerDTO> ans = new ArrayList<>();
        for (Server s : servers) {
            CreateServerDTO temp = new CreateServerDTO();
            temp.setServerId(s.getId());
            temp.setServerName(s.getName());
            ans.add(temp);
        }

        return ans;
    }

}
