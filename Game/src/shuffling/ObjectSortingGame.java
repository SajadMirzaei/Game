/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor. 
 */
package shuffling;

import java.util.ArrayList;
import objectsorting.object.Setting;
import objectsorting.object.GameStatus;

/**
 *
 * @author Chong Chu
 */
public class ObjectSortingGame {
    private String description;
    private GameParameter gamepar;
    private Setting setting;
    private GameStatus status;
    
    public ObjectSortingGame(){
        this.description="";
        gamepar=new GameParameter();
    }
    
    public void setSettings(Setting setting){
    	this.setting=setting;
    }
    
    public Setting getSettings(){
    	return this.setting;
    }
    
    public void setStatus(GameStatus status){
    	this.status=status;
    }
    
    public GameStatus getStatus(){
    	return this.status;
    }
    
    public void setGameParameters(String sGoalNums, String timeout, String allowMatches, String diffGroup){
        
        gamepar.setTimeOut(Long.parseLong(timeout));
    }
    
    public long getGameTime(){
        return gamepar.getTimeOut();
    }

    
    public void setDescription(String description){
        this.description=description;
    }
    
    public String getDescription(){
        return this.description;
    }
    
}
