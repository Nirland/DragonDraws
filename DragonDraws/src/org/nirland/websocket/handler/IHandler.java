package org.nirland.websocket.handler;

import org.nirland.websocket.Message;
import org.nirland.websocket.exception.ProtoException;

/**
 * Simple interface for handlers of incoming commands. 
 * 
 * @author Nirland
 */

public interface IHandler {
	 
	void handleIt(Message message) throws ProtoException;
}
