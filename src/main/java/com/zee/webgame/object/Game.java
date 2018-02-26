package com.zee.webgame.object;

import java.util.List;
import java.util.Map;

public class Game {
	private String gameId;
	
	private int currentRound;
	
	private List<Player> playList ;
	
	private List<Map<String,Object>> roundInfoList ;

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public int getCurrentRound() {
		return currentRound;
	}

	public void setCurrentRound(int currentRound) {
		this.currentRound = currentRound;
	}

	public List<Player> getPlayList() {
		return playList;
	}

	public void setPlayList(List<Player> playList) {
		this.playList = playList;
	}

	public List<Map<String,Object>> getRoundInfoList() {
		return roundInfoList;
	}

	public void setRoundInfoList(List<Map<String,Object>> roundInfoList) {
		this.roundInfoList = roundInfoList;
	}
}
