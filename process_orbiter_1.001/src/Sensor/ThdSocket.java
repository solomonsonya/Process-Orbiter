/**
 * @author Solomon Sonya
 */

package Sensor;

import java.io.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.Timer;
import Driver.*;
import Encryption.Encryption;

import java.net.*;

public class ThdSocket extends Thread implements Runnable, ActionListener
{
	public static final String myClassName = "ThdSocket";
	public volatile Driver driver = new Driver();

	public volatile Encryption encryption = null;
	
	public ThdServerSocket parent = null;
	public Socket mySocket = null;
			
	public volatile BufferedReader brIn = null;
	public volatile PrintWriter pwOut = null;
	
	public volatile String CONNECTION_ADDRESS = "";
	public volatile int distant_end_port = 9998;
	public volatile String distant_end_ip = "";
	
	public volatile boolean continue_run = true;
	
	public static volatile LinkedList<ThdSocket> list_outbound_connections = new LinkedList<ThdSocket>();
	
	/**iterate through this list to send all collected sensor data across*/
	public static volatile LinkedList<ThdSocket> ALL_CONNECTIONS = new LinkedList<ThdSocket>();
	
	Timer tmrUpdate_1_SEC = null;
	
	public ThdSocket(ThdServerSocket par, Socket skt)
	{
		try
		{
			parent = par;
			mySocket = skt;
			
			tmrUpdate_1_SEC = new Timer(1000, this);
			//tmrUpdate_1_SEC.start();
			
			this.start();
			
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "Constructor - 1", e);
		}
		
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		try
		{
			if(ae.getSource() == tmrUpdate_1_SEC)
			{
				//process_interrupt_1_sec();
			}
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "ae", e);
		}
	}
	
	/*public boolean process_interrupt_1_sec()
	{
		try
		{
			this.pwOut.print(" ");
			this.pwOut.flush();
			driver.directive("still up!");
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "process_interrupt_1_sec", e);
		}
		
		this.close_socket();
		this.continue_run = false;
		
		return false;
	}*/
	
	public void run()
	{
		try
		{
			brIn = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
			pwOut = new PrintWriter(new OutputStreamWriter(mySocket.getOutputStream()), true);
			
			if(parent != null && parent.list_connections != null)
			{
				parent.list_connections.addLast(this);
			}
			else
			{
				list_outbound_connections.add(this);
			}
									
			if(parent != null)//received connection from serversocket
				driver.directive("New socket connection received from " + mySocket.getRemoteSocketAddress() + " across interface " + parent.myBoundInterface + ". Total number of connected hosts: " + parent.list_connections.size());
			else//established outbound connection from StandardIn
				driver.directive("SUCCESS! New socket connection established to " + mySocket.getRemoteSocketAddress());
				
			set_connection_address();
			
			ALL_CONNECTIONS.add(this);
			
			this.send("Successfully connected to " + driver.FULL_NAME + " [SENSOR] by Solomon Sonya @Carpenter 1010");
			
			String line = "";
			
			while(continue_run && (line = brIn.readLine()) != null)
			{
				if(line.trim().equals(""))
					continue;
				
				determine_command(line);
				
				if(!continue_run)
					break;
			}
			
			//driver.directive("[SENSOR] --> Commencing termination actions with " + CONNECTION_ADDRESS);
		}
		
		catch(SocketException se)
		{
			if(se.getLocalizedMessage() != null && se.getLocalizedMessage().equalsIgnoreCase("Connection reset"))
			{
				sop("PUNT! Distant end closed socket to me!");
			}
			
			else
			{
				driver.eop(myClassName, "run mtd", se);
			}
			
		}
		
		catch(Exception e)
		{
			/*driver.eop(myClassName, "run", e);
			e.printStackTrace(System.out);*/
			
			driver.sop("\n\n * * * SOCKET CLOSED [" + this.CONNECTION_ADDRESS + "] \n\n ");
		}
						
		//driver.directive("Sensor socket closed to " + this.CONNECTION_ADDRESS);
		
		close_socket();
		
		
		
		
	}
	
	public boolean set_connection_address()
	{
		try
		{
			CONNECTION_ADDRESS = ""+mySocket.getRemoteSocketAddress();
			
			if(parent != null)
				distant_end_port = parent.PORT;
			else
				distant_end_port = this.mySocket.getLocalPort();
			
			try	{				distant_end_ip = CONNECTION_ADDRESS.substring(0, CONNECTION_ADDRESS.lastIndexOf(":")).trim();			}
			catch(Exception e){distant_end_ip = CONNECTION_ADDRESS;}
			
			if(distant_end_ip.startsWith("/"))
				distant_end_ip = distant_end_ip.substring(1).trim();
			if(distant_end_ip.startsWith("\\"))
				distant_end_ip = distant_end_ip.substring(1).trim();
			
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "set_connection_address", e);
		}
		
		return false;
	}
	
	public boolean close_socket()
	{
		try
		{
			try		{			if(parent != null) parent.list_connections.remove(this);}	catch(Exception e){}						
			try		{			this.brIn.close();} catch(Exception e){}
			try		{			this.pwOut.close();} catch(Exception e){}
			try		{			this.mySocket.close();} catch(Exception e){}		
			try		{			this.list_outbound_connections.remove(this);}	catch(Exception e){}
			try		{			this.ALL_CONNECTIONS.remove(this);}	catch(Exception e){}
			
			try		{			sop(""); sop("Sensor Socket Closed for thread: " + this.getId() + " [" + CONNECTION_ADDRESS + "].  Total number of connected hosts: " + parent.list_connections.size());	}	catch(Exception e){}
			
			brIn = null;
			pwOut = null;
			
			this.continue_run = false;
			
			//force an error on readline
			//try		{	brIn.read();	}	catch(Exception e){}
			
			System.gc();
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "close_socket", e);
		}
		
		return false;
	}
	
	public boolean determine_command(String line)
	{
		try
		{
			if(line == null || line.trim().equals(""))
				return false;
			
			line = line.trim();
			
			if(encryption != null)
				line = encryption.decrypt(line);
			
			if(line.equalsIgnoreCase("export"))
			{
				export_process_table();
				export_netstat_table();
			}
				
			
			//sop("Received line across [" + CONNECTION_ADDRESS + "] --> " + line);
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "sop", e);
		}
		
		return false;
	}
	
	public boolean export_process_table()
	{
		try
		{
			if(Node_Process.tree_process == null || Node_Process.tree_process.isEmpty())
			{
				this.send("no process data to export yet...");
				return false;
			}
			
			LinkedList<Node_Process> list = new LinkedList<Node_Process>(Node_Process.tree_process.values());
			
			//
			//sort based on process name
			//
			try
			{
				Collections.sort(list, new Comparator<Node_Process>()
				{
					public int compare(Node_Process t1, Node_Process t2)
					{
						//if(t1.process_name != null && t2.process_name != null)
							return t1.process_name.compareToIgnoreCase(t2.process_name);
						
					}						
					
				});
			}catch(Exception ee){}
			
			//
			//print data
			//
			for(Node_Process node : list)
			{
				if(node == null)
					continue;
				
				this.send(node.toString("\t", true, false, ""));
			}
			
			
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "export_process_table", e);
		}
		
		return false;
	}
	
	public boolean export_netstat_table()
	{
		try
		{
			
			if(Node_Netstat.tree_netstat == null || Node_Netstat.tree_netstat.isEmpty())
			{
				this.send("no netstat data to export yet...");
				return false;
			}
			
			LinkedList<Node_Netstat> list = new LinkedList<Node_Netstat>(Node_Netstat.tree_netstat.values());
			
			//
			//sort based on process name
			//
			try
			{
				Collections.sort(list, new Comparator<Node_Netstat>()
				{
					public int compare(Node_Netstat t1, Node_Netstat t2)
					{
						if(t1.node_process != null && t2.node_process != null)
							return t1.node_process.process_name.compareToIgnoreCase(t2.node_process.process_name);
						
						return 0;
					}						
					
				});
			}catch(Exception ee){}
											
			//
			//print data
			//
			for(Node_Netstat node : list)
			{
				if(node == null)
					continue;
				
				pwOut.println(node.toString());
			}
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "export_netstat_table", e);
		}
		
		return false;
	}
	
	public boolean sop(String out)
	{
		try
		{
			driver.sop("[SocketListener " + this.getId() + "] --> " + out);
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "sop", e);
		}
		
		return false;
	}
	
	
	
	/**
	 * Since multiple threads can exist, handle the encryption prior to sending here
	 * @param out
	 * @return
	 */
	public boolean send(String out)
	{
		try
		{			
						
			//note: this is an OS dependent situation. However, it is possible to hang the output stream if the connected client on the distant side terminates the socket
			//witout sending the close or reset flag to the server running the program.  
			//this will be a known issue at this time... - SSonya
			
			if(encryption != null)
				out = encryption.encrypt(out);
			
			pwOut.println(out);
			pwOut.flush();
			
			return true;
		}
		catch(Exception e)
		{
			//driver.eop(myClassName, "send", e);
		}
		
		//error, close socket!
		driver.directive("\nPunt! It appears socket is closed. Ensuring cleanup actions...\n");
		this.close_socket();
		
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
