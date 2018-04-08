/**
 * @author Solomon Sonya
 */

package Driver;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class Node_Netstat 
{
	public static volatile Driver driver = new Driver();
	public static final String myClassName = "Node_Netstat";
	
	public static volatile long current_time = System.currentTimeMillis();
	
	public static final String delimiter = "\t ";
	
	public volatile String 	PID = "",
							protocol = "",
							local_address_full = "",
							local_address = "",
							local_port = " ",
							foreign_address_full = "",
							foreign_address = "",
							foreign_port = " ",
							connection_state = "";
	

	public volatile Node_Process node_process = null;
	
	/**local_address_full - connection_state - foreign_address_full - PID will be the unique key for each entry*/
	public static volatile TreeMap<String, Node_Netstat> tree_netstat = new TreeMap<String, Node_Netstat>();
	
	/**Organize for unique local addresses i.e. group multiple netstat (and process entities) together based on the same local address e.g. 192.168.0.1:1234, 192.168.0.1:552, etc... all 192.168.0.1 will be under this key entry*/
	public static volatile TreeMap<String, LinkedList<Node_Netstat>> tree_grouped_local_address_netstat_entries = new TreeMap<String, LinkedList<Node_Netstat>>();
	
	/**Organize for unique foreign addresses i.e. group multiple netstat (and process entities) together based on the same foreign address e.g. 172.217.12.46:1234,172.217.12.46:9965, etc... all 172.217.12.46 will be under this key entry*/
	public static volatile TreeMap<String, LinkedList<Node_Netstat>> tree_grouped_foreign_address_netstat_entries = new TreeMap<String, LinkedList<Node_Netstat>>();
	
	public volatile long first_detection_time = System.currentTimeMillis();
	public volatile long last_detection_time = System.currentTimeMillis();
	
	public volatile String last_detection_time_text = driver.getTime_Specified_Hyphenated_with_seconds();
	public volatile String first_detection_time_text = driver.getTime_Specified_Hyphenated_with_seconds();
	
	public Node_Netstat(String pid, String proto, String local_addr_full, String foreign_addr_full, String conn_state)
	{
		try
		{
			PID = pid;
			protocol = proto;
			local_address_full = local_addr_full;
			local_address = local_addr_full;//backup
			foreign_address_full = foreign_addr_full;
			foreign_address = foreign_addr_full;//backup
			connection_state = conn_state;
			
			//
			//parse data
			//
			
			//split address from port
			if(local_address_full != null && local_address_full.contains(":"))
			{
				local_address 	= local_address_full.substring(0, local_address_full.lastIndexOf(":"));
				local_port 		= local_address_full.substring(local_address_full.lastIndexOf(":")+1);
				
				if(local_address_full.equalsIgnoreCase("*:*"))
				{
					local_address = "*:*";
					local_port = " ";
				}
			}
			
			if(foreign_address_full != null && foreign_address_full.contains(":"))
			{
				foreign_address = foreign_address_full.substring(0, foreign_address_full.lastIndexOf(":"));
				foreign_port 	= foreign_address_full.substring(foreign_address_full.lastIndexOf(":")+1);
				
				if(foreign_address_full.equalsIgnoreCase("*:*"))
				{
					foreign_address = "*:*";
					foreign_port = " ";
				}
			}
			
			//
			//link to unique netstat nodes!
			//
			try
			{
				//String key = local_address_full + "_" + connection_state + "_" + foreign_address_full + "_" + PID;
				String key = local_address_full + "_" + foreign_address_full + "_" + PID;
				
				if(!tree_netstat.containsKey(key))
				{
					tree_netstat.put(key,  this);
					
					driver.sop("New netstat Entry: " + this.protocol + " " + this.local_address + ":" + this.local_port + " --> " + this.foreign_address + ":" + this.foreign_port + " PID: [" + this.PID + "]");
				}
				else
				{
					try	
					{	
						tree_netstat.get(key).connection_state = conn_state;	
						tree_netstat.get(key).last_detection_time = System.currentTimeMillis();
						
					}	catch(Exception e){}
				}
			}
			catch(Exception ee){}
									
			//
			//link to tree
			//
			if(tree_grouped_local_address_netstat_entries.containsKey(local_address))
				tree_grouped_local_address_netstat_entries.get(local_address).add(this);
			else
			{
				tree_grouped_local_address_netstat_entries.put(local_address, new LinkedList<Node_Netstat>());
				tree_grouped_local_address_netstat_entries.get(local_address).add(this);
			}
			
			//
			//link to tree
			//
			if(tree_grouped_foreign_address_netstat_entries.containsKey(foreign_address))
				tree_grouped_foreign_address_netstat_entries.get(foreign_address).add(this);
			else
			{
				tree_grouped_foreign_address_netstat_entries.put(foreign_address, new LinkedList<Node_Netstat>());
				tree_grouped_foreign_address_netstat_entries.get(foreign_address).add(this);
			}
			
			
			
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "Constructor - 1", e);
		}
	}
	
	public static boolean update_netstat_parent()
	{
		try
		{
			if(Node_Netstat.tree_netstat == null || Node_Netstat.tree_netstat.isEmpty())
				return false;
			
			LinkedList<Node_Netstat> list = new LinkedList<Node_Netstat>(Node_Netstat.tree_netstat.values());
			
			for(Node_Netstat netstat : list)
			{
				if(netstat == null)
					continue;
				
				//
				//check to update process based on PID
				//
				if(netstat.node_process == null && Node_Process.tree_process.containsKey(netstat.PID))
				{
					Node_Process process = Node_Process.tree_process.get(netstat.PID);
					
					if(process != null)
					{
						//link the process to netstat object
						netstat.node_process = process;
						
						//link netstat object to the process
						if(process.list_netstat == null)
							process.list_netstat = new LinkedList<Node_Netstat>();
							
						if(!process.list_netstat.contains(netstat))
							process.list_netstat.add(netstat);
						
						driver.sop("Netstat entry [" + netstat.foreign_address + " : " + netstat.foreign_port + " has been linked to process [" + process.PID + "] - " + process.process_name);
					}
				}
			}
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "update_netstat_parent", e);
		}
		
		return false;
	}
	
	
	
	
	public String toString()
	{
		try
		{
			if(this.node_process != null)
			{							
				return 	"protocol: " + protocol + delimiter +  
						"local_address: " + local_address + delimiter + 
						"local_port: " + local_port + delimiter +
						"foreign_address: " + foreign_address + delimiter + 
						"foreign_port: " + foreign_port + delimiter +
						"connection_state: " + connection_state + delimiter +
						"PID: " + PID + delimiter + 
						node_process.get_netstat_data(delimiter) + 
						"First Detection Time: " + this.first_detection_time_text + delimiter +
						"First Detection Time_millis: " + this.first_detection_time + delimiter + 
						"Last Detection Time: " + this.last_detection_time_text + delimiter +
						"Last Detection Time_millis: " + this.last_detection_time + delimiter; 
						
			}
			
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "toString", e);
		}
		
		return 	"protocol: " + protocol + delimiter +  
				"local_address: " + local_address + delimiter + 
				"local_port: " + local_port + delimiter +
				"foreign_address: " + foreign_address + delimiter + 
				"foreign_port: " + foreign_port + delimiter +
				"connection_state: " + connection_state + delimiter +
				"PID: " + PID + delimiter + 
				"First Detection Time: " + this.first_detection_time_text + delimiter +
				"First Detection Time_millis: " + this.first_detection_time + delimiter + 
				"Last Detection Time: " + this.last_detection_time_text + delimiter +
				"Last Detection Time_millis: " + this.last_detection_time; 
				
	}
	
	
	
	
	public static boolean update_closed_netstats()
	{
		try
		{
			if(Node_Netstat.tree_netstat == null || Node_Netstat.tree_netstat.isEmpty())
				return false;
			
			current_time = System.currentTimeMillis();
			
			LinkedList<Node_Netstat> list = new LinkedList<Node_Netstat>(Node_Netstat.tree_netstat.values());
			
			for(Node_Netstat node : list)
			{
				if(node == null)
					continue;
				
				if(node.last_detection_time + 6000 > current_time)
				{
					node.connection_state = "CLOSED";
				}
				
			}//end for
			
			return true;
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "update_closed_netstats", e);
		}
		
		return false;
	}
	
	
	
	
	public static File export_netstat_table(boolean print_table_header, String delimiter, boolean open_file_upon_completion)
	{
		try
		{
			if(Node_Netstat.tree_netstat== null || Node_Netstat.tree_netstat.isEmpty())
				return null;
			
			//update parent nodes first
			update_netstat_parent();
			
			//ensure parent file exists
			File top_folder = new File("." + File.separator + Driver.NAME);
			
			File export = new File("." + File.separator + Driver.NAME + File.separator + "export");
			
			if(!export.exists() || !export.isDirectory())
				export.mkdirs();			
									
			if(export == null || !export.exists() || !export.isDirectory())
				export = new File("./");
			
			String path = export.getCanonicalPath().trim();
			
			if(!path.endsWith(File.separator))
				path = path + File.separator;
			
			//create the stream
			File fle = new File(path + "netstat_table.txt");
			PrintWriter pwOut = new PrintWriter(new FileWriter(fle), true);
			
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
			//print header
			//
			if(print_table_header)
				pwOut.println("process_name" + delimiter + "PID" + delimiter + "protocol" + delimiter + "local_address" + delimiter + "local_port" + delimiter + "foreign_address" + delimiter + "foreign_port" + delimiter + "connectoin_state" + delimiter + "command_line" + delimiter + "execution_path" + delimiter + "first_detection_time" + delimiter + "first_detection_time_millis" + delimiter + "last_detection_time" + delimiter + "last_detection_time_millis");
						
			
			//
			//print data
			//
			for(Node_Netstat node : list)
			{
				if(node == null)
					continue;
				
				pwOut.println(node.getTable(delimiter));
			}
			
			pwOut.flush();
			pwOut.close();
			
						
			
			if(open_file_upon_completion && fle != null && fle.exists())
			{
				driver.open_file(fle);
			}
			
			
			
			return fle;	
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "export_netstat_table", e);
		}
		
		return null;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getTable(String delimiter)
	{
		try
		{
			if(this.node_process != null)
			{
				return 	this.node_process.process_name + delimiter + 
						this.PID  + delimiter + 
						protocol + delimiter +  
						local_address + delimiter + 
						local_port + delimiter +
						foreign_address + delimiter + 
						foreign_port + delimiter +
						connection_state + delimiter +						
						node_process.CommandLine + delimiter + 
						node_process.ExecutablePath + delimiter + 
						this.first_detection_time_text + delimiter +
						this.first_detection_time + delimiter + 
						this.last_detection_time_text + delimiter +
						this.last_detection_time + delimiter; 
				
			}
			
			//otw...
			
			return 	" " + delimiter + 
					this.PID  + delimiter + 
					protocol + delimiter +  
					local_address + delimiter + 
					local_port + delimiter +
					foreign_address + delimiter + 
					foreign_port  + delimiter +  
					" " + delimiter + 
					" " + delimiter + 
					this.first_detection_time_text + delimiter +
					this.first_detection_time + delimiter + 
					this.last_detection_time_text + delimiter +
					this.last_detection_time; 
				
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "getTable", e);
		}
		
		return this.PID + "- - - ";
	}
	
	
	public static File export_netstat_tree(boolean print_table_header, String delimiter, boolean open_file_upon_completion, TreeMap<String, LinkedList<Node_Netstat>> tree, String file_name)
	{
		try
		{
			if(tree== null || tree.isEmpty())
				return null;
			
			//update parent nodes first
			update_netstat_parent();
			
			//ensure parent file exists
			File top_folder = new File("." + File.separator + Driver.NAME);
			
			File export = new File("." + File.separator + Driver.NAME + File.separator + "export");
			
			if(!export.exists() || !export.isDirectory())
				export.mkdirs();			
									
			if(export == null || !export.exists() || !export.isDirectory())
				export = new File("./");
			
			String path = export.getCanonicalPath().trim();
			
			if(!path.endsWith(File.separator))
				path = path + File.separator;
			
			//create the stream
			File fle = new File(path + file_name);
			PrintWriter pwOut = new PrintWriter(new FileWriter(fle), true);
			
			for(String key : tree.keySet())
			{
				pwOut.println(key);
				
				LinkedList<Node_Netstat> list = tree.get(key);
				
				if(list == null || list.isEmpty())
					continue;
				
				
				//
				//print data
				//
				for(Node_Netstat node : list)
				{
					if(node == null)
						continue;
					
					pwOut.println("\t" + node.toString());
				}
				
			}
			
			
			
			
			
			pwOut.flush();
			pwOut.close();
			
						
			
			if(open_file_upon_completion && fle != null && fle.exists())
			{
				driver.open_file(fle);
			}
			
			
			
			return fle;	
		}
		catch(Exception e)
		{
			driver.eop(myClassName, "export_netstat_tree", e);
		}
		
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
