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
public class Chat{

	// My info
	public static String myAlias = null;
	public static int myPort = 0;
	// Successor
	public static String ipSuccessor = null;
	public static int portSuccessor = 0;
	// Predecessor
	public static String ipPredecessor = null;
	public static int portPredecessor = 0;

	public static String localhost = "127.0.0.1";
		
	/*new class - Server===================================================================================================================*/

/*****************************//**
* \class Server class "chat.java" 
* \brief It implements the server
**********************************/ 
	private static class Server implements Runnable{
	private Socket clntSock = null;
	private ServerSocket servSock = null;
	private String alias = null;
	private int port =0;
	String operation = null;
	JSONObject msg = null;

	public Server(String a, int p) {
		alias = a;
		port = p;
	}
	
/*****************************//**
* \brief It allows the system to interact with the participants. 
**********************************/   
	public void run() {     
		try{
		//create the server socket
		servSock = new ServerSocket(port);
		System.out.println("Server listening on port "+servSock.getLocalPort());
		while (true) {
			clntSock = servSock.accept(); // Get client connections
			try{
				//Create io streams
				ObjectInputStream  ois = new ObjectInputStream(clntSock.getInputStream());
				ObjectOutputStream oos = new ObjectOutputStream(clntSock.getOutputStream());

				//read json object
				try{
					msg = new JSONObject(ois.readObject().toString());
					operation = msg.getString("type");
				}catch(JSONException e){
					e.printStackTrace();
				}

				//does operations based on json commands
				switch(operation){ 
					case "JOIN": //TODO 
						System.out.println("\njoin!!!\n");
						try{
							

							JSONObject acceptObj = JSONMessage("ACCEPT",localhost,portPredecessor);//write accept msg to my pred
							JSONObject newSuccessorObj = JSONMessage("NEWSUCCESSOR",localhost,portPredecessor); //new predecessor

							portPredecessor = ((JSONObject)msg.get("parameters")).getInt("myPort"); //replace my pred with To_Join

							oos.writeObject(acceptObj.toString()); //send to my pred (to client: my pred)

							clntSock = new Socket(localhost,((JSONObject)newSuccessorObj.get("parameters")).getInt("portSuccessor")); //send to my old predecessor
							oos = new ObjectOutputStream(clntSock.getOutputStream());
							oos.writeObject(newSuccessorObj.toString());
							clntSock.close();

						}catch(IOException e){
							e.printStackTrace();
							System.out.println("---------------------->IO Exception"); //delete me
						}catch(JSONException e){
							e.printStackTrace();
							System.out.println("---------------------->JSON Exception");
						}

						break;

					case "NEWSUCCESSOR":
						System.out.println("\nnewsuccessor!!!\n");
						try{
							portSuccessor = ((JSONObject)msg.get("parameters")).getInt("portSuccessor");

						}catch(JSONException e){
							e.printStackTrace();
						}
						break;
					default:
						System.out.print("System has encountered a problem --> ");
						System.out.println("message"+msg);
						break;
				}	
						

				// close streams
				oos.close();
				ois.close();

				clntSock.close();
			}catch(NullPointerException e){
				e.printStackTrace();
				System.out.println("Server exception");
			}catch(ConnectException e){
				System.out.println("This port does not exist");
			}
		}
		
		}catch(IOException e){
		e.printStackTrace();
		}catch(ClassNotFoundException e){
		e.printStackTrace();
		}
	}//end server run
	}//end Server class



/*new class - client ================================================================================================================*/
	
/*****************************//*
* \brief It implements the client
**********************************/
	//private class Client implements Runnable {       
	private static class Client implements Runnable {       
	
	private Scanner scan;
	
	private ObjectOutputStream oos = null;
	private ObjectInputStream ois = null; 

	private InetAddress ip = null;
	private Socket socket = null;

	private String alias = null;
	private int port = 0;

	public Client(String a, int p){
		alias = a;
		port  = p;
		scan  = new Scanner(System.in);
	}

	/*****************************//**
	* \brief It allows the user to interact with the system. 
	**********************************/    
	public void run(){
		while (true) {
		try {
			int useroption = validInt(scan ,"What would you like to do? \n 1) JOIN\n 2) LEAVE\n 3) SEND MESSAGE\n 4) VIEW NEIGHBORS",4);
			//scan.nextLine(); //consumes return char

			switch(useroption){
			case 1: //JOIN

				int port = validInt(scan, "Enter the port you would like to join");
				
				try{
					// Connect to the server socket
					ip = InetAddress.getByName(localhost);

					socket = new Socket(ip,port);
					System.out.println("Client connected to (remote socket address): "+socket.getRemoteSocketAddress());

					// Create streams
					oos = new ObjectOutputStream(socket.getOutputStream()); 
					ois = new ObjectInputStream(socket.getInputStream()); 

					//Create JSON Message. Attempt to join
					JSONObject joinObj = JSONMessage("JOIN",alias, myPort); 
					oos.writeObject(joinObj.toString()); //send To_Join's port 
					portSuccessor = port;	

					
					JSONObject acceptedMsg = new JSONObject(ois.readObject().toString()); //from server: accept (ip, portPred)
					// if(acceptedMsg.get("type").toString().equals("ACCEPT"))
					portPredecessor = ((JSONObject)acceptedMsg.get("parameters")).getInt("portPred");
					socket.close();

					System.out.println("\n(PRED, SUCC): "+portPredecessor+", " +portSuccessor);

				}catch(ConnectException e){
					System.out.println("Can't connect to this port");
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("sumting wong");
				}

				

				break;

			case 2: //LEAVE
				// Leave the room

				//Create JSON Message
				//TODO implement leave function
/*				JSONObject joinObj = JSONMessage("LEAVE",alias, port);
				oos.writeObject(joinObj.toString());*/

				break;  

			case 3: //SEND MESSAGE
				// send a message

				//Create JSON Message
				//TODO capture input for sender Alias, Receiver alias, and message
				/*JSONObject joinObj = JSONMessage("LEAVE",aliasSender, aliasReiver, message);
				oos.writeObject(joinObj.toString());*/

				break;

			case 4: //VIEW NEIGHBORS
				System.out.println("Predecessor: "+portPredecessor+"  Successor: "+portSuccessor);
				break;
			}

			
			//socket.close();
			ois.close();
			oos.close();

		} catch (IOException e) {
			e.printStackTrace();
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

	this.ipSuccessor = localhost;
	this.ipPredecessor = localhost;
	this.portSuccessor = myPort;
	this.portPredecessor = myPort;


	// Initialization of the peer
	Thread server = new Thread(new Server(this.myAlias, this.myPort));
	Thread client = new Thread(new Client(this.myAlias, this.myPort));
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

	      if (args.length < 2 ) {  
		throw new IllegalArgumentException("Parameter: <myAlias> <myPort>");
	}

	Chat chat = new Chat(args[0], Integer.parseInt(args[1]));


	}
	
	// Creates a JSON Message 
	public static JSONObject JSONMessage(String type, String alias, int port, String... msgArgs){
		JSONObject  obj = new JSONObject();
		JSONObject params = new JSONObject();
				try {
					obj.put("type",type);
					obj.put("parameters", params);
				} catch (JSONException e1) {
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
				params.put("ipPred", localhost);
				params.put("portPred", port);
			}catch(JSONException e){
				e.printStackTrace();
			}
		}else if(type.equals("NEWSUCCESSOR")){
			try{
				params.put("ipSuccessor", localhost);
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


		//keeps asking user for valid integer
		public static int validInt(Scanner in, String message, int... limit){
			// Scanner in = new Scanner(System.in);
			int n = 0;
			while(true){
				System.out.println(message + ": ");
				
				try{
					n = in.nextInt();
						if (limit.length > 0 && n > limit[0]){
						throw new NumberFormatException("number too large");
					}else{
						break;
					}
				}catch(InputMismatchException e){
					System.out.println("not a valid input. Try again");
					in.next(); //consumes return char
				}catch(NumberFormatException e){
					System.out.println("not a valid input. Try again");
				}
			}
			return n;
		}
}





// mutex example

/*private final Lock _mutex = new ReentrantLock(true);

_mutex.lock();

// your protected code here

_mutex.unlock();*/

