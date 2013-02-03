package org.nirland.websocket;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.nirland.websocket.Proto.Command;
import org.nirland.websocket.exception.ProtoException;

/**
 * Network message entity.
 * 
 * @author Nirland
 */

public class Message {
	
	private SocketConnection sender;
	private Command command;
	private Map<String, Object> params = new HashMap<String, Object>();

	public Message(Command command, Map<String, Object> params, SocketConnection sender) {	
		this.sender = sender;
		this.command = command;
		this.params = params;
	}

	public Message(Command command, Map<String, Object> params) {
		this.command = command;
		this.params = params;
	}
	
	public Message(Command command, SocketConnection sender) {		
		this.sender = sender;
		this.command = command;
	}

	public Message(Command command) {
		this.command = command;		
	}
	
	public Command getCommand() {
		return command;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public SocketConnection getSender() {
		return sender;
	}

	public void setSender(SocketConnection sender) {
		this.sender = sender;
	}
	
	public void send() {
		if (sender == null){
			return;
		}
		try {
			this.sender.broadcast(Proto.prepare(this), command.getScope());
		} catch (ProtoException e) {			
			Logger.getLogger(Message.class.getName()).warning(e.getMessage());	
		}
	}
	
}
