package objects;

import java.io.InputStream;

import objectsorting.object.Setting;

public final class Util {
	
	public static final String MAJOR_SEPERATOR = "@@sep@@";
	public static final String ID_SEPERATOR = "--id--";
	public static final String POSITION_SEPERATOR = ",,pos,,";
	public static final String SOURCE_ATTENDER_SIGN = "\\+++++S";
	public static final String CARRYING_SIGN_1 = "+++++1";
	public static final String CARRYING_SIGN_2 = "+++++2";
	public static final String RATE_INDICATOR = "rate";
	public static int MULTI_PORT = 9876;
	public static int UNI_PORT = 9877;
	public static String GROUP_ADDRESS = "224.0.0.4";
	public static String NEW_GROUP_ADDRESS_PREFIX="224.0.0.";
	public static final String BASE_SIGN = "^";
	public static final String OBJ_PLAYER = "p";
	public static final String OBJ_SOURCE = "so";
	public static final String OBJ_SINK = "si";
	public static final String OBJ_LINE_ONE = "l1";
	public static final String OBJ_LINE_TWO = "l2";
	
	public static InputStream load(String path){
		InputStream input = Util.class.getResourceAsStream(path);
		if (input == null){
			input = Util.class.getResourceAsStream("/"+path);
		}
		return input;
	}
	
}
