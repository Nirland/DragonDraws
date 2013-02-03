package org.nirland.websocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * This singleton class saves main objects like a registry pattern.
 * 
 * As well, ServerManager implements simple matchmaking.
 * @see ServerManager#roomCreator 
 * 
 * @author Nirland
 */

public class ServerManager {

	private final static ServerManager instance = new ServerManager();

	private final Set<SocketConnection> members = new CopyOnWriteArraySet<SocketConnection>();

	private final Set<Room> rooms = new CopyOnWriteArraySet<Room>();

	private final BlockingQueue<SocketConnection> queue = new LinkedBlockingQueue<SocketConnection>();

	private final Map<String, String> serverConfig = new HashMap<String, String>();
	
	private final List<String> words = new ArrayList<String>();

	public final Thread roomCreator = new Thread() {
		
		private boolean delayed = false; 
		
		private void create() throws InterruptedException{
			Room room = new Room();
			while (queue.size() > 0) {
				SocketConnection con = queue.take();
				con.setRoom(room);
				room.getMembers().add(con);
			}
			rooms.add(room);
			FrontHandler.getInstance().handleRoomCreation(room);
			delayed = false;
		}
		
		@Override
		public void run() {			
			int delay = Integer.parseInt(serverConfig.get("DELAY_SECONDS"));
			int min_size = Integer.parseInt(serverConfig.get("MIN_ROOM_SIZE"));
			int max_size = Integer.parseInt(serverConfig.get("MAX_ROOM_SIZE"));
			
			while (true) {			
				try {
					if (queue.size() < 2) {
						Thread.sleep(delay * 1000);
						continue;
					} else if (queue.size() == min_size) {
						if (!delayed){
							Thread.sleep(delay * 1000);
							delayed = true;
							continue;
						}
						this.create();						
					} else if (queue.size() > min_size
							&& queue.size() <= max_size) {
						this.create();						
					}
				} catch (InterruptedException e) {
					Logger.getLogger(ServerManager.class.getName()).severe(e.getMessage());	
				}
			}
		}
	};
		
	private ServerManager() {

	}

	public static ServerManager getInstance() {
		return instance;
	}
 
	public Map<String, String> getServerConfig() {
		return serverConfig;
	}
	
	public Set<SocketConnection> getMembers() {
		return members;
	}

	public Set<Room> getRooms() {
		return rooms;
	}

	public List<String> getWords() {
		return words;
	}

	public BlockingQueue<SocketConnection> getQueue() {
		return queue;
	}

	public void addToQueue(SocketConnection member) {
		if (member.getRoom() != null){
			Room room = member.getRoom();
			room.remove(member);			
		}
		try {
			if (!queue.contains(member)){
				queue.put(member);
			}			
		} catch (InterruptedException e) {
			Logger.getLogger(ServerManager.class.getName()).severe(e.getMessage());	
		}
	}
		
}
