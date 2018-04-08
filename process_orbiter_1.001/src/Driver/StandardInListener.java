/**
 * @author Solomon Sonya
 */

package Driver;

import java.io.*;
import java.net.Socket;

import Encryption.Encryption;
import Sensor.ThdServerSocket;
import Sensor.ThdSocket;

public class StandardInListener extends Thread implements Runnable
{
	public static final String myClassName = "StandardInListener";
	public static volatile Driver driver = new Driver();
	
	//public static Process_Solomon exec = new Process_Solomon(0);
	
	public volatile BufferedReader brIn = null;
	
	public static volatile String lower = "";
	
	public static final String space = "\n\t\t\t";
	
	public static final String [] arrHelp = new String[]
	{
		"h:\t\t\tDisplay Help options",
		"v:\t\t\tToggle Verbosity",
		"export\t\t\tExport aggregate to running location",
		//"dump /format:pid\tDump aggregate info in process format",
		//"dump /format:net\tDump aggregate info in network format",
		"encryption\t\tSet encryption key",
		"listen\t\t\tSpecify new port to establish ServerSocket",
		"connect\t\t\tSpecify <address> <port> to establish outbound " + space + "connection",
	};
	
	public StandardInListener(BufferedReader br)
	{
		try
		{			
			brIn = br;
			this.start();			
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "Constructor - 1", e);
		}
		
	}
	
	public StandardInListener()
	{
		try
		{			
			brIn = new BufferedReader(new InputStreamReader (System.in));
			this.start();			
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "Constructor - 1", e);
		}
		
	}
	
	public void run()
	{
		try
		{
			brIn = new BufferedReader(new InputStreamReader(System.in));
			
			//this.display_help();
			
			String line = "";
			while((line = brIn.readLine()) != null)
			{
				determine_command(line);
			}
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "run", e);
		}
	}
	
	public static boolean display_help()
	{
		try
		{
			driver.directive("\nHelp Options\n=============");
			for(String s : arrHelp)
			{
				driver.directive(s);
			}
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "display_help", e);
		}
		
		return false;
	}
	
	public boolean display_status()
	{
		try
		{
			driver.directive("\n /////////// STATUS ////////////");
			driver.directive(driver.FULL_NAME);
			
			driver.directive("");
			driver.directive("Time of First Start: " + driver.TIME_OF_FIRST_START);
			
			if(Start.ENCRYPTION_KEY == null)
				driver.directive("Encryption Key --> " + "//NOT SET//");
			else
				driver.directive("Encryption Key --> " + Start.ENCRYPTION_KEY);
			
			driver.directive("Sensor Name --> " + Start.sensor_name);
			
			driver.directive("Verbose is enabled: " + driver.output_enabled);
			
			if(driver.PID != null && !driver.PID.trim().equals(""))
			{
				driver.directive("PID: " + driver.PID);
				driver.directive("HOST NAME: " + driver.HOST_NAME);
			}
			
			if((ThdServerSocket.list_server_sockets == null || ThdServerSocket.list_server_sockets.isEmpty()))
			{
				driver.directive("No server sockets instantiated yet!");
			}
			else
			{
				for(ThdServerSocket svrskt : ThdServerSocket.list_server_sockets)
				{
					driver.directive("Sensor ServerSocket --> " + svrskt.get_status());
				}
				
				
			}
			
						
			driver.directive("");			
			driver.directive("Heap Size: " + Runtime.getRuntime().totalMemory()/1e6 + "(MB) Max Heap Size: " + Runtime.getRuntime().maxMemory()/1e6 + "(MB) Free Heap Size: " + Runtime.getRuntime().freeMemory()/1e6 + "(MB) Consumed Heap Size: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1e6 + "(MB)");
			driver.directive("");	
			
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "display_status", e);
		}
		
		return false;
	}
	
	public boolean toggle_verbose()
	{
		try
		{
			driver.output_enabled = !Driver.output_enabled;
			
			if(driver.output_enabled)
				driver.directive("Verbose is enabled.");
			else 
				driver.directive("Verbose is disabled.");
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "toggle_verbose", e);
		}
		
		return false;
	}
	
	public boolean connect(String location)
	{
		try
		{
			if(location == null || location.trim().equals(""))
			{
				driver.directive("ERROR! It appears you are missing location parameters for the connect command! Please try again!");
				return false;
			}
			
			location = location.trim();
			
			
			String array [] = null;
			
			if(location.contains(":"))
				array = location.split(":");
			else if(location.contains(","))
				array = location.split(",");
			else 
				array = location.split(" ");
			
			String address = array[0].trim();
			int port = Integer.parseInt(array[1].trim());
			
			if(address.equalsIgnoreCase("localhost") || address.equalsIgnoreCase("local host") || address.equalsIgnoreCase("-localhost") || address.equalsIgnoreCase("-local host"))
				address = "127.0.0.1";
			
			//Connect
			driver.directive("Attempting to connect out to --> " + address + " : " + port);
			
			try
			{
				Socket skt = new Socket(address, port);
				
				ThdSocket thd = new ThdSocket(null, skt);
			}
			catch(Exception ee)
			{
				driver.directive("ERROR! I was unable to establish a connection to PARSER at --> " + address + " : " + port);
			}
			
			return true;
		}
		catch(Exception e)
		{
			driver.directive("ERROR! I was expecting command: parser_connect <ip address> <port>\nPlease try again...");
		}
		
		return false;
	}
	
	public boolean establish_server_socket(String port)
	{
		try
		{
			int PORT = Integer.parseInt(port.trim());
			
			if(PORT < 0)
			{
				throw new Exception("PORT number must be greater than 0!");
			}
			
			ThdServerSocket svrskt = new ThdServerSocket(PORT);
			
			return true;
		}
		catch(Exception e)
		{
			driver.directive("ERROR! Invalid port received. Please run command again and specify valid listen port!");
		}
		
		return false;
	}
	
	
	
	public static boolean set_encryption(String key)
	{
		boolean previous_output_state = driver.output_enabled;
		
		try
		{
			//disable output
			driver.output_enabled = false;
			
			if(key == null || key.trim().equals(""))
			{
				driver.directive("\nENCRYPTION HAS BEEN DISABLED!");	
				
				Start.ENCRYPTION_KEY = null;
				
				//disable for all clients
				for(ThdSocket skt : ThdSocket.ALL_CONNECTIONS)
				{
					skt.encryption = null;
				}
				
			}
			
			else if(key != null && key.trim().equalsIgnoreCase("null"))
			{
				driver.directive("\n\nNOTE: your [null] parameter is a reserved word with this encryption command specifying to disable encryption");
				
				driver.directive("ENCRYPTION HAS BEEN DISABLED!");	
				
				Start.ENCRYPTION_KEY = null;
				
				
			}
			
			else if(key != null)
			{
				key = key.trim();
				
				Start.ENCRYPTION_KEY = key;
				
				driver.directive("Encryption key has been set to [" + key + "]");
				
				//loop through connected clients and grab the new encryption
				for(ThdSocket skt : ThdSocket.ALL_CONNECTIONS)
				{
					skt.encryption = new Encryption(Start.ENCRYPTION_KEY);
				}
			}
			
			
			//set the encryption keys!
			
					
			
			
			driver.output_enabled = previous_output_state;
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "set_encryption", e);
		}
		
		
		driver.output_enabled = previous_output_state;
		
		return true;
	}
	
	public boolean determine_command(String line)
	{
		try
		{
			if(line == null || line.trim().equals(""))
				return false;
			
			line = line.trim();
			lower = line.toLowerCase().trim();
			
			if(line.equalsIgnoreCase("a"))
				Start.worker.execute_tasklist();
			
			/*else if(line.startsWith("bb"))
				exec.exec(line.substring(2));*/
			
			else if(lower.equals("print"))
				Node_Process.print_process_dump("\t", true);
			
			else if(lower.equals("print_tree") || lower.equals("print tree"))
				Node_Process.print_process_tree(null);
			
			else if(lower.equals("export"))
				export();
			
			else if(line.equalsIgnoreCase("status") || line.equalsIgnoreCase("s") || line.equalsIgnoreCase("-status") || line.equalsIgnoreCase("-s") || (line.contains("display") && line.contains("status")))
				display_status();
			
			else if(line.equalsIgnoreCase("verbose") || line.equalsIgnoreCase("v") || line.equalsIgnoreCase("-verbose") || line.equalsIgnoreCase("-v"))
				toggle_verbose();							
						
			else if(line.toLowerCase().startsWith("connect") || line.toLowerCase().startsWith("connect"))
				connect(line.substring(7));
			
			else if(lower.equals("-h") || lower.equals("-help") || lower.equals("h") || lower.equals("h"))
			{
				display_help();
			}
			
						
			else if(line.toLowerCase().startsWith("listen"))
				establish_server_socket(line.substring(6));
			
			else if(line.toLowerCase().startsWith("-listen"))
				establish_server_socket(line.substring(7));
			
			else if(line.toLowerCase().startsWith("-establish_server_socket"))
				establish_server_socket(line.substring(24));
			
			else if(line.toLowerCase().startsWith("establish_server_socket"))
				establish_server_socket(line.substring(23));
			
			else if(line.toLowerCase().startsWith("-establish server socket"))
				establish_server_socket(line.substring(24));
			
			else if(line.toLowerCase().startsWith("establish server socket"))
				establish_server_socket(line.substring(23));
									
			else if(line.toLowerCase().startsWith("-set_encryption") || line.toLowerCase().startsWith("-set encryption"))
				set_encryption(line.substring(15));
			
			else if(line.toLowerCase().startsWith("set_encryption") || line.toLowerCase().startsWith("set encryption"))
				set_encryption(line.substring(14));
			
			else if(line.toLowerCase().startsWith("encryption"))
				set_encryption(line.substring(10));				
			
			else if(line.toLowerCase().equalsIgnoreCase("log"))
				toggle_logging();
			
			else if(line.equalsIgnoreCase("disconnect"))
				disconnect_all();
			
			
			else
			{
				driver.directive("unrecognized command --> " + line);		
			}
				
		
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "determineCommand", e);
		}
		
		return false;
	}
	
	public static boolean export()
	{
		try
		{
			Node_Process.export_process_tree();
			Node_Netstat.export_netstat_tree(false, "\t", true, Node_Netstat.tree_grouped_foreign_address_netstat_entries, "netstat_foreign_address_tree.txt");
			
			Node_Netstat.export_netstat_table(true, "\t", true);
			Node_Process.export_process_table(true,  "\t",  true);
			
			
			return true;
		}
		
		catch(Exception e)
		{
			driver.eop(myClassName, "export", e);
		}
		
		return false;
	}
	
	public boolean disconnect_all()
	{
		try
		{
			driver.directive("executing disconnection actions...");
			
			while(ThdSocket.ALL_CONNECTIONS.size() > 0)
			{
				try
				{					
					ThdSocket thd = ThdSocket.ALL_CONNECTIONS.removeFirst();					
					thd.close_socket();
				}
				catch(Exception e)
				{
					continue;
				}
			}
						
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "disconnect_all", e);
		}
		
		return false;
	}
	
	public boolean toggle_logging()
	{
		try
		{
			Log.toggle_logging();
									
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "toggle_logging", e);
		}
		
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
