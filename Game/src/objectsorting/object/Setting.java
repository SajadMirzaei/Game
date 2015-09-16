package objectsorting.object;

import java.util.ArrayList;
import java.util.List;

import objects.Util;

public class Setting {
	
	public List<Source> sourceList = new ArrayList<>();
	public List<Sink> sinkList = new ArrayList<>();
	public List<Player> playerList = new ArrayList<>();
	public List<Base> baseList = new ArrayList<>();
	
	public int line1Position = 500;
	public int line2Position = 1000;
	
	public int timeWindow;
	public int maxDropOffRate;
	public int gameEndCriterion;

	public Setting() {
		// TODO Auto-generated constructor stub
	}
	
	public Setting(String str) {
		String[] settings = str.split(Util.MAJOR_SEPERATOR);
		for (String s : settings) {
			if (!s.contains("setting")) {
				String[] specs = s.split(Util.ID_SEPERATOR);
				if (specs[0].contains(Util.OBJ_SOURCE)) {
					String[] position = specs[1].split(Util.POSITION_SEPERATOR);
					Source source = new Source();
					source.setPosition(new int[] {Integer.valueOf(position[0]),Integer.valueOf(position[1])});
					sourceList.add(source);
				}else if (specs[0].contains(Util.OBJ_SINK)) {
					String[] position = specs[1].split(Util.POSITION_SEPERATOR);
					Sink sink = new Sink();
					sink.setPosition(new int[] {Integer.valueOf(position[0]),Integer.valueOf(position[1])});
					if (specs[0].contains(Util.CARRYING_SIGN_1)) {
						sink.setAcceptingFirstTypeObject(true);
					}else{
						sink.setAcceptingFirstTypeObject(false);
					}
					sinkList.add(sink);
				}else if (specs[0].contains(Util.OBJ_LINE_ONE)){
					line1Position = Integer.valueOf(specs[1]);
				}else if (specs[0].contains(Util.OBJ_LINE_TWO)){
					line2Position = Integer.valueOf(specs[1]);
				}else if (specs[0].contains(Util.BASE_SIGN)){
					String[] position = specs[1].split(Util.POSITION_SEPERATOR);
					Base base = new Base();
					base.setId(specs[0]);
					base.setPosition(new int[] {Integer.valueOf(position[0]),Integer.valueOf(position[1])});
					baseList.add(base);
				}
			}
		}
		maxDropOffRate = Integer.valueOf(settings[settings.length-2]);
		gameEndCriterion = Integer.valueOf(settings[settings.length-1]);
	}

	@Override
	public String toString() {
		String settingString = "setting";
		for (int i = 0; i < sourceList.size(); i++) {
			Source source = sourceList.get(i);
			int[] position = source.getPosition();
			settingString += Util.MAJOR_SEPERATOR + Util.OBJ_SOURCE + source.getId()
					+ Util.ID_SEPERATOR + position[0] + Util.POSITION_SEPERATOR
					+ position[1];
		}
		for (int i = 0; i < sinkList.size(); i++) {
			Sink sink = sinkList.get(i);
			int[] position = sinkList.get(i).getPosition();
			if (sink.isAcceptingFirstTypeObject()) {
				settingString += Util.MAJOR_SEPERATOR + Util.OBJ_SINK + sink.getId()
						+ Util.CARRYING_SIGN_1 + Util.ID_SEPERATOR
						+ position[0] + Util.POSITION_SEPERATOR + position[1];
			} else {
				settingString += Util.MAJOR_SEPERATOR + Util.OBJ_SINK + sink.getId()
						+ Util.CARRYING_SIGN_2 + Util.ID_SEPERATOR
						+ position[0] + Util.POSITION_SEPERATOR + position[1];
			}
		}
		for (int i = 0; i < baseList.size(); i++) {
			Base base = baseList.get(i);
			int[] position = base.getPosition();
			settingString += Util.MAJOR_SEPERATOR + base.getId() + Util.BASE_SIGN
					+ Util.ID_SEPERATOR + position[0] + Util.POSITION_SEPERATOR
					+ position[1];
		}
		settingString += Util.MAJOR_SEPERATOR + Util.OBJ_LINE_ONE
				+ Util.ID_SEPERATOR + line1Position;
		settingString += Util.MAJOR_SEPERATOR + Util.OBJ_LINE_TWO
				+ Util.ID_SEPERATOR + line2Position;
		settingString += Util.MAJOR_SEPERATOR + maxDropOffRate;
		settingString += Util.MAJOR_SEPERATOR + gameEndCriterion;
		return settingString;
	}
}
