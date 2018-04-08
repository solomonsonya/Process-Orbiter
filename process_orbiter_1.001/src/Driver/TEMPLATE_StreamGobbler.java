package Driver;
/**
 * The purpose of this class is to continuously read input received across a reader stream
 * 
 * For example, when we create a process and wish to run multiple commands across the same process, 
 * then we require a separate reader thread to read the input received across the stream and process it somehwere else
 * @author Solomon Sonya
 *
 */

import Driver.*;
import Sensor.*;
import Worker.*;
import java.io.*;


public class TEMPLATE_StreamGobbler extends Thread implements Runnable
{
	public static final String myClassName = "StreamGobbler";
	public static volatile Driver driver = new Driver();
	
	public volatile BufferedReader brIn = null;
	public volatile boolean is_standard_input_reader = false;
	public volatile boolean is_standard_error_reader = false;
	
	public int myExecution_Action = 0;
	
	public volatile Process parent = null;
	
	public TEMPLATE_StreamGobbler(Process p, int execution_action)
	{
		try
		{
			//use this to instantiate separate threads for the standard output and error streams
			if(p != null)
			{
				parent = p;
				
				//speciy how to handle the input once received from standard in
				this.myExecution_Action = execution_action;
				
				//instantiate the readers
				BufferedReader brIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader brError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				
				//pass the readers to the threads to gobble
				TEMPLATE_StreamGobbler gobbler_in = new TEMPLATE_StreamGobbler(parent, brIn, myExecution_Action, true);
				TEMPLATE_StreamGobbler gobbler_error = new TEMPLATE_StreamGobbler(parent, brError, myExecution_Action, false);
			}
			
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "Constructor - 1");
		}
		
	}
	
	
	public TEMPLATE_StreamGobbler(Process par, BufferedReader br, int execution_action, boolean this_is_standard_input_reader)
	{
		try
		{
			parent = par;
			
			//speciy how to handle the input once received from standard in
			this.myExecution_Action = execution_action;
			
			brIn = br;
			is_standard_input_reader = this_is_standard_input_reader;
			is_standard_error_reader = !this_is_standard_input_reader;
			
			this.start();			
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "Constructor - 1");
		}
		
	}

	
	public void run()
	{
		try
		{
			String line = "";
			
			while((line = brIn.readLine()) != null)
			{
				if(line.trim().equals(""))
					continue;
				
				switch(this.myExecution_Action)
				{
					case Process_Solomon.execution_action_PRINT_TO_SOP:
					{
						driver.directive(line);
						break;
					}
					
					//place other cases here on what to do when input is received
					
					default:
					{
						driver.directive(line);
						break;
					}
					
				}
				
				
			}
			
			driver.directive("\nReader stream closed for thread: " + this.getId());
			
			
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "run", e);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
