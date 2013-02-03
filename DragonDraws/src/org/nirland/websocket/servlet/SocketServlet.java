package org.nirland.websocket.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.nirland.websocket.ServerManager;
import org.nirland.websocket.SocketConnection;
import org.nirland.websocket.User;

/**
 * Jetty WebSocketServlet implementation class SocketServlet
 * 
 * This class is entry point of application.
 * 
 * Init parameters configure different aspects of application work.
 * Room creator parameters:
 * @see ServerManager#roomCreator
 * @param MIN_ROOM_SIZE - minimum players in the room
 * @param MAX_ROOM_SIZE - maximum players in the room
 * @param DELAY_SECONDS - time delay in seconds of checking queue users for room creation,
 * if queue size <= minimum players in the room  
 *  
 * Game parameters:
 * @param ROUND_LENGTH - length of the game round in seconds
 * @see org.nirland.websocket.Game#timeout
 * 
 * @param WORDS_BASE - filename of words base,
 * which uses for select and showing word in game.
 *    
 * @author Nirland
 */
@WebServlet(name = "socketserv", urlPatterns = { "/socket" }, initParams = {
		@WebInitParam(name = "MIN_ROOM_SIZE", value = "2"),
		@WebInitParam(name = "MAX_ROOM_SIZE", value = "5"),
		@WebInitParam(name = "DELAY_SECONDS", value = "5"),
		@WebInitParam(name = "ROUND_LENGTH", value = "60"),
		@WebInitParam(name = "WORDS_BASE", value = "words.txt") })
public class SocketServlet extends WebSocketServlet {

	private static final long serialVersionUID = 1L;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
	
	/**
	 * Accept websocket connection and pass user session. 
	 */
	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request,
			String protocol) {
		return new SocketConnection((User) request.getSession().getAttribute("user"));
	}
	
	/**
	 * Load application settings from init params.
	 * Load words base from text file.
	 * Start new consumer thread for simple matchmaking, 
	 * which distributes players through the rooms.
	 * @see HttpServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();

		ServerManager.getInstance().getServerConfig()
				.put("MIN_ROOM_SIZE", this.getInitParameter("MIN_ROOM_SIZE"));
		ServerManager.getInstance().getServerConfig()
				.put("MAX_ROOM_SIZE", this.getInitParameter("MAX_ROOM_SIZE"));
		ServerManager.getInstance().getServerConfig()
				.put("DELAY_SECONDS", this.getInitParameter("DELAY_SECONDS"));
		ServerManager.getInstance().getServerConfig()
				.put("ROUND_LENGTH", this.getInitParameter("ROUND_LENGTH"));

		File f = new File(this.getServletContext().getRealPath(
				this.getInitParameter("WORDS_BASE")));
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			while (br.ready()) {
				ServerManager.getInstance().getWords().add(br.readLine());
			}
		} catch (FileNotFoundException e) {
			Logger.getLogger(SocketServlet.class.getName()).severe(e.getMessage());
		} catch (IOException e) {
			Logger.getLogger(SocketServlet.class.getName()).severe(e.getMessage());
		} finally {
			try {
				if (br!= null){
					br.close();
				}				
			} catch (IOException e) {
				Logger.getLogger(SocketServlet.class.getName()).severe(e.getMessage());
			}
		}
		
		ServerManager.getInstance().roomCreator.start();
	}

}
