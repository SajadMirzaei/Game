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
public class GameWave {
    private String waveDscrpAttr;
    private ArrayList<ObjectSortingGame> objectSortingGameList;
    
    public GameWave(){
        objectSortingGameList=new ArrayList<ObjectSortingGame>();
        objectSortingGameList.clear();
    }
    
    public void addGame(ObjectSortingGame game){
        objectSortingGameList.add(game);
    }
    
    public ObjectSortingGame getGame(int i){
        return objectSortingGameList.get(i);
    }
    
    public int getNumOfGames(){
        return objectSortingGameList.size();
    }
    
    public void setWaveDscrp(String dscrpt){
        this.waveDscrpAttr=dscrpt;
    }
    
    public String getWaveDscrp(){
        return this.waveDscrpAttr;
    }
}
