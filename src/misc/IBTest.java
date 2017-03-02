import java.lang.invoke.SwitchPoint;
import java.util.*;

import org.json.*;

public class IBTest{
	public static void main(String args[]){
		System.out.println("hihi");


		JSONObject  obj = new JSONObject();
		JSONObject obj2 = new JSONObject();
  		try{
			obj.put("First Tier","YES");
			obj.put("Second Tier", obj2);

			obj2.put("myAlias", "A");
			obj2.put("myPort", "666");

		}catch(JSONException e){
			e.printStackTrace();
		}

		try {
//			System.out.println(((JSONObject) obj.get("First Tier")).get("myAlias"));
			/*String objStr = obj.getString("First Tier");
			System.out.println("objStr: "+objStr);
			System.out.println(((JSONObject) obj.get("Second Tier")));*/
			String strJson = obj.toString();

			JSONObject newObj = new JSONObject(strJson);
			System.out.println(newObj.get("First Tier"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Scanner in = new Scanner(System.in);
		
		/*while(true){
			System.out.println("enter: ");
			String type = in.nextLine();
			JSONObject obj = new JSONObject();
			if(type.equals("PUT")){
				String msg1 = in.nextLine();
				String msg2 = in.nextLine();
				obj = JSONMessage(type,"alias",555,msg1,msg2);
			}else{
				obj = JSONMessage(type,"alias",555);
			}
			System.out.println("Object: "+obj);
		}*/
		
		/*while(true){
			System.out.println("enter number");
			int n = 0;
			try{
				n = in.nextInt();
				if(n > 5){
					throw new InputMismatchException("too big");
				}else{
					break;
				}
			}catch(InputMismatchException e){
				System.out.println("not a number");
				in.next();
			}
		}*/
		
		
		
	}
	
	public static JSONObject JSONMessage(String type, String alias, int port, String... msgArgs){
		JSONObject  obj = new JSONObject();
		JSONObject params = new JSONObject();
				try {
					obj.put("type",type);
					obj.put("parameters", params);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				

		if (type.equals("JOIN")){
			try{
				params.put("myAlias", alias);
				params.put("myPort", port);
			}catch(JSONException e){
				e.printStackTrace();
			}
		}else if (type.equals("ACCEPT") || type.equals("LEAVE")){
			try{
				params.put("ipPred", alias);
				params.put("portPred", port);
			}catch(JSONException e){
				e.printStackTrace();
			}
		}else if(type.equals("NEWSUCCESSOR")){
			try{
				params.put("ipSuccessor", alias);
				params.put("portSuccessor", port);
			}catch(JSONException e){
				e.printStackTrace();
			}
		}else if(type.equals("PUT")){
			try{
				params.put("aliasSender", alias);
				params.put("aliasReceiver", msgArgs[0]);
				params.put("message", msgArgs[1]);
			}catch(JSONException e){
				e.printStackTrace();
			}
		}
		return obj;
		}

}