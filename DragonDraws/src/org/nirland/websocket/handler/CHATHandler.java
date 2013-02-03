package org.nirland.websocket.handler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.nirland.websocket.Game;
import org.nirland.websocket.Message;
import org.nirland.websocket.ServerManager;
import org.nirland.websocket.Proto.Command;
import org.nirland.websocket.SocketConnection;
import org.nirland.websocket.Utils;
import org.nirland.websocket.exception.ProtoException;

/**
 * Chat handler class. 
 * 
 * @author Nirland
 */

public class CHATHandler implements IHandler {
	 
	@Override
	public void handleIt(Message message) throws ProtoException {
		if (message.getParams().get("message") == null || 
			message.getParams().get("timer") == null) {
			throw new ProtoException("Empty input params");
		}
		
		String post = (String) message.getParams().get("message");
		post = Utils.submessage(post);
		
		int timer;
		try{
			 timer = Integer.parseInt(message.getParams().get("timer").toString());
			 if (timer < 0 || timer > Integer.parseInt(ServerManager.getInstance()
					 								.getServerConfig().get("ROUND_LENGTH"))){
				 throw new ProtoException("Incorrect timer in chat message");
			 }
		} catch(NumberFormatException ex){
			throw new ProtoException("Incorrect timer in chat message");
		}				
		
		post = Utils.escapeHTML(post);
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		String time = format.format(new Date());
		
		SocketConnection target = message.getSender();
		Message response = new Message(Command.CHATMESSAGE, target);				
		response.getParams().put("time", time);
		response.getParams().put("uname", target.getUser().getName());
		response.getParams().put("message", post);		
		response.send();	
		
		Game game = target.getRoom().getGame();
		if (game.process(target, post, timer)){
			game.loop();
		}
	}	
	
}
