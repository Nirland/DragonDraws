package org.nirland.websocket; 

import java.util.logging.Logger;

import org.nirland.websocket.Proto.Command;
import org.nirland.websocket.exception.ProtoException;
import org.nirland.websocket.handler.IHandler;

/**
 * Simple singleton class which implements front controller pattern.
 * @see FrontHandler#handleMessage 
 * As well handles main events of application.
 * 
 * @author Nirland
 */

public class FrontHandler {

	private final static FrontHandler instance = new FrontHandler();	
	
	private FrontHandler() {

	}

	public static FrontHandler getInstance() {
		return instance;
	}
			
	public void handleOpen(SocketConnection socket){
		Message message = new Message(Command.CONNECT, socket);			
		message.send();			
		
		message = new Message(Command.FINDROOM, socket);			
		message.getParams().put("uid", socket.getUser().getId());
		message.send();
	}
	
	public void handleMessage(SocketConnection socket, String rawMessage) {
		try {			
			Logger.getLogger(FrontHandler.class.getName()).fine(rawMessage);
			
			Message message = Proto.parse(rawMessage);
			message.setSender(socket);			
												
			Command command = message.getCommand();
			IHandler handler = command.getHandler();
			if (handler != null) {
				handler.handleIt(message);
			}
			
			String data = (command.getParams()!= null)? 
						 	Proto.prepare(message) : 
						 	rawMessage;			
			socket.broadcast(data, command.getScope());
		} catch (ProtoException e) {
			Logger.getLogger(FrontHandler.class.getName()).warning(e.getMessage());			
		}
	}
	
	public void handleClose(SocketConnection socket){
		if (socket.getRoom() == null){
			return;
		}						
		Message message = new Message(Command.DISCONNECT, socket);
		message.getParams().put("uid", socket.getUser().getId());
		message.getParams().put("uname", socket.getUser().getName());
		message.send();
	}
	
	public void handleRoomCreation(Room room){
		Message message;
		for(SocketConnection socket : room.getMembers()){
			message = new Message(Command.CREATEROOM, socket);
			message.getParams().put("round", ServerManager.getInstance()
											.getServerConfig().get("ROUND_LENGTH"));
			message.send();				
		}
		room.gameInit();
	}	
}
