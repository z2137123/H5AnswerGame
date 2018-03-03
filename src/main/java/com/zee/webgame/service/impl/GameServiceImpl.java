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

import com.zee.webgame.mapper.QuestionMapper;
import com.zee.webgame.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	private static String GAME_COMMAND_ACCPT = "accpt";
	
	private static String GAME_COMMAND_SETTLE = "settle";
	
	private static String GAME_COMMAND_NEXT = "next";
	
	private static String GAME_COMMAND_OVER = "over";

	@Autowired
	private QuestionMapper questionMapper;

	private static LinkedBlockingQueue<Player> ReadyPlayers = new LinkedBlockingQueue<Player>(); 
	
	public class AnswerGame extends Game{
		
		
		private List<Question> qustionsList ;

		
		public AnswerGame(){
			this.setGameId(String.valueOf(System.currentTimeMillis()));
			this.setPlayList(new ArrayList<Player>());
			this.qustionsList = questionMapper.getQuestionList(0);
			this.setRoundInfoList(new ArrayList<Map<String,Object>>());
			this.setCurrentRound(0);
		}
		
		public void playerMove(Player player,JSONObject moveObj){
			
			//判断是否已经移动
			if (this.getRoundInfoList().size() > this.getCurrentRound()){
				Map<String,Object> roundInfo = this.getRoundInfoList().get(this.getCurrentRound());
				if(null != roundInfo.get(player.getPlayerId())){
					try {
						sendMsgToPlayer(player,"0","已经选择答案，无法修改",null);
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
			}
			
			//记录移动信息
			if (this.getRoundInfoList().size() == this.getCurrentRound()) {
				Map<String,Object> roundInfo = new HashMap<String,Object>();
				roundInfo.put(player.getPlayerId(), moveObj.get("answer"));
				this.getRoundInfoList().add(roundInfo);
			}else{
				Map<String,Object> roundInfo = this.getRoundInfoList().get(this.getCurrentRound());
				roundInfo.put(player.getPlayerId(), moveObj.get("answer"));
			}
			
			//移动结果推送
			String result = checkRoundAnswer(player)?"true":"false";
			moveObj.put("result", result);
			try {
				sendMsgToPlayer(player,"0",moveObj.toJSONString()  ,GAME_COMMAND_ACCPT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		public boolean roundSettle(Player player){

			int movedPlayer = getMovedPlayer();
			List<Player> playerList = getPlayList();
			if (movedPlayer == playerList.size()) {
				//答题玩家人数等于玩家人数说明回合结束，做回合结算
				for (Player playerTmp:playerList){
					Map<String,Object> currentRound = this.getRoundInfoList().get(this.getCurrentRound());
					Set<String> roundSet = currentRound.keySet();
					Iterator<String> it = roundSet.iterator();
					String tempId; 
					Map<String,String> msg = new HashMap<String,String>();
					while (it.hasNext()){
						tempId = it.next();
						if (!tempId.equals(playerTmp.getPlayerId())){
							msg.put("opponentAnswer", (String) currentRound.get(tempId));
						}
					}
					
					//正确答案
					String currentQuestion = getCurrentQuest().getRightAnswer();
					msg.put("rightAnswer", currentQuestion);
					try {
						sendMsgToPlayer(playerTmp,"0", JSON.toJSONString(msg) ,GAME_COMMAND_SETTLE);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				return true;
			}else{
				//回合未结束
				try {
					sendMsgToPlayer(player,"0","作答成功，等待对手作答",null);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		}

		public void gameStart(){
			Map<String,Object> msgMap = this.formatQuestionMap(this.getCurrentQuest());


			List<Player> playList =  this.getPlayList();
			for (Player player:playList){
				player.setStatus(Player.PLAYER_STATUS_PALYING);
				player.setGame(this);
				try {
					sendMsgToPlayer(player,"0",JSON.toJSONString(msgMap),GAME_COMMAND_START);
				}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		
		
		private void gameSettle(){
			if (this.getCurrentRound() + 1 >= this.qustionsList.size()){
				//游戏结束
				for (Player player:this.getPlayList()){
					try {
						sendMsgToPlayer(player,"0", "游戏结束" ,GAME_COMMAND_OVER);
						player.setGame(null);
						player.setStatus(Player.PLAYER_STATUS_FREE);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else{
				//进入下回合
				nextRound();
			}
		}
		
		private void nextRound(){
			addRound();
			for (Player player:this.getPlayList()){
				try {
					sendMsgToPlayer(player,"0", JSON.toJSONString(formatQuestionMap(this.getCurrentQuest())) ,GAME_COMMAND_NEXT);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private Map<String,Object> formatQuestionMap(Question question){
			List<Map<String,String>> answerList = new ArrayList<Map<String,String>>();
			Map<String,String> am1 = new HashMap<String, String>();
			am1.put("value", "1");
			am1.put("answer", question.getFistAnswer());
			answerList.add(am1);
			Map<String,String> am2 = new HashMap<String, String>();
			am2.put("value", "2");
			am2.put("answer", question.getSecondAnswer());
			answerList.add(am2);
			Map<String,String> am3 = new HashMap<String, String>();
			am3.put("value", "3");
			am3.put("answer", question.getThirdAnswer());
			answerList.add(am3);
			Map<String,String> am4 = new HashMap<String, String>();
			am4.put("value", "4");
			am4.put("answer", question.getFourthAnswer());
			answerList.add(am4);

			Map<String,Object> resultMap = new HashMap<String, Object>();
			resultMap.put("question", question.getQuestionName());
			resultMap.put("answerList", answerList);
			return resultMap;
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
			Question currentQuestion =  qustionsList.get(this.getCurrentRound());
			return moveInfo.equals(currentQuestion.getRightAnswer());
		}
		
		
		public Question getCurrentQuest(){
			return 	this.qustionsList.get(this.getCurrentRound());
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
		game.gameStart();
	}

	public String play(Player player, JSONObject data) {
		
		AnswerGame agame = (AnswerGame) player.getGame();
		agame.playerMove(player, data);
		
		if(agame.roundSettle(player)){
			agame.gameSettle();			
		}
		
		
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
