package com.zee.webgame.service;


import com.alibaba.fastjson.JSONObject;
import com.zee.webgame.object.Player;

public interface GameService {
	public String join(Player player);
	
	public String play(Player player,JSONObject data);
	
	public String quit(Player player);
}