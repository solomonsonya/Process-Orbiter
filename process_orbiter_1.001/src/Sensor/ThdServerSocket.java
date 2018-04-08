/**
 * Thread created only to transport Sensory Information across the Sockets
 * 
 * Connect to this port only to receive sensor data
 * 
 * @author Solomon Sonya
 */

package Sensor;

import java.io.*;
import java.util.*;

import Driver.Driver;

import java.net.*;

public class ThdServerSocket extends Thread implements Runnable
{
	public static final String myClassName = "SensorServerSocket";
	public volatile static Driver driver = new Driver();

	public static volatile LinkedList<ThdServerSocket> list_server_sockets = new LinkedList<ThdServerSocket>();
	
	public static final int DEFAULT_SENSOR_PORT = 9998;
	public static final int DEFAULT_PARSER_PORT = 9997;
	
	public static final int DEFAULT_PORT = DEFAULT_SENSOR_PORT;
	
	public volatile int PORT = DEFAULT_PORT;
	
	public volatile ServerSocket svrskt = null;
	
	public static volatile boolean continue_run = true;
	
	public volatile String myBoundInterface = "";
	
	public volatile LinkedList<ThdSocket> list_connections = new LinkedList<ThdSocket>();
	
	public ThdServerSocket(int preferred_port)
	{
		try
		{
			PORT = preferred_port;
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
			//start serversocket
			driver.directive("\nAttempting to establish sensor server socket on port [" + PORT + "]");
			
			try
			{
				svrskt = new ServerSocket(PORT);
			}
			catch(Exception e)
			{
				driver.sop("ERROR! I WAS UNABLE TO BIND SENSOR SERVER SOCKET TO PORT: " + PORT + ".  It is appears this port is already bound by a separate process!  I am attempting to bind to a free port...");
				svrskt = new ServerSocket(0);
				PORT = svrskt.getLocalPort();
			}
			
			myBoundInterface = svrskt.getInetAddress().getHostAddress() + ":" + PORT;
			
			driver.directive("SUCCESS! " + myClassName + " is bound to " + svrskt.getInetAddress().getHostAddress() + ":" + PORT + ".  Ready for new connections across port " + PORT);
			
			//add self to list
			list_server_sockets.add(this);
			
			//
			//LISTEN FOR NEW CONNECTIONS
			//
			while(continue_run)
			{
				Socket skt = svrskt.accept();
				
				ThdSocket thd = new ThdSocket(this, skt);
			}
			
			driver.directive("\nPUNT PUNT! SENSOR ServerSocket is closed for " + myBoundInterface);
		}
		catch(Exception e)
		{
			
		}
	}
	
	
	public String get_status()
	{
		try
		{
			try
			{	return "" + svrskt.getInetAddress().getHostAddress() + ":" + PORT + " \tNum Connections: [" + this.list_connections.size() + "]";	}
			catch(Exception ee)
			{	return myBoundInterface;	}
					
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "get_status", e);			
		}
		
		return "ServerSocket - " + PORT;
	}
	
	public String displayStatus()
	{
		try
		{
			return this.get_status();					
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "get_status", e);			
		}
		
		return "ServerSocket - " + PORT;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
