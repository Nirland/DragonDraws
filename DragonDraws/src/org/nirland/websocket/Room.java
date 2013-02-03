package org.nirland.websocket;

import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Room entity.
 * 
 * Created room initializes a new game.
 * @see Room#gameInit 
 * 
 * @author Nirland
 */

public class Room {
	
	private final Set<SocketConnection> members = new CopyOnWriteArraySet<SocketConnection>();
	
	private Game game;
	
	public void gameInit() {
		game = new Game(new LinkedList<SocketConnection>(members));
		game.loop();
	}
	
	public Game getGame(){
		return game;
	}
	
	public Set<SocketConnection> getMembers() {
		return members;
	}
	
	public void remove(SocketConnection socket) {		
		game.leave(socket);
		
		members.remove(socket);
		
		socket.setRoom(null);
		
		if (members.size() < 1){
			ServerManager.getInstance().getRooms().remove(this);
		} 
	}
}
