/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chong;

/**
 *
 * @author Chong Chu
 */
public class WaveManager extends Thread {
    
    private ConfigurationParser cp;
    private static int waveRound;
    private multithreadgameserversyncAPI mtserver;
    private String port;
    private SumOfNumServer sms;
    
    public WaveManager(SumOfNumServer sms, String port){
        this.sms=sms;
        this.port=port;
    }
    
    public void manageWave(){
        int waveSize=cp.getNumOfWaves();
        
        for(int i=0;i<waveSize;i++){//for each round
            GameWave gw=cp.getGameWave(i);
            int gameSize=gw.getNumOfGames();
            
            //for each game, we have a status array to record currently where (index of the goal array) we are.
            int[] goalIndex = new int[gameSize];
            boolean[] bBroadcast = new boolean[gameSize];
            boolean[] bGameFinished=new boolean[gameSize];
            for(int gi=0;gi<gameSize;gi++){
                goalIndex[gi]=0;//all start from the first goal
                bBroadcast[gi]=true;// allow BroadCast at beginning 
                bGameFinished[gi]=false;
            }
            
            int numFinishedGame=0;
            boolean bWaveFns=true;
            
            while(true){
                for(int j=0;j<gameSize;j++){// for each game in a round
                    if(bGameFinished[j]==true) continue;
                    SumNumGame sumGame=gw.getGame(j);    
                    int playerSize=sumGame.getNumOfPlayers();
                                        
                    int[] playerID=new int[playerSize];
                    for(int p=0; p<playerSize; p++)
                        playerID[p]=sumGame.getPlayerId(p);
                    
                    //first check whether have run all the goals for a game
                    //if so, then let players of this game wait there
                    //otherwise, move on to the next goal
                    int curGoal=sumGame.getGoalNumAtPos(goalIndex[j]);
                    if(bBroadcast[j]==true)
                    {
                        mtserver.sendCommandToSpecificClients(playerID, playerSize,"Goal Number: "+curGoal+"\n");
                        bBroadcast[j]=false;
                    }
                    
                    boolean bAllChanged=mtserver.allNumbersChangesInGroup(playerID, playerSize);
                    if(bAllChanged==false)
                        continue;
                    boolean bEqual=mtserver.validateGoal(playerID, playerSize, curGoal);
                    if(bEqual==true){
                        mtserver.sendCommandToSpecificClients(playerID, playerSize,"Success!\n");
                        goalIndex[j]++;//go to next Goal Num
                        if(goalIndex[j]==sumGame.getNumOfGoalNums()){
                            mtserver.sendCommandToSpecificClients(playerID, playerSize,"Game Finished! Start lift time game!\n");
                            numFinishedGame++;
                            bGameFinished[j]=true;
                        }
                        else{
                            bBroadcast[j]=true;//Allow print out the other next goal num
                        }
                    }
                    else{
                        mtserver.sendCommandToSpecificClients(playerID, playerSize,"Failed, try again!\n");
                    }
                }
                if(numFinishedGame==gameSize)
                    break;
                
                try{
                    Thread.sleep(100);
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }
            
            //wake up all
            if(i<(waveSize-1))
                mtserver.broadCastAll("Working for a new wave! Wake up!\n");
        }
        mtserver.broadCastAll("All waves has been finished!\n");
    }
    
    public void run() {
        int portNum=Integer.parseInt(port);
        startServer(portNum);
    }
    
    public void loadConfiguration(String fconfig){
        cp=new ConfigurationParser(fconfig);
        cp.loadConfigFile();
    }
    
    private void startServer(int portNum){
        mtserver=new multithreadgameserversyncAPI(sms);   
        mtserver.connect(portNum);
    }
}
