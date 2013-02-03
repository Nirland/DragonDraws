package org.nirland.websocket;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.nirland.websocket.SocketConnection.Scope;
import org.nirland.websocket.exception.ProtoException;
import org.nirland.websocket.handler.*;

/**
 * Implementation of simple protocol based on JSON.
 * 
 * @author Nirland
 */

public class Proto {

	public enum Command {

		// Incoming commands
		PING(Scope.NONE, new PINGHandler()), 
		CANVAS(Scope.ROOM),
		GETUSERS(Scope.NONE, new GETUSERSHandler()),
		GETUSERCOUNT(Scope.NONE, new GETUSERCOUNTHandler()),
		CHAT(Scope.NONE, Arrays.asList("message", "timer"), new CHATHandler()),
		TIMEOUT(Scope.NONE, new TIMEOUTHandler()),
		ENTERQUEUE(Scope.NONE, new ENTERQUEUEHandler()),
		
		// Outgoing commands
		PONG(Scope.SELF), 
		CONNECT(Scope.SERVER),
		FINDROOM(Scope.SELF, Arrays.asList("uid")), 
		CREATEROOM(Scope.SELF, Arrays.asList("round")),
		USERLIST(Scope.SELF, Arrays.asList("users")),
		USERCOUNT(Scope.SELF, Arrays.asList("count")),
		CHATMESSAGE(Scope.SELF_ROOM, Arrays.asList("time", "uname", "message")),
		STARTROUND(Scope.SELF, Arrays.asList("word")),
		JOINROUND(Scope.ROOM, Arrays.asList("drawer")),
		ENDROUND(Scope.SELF_ROOM, Arrays.asList("results")),
		SKIPROUND(Scope.SELF_ROOM),
		ENDGAME(Scope.SELF_ROOM, Arrays.asList("results")),
		CRASHGAME(Scope.ROOM),
		DISCONNECT(Scope.ROOM, Arrays.asList("uid", "uname"));
		
		// Content definition
		private Scope scope;
		private List<String> params;
		private IHandler handler;

		private Command(Scope scope, List<String> params, IHandler handler) {
			this.scope = scope;
			this.params = params;
			this.handler = handler;
		}

		private Command(Scope scope, List<String> params) {
			this.scope = scope;
			this.params = params;
		}

		private Command(Scope scope, IHandler handler) {
			this.scope = scope;
			this.handler = handler;
		}

		private Command(Scope scope) {
			this.scope = scope;
		}

		public Scope getScope() {
			return scope;
		}

		public List<String> getParams() {
			return params;
		}

		public IHandler getHandler() {
			return handler;
		}
	}

	@SuppressWarnings("unchecked")
	public static String prepare(Message message) throws ProtoException {
		Command command = message.getCommand();
		Map<String, Object> params = message.getParams();
		List<String> definedParams = command.params;

		JSONObject obj = null;
		if (definedParams != null) {
			if (definedParams.size() != params.size()) {
				throw new ProtoException("Prepare error with params: "
						+ params.toString());
			}
			obj = new JSONObject();
			for (String key : definedParams) {
				Object value = params.containsKey(key) ? params.get(key) : null;
				obj.put(key, value);
			}
		}

		JSONObject result = new JSONObject();
		result.put("command", command.toString());
		result.put("params", obj);
		return result.toString();
	}

	public static Message parse(String rawMessage) throws ProtoException {
		JSONParser parser = new JSONParser();
		JSONObject jsonObj;		
		try {						
			Object obj = parser.parse(rawMessage);
			jsonObj = (JSONObject) obj;
		} catch (ParseException e) {
			throw new ProtoException("Parse error: " + rawMessage);
		} catch (Exception e) {
			throw new ProtoException("Incorrect message: " + rawMessage);
		}

		String rawCommand;
		Object rawParams;
		if (jsonObj.containsKey("command") && jsonObj.containsKey("params")) {
			rawCommand = (String) jsonObj.get("command");
			rawParams = jsonObj.get("params");
		} else {
			throw new ProtoException("Incorrect message format: "
					+ jsonObj.toString());
		}

		Proto.Command command;
		try {
			command = Proto.Command.valueOf(rawCommand.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ProtoException("Incorrect command: " + rawCommand);
		}

		if (command.params != null) {
			JSONObject jsonParams;
			if (rawParams != null) {
				try {
					 jsonParams = (JSONObject) rawParams;
				} catch (ClassCastException e) {
					throw new ProtoException("Incorrect params: "
							+ rawParams.toString());
				}
			} else{
				throw new ProtoException("Empty params");
			}
			
			if (command.params.size() != jsonParams.size()) {
				throw new ProtoException("Incorrect params length: "
						+ jsonParams.toString());
			}

			Map<String, Object> messageParams = new HashMap<String, Object>();
			for (String param : command.params) {
				Object value = (jsonParams.containsKey(param)) ? jsonParams.get(param) : null;
				messageParams.put(param, value);
			}

			return new Message(command, messageParams);
		} else {
			return new Message(command);
		}
	}

}
