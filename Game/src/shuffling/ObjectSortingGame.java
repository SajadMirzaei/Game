/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor. 
 */
package shuffling;

import java.util.ArrayList;

/**
 *
 * @author Chong Chu
 */
public class ObjectSortingGame {
    private String description;
    private ArrayList<Integer> playerList; 
    private GameParameter gamepar;
    
    public ObjectSortingGame(){
        this.description="";
        playerList=new ArrayList<Integer>();
        playerList.clear();
        gamepar=new GameParameter();
    }
    
    public void addPlayerToGame(int playerNum){
        this.playerList.add(playerNum);
    }
    
    public void setGameParameters(String sGoalNums, String timeout, String allowMatches, String diffGroup){
        
        gamepar.setTimeOut(Long.parseLong(timeout));
    }
    
    public long getGameTime(){
        return gamepar.getTimeOut();
    }
    
    public int getNumOfPlayers(){
        return playerList.size();
    }
    
    public int getPlayerId(int pos){
        return playerList.get(pos);
    }
    
    
    public void setDescription(String description){
        this.description=description;
    }
    
    public String getDescription(){
        return this.description;
    }
    
}
