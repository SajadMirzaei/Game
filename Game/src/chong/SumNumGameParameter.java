/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chong;

import java.util.regex.PatternSyntaxException;

/**
 *
 * @author Chong Chu
 */
public class SumNumGameParameter {
    private int[] goalNums;
    private int timeOut;
    private int allowMatches;
    private int differentGroup;
    private int goalNumSize;
    
    public void setGoalNums(String sNums){
        try {
            String[] splitArray = sNums.split("\\s+");
            //System.out.println(splitArray[0]);
            goalNumSize=splitArray.length;
            this.goalNums=new int[splitArray.length];
            for(int i = 0; i < splitArray.length; i++) {
                this.goalNums[i] = Integer.parseInt(splitArray[i]);
            }
        } catch (PatternSyntaxException ex) {
            //
            System.out.println(ex.getMessage());
        }
    }
    
    public int getNumOfGoals(){
        return goalNumSize;
    }
    
    public int getGoalNumAtPos(int pos){
        return this.goalNums[pos];
    }
    
    public void setTimeOut(int timeOut){
        this.timeOut=timeOut;    
    }
    
    public int getTimeOut(){
        return this.timeOut;
    }
}
