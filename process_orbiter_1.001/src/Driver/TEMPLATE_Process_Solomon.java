/**
 * There are times where we wish to execute multiple commands within the same process. 
 * For instance, forensics, if we want to orbit the tasklist and routinely grab the data that is present, 
 * Just using the Runtime.exec command creates a separate cmd.exe / conhost.exe on Windows machines...
 * Running that for a while is problematic as it will fill the process table with lots of unneccesary data.
 * 
 * To alleviate this issue, we have created our own process class that handles the instantiation of a process builder and a process
 * Then we send commands to this process, and the error streams and input streams are gobbled and handled separately by the 
 * StreamGobbler threads to allow multi-threaded execution in parallel
 * 
 * @author Solomon Sonya
 */

package Driver;

import java.io.*;

public class TEMPLATE_Process_Solomon extends Thread implements Runnable
{
	public static volatile Driver driver = new Driver();
	public static final String myClassName = "Process_Solomon";
	public static volatile boolean verbose = true;
	
	public volatile Process process = null;		
	public volatile ProcessBuilder process_builder = null;
	public volatile StreamGobbler stream_gobbler = null;
	public volatile BufferedWriter buffered_writer = null;
	
	//speciy what action to execute when we receive input across the reader stream (this is to pass on  to the gobblers)
	public static final int execution_action_PRINT_TO_SOP = 0;
	
	
	
	public int myExecution_Action = 0;
	
	public TEMPLATE_Process_Solomon(int execution_action)
	{
		try
		{
			//speciy how to handle the input once received from standard in
			this.myExecution_Action = execution_action;
			
			//attach to cmd and conhost process		
			if(driver.isWindows)
				process_builder = new ProcessBuilder("cmd.exe");
			
			else if(driver.isLinux)
				process_builder = new ProcessBuilder("/bin/bash");
			
			process = process_builder.start();
			
			//create gobblers to read any data that is received
			stream_gobbler = new StreamGobbler(process, myExecution_Action, true, null);
			
			//create our printwriter
			buffered_writer = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream()));
			
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
			
			
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "run", e);
		}
	}
	
	public boolean exec(String cmd)
	{
		try
		{
			if(verbose)
				driver.directive("Executing -->" + cmd);
			
			buffered_writer.write(cmd);
			buffered_writer.newLine();
			buffered_writer.flush();
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "exec", e);
		}
		
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

}
