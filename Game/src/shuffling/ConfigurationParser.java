/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shuffling;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import objects.Util;
import objectsorting.object.Base;
import objectsorting.object.Player;
import objectsorting.object.Setting;
import objectsorting.object.Sink;
import objectsorting.object.Source;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import objectsorting.object.GameStatus;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
/**
 *
 * @author Chong Chu
 */
public class ConfigurationParser extends JFrame {
    
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
    
    
	public void readConfigs() {
		SAXBuilder sxb = new SAXBuilder();
		try {
			//Document configDoc = sxb.build(Util.load(sConfigFile));
            Document configDoc = sxb.build(new File(sConfigFile));
			Element rootElmt = configDoc.getRootElement();
			Iterator<Element> iterator = rootElmt.getChildren().iterator();
			
			while (iterator.hasNext()) {//for each wave
				Element waveElem= (Element)iterator.next();
	            String waveName=waveElem.getName();
	            if(waveName.equals("WaveDescription")){
	            	
	            	String waveDscrpt=waveElem.getAttributeValue("Number");
	                GameWave gamewave=new GameWave();//a new wave
	                gamewave.setWaveDscrp(waveDscrpt);//set the wave description
	            	
					List listGames=waveElem.getChildren();
	                Iterator igames = listGames.iterator();
	                
	                Setting setting = new Setting();
					
					ObjectSortingGame osgame=new ObjectSortingGame();
					
	                while(igames.hasNext()){
	                	Element element= (Element)igames.next();

	                	if (element.getName().equals("GeneralSetting")) {
							Element screenSizeChild = element.getChild("ScreenSize");
							setting.screenSize[0] = Integer.valueOf(screenSizeChild
									.getAttributeValue("x"));
							setting.screenSize[1] = Integer.valueOf(screenSizeChild
									.getAttributeValue("y"));
							List<Element> settingElements = element.getChildren("Object");
							for (Element element2 : settingElements) {
								Color color = new Color(Integer.valueOf(element2.getAttributeValue("r")),Integer.valueOf(element2.getAttributeValue("g")),Integer.valueOf(element2.getAttributeValue("b")));
								setting.objectColors.put(Integer.valueOf(element2.getAttributeValue("type")), color);
							}
							Element settingElement = element.getChild("SourceAttender");
							Element colorElement = settingElement.getChild("SelfColor");
							Color color = new Color(Integer.valueOf(colorElement.getAttributeValue("r")),Integer.valueOf(colorElement.getAttributeValue("g")),Integer.valueOf(colorElement.getAttributeValue("b")));
							setting.soSelfColor = color;
							colorElement = settingElement.getChild("OtherColor");
							color = new Color(Integer.valueOf(colorElement.getAttributeValue("r")),Integer.valueOf(colorElement.getAttributeValue("g")),Integer.valueOf(colorElement.getAttributeValue("b")));
							setting.soOtherColor = color;
							setting.soSelfSize = Integer.valueOf(settingElement.getChild("SelfSize").getValue());
							setting.soOtherSize = Integer.valueOf(settingElement.getChild("OtherSize").getValue());
							setting.soSpeedCarrying = Integer.valueOf(settingElement.getChild("SpeedCarrying").getValue());
							setting.soSpeedUnladen = Integer.valueOf(settingElement.getChild("SpeedUnladen").getValue());
							settingElement = element.getChild("SinkAttender");
							colorElement = settingElement.getChild("SelfColor");
							color = new Color(Integer.valueOf(colorElement.getAttributeValue("r")),Integer.valueOf(colorElement.getAttributeValue("g")),Integer.valueOf(colorElement.getAttributeValue("b")));
							setting.siSelfColor = color;
							colorElement = settingElement.getChild("OtherColor");
							color = new Color(Integer.valueOf(colorElement.getAttributeValue("r")),Integer.valueOf(colorElement.getAttributeValue("g")),Integer.valueOf(colorElement.getAttributeValue("b")));
							setting.siOtherColor = color;
							setting.siSelfSize = Integer.valueOf(settingElement.getChild("SelfSize").getValue());
							setting.siOtherSize = Integer.valueOf(settingElement.getChild("OtherSize").getValue());
							setting.siSpeedCarrying = Integer.valueOf(settingElement.getChild("SpeedCarrying").getValue());
							setting.siSpeedUnladen = Integer.valueOf(settingElement.getChild("SpeedUnladen").getValue());
							// SOURCE READ
						} else if (element.getName().equals("SourceDescription")) {
							setting.line1Position = Integer.valueOf(element.getChild(
									"LeftBoundary").getValue());
							List<Element> sourceList = element.getChildren("Source");
							for (Element sourceElement : sourceList) {
								Source source = new Source();
								source.setId(sourceElement.getAttributeValue("Number"));
								List<Element> objects = sourceElement.getChild("Proportions").getChildren();
								double sum = 0;
								for (Element element2 : objects) {
									source.getProportionMap().put(Integer.valueOf(element2.getAttributeValue("type")), Double.valueOf(element2.getAttributeValue("proportion")));
									sum += Double.valueOf(element2.getAttributeValue("proportion"));
								}
								if (sum != 1) {
									throw new Exception("Proportion values should add up to 1");
								}
								source.setPosition(new int[] {
										Integer.valueOf(sourceElement
												.getChild("Location").getChild("X")
												.getValue()),
										Integer.valueOf(sourceElement
												.getChild("Location").getChild("Y")
												.getValue()) });
								source.setSize(Integer.valueOf(sourceElement
										.getChild("Appearance").getChild("Size")
										.getValue()));
								Element colorElement = sourceElement.getChild("Appearance").getChild("Color");
								source.setColor(new Color(Integer.valueOf(colorElement
										.getAttributeValue("r")), Integer
										.valueOf(colorElement.getAttributeValue("g")),
										Integer.valueOf(colorElement
												.getAttributeValue("b"))));
								setting.sourceList.add(source);
							}
							// SINK READ
						} else if (element.getName().equals("SinkDescription")) {
							setting.line2Position = Integer.valueOf(element.getChild(
									"RightBoundary").getValue());
							List<Element> sinkList = element.getChildren("Sink");
							for (Element sinkElement : sinkList) {
								Sink sink = new Sink();
								sink.setId(sinkElement.getAttributeValue("Number"));
								sink.setPosition(new int[] {
										Integer.valueOf(sinkElement
												.getChild("Location").getChild("X")
												.getValue()),
										Integer.valueOf(sinkElement
												.getChild("Location").getChild("Y")
												.getValue()) });
								sink.setAcceptingObject(Integer.valueOf(sinkElement.getChild("TargetType").getValue()));
								sink.setSize(Integer.valueOf(sinkElement
										.getChild("Appearance").getChild("Size")
										.getValue()));
								Element colorElement = sinkElement.getChild("Appearance").getChild("Color");
								sink.setColor(new Color(Integer.valueOf(colorElement
										.getAttributeValue("r")), Integer
										.valueOf(colorElement.getAttributeValue("g")),
										Integer.valueOf(colorElement
												.getAttributeValue("b"))));
								setting.sinkList.add(sink);
							}
							// BASE READ
						} else if (element.getName().equals("BaseDescription")) {
							List<Element> baseList = element.getChildren();
							for (Element baseElement : baseList) {
								Base base = new Base();
								base.setId(baseElement.getAttributeValue("Number"));
								base.setPosition(new int[] {
										Integer.valueOf(baseElement
												.getChild("Location").getChild("X")
												.getValue()),
										Integer.valueOf(baseElement
												.getChild("Location").getChild("Y")
												.getValue()) });
								base.setSize(Integer.valueOf(baseElement
										.getChild("Appearance").getChild("Size")
										.getValue()));
								Element colorElement = baseElement.getChild("Appearance").getChild("Color");
								base.setColor(new Color(Integer.valueOf(colorElement
										.getAttributeValue("r")), Integer
										.valueOf(colorElement.getAttributeValue("g")),
										Integer.valueOf(colorElement
												.getAttributeValue("b"))));
								if (baseElement.getAttribute("enabled")
										.getBooleanValue()) {
									setting.baseList.add(base);
								}
							}
						} else if (element.getName().equals("FeedbackDisplay")) {
							setting.gameEndCriterion = Integer.valueOf(element
									.getChild("GameEndCriterion").getValue());
							setting.maxDropOffRate = Integer.valueOf(element.getChild(
									"MaxDropOffRate").getValue());
							setting.timeWindow = Integer.valueOf(element.getChild(
									"TimeWindow").getValue());
						} else if (element.getName().equals("GameParameters")){							
							long maxRunningTime=Long.valueOf(element
									.getChild("TimeOut").getValue());
							osgame.setGameTime(maxRunningTime);
						} else if (element.getName().equals("Game")) {	
							
							List listGroups=element.getChildren();
			                Iterator igroups = listGroups.iterator();
			                
			                while(igroups.hasNext()){//for each group
			                	Element groupElmt= (Element)igroups.next();
			                	
			                	Setting temp=new Setting();
				                temp.copyGeneralInfo(setting);
				                GameStatus status = new GameStatus();
								if (groupElmt.getName().equals("Group")) {
									List<Element> playerList = groupElmt.getChildren();
									for (Element playerElement : playerList) {
										Player player = new Player();
										player.setId(playerElement.getAttributeValue("Number"));
										player.setSpeedMultiplier(Double.valueOf(playerElement.getChild("SpeedMultiplier").getValue()));
										player.setPosition(new int[] {
												Integer.valueOf(playerElement
														.getChild("Location").getChild("X")
														.getValue()),
												Integer.valueOf(playerElement
														.getChild("Location").getChild("Y")
														.getValue()) });
										player.setSourceAttender((playerElement
												.getChild("Type").getValue().equals("1")));
										
										temp.playerList.add(player);
										status.players.add(player);
										status.playerDropOffMap.put(player.getId(),
												new ArrayList<Long>());
										
									}
									osgame.groupSettingList.add(temp);	
									osgame.groupStatusList.add(status);
								}
			                }//end of each group 
						}//end of each game
	            	}	                
	                gamewave.addGame(osgame);
	                this.gameWaveList.add(gamewave);
				}//end of if
	            
			}//for each wave
		} catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error Reading Setting File: \n" + e.getMessage());
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
