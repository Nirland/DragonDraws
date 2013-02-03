package org.nirland.websocket.handler;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.nirland.websocket.Message;
import org.nirland.websocket.SocketConnection;
import org.nirland.websocket.User;
import org.nirland.websocket.Proto.Command;
import org.nirland.websocket.exception.ProtoException;

/**
 * Send user list in current room. 
 * 
 * @author Nirland
 */

public class GETUSERSHandler implements IHandler {

	@SuppressWarnings("unchecked")
	@Override	
	public void handleIt(Message message) throws ProtoException {
		SocketConnection target = message.getSender();
		JSONArray jsonArr = new JSONArray();
		for (SocketConnection socket : target.getRoom().getMembers()) {
			User user = socket.getUser();
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("uid", user.getId());
			jsonObj.put("uname", user.getName());
			jsonArr.add(jsonObj);
		}

		Message response = new Message(Command.USERLIST, target);		
		response.getParams().put("users", jsonArr);
		response.send();
	}

}
