/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shuffling;


/**
 *
 * @author Chong Chu
 */
public class GameParameter {
  
    private long timeOut;
    private int allowMatches;
    private int differentGroup;
    
    
    public void setTimeOut(long timeOut){
        this.timeOut=timeOut;    
    }
    
    public long getTimeOut(){
        return this.timeOut;
    }
}
