/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor. 
 */
package chong;

import java.util.ArrayList;

/**
 *
 * @author Chong Chu
 */
public class SumNumGame {
    private String description;
    private ArrayList<Integer> playerList; 
    private SumNumGameParameter gamepar;
    
    public SumNumGame(){
        this.description="";
        playerList=new ArrayList<Integer>();
        playerList.clear();
        gamepar=new SumNumGameParameter();
    }
    
    public void addPlayerToGame(int playerNum){
        this.playerList.add(playerNum);
    }
    
    public void setGameParameters(String sGoalNums, String timeout, String allowMatches, String diffGroup){
        gamepar.setGoalNums(sGoalNums);
        gamepar.setTimeOut(Integer.parseInt(timeout));
    }
    
    public int getNumOfPlayers(){
        return playerList.size();
    }
    
    public int getPlayerId(int pos){
        return playerList.get(pos);
    }
    
    public int getNumOfGoalNums(){
        return gamepar.getNumOfGoals();
    }
    
    public int getGoalNumAtPos(int pos){
        return gamepar.getGoalNumAtPos(pos);
    }
    
    public void setDescription(String description){
        this.description=description;
    }
    
    public String getDescription(){
        return this.description;
    }
    
}
