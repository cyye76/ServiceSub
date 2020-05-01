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
import java.util.Scanner;
import java.util.Map.Entry;

public class DataEntryHandlerOld extends Thread
{
	public void run( )
	{
		try
		{
			System.out.print("Enter type of action (SRT or PRT?) : ");
			Scanner scanInput = new Scanner(System.in);
			while(true)
			{
				//STRAT: main if condition
				if(scanInput.hasNext())
				{
					String dataType = scanInput.nextLine();
					if(dataType.equalsIgnoreCase("SRT"))
					{
						System.out.print("Enter Publication Filename : ");
						String pubFileName = scanInput.nextLine() + ".txt";
						try
						{
							String pubFileNameWithFullPath = getFileNameWithFullPath(pubFileName);
							File pubFile = new File(pubFileNameWithFullPath);
							if (!pubFile.isFile()) 
							{
						        System.out.println(pubFileName + "file does not exists!!!");
						        System.out.print("Enter type of action (SRT or PRT?) : ");
						        continue;
						    }
							BufferedReader br = new BufferedReader(new FileReader(pubFile));
							String line = null;//to read line from file
							while ((line = br.readLine()) != null) 
							{
						        String lineArray[] = line.split(",");
						        String pubIndex = lineArray[0];
						        RowSRT rowSRT = new RowSRT();
						        rowSRT.setPub(pubIndex);
						        for(int i=1;i<lineArray.length;i++)
									rowSRT.tasks.add(lineArray[i]);
						        Broker.insertToPubList(pubIndex, rowSRT.tasks);
							    Broker.newPubMatchWithPRT(rowSRT, pubIndex);   
							    //display all the tables contents together
							    Broker.displayAllTable();
							    //sleep for 10s to give time to other broker for completing their tasks
							    Thread.sleep(10000);	
						    }
							br.close();//close the buffer
						}//END: try
						catch(IOException e) {e.printStackTrace(); }
						System.out.print("Enter type of action (SRT or PRT?) : ");
					}//END: if: SRT
					else if(dataType.equalsIgnoreCase("PRT"))
					{
						System.out.print("Enter Subscription Filename : ");
						String subFileName = scanInput.nextLine() + ".txt";
						try
						{
							String subFileNameWithFullPath = getFileNameWithFullPath(subFileName);
							File subFile = new File(subFileNameWithFullPath);
							if (!subFile.isFile()) 
							{
						        System.out.println(subFileName + "file does not exists!!!");
						        System.out.print("Enter type of action (SRT or PRT?) : ");
						        continue;
						    }
							BufferedReader br = new BufferedReader(new FileReader(subFile));
							String line = null;//to read line from file
							while ((line = br.readLine()) != null) 
							{
						        String lineArray[] = line.split(",");
						        String subIndex = lineArray[0];
						        RowPRT rowPRT = new RowPRT();
						        rowPRT.setSub(subIndex);
						        ArrayList<String> tasks4SubList = new ArrayList<String>();
						        for(int i=1;i<lineArray.length;i++)
						        {
									rowPRT.tasks.add(lineArray[i]);
									tasks4SubList.add(lineArray[i]);
						        }
						        Broker.insertToSubList(subIndex, tasks4SubList);
						        //set startTime of this subIndex
						        Broker.resultSet.put(subIndex, new ResultSet());
						        Broker.resultSet.get(subIndex).setStartTime(System.currentTimeMillis());
								Broker.matchWithPRT(rowPRT,false);
								//set endTime of this subIndex
								Broker.resultSet.get(subIndex).setEndTime(System.currentTimeMillis());
						       
							    //display all the tables contents together
							    Broker.displayAllTable();
							    //sleep for 10s to give time to other broker for completing their tasks
							    Thread.sleep(10000);	
						    }
							br.close();//close the buffer
						}//END: try
						catch(IOException e) {e.printStackTrace(); }
						System.out.print("Enter type of action (SRT or PRT?) : ");
					}//END: else if: PRT
					else if(dataType.equalsIgnoreCase("RESULT") || dataType.equalsIgnoreCase("RES"))
					{
						displayResultSet();
						try
						{
							String outFileName = "output" + Broker.brokerName + ".txt";
							String outFileNameWithFullPath = getFileNameWithFullPath(outFileName);
							File outFile = new File(outFileNameWithFullPath);
							PrintWriter output = new PrintWriter(new FileWriter(outFile));
							writeResultSetIntoFile(output);
							output.close();
						}
						catch(IOException e){e.printStackTrace();}
					}
					else
					{
						System.out.print("OOps!!! Enter type of action (SRT or PRT?) : ");
					}
				}//END: main if condition
				else
					Thread.sleep(1000);		
			}//END: while loop			
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
			out.print(temp.getTotalTime() + " ms (" + temp.getEndTime() + " - " + temp.getStartTime() +")");
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