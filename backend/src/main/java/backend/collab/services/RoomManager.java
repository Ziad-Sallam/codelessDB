package backend.collab.services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import backend.collab.Room;

@Service
public class RoomManager {
	@Autowired
	private UpdateWriter updateWriter;

	@Autowired
	private SnapshotService snapshotService;

	private Map<UUID, Room> activeRooms = new ConcurrentHashMap<>();

	
}
