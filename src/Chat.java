import java.net.*;
import java.io.*;
import java.util.*;

import org.json.*;

import java.util.concurrent.locks.*;
/*****************************//**
* \brief This program implements a distributed chat. 
* It creates a ring and delivers messages
* using flooding 
* \author	Mingtau Li, 011110539
* \author	Mark Spencer Tan, 012192282
* \author	Kevin Duong, 011715000
**********************************/
public class Chat{

	/// process alias
	public static String myAlias = null;
	/// process port
	public static int myPort = 0;
	// process successor ip
	public static String ipSuccessor = null;
	/// process successor port
	public static int portSuccessor = 0;
	/// process predecessor ip
	public static String ipPredecessor = null;
	/// process predecessor port
	public static int portPredecessor = 0;
	/// IP information
	public static String localhost = "127.0.0.1";
	/// Sentinel for joining chatroom
	private static boolean joined = false;
	/// mutex lock
	private static final Lock mutex = new ReentrantLock(true);
		
	/*****************************//**
	* \class Server class "chat.java" 
	* \brief It implements the server
	**********************************/ 
	private static class Server implements Runnable{
		/// client socket
		private Socket clntSock = null;
		/// server socket
		private ServerSocket servSock = null;
		/// operation type
		String operation = null;
		/// message received
		JSONObject msg = null;

		/*****************************//**
		* \brief It initializes constructor for Server
		* \param a alias of server for unique identification
		* \param p server port 
		**********************************/   
		public Server(String a, int p) {
			myAlias = a;
			myPort = p;
		}
		
		/*****************************//**
		* \brief It allows the system to interact with the participants. 
		**********************************/   		
		public void run() {     
			try{
			//create the server socket
			servSock = new ServerSocket(myPort);
			while (true) {
				// Get client connections
				clntSock = servSock.accept(); 
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
						//join chat
						case "JOIN":	
							try{
								JSONObject acceptObj = JSONMessage("ACCEPT",localhost,portPredecessor);//write accept msg to my pred
								JSONObject newSuccessorObj = JSONMessage("NEWSUCCESSOR",localhost,((JSONObject)msg.get("parameters")).getInt("myPort")); //new predecessor

								//prints out who is trying to join
								String joinalias = ((JSONObject)msg.get("parameters")).getString("myAlias");
								System.out.println("\n" + joinalias + " joined the chat.");

								mutex.lock();
								//update predecessor
								portPredecessor = ((JSONObject)msg.get("parameters")).getInt("myPort");

								//updates successor as well if this is the first join in the room
								if(portSuccessor == myPort){
									portSuccessor = portPredecessor;
									joined = true;
								}
								mutex.unlock();

								//send to my pred (to client: my pred)
								oos.writeObject(acceptObj.toString());

								//close sockets
								clntSock.close();

								// open new socket and send message to predecessor for acceptance request
								clntSock = new Socket(localhost,((JSONObject)acceptObj.get("parameters")).getInt("portPred")); //send to my old predecessor
								oos = new ObjectOutputStream(clntSock.getOutputStream());
								oos.writeObject(newSuccessorObj.toString());

							}catch(IOException e){
								e.printStackTrace();
							}catch(JSONException e){
								e.printStackTrace();
							}

							break;

						//new successor
						case "NEWSUCCESSOR":	
							try{
								mutex.lock();
								// connect to new successor
								portSuccessor = ((JSONObject)msg.get("parameters")).getInt("portSuccessor");
								mutex.unlock();

							}catch(JSONException e){
								e.printStackTrace();
							}
							break;
						//send message
						case "PUT":		
							try {
								//sender information
								String message = ((JSONObject)msg.get("parameters")).getString("message");
								String receiverAlias = ((JSONObject)msg.get("parameters")).getString("aliasReceiver");
								String senderAlias = ((JSONObject)msg.get("parameters")).getString("aliasSender");

								//if sender is self, it client is not in chat
								if(senderAlias.equalsIgnoreCase(myAlias)) {
									System.out.println("\nClient has either been disconnected or cannot be found");
									System.out.println("\n"+myAlias + " What would you like to do? \n 1) LEAVE\n 2) SEND MESSAGE\n 3) DISPLAY PROFILE");
								//if I am the receiver, display message
								}else if(receiverAlias.equalsIgnoreCase(myAlias)) {
									System.out.println("\n--> "+senderAlias+": "+message);
									System.out.println("\n" + myAlias + " What would you like to do? \n 1) LEAVE\n 2) SEND MESSAGE\n 3) DISPLAY PROFILE");

								}else {
									//send messages to successor
									System.out.println("I am not" + receiverAlias);
									clntSock = new Socket(localhost,portSuccessor);
									oos = new ObjectOutputStream(clntSock.getOutputStream());
									oos.writeObject(msg.toString());
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
								
							break;
						//leave chat
						case "LEAVE":	
							try{
								mutex.lock();
								//connect to new predecessor
								portPredecessor = ((JSONObject)msg.get("parameters")).getInt("portPred");
								mutex.unlock();
								//close socket
								clntSock.close();

								//send new successor info to new predecessor
								clntSock = new Socket(localhost,portPredecessor); //send to my updated predecessor
								JSONObject newSuccessorObj = JSONMessage("NEWSUCCESSOR",localhost,myPort); //old predecessor's new successor is me
								oos = new ObjectOutputStream(clntSock.getOutputStream());
								oos.writeObject(newSuccessorObj.toString());

								Scanner scan = new Scanner(System.in);


							}catch(JSONException e){
								e.printStackTrace();
							}catch(Exception e){
								e.printStackTrace();
							}


							break;
						// default behavior for corrupted messages
						default:
							System.out.print("System has encountered a problem --> ");
							System.out.println("message"+msg);
							break;
					}
					oos.close();
					ois.close();
				}catch(NullPointerException e){
					e.printStackTrace();
					System.out.println("Server exception");
				}catch(ConnectException e){
					System.out.println("This port does not exist");
				}
				clntSock.close();
			}

			}catch(IOException e){
			e.printStackTrace();
			}catch(ClassNotFoundException e){
			e.printStackTrace();
			}
		}
	}

	/*****************************//**
	* \class Client class "chat.java" 
	* \brief It implements the client
	**********************************/ 
	private static class Client implements Runnable {       
	/// accepts user input
	private Scanner scan;
	/// stream writing
	private ObjectOutputStream oos = null;
	/// stream for reading
	private ObjectInputStream ois = null; 
	/// ip inet address
	private InetAddress ip = null;
	/// socket for connection
	private Socket socket = null;


	/*****************************//**
	* \brief It initializes constructor for Client
	* \param a alias of server for unique identification
	* \param p server port 
	**********************************/  
	public Client(String a, int p){
		myAlias = a;
		myPort  = p;
		scan  = new Scanner(System.in);
	}

	/*****************************//**
	* \brief It allows the user to interact with the system. 
	**********************************/    
	public void run(){
		while (true) {
		try {
			int useroption = validInt(scan ,"\n" + myAlias + " What would you like to do? \n"+(joined ? " 1) LEAVE\n" : " 1) JOIN\n")+" 2) SEND MESSAGE\n 3) DISPLAY PROFILE", 3);
			// Connect to the server socket
			ip = InetAddress.getByName(localhost);

			switch(useroption){
			case 1: //JOIN or LEAVE
				if(!joined){

					//JOIN
					int port = validInt(scan, "Enter the port you would like to join");

					try{

						socket = new Socket(ip,port);
						System.out.println(myAlias + " has connected to (remote socket address): "+socket.getRemoteSocketAddress());

						// Create streams
						oos = new ObjectOutputStream(socket.getOutputStream());
						ois = new ObjectInputStream(socket.getInputStream());

						//Create JSON Message. Attempt to join
						JSONObject joinObj = JSONMessage("JOIN",myAlias, myPort);
						oos.writeObject(joinObj.toString()); //send To_Join's port

						mutex.lock();
						//update successor
						portSuccessor = port;
						mutex.unlock();


						JSONObject acceptedMsg = new JSONObject(ois.readObject().toString()); //from server: accept (ip, portPred)
						mutex.lock();
						portPredecessor = ((JSONObject)acceptedMsg.get("parameters")).getInt("portPred");
						mutex.unlock();
						socket.close();

						joined = true; //if exception occurs before completion of join, joined stays false

						System.out.println("\n(PRED, SUCC): "+portPredecessor+", " +portSuccessor);

					}catch(ConnectException e){
						System.out.println("Can't connect to this port");
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{//if !joined
					//LEAVE

						// Leave the room

					try{
						//connect to my successor
						socket = new Socket(ip,portSuccessor);

						// Create streams
						oos = new ObjectOutputStream(socket.getOutputStream());
						ois = new ObjectInputStream(socket.getInputStream());

						//Create JSON Message. Attempt to leave
						JSONObject leaveObj = JSONMessage("LEAVE",localhost, portPredecessor);
						oos.writeObject(leaveObj.toString());

						//redirect predecessor and successor to myself
						mutex.lock();
						portSuccessor = myPort;
						portPredecessor = myPort;
						mutex.unlock();

						//socket.close();
						//oos.close();

						System.out.println(myAlias+" ("+myPort+") has left the conversation");

						joined = false; //upon successful leave, joined becomes false

					}catch(ConnectException e){
						System.out.println("Can't connect to this port");
					}catch(Exception e){
						e.printStackTrace();
						System.out.println("sumting wong");
					}

				}

				break;

			case 2: //SEND MESSAGE
				if(!joined){
					System.out.println("\nYou must join a port first");
				}else{
					//stops client from sending invalid messages
					boolean validMessage = true;
					//saves receiver alias
					String receiverAlias = null;
					scan.nextLine();

					//asks user for message. message is not valid if you send to yourself
					do{
						System.out.println("Who do you want to send a message to?");
						receiverAlias = scan.nextLine();
						if(receiverAlias.equalsIgnoreCase(myAlias)){
							System.out.println("\nYou cannot send messages to yourself!\n");
							validMessage = false;
						}else{
							validMessage = true;
						}
					}while(!validMessage);

					//asks user for message
					System.out.println("What do you want to say?");
					String message = scan.nextLine();

					//Create JSON Message for sending message
					JSONObject sendObj = JSONMessage("PUT",myAlias, myPort,receiverAlias, message);

					//send message to successor
					socket = new Socket(ip,portSuccessor);
					System.out.println("Socket connected...");
					oos = new ObjectOutputStream(socket.getOutputStream());
					oos.writeObject(sendObj.toString());
					System.out.println("Message sent");
				}
				break;

			case 3: //DISPLAY PROFILE

				//display alias, port, predecessor, and successsor
				System.out.println("\n" + myAlias.toUpperCase() +" at port: " +myPort);
				if(joined){
					System.out.println("Predecessor: "+portPredecessor+"  Successor: "+portSuccessor);
				}
				break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		}

	}
	}
	
	
	/*****************************//**
	* Starts the threads with the client and server:
	* \param myAlias unique identifier of the process
	* \param myPort where the server will listen
	**********************************/  
	public Chat(String myAlias, int myPort) {

		this.ipSuccessor = localhost;
		this.ipPredecessor = localhost;
		this.portSuccessor = myPort;
		this.portPredecessor = myPort;


		// Initialization of the peer
		Thread server = new Thread(new Server(myAlias, myPort));
		Thread client = new Thread(new Client(myAlias, myPort));
		// start threads
		server.start();
		client.start();
		try {
			client.join();
			server.join();
		} catch (InterruptedException e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws JSONException {

		//throws exception when no arguments are found
	    if (args.length < 2 ) {  
		throw new IllegalArgumentException("Parameter: <myAlias> <myPort>");
	}

	// start chat
	Chat chat = new Chat(args[0], Integer.parseInt(args[1]));

	}
	
	/*****************************//**
	* \brief Creates JSON Messages based on type
	* \param type message type
	* \param alias my alias
	* \param port my port
	* \param msgArgs vararg for additional information
	**********************************/ 
	public static JSONObject JSONMessage(String type, String alias, int port, String... msgArgs){
		JSONObject  obj = new JSONObject();
		JSONObject params = new JSONObject();
			try {
				obj.put("type",type);
				obj.put("parameters", params);

				if (type.equals("JOIN")){
					params.put("myAlias", alias);
					params.put("myPort", port);

				}else if (type.equals("ACCEPT") || type.equals("LEAVE")){
					params.put("ipPred", localhost);
					params.put("portPred", port);

				}else if(type.equals("NEWSUCCESSOR")){
					params.put("ipSuccessor", localhost);
					params.put("portSuccessor", port);

				}else if(type.equals("PUT")){
					params.put("aliasSender", alias);
					params.put("aliasReceiver", msgArgs[0]);
					params.put("message", msgArgs[1]);
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		return obj;
		}


	/*****************************//**
	* \brief Asks user for valid numerical input
	* \param in scanner object
	* \param message message displayed to user
	* \param limit optional limit on number length
	**********************************/ 
	public static int validInt(Scanner in, String message, int... limit){			
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

