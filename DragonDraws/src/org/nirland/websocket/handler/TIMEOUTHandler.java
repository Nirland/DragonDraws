package org.nirland.websocket.handler;

import org.nirland.websocket.Game;
import org.nirland.websocket.Message;
import org.nirland.websocket.exception.ProtoException;

/**
 * Handle game timeout event. 
 * 
 * @author Nirland
 */

public class TIMEOUTHandler implements IHandler {			
	
	@Override
	public void handleIt(Message message) throws ProtoException {						
		Game game = message.getSender().getRoom().getGame();		
		game.timeout();				
	}

}
