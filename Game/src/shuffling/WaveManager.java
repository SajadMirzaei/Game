/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shuffling;

import objectsorting.Server;
import objectsorting.object.Setting;
import objectsorting.object.GameStatus;
/**
 *
 * @author Chong Chu
 */
public class WaveManager extends Thread{
    private ConfigurationParser cp;
    private static int waveRound;
    
    private long startTime;
    private long curTime;
    private long timeThreshold;
    
    public static int icurWave;
    public static int icurGame;
    
    public Server server;
    
    public boolean gameFinished;
    
    public WaveManager(Server server){
        icurWave=0;
        icurGame=0;
        
        this.server=server;
        this.gameFinished=false;
    }
    
    public void setServer(Server server){
        this.server=server;
    }
    
    public void setGameFinished(boolean b){
    	this.gameFinished=b;
    }
    
    public void loadConfiguration(String fconfig){
        cp=new ConfigurationParser(fconfig);
        cp.readConfigs();
    }
    
    public void setFirstGame(){
    	if(cp.getNumOfWaves()<=0){
    		ObjectSortingGame objGame=new ObjectSortingGame();
    		server.setSettingStatusList(objGame);
    	}   		
    	else{    		
    		server.setSettingStatusList(cp.getGameWave(icurWave).getGame(icurGame));
    	}
    }
    
    @Override
    public void run(){    	
        timeThreshold=cp.getGameWave(icurWave).getGame(icurGame).getGameTime();
        startTime = System.currentTimeMillis();
        while(true){
            curTime = System.currentTimeMillis();
            long runTime=curTime-startTime;
                        
            if(runTime>=timeThreshold || this.gameFinished==true){
                //stop the current game
                server.stopCurrentGame();
                
                while(Server.bAllGameNotifiedToStop==false){
                	try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//System.out.println("Block here!!!");
                }
                
                icurGame++;
                //start the next game or go to next wave
                int gameSize=cp.getGameWave(icurWave).getNumOfGames();
                if(icurGame>=gameSize){
                    //go to next wave
                    icurWave++;
                    if(icurWave >= cp.getNumOfWaves()){//has go through all waves
                        server.broadCastAllFinished();
                        break;
                    }
                    else{//new wave
                    	this.gameFinished=false;
                        icurGame=0;
                        GameWave curGW=cp.getGameWave(icurWave);
                        ObjectSortingGame curGame=curGW.getGame(icurGame);
                        timeThreshold=cp.getGameWave(icurWave).getGame(icurGame).getGameTime();
                        
                        server.startNewGame(curGame);
                        startTime = System.currentTimeMillis();
                    }
                }
                else{//another game of the same wave 
                    this.gameFinished=false;
                    GameWave curGW=cp.getGameWave(icurWave);
                    ObjectSortingGame curGame=curGW.getGame(icurGame);
                    timeThreshold=cp.getGameWave(icurWave).getGame(icurGame).getGameTime();
                    
                    server.startNewGame(curGame);
                    startTime = System.currentTimeMillis();
                }
            }
                       
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    

    
}
