import java.util.ArrayList;
import java.util.Scanner;
public class DataEntryHandler4B4 extends Thread
{
	public void run( )
	{
		try
		{
			System.out.print("Enter data type (SRT or PRT?) : ");
			Scanner scanInput = new Scanner(System.in);
			while(true)
			{
				if(scanInput.hasNext())
				{
					String dataType = scanInput.nextLine();
					if(dataType.equalsIgnoreCase("SRT"))
					{
						System.out.print("Enter tasks separated by coma(,) : ");
						String tasks = scanInput.nextLine();
						System.out.print("Enter publication name : ");
						String pub = scanInput.nextLine(); 
						//scanInput.close();  
						RowSRT rowSRT = new RowSRT();
						rowSRT.setPub(pub);
						String recTasks []= tasks.split(",");
						for(int i=0;i<recTasks.length;i++)
							rowSRT.tasks.add(recTasks[i]);
					    BrokerFour.insertToPubList(pub, rowSRT.tasks);
					    BrokerFour.newPubMatchWithPRT(rowSRT, pub);   
					    //display all the tables contents together
					    BrokerFour.displayAllTable();
						System.out.print("Enter data type (SRT or PRT?) : ");
					}//END: if: SRT
					else if(dataType.equalsIgnoreCase("PRT"))
					{
						System.out.print("Enter tasks separated by coma(,) : ");
						String tasks = scanInput.nextLine();
						System.out.print("Enter subscription name : ");
						String sub = scanInput.nextLine(); 
						//scanInput.close();  
						ArrayList<String> tasks4SubList = new ArrayList<String>();
						RowPRT rowPRT = new RowPRT();
						rowPRT.setSub(sub);
						String recTasks []= tasks.split(",");
						for(int i=0;i<recTasks.length;i++)
						{
							rowPRT.tasks.add(recTasks[i]);
							tasks4SubList.add(recTasks[i]);
						}
						BrokerFour.insertToSubList(sub, tasks4SubList);
						BrokerFour.matchWithPRT(rowPRT,false);  
					    //display all the tables contents together
						BrokerFour.displayAllTable();
					    System.out.print("Enter data type (SRT or PRT?) : ");
					}//END: else if: PRT
					else
					{
						System.out.print("OOps!!! Enter data type (SRT or PRT?) : ");
					}
				}
				else
					Thread.sleep(1000);		
			}//END: while loop			
		}
		catch(Exception e){e.printStackTrace( );}
	}//END: run()
}
