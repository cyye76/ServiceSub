import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;

public class DataEntryHandler extends Thread
{
	public void run( )
	{
		try
		{
			String dsFileName = "dataset" + Broker.brokerName + ".txt";
			String dsFileNameWithFullPath = getFileNameWithFullPath(dsFileName);
			File dsFile = new File(dsFileNameWithFullPath);
			if (!dsFile.isFile()) 
			{
		        System.out.println(dsFileName + " file does not exists!!!");
		        System.out.println(Broker.brokerName + " is over!!!");
		    }
			BufferedReader br = new BufferedReader(new FileReader(dsFile));
			String line = null;//to read line from file
			long waitUnit = 20000;//10s
			while((line = br.readLine()) != null)
			{
				String lineArray[] = line.split(",");
				String lineType = lineArray[0];//input or output or wait
				long waitTime = Long.parseLong(lineArray[1]);
				//STRAT: if: wait
				if(lineType.equalsIgnoreCase("WAIT"))
				{
					//Thread.sleep(1*waitUnit);
					Thread.sleep(waitTime*waitUnit);
				}//END: if: wait
				else if(lineType.equalsIgnoreCase("IN"))
				{
					String lineBroker = lineArray[2];
					if(!Broker.brokerName.equals(lineBroker))
					{
						Thread.sleep(waitUnit);
						continue;
					}
					String inType = lineArray[3];//SRT or PRT
					String pubORsubIndex = lineArray[4];
					ArrayList<String> pubORsubTasks = new ArrayList<String>();
					for(int i=5;i<lineArray.length;i++)
						pubORsubTasks.add(lineArray[i]);
					//Thread.sleep(waitTime*waitUnit);
					//START: if: SRT
					if(inType.equalsIgnoreCase("SRT"))
					{
						RowSRT rowSRT = new RowSRT();
				        rowSRT.setPub(pubORsubIndex);
				        for(int j=0;j<pubORsubTasks.size();j++)
							rowSRT.tasks.add(pubORsubTasks.get(j));
						Broker.insertToPubList(pubORsubIndex, pubORsubTasks);
					    Broker.newPubMatchWithPRT(rowSRT, pubORsubIndex);   
					    //display all the tables contents together
					    Broker.displayAllTable();	
					}//END: if: SRT
					//START: if: PRT
					else if(inType.equalsIgnoreCase("PRT"))
					{
						RowPRT rowPRT = new RowPRT();
				        rowPRT.setSub(pubORsubIndex);
				        for(int j=0;j<pubORsubTasks.size();j++)
							rowPRT.tasks.add(pubORsubTasks.get(j));
				        Broker.insertToSubList(pubORsubIndex, pubORsubTasks);
				        //set startTime of this subIndex
				        Broker.resultSet.put(pubORsubIndex, new ResultSet());
				        Broker.resultSet.get(pubORsubIndex).setStartTime(System.currentTimeMillis());
						Broker.matchWithPRT(rowPRT,false);
						//set endTime of this subIndex
						Broker.resultSet.get(pubORsubIndex).setEndTime(System.currentTimeMillis());
					    //display all the tables contents together
					    Broker.displayAllTable();
					}//END: if: PRT
				}//END: else if: in
				else if(lineType.equalsIgnoreCase("OUT"))
				{
					//Thread.sleep(waitTime*waitUnit*2);
					Thread.sleep(waitUnit + 60000*2);
					displayResultSet();
					try
					{
						String outFileName = "output" + Broker.brokerName + ".txt";
						String outFileNameWithFullPath = getFileNameWithFullPath(outFileName);
						File outFile = new File(outFileNameWithFullPath);
						PrintWriter output = new PrintWriter(new FileWriter(outFile));
						writeResultSetIntoFile(output);
						output.close();
						//System.out.println("ResultSet Printed Successfully in File: " + outFileName);
					}
					catch(IOException e){e.printStackTrace();}
				}//END: else if: out
			}//END: while loop	
			br.close();//close the buffer
		}//END: try case
		catch(Exception e){e.printStackTrace( );}
	}//END: run()
	
	//START: getFileNameWithFullPath()*****************************************************
	//This method return the file name with full path
	public String getFileNameWithFullPath(String fileName)
	{
		String fileNameWithFullPath = "";
		String workingDir = System.getProperty("user.dir");
    	String your_os = System.getProperty("os.name").toLowerCase();
    	//System.out.println(workingDir +" and "+ your_os);
    	if(your_os.indexOf("win") >= 0)
    	{
    		fileNameWithFullPath = workingDir + "\\textfile\\" + fileName;
    	}
    	else if(your_os.indexOf( "nix") >=0 || your_os.indexOf( "nux") >=0)
    	{
    		fileNameWithFullPath = workingDir + "/textfile/" + fileName;
    	}
    	else
    	{
    		fileNameWithFullPath = workingDir + "{others}" + fileName;
    	}
    	return fileNameWithFullPath;
	}//END: getFileNameWithFullPath()
	//END: getFileNameWithFullPath()*****************************************************
	
	//START: writeResultSetIntoFile()*****************************************************
	public static synchronized void writeResultSetIntoFile(PrintWriter out)
	{
		// Get a set of the entries 
		Set<Entry<String, ResultSet>> set = Broker.resultSet.entrySet(); 
		// Get an iterator 
		Iterator<Entry<String, ResultSet>> i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) 
		{ 
			Map.Entry<String, ResultSet> me = (Map.Entry<String, ResultSet>)i.next(); 
			out.print(me.getKey() + ": "); 
			ResultSet temp = (ResultSet)me.getValue();
			//out.print(temp.getTotalTime() + " ms (" + temp.getEndTime() + " - " + temp.getStartTime() +")");
			out.print(temp.getTotalTime() + "ms\t");
			out.print(Broker.subList.get(me.getKey()).tasks.toString());
			out.print(" => ");
			for(int j=0;j<Broker.subList.get(me.getKey()).pubIndex.size();j++)
				out.print(Broker.pubList.get(Broker.subList.get(me.getKey()).pubIndex.get(j)).tasks.toString());
			out.print(" = " + Broker.subList.get(me.getKey()).pubIndex.toString());
			out.println();
		}//END: while loop
		out.println("SRT Size: "+Broker.srt.keySet().size());
		out.println("PRT Size: "+Broker.prt.keySet().size());
	}//END: method writeResultSetIntoFile() 
	//END: writeResultSetIntoFile()*****************************************************
	
	//START: displayResultSet()*****************************************************
	public static synchronized void displayResultSet()
	{
		System.out.println("");
		System.out.println("**********::Contents of ResultSet ("+Broker.brokerName+")::**********");
		// Get a set of the entries 
		Set<Entry<String, ResultSet>> set = Broker.resultSet.entrySet(); 
		// Get an iterator 
		Iterator<Entry<String, ResultSet>> i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) 
		{ 
			Map.Entry<String, ResultSet> me = (Map.Entry<String, ResultSet>)i.next(); 
			System.out.print(me.getKey() + ": "); 
			ResultSet temp = (ResultSet)me.getValue();
			System.out.print(temp.getTotalTime() + " ms (" + temp.getEndTime() + " - " + temp.getStartTime() +")");
			System.out.println();
		}//END: while loop
		System.out.println("SRT Size: "+Broker.srt.keySet().size());
		System.out.println("PRT Size: "+Broker.prt.keySet().size());
	}//END: method displayResultSet() 
	//END: displayResultSet()*****************************************************
	
}//END: DataEntryHandler class