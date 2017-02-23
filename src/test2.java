import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import org.json.*;

public class test2 {
	public static void main(String[] args){
		String json = readFile("messages.json");

	    //System.out.println(json);
	    ArrayList<String> types = new ArrayList<String>();
	    JSONObject object = null;
		try {
			object = new JSONObject(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    String x = null;
		try {
			x = object.getString("type");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    System.out.println(x);
	    JSONObject param = null;
		try {
			param = new JSONObject(json).getJSONObject("parameters");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    String y = null;
		try {
			y = param.getString("myAlias");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    String z = null;
		try {
			z = param.getString("myPort");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
