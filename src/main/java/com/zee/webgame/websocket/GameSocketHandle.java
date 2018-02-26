package com.zee.webgame.websocket;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zee.webgame.object.Player;
import com.zee.webgame.service.GameService;

import java.io.IOException;  
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.web.socket.WebSocketMessage;  
import org.springframework.web.socket.WebSocketSession;  

public class GameSocketHandle extends TextWebSocketHandler {
	// 线上人数  
    private static int count;  
    private static CopyOnWriteArraySet<WebSocketSession> set = new CopyOnWriteArraySet<WebSocketSession>();  
    private static ConcurrentMap<String,Player> OnlinePlayer = new ConcurrentHashMap<String,Player>(); 
    private WebSocketSession session;  
   
    @Autowired
    private GameService gameService;
    
    @Override  
    public void afterConnectionEstablished(WebSocketSession session) {  
       this.session = session;  
       try{  
           set.add(this.session);  
       }catch(Exception e) {  
           e.printStackTrace();  
       }  
       GameSocketHandle.addOnlineCount();  
       System.out.println("目前连接人数：" + getOnlineCount());  
    }  
   
    public void afterConnectionClosed(WebSocketSession session,CloseStatus closeStatus) {  
       this.session = session;  
       Player player = getPlayerBySession(session);
       getGameService().quit(player); 
       OnlinePlayer.remove(session.getId());
       set.remove(this.session);  
       subOnlineCount();  
       System.out.println("目前连接人数：" + getOnlineCount());  
    }  
     
    public void handleMessage(WebSocketSession session,WebSocketMessage<?> message){  
       System.out.println("text message: "+ session.getId()+ "-"+ message.getPayload()); 
       Player player = getPlayerBySession(session);
       String msgStr = (String) message.getPayload();
       if (null != msgStr) {
    	   JSONObject msgObj = JSON.parseObject(msgStr);
    	   String command = msgObj.getString("command");
    	   try {
    		   if ("join".equals(command)){
    			   getGameService().join(player);  		   
    		   }else if("quit".equals(command)){
    			   getGameService().quit(player);
    		   }else if("play".equals(command)){
    			   getGameService().play(player, msgObj);
    		   }else{    		   
    			   session.sendMessage(message);
    		   }
    	   } catch (IOException e) {
    		   // TODO Auto-generated catch block
    		   e.printStackTrace();
    	   }  
       }
    }  
    
    private Player getPlayerBySession(WebSocketSession session){
    	Player player  = OnlinePlayer.get(session.getId());
    	if (null == player){
    		player = new Player(session);
    		OnlinePlayer.put(session.getId(), player);
    	}
    	
    	return player;
    }
     
    public static int getOnlineCount() {  
       return count;  
    }  
   
    public static void addOnlineCount() {  
       count++;  
    }  
   
    public static void subOnlineCount() {  
       count--;  
    }

	public GameService getGameService() {
		return gameService;
	}

	public void setGameService(GameService gameService) {
		this.gameService = gameService;
	}  
}
