import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

//Alec Ibarra
public class Server {

	static final int PORT =  7777;
	static ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
    
	public static void main(String[] args) throws Exception 
	{
		System.out.println("The server is running on port: "+PORT);
		ServerSocket listener = new ServerSocket(PORT);
		try
		{
        	while (true) 
        	{
        		new Handler(listener.accept()).start();
        		delay(100);
        	}
		} 
		finally 
		{
			listener.close();
		}
	}
	
	private static class OutputHandler extends Thread
	{
		private Socket socket;
		private PrintWriter out;
		
		public OutputHandler(Socket socket)
		{
			this.socket = socket;
		}
		
		public void run()
		{
            try
            {
            	while(!socket.isClosed())
            	{
            		out = new PrintWriter(socket.getOutputStream(),true);
            		
            		//Process output (only send if value is different)
            		{
            			/*for(int k = 0; k < clients.size(); k++)
            			{
            				out.println(clients.get(k).id+"xpos"+clients.get(k).xpos);
            				out.println(clients.get(k).id+"ypos"+clients.get(k).ypos);
            				out.println(clients.get(k).id+"rota"+clients.get(k).rotation);
            				out.println(clients.get(k).id+"heal"+clients.get(k).health);
               			out.println(clients.get(k).id+"poin"+clients.get(k).points);
            			}*/
            		}
            		delay(15);//64 tps (same as client)
            	}
            } catch(IOException e){}
		}
	}

	private static class Handler extends Thread 
	{
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		private String input;
		private String name;

        public Handler(Socket socket) 
        {
            this.socket = socket;
        }
        
        public void run() 
        {
        	try
        	{
        		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        		out = new PrintWriter(socket.getOutputStream(),true);
        		input = in.readLine();
        		writers.add(out);
       		out.println("ServerUTTT");//Verification that this is a game server
        		
       			if(input.contains("ping"))
       			{
       				System.out.println("pinged");
       				socket.close();
       				return;
       			}
       			else if(input.contains("name"))
        		{
        			name = input.substring(4);
        			System.out.println(name+" has joined the game.");
        		}
        		new OutputHandler(socket).start();

            	while(!socket.isClosed())
                {
            		while((input = in.readLine()) != null)
                	{
                		input = input.toLowerCase();
                		//System.out.println(":IN:"+input);
                		
                		//Process input
                		{
                			if(input.contains("xpos"))
                			{
                				//clients.get(clientNum).xpos = Float.parseFloat(input.substring(4));
                			}
                			else if(input.contains("disconnect"))
                       	{
                       			socket.close();
                       	}
                		}
                }
                }
            }
            catch (IOException e){}
        	finally
        	{
        		try
            	{
				socket.close();
			} catch (IOException e) {}
        		writers.remove(out);
        		System.out.println(name+" has left the game.");
        	}	
        }
    }

	public static void delay(long n)
	{
		try 
		{
			Thread.sleep(n);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}