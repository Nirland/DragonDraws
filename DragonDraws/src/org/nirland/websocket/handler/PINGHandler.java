package org.nirland.websocket.handler;

import org.nirland.websocket.Message;
import org.nirland.websocket.Proto.Command;
import org.nirland.websocket.exception.ProtoException;

/**
 * Send PONG message to client. 
 * 
 * @author Nirland
 */

public class PINGHandler implements IHandler {

	@Override
	public void handleIt(Message message) throws ProtoException {										
		Message response = new Message(Command.PONG, message.getSender());				
		response.send();		
	}

}
