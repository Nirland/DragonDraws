package org.nirland.websocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.nirland.websocket.Proto.Command;

/**
 * This class implements game logic, handles game states and saves results.
 * 
 * @author Nirland
 */

public class Game {

	private List<SocketConnection> gamers;
	private Map<SocketConnection, Integer> results;

	private SocketConnection drawer;	
	private String word;
	
	private boolean ended;
	private boolean answered;
	private boolean leaved;
	
	private long lastTimeout = 0;

	private final static int ROUND_LENGTH = Integer.parseInt(ServerManager.getInstance()
											.getServerConfig().get("ROUND_LENGTH"));	
	
	private final static int RIGHT_ANSWER_POINTS = ROUND_LENGTH * 2;
	private final static int RIGHT_DRAWER_POINTS = ROUND_LENGTH;
	
	public Game(List<SocketConnection> gamers) {
		this.gamers = gamers;
		this.results = new HashMap<SocketConnection, Integer>();
		for (SocketConnection gamer : this.gamers) {
			results.put(gamer, 0);
		}
	}

	public synchronized void loop() {
		if (gamers.size() > 0 && !ended) {
			answered = false;
			leaved = false;
			this.startRound();
		} else {
			this.endGame();
		}
	}

	public synchronized boolean process(SocketConnection socket,
			String receivedWord, int time) {
		if (ended || !this.isRightWord(receivedWord) || answered) {
			return false;
		}
		
		float timeMod = ((float)time)/ROUND_LENGTH;
		
		int answerPoints = results.get(socket) + Math.round(RIGHT_ANSWER_POINTS * timeMod);
		results.put(socket, answerPoints);
		
		if (!leaved){
			int drawerPoints = results.get(drawer) +  Math.round(RIGHT_DRAWER_POINTS * timeMod);
			results.put(drawer, drawerPoints);
		}		
		
		answered = true;
		this.endRound();

		return true;
	}
	
	public synchronized void timeout(){		
		long timediff = (System.currentTimeMillis() - lastTimeout) / 1000 ;				
		if (timediff >= ROUND_LENGTH && !ended){
			this.skipRound();
			this.loop();
			lastTimeout = System.currentTimeMillis();
		}				
	}
		
	public synchronized void leave(SocketConnection leaver){		
		Set<SocketConnection> members = leaver.getRoom().getMembers();
		if (members.size() == 2 && !ended){
			this.crashGame(leaver);
			return;
		}
		
		if (gamers.contains(leaver)){
			gamers.remove(leaver);
		}
										
		if (leaver.equals(drawer)){
			for(SocketConnection member : members){
				if (!member.equals(leaver)){
					drawer = member;
					leaved = true;
					break;
				}
			}
		}
	}

	private void startRound() {
		drawer = Utils.randomPoll(gamers);
		this.loadWord();
		Message message = new Message(Command.STARTROUND, drawer);
		message.getParams().put("word", word);
		message.send();

		message = new Message(Command.JOINROUND, drawer);
		message.getParams().put("drawer", drawer.getUser().getId());
		message.send();
	}

	private void endRound() {
		Message message = new Message(Command.ENDROUND, drawer);
		message.getParams().put("results", Utils.packResults(results));
		message.send();
	}

	private void skipRound(){		
		new Message(Command.SKIPROUND, drawer).send();
	}
	
	private void endGame() {
		ended = true;
		results = Utils.sortByValue(results, true);				
		Message message = new Message(Command.ENDGAME, drawer);
		message.getParams().put("results", Utils.packResults(results));		
		message.send();
	}	
	
	private void crashGame(SocketConnection leaver){
		ended = true;
		new Message(Command.CRASHGAME, leaver).send();
	}
	
	private void loadWord() {
		List<String> words = ServerManager.getInstance().getWords();
		if (words.size() > 0){
			word = words.get(new Random().nextInt(words.size()));
		} else{
			word = "error";
		}
		
	}

	private boolean isRightWord(String receivedWord) {
		return word.equals(receivedWord.toLowerCase().trim());
	}

}
