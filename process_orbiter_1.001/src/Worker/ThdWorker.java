/**
 * This thread is just to help with repetitive tasks 
 * 
 *  @author Solomon Sonya
 */

package Worker;

import Driver.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;

import javax.swing.Timer;

import java.util.*;


public class ThdWorker extends Thread implements Runnable, ActionListener
{
	public static Driver driver = new Driver();
	public static final String myClassName = "ThdWorker";
	
	public volatile Timer tmr_process_2_sec = null;
	public volatile boolean handle_interrupt_1_sec = true;
	
	public volatile Timer tmr_process_3_sec = null;
	public volatile boolean handle_interrupt_2_sec = true;
	
		
	public static Process_Solomon process_tasklist = new Process_Solomon(Process_Solomon.execution_action_TASKLIST);
	public static Process_Solomon process_netstat = new Process_Solomon(Process_Solomon.execution_action_NETSTAT);
	public static Process_Solomon process_wmic_tasklist = new Process_Solomon(Process_Solomon.execution_action_WMIC_PROCESS);
	
		
	
	
	public volatile Timer tmrUpdate_1_SEC = null;
	public volatile Timer tmrUpdate_5_SEC = null;
	public volatile Timer tmrUpdate_10_SEC = null;	
	public volatile Timer tmrUpdate_60_SEC = null;
	public volatile Timer tmrUpdate_60_MINS = null;
	public volatile Timer tmrUpdate_10_MINS = null;
	
	public volatile boolean handle_interrupt_1_SEC = true;
	public volatile boolean handle_interrupt_5_SEC = true;
	public volatile boolean handle_interrupt_10_SEC = true;
	public volatile boolean handle_interrupt_60_SEC = true;
	public volatile boolean handle_interrupt_60_MINS = true;
	public volatile boolean handle_interrupt_10_MINS = true;
	
	public static volatile boolean refresh_jtable_protocol = false;
	public static volatile boolean refresh_jtable_resolution = false;
	
	/*public static volatile LinkedList<Resolution> list_resolve_resolution = new LinkedList<Resolution>();
	public volatile Resolution resolution = null;*/
	
	public ThdWorker()
	{
		try
		{
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
			//start timers
			tmrUpdate_1_SEC = new Timer(1000, this);
			tmrUpdate_1_SEC.start();
			
			tmrUpdate_5_SEC = new Timer(5000, this);
			tmrUpdate_5_SEC.start();
			
			tmrUpdate_10_SEC = new Timer(10000, this);
			tmrUpdate_10_SEC.start(); 
			
			tmrUpdate_60_SEC = new Timer(60000, this);
			tmrUpdate_60_SEC.start();
			
			tmrUpdate_10_MINS = new Timer(60000*10, this);
			tmrUpdate_10_MINS.start();
			
			tmrUpdate_60_MINS = new Timer(60000*60, this);
			tmrUpdate_60_MINS.start();
			
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "run", e);
		}
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		try
		{
			if(ae.getSource() == this.tmr_process_2_sec && this.handle_interrupt_1_sec)
			{
				process_interrupt_main_work_1_sec();
			}
			
			else if(ae.getSource() == this.tmr_process_3_sec && this.handle_interrupt_2_sec)
			{
				process_interrupt_main_work_2_sec();
			}
				
			
			else if(ae.getSource() == this.tmrUpdate_1_SEC && this.handle_interrupt_1_SEC)
			{
				processInterrupt_1_SEC();
			}
			
			else if(ae.getSource() == this.tmrUpdate_5_SEC && this.handle_interrupt_5_SEC)
			{
				processInterrupt_5_SEC();
			}
			
			else if(ae.getSource() == this.tmrUpdate_10_SEC && this.handle_interrupt_10_SEC)
			{
				processInterrupt_10_SEC();
			}
			
			else if(ae.getSource() == this.tmrUpdate_60_SEC && this.handle_interrupt_60_SEC)
			{
				processInterrupt_60_SEC();
			}
			
			else if(ae.getSource() == this.tmrUpdate_10_MINS && this.handle_interrupt_10_MINS)
			{
				processInterrupt_10_MINS();
			}
			
			else if(ae.getSource() == this.tmrUpdate_60_MINS && this.handle_interrupt_60_MINS)
			{
				processInterrupt_60_MINS();
			}
			
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "ae", e);
		}
	}
	
	public boolean processInterrupt_1_SEC()
	{
		try
		{
			if(!handle_interrupt_1_SEC)
				return true;
			//
			//lock semaphone
			//
			handle_interrupt_1_SEC = false;
			
			//
			//WORK
			//			
			//if(!list_resolve_resolution.isEmpty())
				//process_resolution_list();
				
			
			
			//
			//release semaphore
			//
			this.handle_interrupt_1_SEC = true;
			return true;
		}
		catch(ConcurrentModificationException cme)
		{
			driver.directive("* Holdfast, I'm updating the resolution list...");
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "processInterrupt_1_SEC", e);
		}
		
		//
		//release semaphore
		//
		this.handle_interrupt_1_SEC = true;
		return false;
	}
	
	//for /F "tokens=1-5 delims= " %A in ('netstat -ano') do echo %A,%B,%C,%D,%E
	//tasklist /v /fo csv
	//wmic process get /format:csv
	
	public boolean execute_netstat()
	{
		try
		{
			//update closed netstats
			Node_Netstat.update_closed_netstats();			
			
			process_netstat.exec("for /F \"tokens=1-5 delims= \" %A in ('netstat -ano') do echo %A,%B,%C,%D,%E");
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "execute_netstat", e);
		}
		
		return false;
	}
	
	public boolean execute_tasklist()
	{
		try
		{
			process_tasklist.exec("tasklist /v /fo csv");
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "execute_tasklist", e);
		}
		
		return false;
	}
	
	public boolean execute_wmic_tasklist()
	{
		try
		{
			process_wmic_tasklist.exec("wmic process get /format:csv");
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "execute_wmic_tasklist", e);
		}
		
		return false;
	}

	
	public boolean process_interrupt_main_work_1_sec()
	{
		try
		{
			if(!this.handle_interrupt_1_sec)
				return false;
			
			handle_interrupt_1_sec = false;
			
			//execute_tasklist();
			execute_netstat();
			
			
			
			handle_interrupt_1_sec = true;
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "process_interrupt_main_work_1_sec", e);
		}
		
		handle_interrupt_1_sec = true;
		return false;
	}
	
	public boolean process_interrupt_main_work_2_sec()
	{
		try
		{
			if(!this.handle_interrupt_2_sec)
				return false;
			
			handle_interrupt_2_sec = false;
			
			execute_wmic_tasklist();
			
			handle_interrupt_2_sec = true;
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "process_interrupt_main_work_2_sec", e);
		}
		
		handle_interrupt_2_sec = true;
		return false;
	}
	
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	
	public boolean processInterrupt_5_SEC()
	{
		try
		{
			if(!handle_interrupt_5_SEC)
				return true;
			//
			//lock semaphone
			//
			handle_interrupt_5_SEC = false;
			
			//
			//WORK
			//	
			
			
			//
			//release semaphore
			//
			this.handle_interrupt_5_SEC = true;
			return true;
		}
		
		catch(Exception e)
		{
			driver.eop(myClassName, "processInterrupt_5_SEC", e);
		}
		
		//
		//release semaphore
		//
		this.handle_interrupt_5_SEC = true;
		return false;
	}
	
	
		
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	
	public boolean processInterrupt_10_SEC()
	{
		try
		{
			if(!handle_interrupt_10_SEC)
				return true;
			//
			//lock semaphone
			//
			handle_interrupt_10_SEC = false;
			
			//
			//WORK
			//			
			//driver.sop("ready to process intr 10 sec... " + driver.time.getTime_Current(":", true));
			
			
			
			//
			//release semaphore
			//
			this.handle_interrupt_10_SEC = true;
			return true;
		}
		
		catch(Exception e)
		{
			driver.eop(myClassName, "processInterrupt_10_SEC", e);
		}
		
		//
		//release semaphore
		//
		this.handle_interrupt_10_SEC = true;
		return false;
	}
		
		
		
		
		
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
		
	public boolean processInterrupt_60_SEC()
	{
		try
		{
			if(!handle_interrupt_60_SEC)
				return true;
			//
			//lock semaphone
			//
			handle_interrupt_60_SEC = false;
			
			//
			//WORK
			//			
			
			
			
			//
			//GC
			//
			System.gc();
			
			//
			//Update OUI
			//
			//driver.driver_oui.update_MAC_Registration_Data();
			
			//
			//Update GEO
			//
			//driver.driver_geo.updateSSID();
			
			
			//
			//release semaphore
			//
			this.handle_interrupt_60_SEC = true;
			return true;
		}
		
		catch(Exception e)
		{
			driver.eop(myClassName, "processInterrupt_60_SEC", e);
		}
		
		//
		//release semaphore
		//
		this.handle_interrupt_60_SEC = true;
		return false;
	}	
		
		

	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////

	public boolean processInterrupt_10_MINS()
	{
		try
		{
			if(!handle_interrupt_10_MINS)
				return true;
			//
			//lock semaphone
			//
			handle_interrupt_10_MINS = false;

			//
			//WORK
			//			
			
			
			//
			//GC
			//
			System.gc();

			

			//
			//release semaphore
			//
			this.handle_interrupt_10_MINS = true;
			return true;
		}

		catch(Exception e)
		{
			driver.eop(myClassName, "processInterrupt_10_MINS", e);
		}

		//
		//release semaphore
		//
		this.handle_interrupt_60_MINS = true;
		return false;
	}	






	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////	
		
		
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
		
	public boolean processInterrupt_60_MINS()
	{
		try
		{
			if(!handle_interrupt_60_MINS)
				return true;
			//
			//lock semaphone
			//
			handle_interrupt_60_MINS = false;
			
			//
			//WORK
			//			
			
			
			
			//
			//GC
			//
			System.gc();
			
			//
			//Update OUI
			//
			//driver.driver_oui.update_MAC_Registration_Data();
			
			//
			//Update GEO
			//
			//driver.driver_geo.updateSSID();
			
			
			//
			//release semaphore
			//
			this.handle_interrupt_60_MINS = true;
			return true;
		}
		
		catch(Exception e)
		{
			driver.eop(myClassName, "processInterrupt_60_SEC", e);
		}
		
		//
		//release semaphore
		//
		this.handle_interrupt_60_MINS = true;
		return false;
	}	
		
		
		
		
		
		
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////	
	
		
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////	
	
		
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////
	
}
