/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shuffling;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Chong Chu
 */
public class ConfigurationParser {
    
    private String sConfigFile;//configuration file name 
    private ArrayList<GameWave> gameWaveList;
    private int intermissionPreLife;
    
    private Document configDoc;
    private Element rootElmt;
    
    public ConfigurationParser(String sConfig){
        this.sConfigFile=sConfig;
        gameWaveList=new ArrayList<GameWave>();
        gameWaveList.clear();
    }
    
    public void loadConfigFile(){
        SAXBuilder sxb = new SAXBuilder();
        try{
           configDoc = sxb.build(new File(this.sConfigFile));
           rootElmt=configDoc.getRootElement();
        } 
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        
        List listWaves=rootElmt.getChildren();
        Iterator iwaves = listWaves.iterator();
        while(iwaves.hasNext()){//for each wave
            
            Element waveElem= (Element)iwaves.next();
            String waveName=waveElem.getName();
            if(waveName.equals("WaveDescription")){
                String waveDscrpt=waveElem.getAttributeValue("Number");
                GameWave gamewave=new GameWave();//a new wave
                gamewave.setWaveDscrp(waveDscrpt);//set the wave description
                
                List listGames=waveElem.getChildren();
                Iterator igames = listGames.iterator();
                while(igames.hasNext()){// for each game of one wave
                    Element gameElem= (Element)igames.next();
                    String gameDscrpt=gameElem.getAttributeValue("Description");
                    ObjectSortingGame osgame=new ObjectSortingGame();
                    osgame.setDescription(gameDscrpt);
                    
                    List listGameInfo=gameElem.getChildren();
                    Iterator igameinfo=listGameInfo.iterator();
                    while(igameinfo.hasNext()){
                        Element gameinfoElem=(Element)igameinfo.next();
                        String infoName=gameinfoElem.getName();
                        
                        List listInfoDetail=gameinfoElem.getChildren();
                        Iterator idetail = listInfoDetail.iterator();
                        if(infoName.equals("Players")){
                            while(idetail.hasNext()){//parse out all player IDs 
                                Element playerElmt=(Element)idetail.next();
                                String playernum=playerElmt.getText();
                                osgame.addPlayerToGame(Integer.parseInt(playernum));
                            }
                        }
                        else if(infoName.equals("GameParameters")){
                            String goalseq="",timeout="",allowmatch="",diffgroup="";
                            while(idetail.hasNext()){
                                Element parElmt=(Element)idetail.next();
                                String parValue=parElmt.getText();
                                String parName=parElmt.getName();
                                
                                if(parName.equals("GoalNumberSequence")){
                                    goalseq=parValue;
                                }
                                else if(parName.equals("TimeOut")){
                                    timeout=parValue;
                                }else if(parName.equals("AllowMatches")){
                                    allowmatch=parValue;
                                }
                                else if(parName.equals("DifferentGroup")){
                                    diffgroup=parValue;
                                }
                            }
                            osgame.setGameParameters(goalseq,timeout,allowmatch,diffgroup);
                        }     
                    }
                    gamewave.addGame(osgame);//add the game to the wave
                }   
                
                this.gameWaveList.add(gamewave);//add a wave to wave list
            }
            else if(waveName.equals("IntermissionPreLife")){
                intermissionPreLife=Integer.parseInt(waveElem.getText());
            }               
        }
    }
    
    public int getNumOfWaves(){
        return this.gameWaveList.size();
    }
    
    public GameWave getGameWave(int pos){
        return this.gameWaveList.get(pos);
    }
    
    public void setConfigFileName(String sConfig){
        this.sConfigFile=sConfig;
    }
}
