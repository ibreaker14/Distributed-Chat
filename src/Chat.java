import java.net.*;
import java.io.*;
import java.util.*;

import org.json.*;

import java.util.concurrent.locks.*;
/*****************************//**
* \brief It implements a distributed chat. 
* It creates a ring and delivers messages
* using flooding 
**********************************/
public class Chat {

/*
   Json Messages:
 
  {
        "type" :  "JOIN",
        "parameters" :
               {   
                    "myAlias" : string,
                    "myPort"  : number
               }
   }
 
   {
        "type" :  "ACCEPT",
        "parameters" :
               {   
                   "ipPred"    : string,
                   "portPred"  : number
               }
    }
 
    {
         "type" :  "LEAVE",
         "parameters" :
         {
             "ipPred"    : string,
             "portPred"  : number
         }
    }

   {
         "type" :  "Put",
        "parameters" :
         {
             "aliasSender"    : string,
             "aliasReceiver"  : string,
             "message"        : string
        }
   }
 
 {
        "type" :  "NEWSUCCESSOR",
        "parameters" :
        {
            "ipSuccessor"    : string,
            "portSuccessor"  : number
        }
 }
 */

// My info
	public static String myAlias;
	public static int myPort;
// Successor
	public static String ipSuccessor;
	public static int portSuccessor;
// Predecessor
	public static String ipPredecessor;
	public static int portPredecessor;
    
 
  
  /*new class - Server===================================================================================================================*/

/*****************************//**
* \class Server class "chat.java" 
* \brief It implements the server
**********************************/ 
	private static class Server implements Runnable {
		public Server() {}
    
/*****************************//**
* \brief It allows the system to interact with the participants. 
**********************************/   
		public void run() {
			Socket clntSock = null;
			ServerSocket servSock = null;
			try {
				//servSock = new ServerSocket(myPort);
				servSock = new ServerSocket(6666);//test
				while (true) {
					System.out.println("Server running on port "+servSock.getLocalPort());
					try{
						clntSock = servSock.accept(); // Get client connections
					}catch(IOException e){
						e.printStackTrace();
					}
					//Create a new thread to handle the connection
					System.out.println("Server connected to port "+clntSock.getRemoteSocketAddress());

					ObjectInputStream  ois = new ObjectInputStream(clntSock.getInputStream());
					ObjectOutputStream oos = new ObjectOutputStream(clntSock.getOutputStream());

					//String m = "I am a message";
					//ois.read();    //reads the message using JsonParser and handle the messages
					try{
					System.out.println("Message from client: "+ois.readObject().toString());
					}catch(Exception e){}
					//oos.write(m);   //TODO only if the message requires a response
					oos.writeObject("hihhhii");   //test message
				/*	oos.close();
					ois.close();*/
				}
			}catch (IOException e) {
					// TODO Handle the exception
				e.printStackTrace();
			}finally{
				try{
					clntSock.close();
				}catch(IOException e){
					e.printStackTrace();
				}	
			}
		}//end server run
	}//end Server class



/*new class - client ================================================================================================================*/
    
/*****************************//*
* \brief It implements the client
**********************************/
  //private class Client implements Runnable {       
 	private static class Client implements Runnable {       
    
    public Client(){}

/*****************************//**
* \brief It allows the user to interact with the system. 
**********************************/    
    public void run(){
      while (true) {

          // Create a simple user interface
        
         /* TODO  The first thing to do is to join
             ask the ip and port when joining and set ipSuccessor = ip, portSuccessor = port
          Socket socket = new Socket(ipSuccessor, portSuccessor);*/
          
          // Create the mssages m using JsonWriter and send it as stream
          	
    	  InetAddress ip = null;
    	  Socket socket = null;
		try {
			ip = InetAddress.getByName("127.0.0.1");
			socket = new Socket(ip,6666);
			System.out.println("Client Running on port " +socket.getLocalPort()+" and connected to server: "+socket.getRemoteSocketAddress());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//temp
    	  //temp
    	  
    	  //Socket socket = new Socket(ipSuccessor,portSuccessor);
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null; 
          try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //  System.out.println("blah blah blah");//delete me


          try{
          //oos.write(m);  //TODO this sends the message (json stream)
          JSONObject  obj = new JSONObject();
          obj.put("type","JOIN");
          Map<String, Object> t = new HashMap<String, Object>();
          t.put("myAlias", socket.getRemoteSocketAddress());
          t.put("myPort",socket.getLocalPort());
          obj.put("parameters",t);

          oos.writeObject(obj.toString());
          System.out.println("server message: " + ois.readObject().toString());   // reads the response and parse it using JsonParser
          socket.close();
          Scanner s = new Scanner(System.in);
          int i = s.nextInt();
         /* ois.close();
          oos.close();*/
          }catch(Exception e){
        	  e.printStackTrace();
		  }
         
          }
          /* TODO Use mutex to handle race condition when reading and writing the global variable (ipSuccessor, 
                portSuccessor, ipPredecessor, portPredecessor)*/
           
      }
    }
  
  
/*****************************//**
* Starts the threads with the client and server:
* \param Id unique identifier of the process
* \param port where the server will listen
**********************************/  
  public Chat(String myAlias, int myPort) {
     
      this.myAlias = myAlias;
      this.myPort = myPort;
      // Initialization of the peer
      Thread server = new Thread(new Server());
      Thread client = new Thread(new Client());
      server.start();
      client.start();
      try {
          client.join();
          server.join();
      } catch (InterruptedException e)
      {
            // Handle Exception
      
      }
  }
  
  public static void main(String[] args) throws JSONException {
      
/*      if (args.length < 2 ) {  
          throw new IllegalArgumentException("Parameter: <myAlias> <myPort>");
      }
      
      Chat chat = new Chat(args[0], Integer.parseInt(args[1]));
      */
      Chat chat = new Chat("127.0.0.1",555);


      // https://www.codevoila.com/post/65/java-json-tutorial-and-example-json-java-orgjson
      /*String json = "";
      ArrayList<String> types = new ArrayList<String>();
      JSONArray jArray = (JSONArray) new JSONTokener(json).nextValue(); //creates JSON OBJECT array
      for(int x = 0; x < jArray.length(); x++) {
    	  JSONObject object = jArray.getJSONObject(x);
    	  types.add(object.getString("type"));
      }*/
      
  }
}



// mutex example

/*private final Lock _mutex = new ReentrantLock(true);

_mutex.lock();

// your protected code here

_mutex.unlock();*/

