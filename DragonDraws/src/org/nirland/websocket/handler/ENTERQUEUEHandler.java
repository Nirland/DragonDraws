package org.nirland.websocket.handler;

import org.nirland.websocket.Message;
import org.nirland.websocket.ServerManager;
import org.nirland.websocket.Proto.Command;
import org.nirland.websocket.exception.ProtoException;

/**
 * New game event handler.
 * Remove user from current room and add to matchmaking queue again. 
 * 
 * @author Nirland
 */

public class ENTERQUEUEHandler implements IHandler {

	@Override
	public void handleIt(Message message) throws ProtoException {				
		Message response = new Message(Command.DISCONNECT, message.getSender());
		response.getParams().put("uid", message.getSender().getUser().getId());
		response.getParams().put("uname", message.getSender().getUser().getName());
		response.send();
		
		ServerManager.getInstance().addToQueue(message.getSender());		
		
		response = new Message(Command.FINDROOM, message.getSender());
		response.getParams().put("uid", message.getSender().getUser().getId());
		response.send();
	}

}
