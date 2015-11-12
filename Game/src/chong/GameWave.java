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
public class GameWave {
    private String waveDscrpAttr;
    private ArrayList<SumNumGame> sumNumGameList;
    
    public GameWave(){
        sumNumGameList=new ArrayList<SumNumGame>();
        sumNumGameList.clear();
    }
    
    public void addGame(SumNumGame game){
        sumNumGameList.add(game);
    }
    
    public SumNumGame getGame(int i){
        return sumNumGameList.get(i);
    }
    
    public int getNumOfGames(){
        return sumNumGameList.size();
    }
    
    public void setWaveDscrp(String dscrpt){
        this.waveDscrpAttr=dscrpt;
    }
    
    public String getWaveDscrp(){
        return this.waveDscrpAttr;
    }
}
