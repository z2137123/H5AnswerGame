package com.zee.webgame.service.impl;

import java.io.IOException;
import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zee.webgame.object.Game;
import com.zee.webgame.object.Player;
import com.zee.webgame.service.GameService;

@Component("gameService")
public class GameServiceImpl implements GameService {
	
	private static String GAME_COMMAND_START = "start";
	
	private static String GAME_COMMAND_QUIT = "quit";
	
	private static String GAME_COMMAND_ACCAPT = "accapt";
	

	private static LinkedBlockingQueue<Player> ReadyPlayers = new LinkedBlockingQueue<Player>(); 
	
	public class AnswerGame extends Game{
		
		
		private List<String> qustionsList ;
		
		
		public AnswerGame(){
			this.setGameId(String.valueOf(System.currentTimeMillis()));
			this.setPlayList(new ArrayList<Player>());
			this.qustionsList = new ArrayList<String>();
			this.setRoundInfoList(new ArrayList<Map<String,Object>>());
			this.setCurrentRound(0);
			qustionsList.add("1");
		}
		
		public void playerMove(Player player,Object moveObj){
			
			if (this.getRoundInfoList().size() == 0) {
				Map<String,Object> roundInfo = new HashMap<String,Object>();
				roundInfo.put(player.getPlayerId(), moveObj);
				this.getRoundInfoList().add(roundInfo);
			}else{
				Map<String,Object> roundInfo = this.getRoundInfoList().get(this.getCurrentRound());
				roundInfo.put(player.getPlayerId(), moveObj);
			}
			
			
		}
		
		public void roundSettle(Player player){

			int movedPlayer = getMovedPlayer();
			List<Player> playerList = getPlayList();
			if (movedPlayer == playerList.size()) {
				//答题玩家人数等于玩家人数说明回合结束，做回合结算
				for (Player playerTmp:playerList){
					StringBuffer sb = new StringBuffer();
					Map<String,Object> currentRound = this.getRoundInfoList().get(this.getCurrentRound());
					Set<String> roundSet = currentRound.keySet();
					Iterator<String> it = roundSet.iterator();
					String tempId; 
					while (it.hasNext()){
						tempId = it.next();
						if (tempId.equals(player.getPlayerId())){
							sb.append("，你的的答案：").append(currentRound.get(tempId));	
						}else{
							sb.append("，对手的答案：").append(currentRound.get(tempId));						
						}
					}
					
					String currentQuestion = (String) getCurrentQuest();
					String playerAnswer = (String) currentRound.get(playerTmp.getPlayerId());
					String answerResult = currentQuestion.equals(playerAnswer)? "正确":"错误";
					try {
						//广播结算结果
						sendMsgToPlayer(playerTmp,"0","答题结果：" +answerResult +  ",正确答案：" + currentQuestion + sb.toString()  ,GAME_COMMAND_ACCAPT);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}else{
				//回合未结束
				try {
					sendMsgToPlayer(player,"0","作答成功，等待对手作答",GAME_COMMAND_ACCAPT);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private int getMovedPlayer(){
			Map<String,Object> roundInfo = this.getRoundInfoList().get(this.getCurrentRound());
			if (null != roundInfo) {
				return roundInfo.size();
			}else{
				return 0;
			} 
		}
		
		private boolean checkRoundAnswer(Player player){
			Map<String,Object> roundInfo = this.getRoundInfoList().get(this.getCurrentRound());
			String moveInfo = (String) roundInfo.get(player.getPlayerId());
			String rightAnswer =  qustionsList.get(this.getCurrentRound());
			return rightAnswer.equals(moveInfo);
		}
		
		
		public Object getCurrentQuest(){
			Object currentQuest = this.qustionsList.get(this.getCurrentRound());
			return currentQuest;
		}
		
		public int addPlayer(Player player){
			this.getPlayList().add(player);
			return this.getPlayList().size();
		}


		 
		 
	}
	
	public String join(Player joinPlayer) {
		try {
			if(joinPlayer.getStatus().equals(Player.PLAYER_STATUS_FREE)){
				Player redeyPlayer = getReadyPlayer();
				
				if(null != redeyPlayer){
					AnswerGame agame = (AnswerGame) redeyPlayer.getGame();
					int playCount = agame.addPlayer(joinPlayer);
					System.out.println("game :" + agame.getGameId() + ",players:" + playCount);
					sendMsgToPlayer(joinPlayer,"0","对手是" + redeyPlayer.getName(),null);
					sendMsgToPlayer(redeyPlayer,"0","对手是" + joinPlayer.getName(),null);
					startGame(agame);
					return null;
				}else{
					AnswerGame agame = new AnswerGame();
					joinPlayer.setGame(agame);
					joinPlayer.setStatus(Player.PLAYER_STATUS_READY);
					int playCount = agame.addPlayer(joinPlayer);
					ReadyPlayers.put(joinPlayer);	
					System.out.println("game :" + agame.getGameId() + ",players:" + playCount);
					sendMsgToPlayer(joinPlayer,"0","匹配中，游戏编号" + agame.getGameId(),null);
					return null;
				}
			}else if(joinPlayer.getStatus().equals(Player.PLAYER_STATUS_READY)){
				sendMsgToPlayer(joinPlayer,"0","匹配中..",null);
			}else if(joinPlayer.getStatus().equals(Player.PLAYER_STATUS_PALYING)){
				sendMsgToPlayer(joinPlayer,"0","游戏中..",null);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private Player getReadyPlayer(){
		Player redeyPlayer = ReadyPlayers.poll();
		//需要找到第一个状态为准备中的玩家
		if(null != redeyPlayer && !redeyPlayer.getStatus().equals(Player.PLAYER_STATUS_READY)){
			redeyPlayer = getReadyPlayer();
		}
		
		return redeyPlayer;
	}
	
	private void startGame(AnswerGame game) throws IOException{
		List<Player> playList =  game.getPlayList();
		for (Player player:playList){
			player.setStatus(Player.PLAYER_STATUS_PALYING);
			player.setGame(game);
			sendMsgToPlayer(player,"0","游戏开始，游戏编号" + game.getGameId(),GAME_COMMAND_START);
		}
	}

	public String play(Player player, JSONObject data) {
		
		AnswerGame agame = (AnswerGame) player.getGame();
		agame.playerMove(player, data.getString("answer"));
		
		agame.roundSettle(player);
		
		//TODO 这里应该判断游戏是否结束，目前先直接结束游戏
		this.quit(player);
		
		return null;
	}

	public String quit(Player player) {
		//玩家状态是游戏中结束当前游戏，并广播结束通知，释放游戏资源
		if(Player.PLAYER_STATUS_PALYING.equals(player.getStatus()) && null != player.getGame()){
			List<Player> playList =  ((AnswerGame) player.getGame()).getPlayList();
			for (Player playerTmp:playList){
				playerTmp.setStatus(Player.PLAYER_STATUS_FREE);
				try {
					sendMsgToPlayer(playerTmp,"0",player.getName() + "退出游戏，游戏结束",GAME_COMMAND_QUIT);
				} catch (IOException e) {
					e.printStackTrace();
				}
				playerTmp.setGame(null);
			}
		}else if (Player.PLAYER_STATUS_READY.equals(player.getStatus())){
			//玩家状态是准备中，置状态为空闲，防止在等待队列中被选中
			player.setStatus(Player.PLAYER_STATUS_FREE);
			try {
				sendMsgToPlayer(player,"0",player.getName() + "退出匹配",GAME_COMMAND_QUIT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			player.setStatus(Player.PLAYER_STATUS_FREE);
		}
		return null;
	}
	
	
	private void sendMsgToPlayer(Player player,String returnCode , String msg,String command) throws IOException{
		WebSocketSession se = player.getSession();
		Map<String,String> returnMap = new HashMap<String,String>();
		returnMap.put("returnCode", returnCode);
		returnMap.put("msg", msg);
		returnMap.put("command", command);
		se.sendMessage(new TextMessage(JSON.toJSONString(returnMap)));
	}
	
	
	

}