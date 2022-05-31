import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JOptionPane;

/**
 * The Client class manages the client-side connection to a server.
 * This class is capable to scanning the local network for a gameserver.
 * It is also capable of connecting to online games.
 * @author Alec Ibarra
 */
public class Client {
	
	private static int id;
	private static String input;
	private static int port = 7777;
	private static PrintWriter out;
	private static BufferedReader in;
	private static boolean started = false;
	private static boolean scanning = false;
	private static boolean scanningEnabled = true;
	private static String serverAddress = "";
	private static Socket socket = new Socket();
	private static ArrayList<Integer> ids = new ArrayList<Integer>();
	private static ArrayList<String> queue = new ArrayList<String>();
	private static ArrayList<String> localServers = new ArrayList<String>();
		
	/**
	 * Prepare the Client Class for connection
	 */
	public static void prepare()
	{
		try
        {
			socket.close();
		} catch (IOException e) {}
	}
	
	/**
	 * Scan the local network for a gameserver
	 */
	public static void scan()
	{
		if(!scanning && scanningEnabled && Logic.multiplayer)
		{
			scanning = true;
			for(int k = 0; k < 256; k++)//about (256*100)/1000 = 25.6 seconds to run
			{
				new FindHost().setNum(k);
			}
		}
	}
	
	/**
	 * Start up the Client
	 */
	public static void start()
	{
		if(socket.isClosed() && !started && Logic.multiplayer)//if client is not running, return true if started else false
		{	
			started = true;
			//scan();
			serverAddress = JOptionPane.showInputDialog("Enter Server IP");
			new Handler().start();
		}
	}
	
	/**
	 * Stops the Client
	 */
	public static void stopClient()
	{
		try
		{
			if(!socket.isClosed())
			{
				out.println(id+"disconnect");
				socket.close();
				ids.clear();
				queue.clear();
				localServers.clear();
				System.out.println("Disconnected from: "+socket.getInetAddress());
				serverAddress = "";
				started = false;
				id = -1;
			}
		} catch (IOException e){}
	}
	
	/**
	 * Used in the Client Class in the scan method. This is used to enable multithreading
	 * when searching for a gameserver, resulting in quicker scans. If the computer running
	 * the game is found to be offline, offlineMode will be automatically enabled.
	 */
	private static class FindHost extends Thread
	{
		private int k;
		
		public void run()
		{		
			for(int j = 0; j < 256; j++)
			{
				if(!Logic.multiplayer)
				{
					return;
				}
				else try
				{
					String ip = "";
					String localIP = InetAddress.getLocalHost().toString().split("/")[1];
					String[] splitIP = localIP.split("\\.");
					try
					{
						ip = splitIP[0]+"."+splitIP[1]+"."+k+"."+j;
					} catch(IndexOutOfBoundsException e)
					{
						//triggers if not connected to internet
						Logic.multiplayer = false;
						return;
					}
				
					Socket s = new Socket();
					s.connect(new InetSocketAddress(ip,port),100);
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					new PrintWriter(s.getOutputStream()).println("ping");
					if(!in.readLine().equals("ServerUTTT"))//Check if this is a server for this game
					{
						System.out.println(socket.getInetAddress()+" is not a gameserver.");
					}
					else
					{
						localServers.add(ip);
					}
					s.close();
										
					if(k == 255 && j == 255)//last ping just finished
					{
						scanning = false;
					}
					
				} catch (IOException e) {}	
			}
		}
		
		public void setNum(int k)
		{
			this.k = k;
			this.start();
		}
	}
	
	/**
	 * Used in the Client Class. Handles all of the actual data processing for input.
	 */
	private static class Handler extends Thread
	{	
		public void run()
		{
			try
			{					
				socket = new Socket(serverAddress,port);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(),true);
				out.println("name"+Logic.clientName);
				if(!in.readLine().equals("ServerUTTT"))//Check if this is a server for this game
				{
					System.out.println(socket.getInetAddress()+" is not a gameserver.");
					socket.close();	
				}
				else
				{
					System.out.println("Connecting to server: "+socket.getInetAddress());
					new OutputHandler(socket,out).start();
				}
				
			} catch (IOException e){
				System.out.println("Failed to connect, please retry.");
				Logic.multiplayer = false;
				return;
			}
			
			try{
				while(!socket.isClosed())
				{	
					while((input = in.readLine()) != null)
					{
						input = input.toLowerCase();
						//System.out.println(":IN:"+input);
						
						input = input.substring(4);
						
						//Process input
						{
							if(input.contains("xpos"))
							{
								//Tracker.players.get(idNum).moveTo(Float.parseFloat(input.substring(4)),-1,3f);
							}
						}
					}
				}
				
			} catch (IOException e){}	
		}
	}
	
	/**
	 * Used in Client Class. Handles all of the data processing for output.
	 */
	private static class OutputHandler extends Thread
	{
		private Socket socket;
		private PrintWriter out;
		
		public OutputHandler(Socket socket, PrintWriter out)
		{
			this.socket = socket;
			this.out = out;
		}
		
		public void run()
		{
            while(!socket.isClosed())
            {		            
				//Process output (only send if value is different)
				{
					//out.println(id+"xpos"+Logic.player.getXPos());
				}
           		Logic.delay(15);//64 tps (same as server)
           	}
		}
	}
	
	public static void setServerAddress(String serverAddress) {
		Client.serverAddress = serverAddress;
	}
	
	public static String getServerAddress() {
		return serverAddress;
	}

	public static ArrayList<String> getLocalServers() {
		return localServers;
	}

	public static ArrayList<String> getQueue() {
		return queue;
	}
}
