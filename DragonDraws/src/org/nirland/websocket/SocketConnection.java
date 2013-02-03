package org.nirland.websocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.jetty.websocket.WebSocket;
import org.nirland.websocket.exception.ProtoException;

/**
 * Web socket implementation class.
 * 
 * @author Nirland
 */

public class SocketConnection implements WebSocket.OnTextMessage {

	private Connection conn;

	private User user;

	private Room room;

	private boolean unique = true;

	public SocketConnection(User user) {
		this.user = user;
	}

	@Override
	public void onOpen(Connection conn) {		
		this.conn = conn;
		if (this.user != null 
			&& !ServerManager.getInstance().getMembers().contains(this)) {			
			ServerManager.getInstance().getMembers().add(this);
			ServerManager.getInstance().addToQueue(this);
			FrontHandler.getInstance().handleOpen(this);
		} else {
			unique = false;
			this.conn.close();
		}
	}

	@Override
	public void onClose(int closeCode, String message) {
		if (!unique) {
			return;
		}
		
		FrontHandler.getInstance().handleClose(this);		
		
		if (room != null) {
			room.remove(this);			
		}
		
		conn = null;
		ServerManager.getInstance().getQueue().remove(this);
		ServerManager.getInstance().getMembers().remove(this);
	}

	@Override
	public void onMessage(String data) {
		FrontHandler.getInstance().handleMessage(this, data);
	}

	public User getUser() {
		return user;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	//This enum defines scope of broadcasting message.
	public enum Scope {
		SERVER, // broadcast message to all SERVER members except himself.
		ROOM, // broadcast message to all ROOM members except himself.
		SELF_SERVER, // broadcast message to all SERVER members AND himself.
		SELF_ROOM, // broadcast message to all ROOM members AND himself.
		SELF, // broadcast message to himself.
		NONE; // Don't broadcast message! Conversation with server only. 
	}

	//Broadcasting implementation, using Scope enum. 
	public void broadcast(String data, Scope scope) throws ProtoException{
		if (scope == Scope.NONE) {
			return;
		}
		
		if (data.length() > this.conn.getMaxTextMessageSize()){
			throw new ProtoException("Message too long: " + data);
		}
		
		Set<SocketConnection> members;

		if (scope == Scope.SERVER || scope == Scope.SELF_SERVER) {
			members = ServerManager.getInstance().getMembers();			
		} else if (scope == Scope.ROOM || scope == Scope.SELF_ROOM) {
			if (room == null){
				return;
			}
			members = room.getMembers();			
		} else if (scope == Scope.SELF) {
			members = new HashSet<SocketConnection>();
			members.add(this);
		} else {
			return;
		}
		
		for (SocketConnection member : members) {
			try {
				if ((scope == Scope.SERVER || scope == Scope.ROOM) && (member == this)){
					continue;
				}				
				if (member.conn.isOpen()){
					member.conn.sendMessage(data);
				}				
			} catch (IOException e) {
				Logger.getLogger(SocketConnection.class.getName()).warning(e.getMessage());	
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SocketConnection))
			return false;
		SocketConnection other = (SocketConnection) obj;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

}
