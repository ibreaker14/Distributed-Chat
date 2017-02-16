import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import org.json.*;

public class test2 {
	public static void main(String[] args) throws JSONException{
		String json = readFile("messages.json");

	    //System.out.println(json);
	    ArrayList<String> types = new ArrayList<String>();
	    JSONObject object = new JSONObject(json);
	    String x = object.getString("type");
	    System.out.println(x);
	    JSONObject param = new JSONObject(json).getJSONObject("parameters");
	    String y = param.getString("myAlias");
	    String z = param.getString("myPort");

	    System.out.println(y);
	    System.out.println(z);
	    
	    
	}
	public static String readFile(String filename) {
	    String result = "";
	    try {
	        BufferedReader br = new BufferedReader(new FileReader(filename));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            line = br.readLine();
	        }
	        result = sb.toString();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}
}
