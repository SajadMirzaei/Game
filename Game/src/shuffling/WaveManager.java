/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shuffling;

import objectsorting.Server;

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
    
    public WaveManager(Server server){
        icurWave=0;
        icurGame=0;
        startTime = System.currentTimeMillis();
        this.server=server;
    }
    
    public void setServer(Server server){
        this.server=server;
    }
    
    public void manageWave(){
        
    }
    
    @Override
    public void run(){
        timeThreshold=cp.getGameWave(icurWave).getGame(icurGame).getGameTime(); 
        while(true){
            curTime = System.currentTimeMillis();
            long runTime=curTime-startTime;
                        
            if(runTime>=timeThreshold){              
                //stop the current game
                server.stopCurrentGame();               
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
                        icurGame=0;
                        GameWave curGW=cp.getGameWave(icurWave);
                        ObjectSortingGame curGame=curGW.getGame(icurGame);
                        timeThreshold=cp.getGameWave(icurWave).getGame(icurGame).getGameTime();
                        server.startNewGame(curGame);
                        startTime = System.currentTimeMillis();
                    }
                }
                else{//another game of the same wave
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
    
    public void loadConfiguration(String fconfig){
        cp=new ConfigurationParser(fconfig);
        cp.loadConfigFile();
    }
    
}
