package backend.databaseManagement;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import backend.agent.WebSocketHandler.OnlineUserTracker;
import backend.databaseManagement.dto.CreateDatabaseDTO;
import backend.databaseManagement.dto.CreateServerDTO;
import backend.databaseManagement.dto.InitiateDatabaseDTO;
import backend.databaseManagement.dto.SendDatabasesDTO;
import backend.databaseManagement.exception.DatabaseException.DatabaseAlreadyExistsException;
import backend.databaseManagement.exception.DatabaseException.DatabaseNotFoundException;
import backend.databaseManagement.exception.DatabaseException.MissingFieldException;
import backend.databaseManagement.exception.DatabaseException.ServerAlreadyExistsException;
import backend.databaseManagement.exception.DatabaseException.ServerNotFoundException;
import backend.databaseManagement.exception.DatabaseException.UnauthorizedAccessException;
import backend.entities.Server;
import backend.entities.User;
import backend.entities.UserDatabase;
import backend.entities.joins.UserDatabaseAccess;
import backend.user.Role;
import backend.user.UserRepository;
import backend.user.exceptions.UserException.UserNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatabaseManagementService {
    private final UserDatabaseRepository userDatabaseRepository;
    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final OnlineUserTracker tracker;

    public CreateServerDTO createServer(CreateServerDTO createServerDTO, int ownerId) {
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
                throw new UnauthorizedAccessException("Unauthorized Access");

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

        boolean exists = owner.getDatabaseAccess().stream()
                .map(UserDatabaseAccess::getDatabase)
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

        UserDatabaseAccess ownerAccess = UserDatabaseAccess.builder()
                .user(owner)
                .database(db)
                .role(Role.OWNER)
                .build();
        owner.getDatabaseAccess().add(ownerAccess);

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
        dto.setDdl(database.getDdl());
        dto.setContainerId(id);
        dto.setWsUrl("ws://localhost:8080/agent-ws");
        dto.setContainerName("mysql" + id);
        return dto;
    }

    public SendDatabasesDTO getUserDatabases(int userId) {
        User usr = userRepository.findById(userId);
        SendDatabasesDTO ans = new SendDatabasesDTO();

        for (UserDatabaseAccess access : usr.getDatabaseAccess()) {
            UserDatabase db = access.getDatabase();
            Database temp = new Database();
            temp.setDatabaseId(db.getId());
            temp.setServerName(db.getServer().getName());
            temp.setDatabaseName(db.getName());
            temp.setDatabaseddl(db.getDdl());
            temp.setConnected(tracker.isOnline(Integer.toString(db.getId())));
            temp.setRole(access.getRole().toString());
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

    public boolean checkDatabasePassword(int databaseId, String password) {
        UserDatabase database = userDatabaseRepository.findById(databaseId)
                .orElseThrow(() -> new DatabaseNotFoundException("Database not found"));

        return database.getPassword().equals(password);
    }

    public void deleteDatabase(int userId, int databaseId) {
        User user = userRepository.findById(userId);
        if (user == null)
            throw new UserNotFoundException("User not found");

        UserDatabase database = userDatabaseRepository.findById(databaseId)
                .orElseThrow(() -> new DatabaseNotFoundException("Database not found"));
        User owner = database.getOwner();
        if (owner.getId() != userId)
            throw new UnauthorizedAccessException("Unauthorized Access");

        userDatabaseRepository.delete(database);
    }

    public void addDatabaseToUser(int databaseId, int userId, int ownerId, String roleStr) {
        if (databaseId <= 0 || userId <= 0 || ownerId <= 0) {
            throw new IllegalArgumentException("Invalid database ID");
        }
        if (databaseId == ownerId)
            throw new IllegalArgumentException("Database ID and Owner ID cannot be the same");
        UserDatabase database = userDatabaseRepository.findById(databaseId)
                .orElseThrow(() -> new DatabaseNotFoundException("Database not found"));
        User owner = userRepository.findById(ownerId);

        if (owner == null)
            throw new UserNotFoundException("Owner not found");
        if (owner.getId() != ownerId)
            throw new UnauthorizedAccessException("Unauthorized Access");

        User user = userRepository.findById(userId);
        if (user == null)
            throw new UserNotFoundException("User not found");

        Role role = Role.valueOf(roleStr.toUpperCase());

        UserDatabaseAccess access = UserDatabaseAccess.builder()
                .user(user)
                .database(database)
                .role(role)
                .build();
        user.getDatabaseAccess().add(access);
        userRepository.save(user);
    }

    public void removeDatabaseFromUser(int databaseId, int userId) {
        userDatabaseRepository.findById(databaseId)
                .orElseThrow(() -> new DatabaseNotFoundException("Database not found"));
        User user = userRepository.findById(userId);
        if (user == null)
            throw new UserNotFoundException("User not found");

        user.getDatabaseAccess().removeIf(access -> access.getDatabase().getId() == databaseId);
        userRepository.save(user);
    }

}
