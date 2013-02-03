package org.nirland.websocket.handler;

import org.nirland.websocket.Message;
import org.nirland.websocket.Proto.Command;
import org.nirland.websocket.ServerManager;
import org.nirland.websocket.exception.ProtoException;

/**
 * Send users online count. 
 * 
 * @author Nirland
 */

public class GETUSERCOUNTHandler implements IHandler {

	@Override
	public void handleIt(Message message) throws ProtoException {	
		Message response = new Message(Command.USERCOUNT, message.getSender());		
		response.getParams().put("count", ServerManager.getInstance().getMembers().size());
		response.send();
	}

}
