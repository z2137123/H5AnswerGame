package com.zee.webgame.object;

import org.springframework.web.socket.WebSocketSession;

public class Player {

	public final static String PLAYER_STATUS_FREE = "0";
	
	public final static String PLAYER_STATUS_READY = "1";
	
	public final static String PLAYER_STATUS_PALYING = "2";
	
	
	private WebSocketSession session;
	
	private String name;
	
	private String status;
	
	private Game game;
	
	private String playerId;
	
	private Object currentMove;
	
	public Player(WebSocketSession se){
		this.session = se;
		this.status = "0";
		this.name = "玩家" + se.getId();
		this.playerId = se.getId();
	}

	public WebSocketSession getSession() {
		return session;
	}

	public void setSession(WebSocketSession session) {
		this.session = session;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Object getCurrentMove() {
		return currentMove;
	}

	public void setCurrentMove(Object currentMove) {
		this.currentMove = currentMove;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}



}
