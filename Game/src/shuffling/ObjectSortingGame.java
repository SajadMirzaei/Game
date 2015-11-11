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
	
	public ArrayList<Setting> groupSettingList;
	public ArrayList<GameStatus> groupStatusList;
	
	
    private String description;
    private GameParameter gamepar;
    
    
    public ObjectSortingGame(){
        this.description="";
        gamepar=new GameParameter();
        groupSettingList=new ArrayList<Setting>();
        groupSettingList.clear();
        groupStatusList=new ArrayList<GameStatus>();
        groupStatusList.clear();
        
    }
    
    
    public void setGameParameters(String sGoalNums, String timeout, String allowMatches, String diffGroup){
        
        gamepar.setTimeOut(Long.parseLong(timeout));
    }
    
    public void setGameTime(long timeout){
    	gamepar.setTimeOut(timeout);
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
