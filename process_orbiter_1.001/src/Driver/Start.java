/**
 * @author Solomon Sonya
 */

package Driver;

import java.io.*;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import Worker.*;
import Sensor.*;


public class Start extends Thread implements Runnable
{
	public static final String myClassName = "Start";
	public static volatile Driver driver = new Driver();
	
	public static volatile BufferedReader brIn = null;
	
	public static volatile String sensor_name = Driver.NAME + "_" + System.currentTimeMillis();
	
	
	public static volatile String arg = "";
	public static volatile String specific_interface = null;
	public volatile ThdServerSocket svrskt = null;
	public static volatile int specific_SENSOR_port = ThdServerSocket.DEFAULT_PORT;
	public static volatile ThdWorker worker = new ThdWorker();
	public static volatile StandardInListener std_in = null;
	
	public static volatile int interrupt_millis = 2000;
	
	public static volatile String ENCRYPTION_KEY = null;
	public static volatile int PORT = 8090;
	
	public static volatile String OUTBOUND_ADDRESS  = null;
	public static volatile int OUTBOUND_PORT  = 0;
	

	public static String [] args = null;
	
	public Start(String [] argv)
	{
		try
		{
			args = argv;
			initialize();			
		}
		catch(Exception e)
		{
			
		}
	}
	
	
	public boolean initialize()
	{
		try
		{
			//establish the bufferedreader on the standard_in. 
			//procure arguments from user
			//when finished, transfer control to StandardInListener to handle remainder of user input
									
			//notify user
			driver.directivesp("\nWelcome to " + Driver.FULL_NAME + " by Solomon Sonya @Carpenter1010\n\n");
			
			//launch bufferedreader
			String line = "";
			brIn = new BufferedReader(new InputStreamReader(System.in));
			
			StandardInListener.display_help();
			
			this.analyze_input(args);
			
			//store the PID of this program in the list of PIDs to ignore if applicable
			if(driver.PID != null && !driver.PID.trim().equals(""))
			{
				//Node_Process.list_PID_TO_IGNORE.add(driver.PID);
			}
			
			//Add specific executable names to ignore (do this bcs some processes will spawn wmic.exe, cmd.exe, netstat.exe, and tasklist.exe multiple times, even though this is from us, it may clutter the list of process trees.
			//I might come back and fix this later... we'll see
			Node_Process.list_PROCESS_NAMES_TO_IGNORE.add("netstat.exe");
			Node_Process.list_PROCESS_NAMES_TO_IGNORE.add("NETSTAT.EXE");
			
			Node_Process.list_PROCESS_NAMES_TO_IGNORE.add("TASKLIST.EXE");
			Node_Process.list_PROCESS_NAMES_TO_IGNORE.add("tasklist.exe");
			
			Node_Process.list_PROCESS_NAMES_TO_IGNORE.add("WMIC.EXE");
			Node_Process.list_PROCESS_NAMES_TO_IGNORE.add("wmic.exe");
			
			this.start();
			
			std_in = new StandardInListener(brIn);
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "initialize", e);
		}
		
		return false;
	}
	
	
	public void run()
	{
		try
		{
			//start program
			driver.directive("\nConfiguration parameters:\n=========================");
			
			if(this.ENCRYPTION_KEY == null)
				driver.directive("Encryption key: // NOT SET //");
			else
				driver.directive("Encryption key: " + this.ENCRYPTION_KEY);
			
			driver.directive("ServerSocket Port: " + this.PORT);
			
			if(this.OUTBOUND_ADDRESS != null)
				driver.directive("Outbound socket connection: " + this.OUTBOUND_ADDRESS + " : " + this.OUTBOUND_PORT);
			
			driver.directive("Interrupt [milliseconds]: " + this.interrupt_millis);
			
			//establish serversocket
			ThdServerSocket svr_skt = new ThdServerSocket(PORT);
			
			//worker thread
			worker.tmr_process_2_sec = new Timer(interrupt_millis, worker);
			worker.tmr_process_2_sec.start();
			
			worker.tmr_process_3_sec = new Timer(1000*3, worker);
			worker.tmr_process_3_sec.start();
			
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "run", e);
		}
	}
	
	
	
	
	public boolean analyze_input(String [] args)
	{
		try
		{		
			if(args == null || args.length < 1)
				return false;
			
			//note: expecting in format: <-cmd> <params> e.g. <-e> solomonsonya --> to set encryption key to solomonsonya
			//if even one is missing, then it will throw off entire analysis
			
			int i = 0; 
			String cmd = "";
			String lower = "";
			String param = "";
			while(i < args.length)
			{
				try
				{
					cmd = args[i++];
					
					if(cmd == null || cmd.trim().equals(""))
						continue;
					
					lower = cmd.toLowerCase().trim();
					
					//
					//-e
					//
					if(lower.startsWith("-e"))
						this.ENCRYPTION_KEY = args[i++].trim();
					
					//
					//-p
					//
					else if(lower.startsWith("-p"))
					{
						try
						{
							param = args[i++].trim();
							int port = Integer.parseInt(param);
							
							//sucess, set the value
							this.PORT = port;
						}
						catch(Exception e)
						{
							driver.directive("Invalid port specification on value [" + param + "]");
						}
					}
					
					//
					//-a
					//
					else if(lower.startsWith("-a"))
					{
						try
						{
							String addr = args[i++];
							int port = Integer.parseInt(args[i++]); 
							
							OUTBOUND_ADDRESS = addr.trim();
							OUTBOUND_PORT = port;
						}
						catch(Exception e)
						{
							driver.directive("Error! Outbound address invalid! Expecting <Address> <port>");
						}									
					}
					
					//
					//-int
					//
					else if(lower.startsWith("-int"))
					{
						try
						{
							param = args[i++].trim();
							int millis = Integer.parseInt(param);
							
							//sucess, set the value
							this.interrupt_millis = millis;
							
							if(interrupt_millis < 1)
								interrupt_millis = 2000;
						}
						catch(Exception e)
						{
							driver.directive("Invalid interrupt millisecond specification on value [" + param + "]");
						}						
						
					}
					
					else
					{
						driver.directive("ERROR! Invalid command received... [" + args[i] + "]");
						++i;
					}
						
				}
				catch(Exception e)
				{
					
				}
				
			}//end while
			
						
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "analyze_input", e);			
		}
		
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

