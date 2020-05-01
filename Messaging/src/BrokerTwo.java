import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

//client
class BrokerTwo
{
	static String brokerName;//name of this broker
	static HashMap<String,RowSRT> srt;	//SRT table
	static HashMap<String,RowPRT> prt;	//PRT table
	static HashMap<String,RowPubList> pubList;	//Publication table
	static HashMap<String,RowSubList> subList;	//Subscription table
	
	static ArrayList<String>inQueue; //store incoming msg
	static ArrayList<String>outQueue; //store outgoing msg
	static HashMap<String,Neighbors> neighbors; // neighbors of this broker
	
	static int isFullRowSRT; //for checking new row of SRT(full or only matching)
	static HashMap<String,String> pubList4Check;//pubIndex and resBroker of publication

	public static void main(String[] args)
	{
		try 
		{
			brokerName = "B2";
			srt = new HashMap<String,RowSRT>();
			prt = new HashMap<String,RowPRT>();
			pubList= new HashMap<String,RowPubList>();
			subList = new HashMap<String,RowSubList>();
			
			neighbors = new HashMap<String,Neighbors>();
			inQueue = new ArrayList<String>();
			outQueue = new ArrayList<String>();
			
			isFullRowSRT = 1; //for checking new row of SRT(1=full or 0=only matching forward)
			pubList4Check = new HashMap<String, String>();
			
			System.out.println("***************::BrokerTwo (B2)::***************");
			//bind with neighbor(server)
			Socket clientSocket = new Socket("localhost", 3333);
			//output stream for writing
			PrintStream os = new PrintStream(clientSocket.getOutputStream());
			//input stream for reading
			InputStreamReader is = new InputStreamReader(clientSocket.getInputStream( ));
			//put the neighbor(server)into it's own neighbors list
			neighbors.put("B3", new Neighbors(clientSocket, is, os));
			//this thread handle incoming msg from neighbor
			InMsgHandler4B2 inMsgThread = new InMsgHandler4B2("B3");
			inMsgThread.start( );
			//this thread handle outgoing msg to neighbor by output message queue
			OutputQueueHandler4B2 outQueueThread = new OutputQueueHandler4B2();
			outQueueThread.start();
			//this thread handle the input message queue
			InputQueueHandler4B2 inQueueThread = new InputQueueHandler4B2();
			inQueueThread.start();
			//this thread handle the input data entered into the console
			DataEntryHandler4B2 dataEntryThread = new DataEntryHandler4B2();
			dataEntryThread.start();
		}
		catch(Exception e){e.printStackTrace( );}
		
	}//END: main()	
	
	//START: searchSUb()***************************************************************
	public static synchronized void forward4SearchSub(RowPRT rowPRT)
	{
		String indexTask = rowPRT.tasks.get(0);
		RowPRT localRowPRT = new RowPRT();
		localRowPRT.sub.add(rowPRT.sub.get(0));
		for(int s=0;s<rowPRT.tasks.size();s++)
			localRowPRT.tasks.add(rowPRT.tasks.get(s));
		if(srt.containsKey(indexTask))
		{
			RowSRT rowSRT = (RowSRT)srt.get(indexTask);
			//for checking the size of the rowPRT which have to be >= rowSRT,
			//otherwise there is no matching in the SRT exists for this rowPRT
			while(rowSRT.tasks.size() <= localRowPRT.tasks.size())
			{
				if(tasksMatching(rowSRT.tasks, localRowPRT.tasks) == 1)
				{
					if(rowSRT.sentToBroker.size()>0)
					{
						for(int i=0;i<rowSRT.sentToBroker.size();i++)
							outQueueMsgWritingSEARCH(rowSRT.sentToBroker.get(i), localRowPRT.sub.get(0));
						break;
					}
					else if(rowSRT.followKeys.size()>0 && localRowPRT.tasks.size()>rowSRT.tasks.size())
					{
						for(int i=0;i<rowSRT.followKeys.size();i++)
						{
							if(rowSRT.followKeys.get(i).equals(localRowPRT.tasks.get(rowSRT.tasks.size())))
							{
								for(int j=0;j<rowSRT.tasks.size();j++)
									localRowPRT.tasks.remove(j);
								rowSRT = (RowSRT)srt.get(rowPRT.tasks.get(0));
								break;
							}
						}
					}//END: else if condition
					else
						break;
				}//END: first if condition: inside while loop
			}//END: while loop
		}//END: main if condition
	}//END: method searchSUb()
	//END: searchSUb()***************************************************************
	
	//START: newPubMatchWithPRT()******************************************************
	//this method check PRT for the matching if new Publication added in SRT
	public static synchronized void newPubMatchWithPRT(RowSRT rowSRT, String pubIndex)
	{
		String indexTask = rowSRT.tasks.get(0);
		ArrayList<String> backupTasksOfNewPUB = new ArrayList<String>();
		for(int x=0;x<rowSRT.tasks.size();x++)
			backupTasksOfNewPUB.add(rowSRT.tasks.get(x));
		//insert new publication into the SRT first
		RowSRT insertRowSRT = prepareRow4InsertToSRT(rowSRT);
		insertToSRT(insertRowSRT, pubIndex);
		//insertToSRT(rowSRT, pubIndex);
		//START: main if condition inside method
		if(prt.containsKey(indexTask))
		{
			RowPRT rowPRT = (RowPRT)prt.get(indexTask);
			//RowPRT commonRowPRT = new RowPRT();
			ArrayList<String> commonTasks = new ArrayList<String>();
			//for checking the size of the rowPRT which have to be >= rowSRT,
			//otherwise there is no matching in the SRT exists for this rowPRT
			//if(rowSRT.tasks.size() <= rowPRT.tasks.size())
			while(tasksMatching(rowSRT.tasks, rowPRT.tasks) == 1)
			{
				//that means new Pub still need to check PRT
				if(rowSRT.tasks.size() > rowPRT.tasks.size())
				{
					//Copy tasks from rowSRT.tasks until rowPRT.tasks.size to commonTasks
					for(int i=0;i<rowPRT.tasks.size();i++)
						commonTasks.add(rowSRT.tasks.get(i));
					//remove the matching rowSRT.tasks up to rowPRT.tasks.size
					for(int i=0;i<rowPRT.tasks.size();i++)
					{
						rowSRT.tasks.remove(0);
					}
					//now next matching index of PRT will be checked with the remaining rowSRT
					if(prt.containsKey(rowSRT.tasks.get(0)))
					{
						rowPRT =  (RowPRT)prt.get(rowSRT.tasks.get(0));
						continue;
					}
					//no complete matching with new Pub in the PRT, so return
					else
						return;
				}
				//START: else if: that means new Pub found in PRT
				else if(rowSRT.tasks.size() <= rowPRT.tasks.size())
				{
					//START: if: Check whether rowPRT has sub
					if(rowPRT.sub.size()>0)
					{
						//START: for loop: check for all the subs in rowPRT
						for(int i=0;i<rowPRT.sub.size();i++)
						{
							//START: if: check for the availability in the subList
							if(subList.containsKey(rowPRT.sub.get(i)))
							{
								if(subList.get(rowPRT.sub.get(i)).pubIndex.size() >0 && subList.get(rowPRT.sub.get(i)).rTaskIndex.equals("NULL"))
								{
									continue;
								}
								else if(subList.get(rowPRT.sub.get(i)).pubIndex.size() >0 && !subList.get(rowPRT.sub.get(i)).rTaskIndex.equals("NULL"))
								{
									RowPRT upRowPRT = new RowPRT();
									for(int m=0;m<subList.get(rowPRT.sub.get(i)).tasks.size();m++)
										upRowPRT.tasks.add(subList.get(rowPRT.sub.get(i)).tasks.get(m));
									upRowPRT.sub.add(rowPRT.sub.get(i));
									int position=0;//get the position of remaining tasks index
									for(int j=0; j<subList.get(rowPRT.sub.get(i)).pubIndex.size();j++)
										position += pubList.get(subList.get(rowPRT.sub.get(i)).pubIndex.get(j)).tasks.size();
									//remove all the tasks before the remaining tasks index position 
									for(int k=0;k<position;k++)
										upRowPRT.tasks.remove(0);
									//this condition is not required, but for reducing the call of matchWithSRT(), is written
									if(upRowPRT.tasks.size()>=backupTasksOfNewPUB.size())
										matchWithSRT(upRowPRT,true);//add boolean parameter for only update
								}
								else if(subList.get(rowPRT.sub.get(i)).pubIndex.size() == 0 && subList.get(rowPRT.sub.get(i)).rTaskIndex.equals("NULL"))
								{
									RowPRT upRowPRT = new RowPRT();
									for(int m=0;m<subList.get(rowPRT.sub.get(i)).tasks.size();m++)
										upRowPRT.tasks.add(subList.get(rowPRT.sub.get(i)).tasks.get(m));
									upRowPRT.sub.add(rowPRT.sub.get(i));
									//this condition is not required, but for reducing the call of matchWithSRT() is written
									if(upRowPRT.tasks.size()>=backupTasksOfNewPUB.size())
										matchWithSRT(upRowPRT,true);//add boolean parameter for only update
								}
							}//END: if: check for the availability in the subList
						}//END: for loop: check for all the subs in rowPRT
					}//END: if: Check whether rowPRT has sub
					
					//START: if: Check whether rowPRT has followKeys
					if(rowPRT.followKeys.size()>0)
					{
						ArrayList<RowPRT> listOfRowPRT=new ArrayList<RowPRT>();
						//START: for loop: check for all followKeys in the rowPRT
						for(int i=0;i<rowPRT.followKeys.size();i++)
						{
							listOfRowPRT.add((RowPRT)prt.get(rowPRT.followKeys.get(i)));
							
						}//END: for loop: check for all followKeys in the rowPRT
						//START: for loop: check further for all followKeys 
						for(int i= 0;i<listOfRowPRT.size();i++)
						{
							if(listOfRowPRT.get(i).followKeys.size()>0)
								for(int j=0;j<listOfRowPRT.get(i).followKeys.size();j++)
									listOfRowPRT.add((RowPRT)prt.get(listOfRowPRT.get(i).followKeys.get(j)));
							//for avoiding infinite loop and to many iteration, set the threshold
							if(i==10)
								break;
						}//END: for loop: check further for all followKeys 
						//START: for loop: check all rowPRT in listOfRowPRT for matching SUB with new Pub
						for(int n=0;n<listOfRowPRT.size();n++)
						{
							rowPRT = (RowPRT)listOfRowPRT.get(n);
							//START: 2nd time: same checking as before in the previous if condition
							//START: if: Check whether rowPRT has sub
							if(rowPRT.sub.size()>0)
							{
								//START: for loop: check for all the subs in rowPRT
								for(int i=0;i<rowPRT.sub.size();i++)
								{
									//START: if: check for the availability in the subList
									if(subList.containsKey(rowPRT.sub.get(i)))
									{
										if(subList.get(rowPRT.sub.get(i)).pubIndex.size() >0 && subList.get(rowPRT.sub.get(i)).rTaskIndex.equals("NULL"))
										{
											continue;
										}
										else if(subList.get(rowPRT.sub.get(i)).pubIndex.size() >0 && !subList.get(rowPRT.sub.get(i)).rTaskIndex.equals("NULL"))
										{
											RowPRT upRowPRT = new RowPRT();
											for(int m=0;m<subList.get(rowPRT.sub.get(i)).tasks.size();m++)
												upRowPRT.tasks.add(subList.get(rowPRT.sub.get(i)).tasks.get(m));
											upRowPRT.sub.add(rowPRT.sub.get(i));
											int position=0;//get the position of remaining tasks index
											for(int j=0; j<subList.get(rowPRT.sub.get(i)).pubIndex.size();j++)
												position += pubList.get(subList.get(rowPRT.sub.get(i)).pubIndex.get(j)).tasks.size();
											//remove all the tasks before the remaining tasks index position 
											for(int k=0;k<position;k++)
												upRowPRT.tasks.remove(0);
											//this condition is not required, but for reducing the call of matchWithSRT() is written
											if(upRowPRT.tasks.size()>=backupTasksOfNewPUB.size())
												matchWithSRT(upRowPRT,true);//add boolean parameter for only update
										}
										else if(subList.get(rowPRT.sub.get(i)).pubIndex.size() == 0 && subList.get(rowPRT.sub.get(i)).rTaskIndex.equals("NULL"))
										{
											RowPRT upRowPRT = new RowPRT();
											for(int m=0;m<subList.get(rowPRT.sub.get(i)).tasks.size();m++)
												upRowPRT.tasks.add(subList.get(rowPRT.sub.get(i)).tasks.get(m));
											upRowPRT.sub.add(rowPRT.sub.get(i));
											//this condition is not required, but for reducing the call of matchWithSRT() is written
											if(upRowPRT.tasks.size()>=backupTasksOfNewPUB.size())
												matchWithSRT(upRowPRT,true);//add boolean parameter for only update
										}
									}//END: if: check for the availability in the subList
								}//END: for loop: check for all the subs in rowPRT
							}//END: if: Check whether rowPRT has sub
							//END: 2nd time: same checking as before in the previous if condition
						}//END: for loop: check all rowPRT in listOfRowPRT for matching SUB with new Pub
					}//END: if: Check whether rowPRT has followKeys
				}//END: else if: that means new Pub found in PRT
				break;//this is never happen but for security reason of infinite loop
			}//END: while loop
		}//END: main if condition inside method
	}//END: method
	//END: newPubMatchWithPRT()********************************************************
	
	//START: matchWithSRT(RowPRT,boolean)********************************************
	public static synchronized void matchWithSRT(RowPRT rowPRT, boolean upOnly)
	{
		String indexTask = rowPRT.tasks.get(0);
		//main if condition
		if(srt.containsKey(indexTask))
		{
			RowSRT rowSRT = (RowSRT)srt.get(indexTask);
			RowPRT commonRowPRT = new RowPRT();
			ArrayList<String> commonTasks = new ArrayList<String>();
			//for checking the size of the rowPRT which have to be >= rowSRT,
			//otherwise there is no matching in the SRT exists for this rowPRT
			if(rowSRT.tasks.size() <= rowPRT.tasks.size())
			while(tasksMatching(rowSRT.tasks, rowPRT.tasks) == 1)
			{
				//start: first if condition inside while loop
				if(rowSRT.pub.size() > 0)
				{
					ArrayList<String> combineTasks = new ArrayList<String>();
					if(commonTasks.size()>0)
					{
						for(int j=0;j<commonTasks.size();j++)
							combineTasks.add(commonTasks.get(j));
					}
					for(int k=0;k<rowPRT.tasks.size();k++)
						combineTasks.add(rowPRT.tasks.get(k));
				
					//final checking of compatibility with publication
					for(int i=0;i<rowSRT.pub.size();i++)
					{
						//for avoiding unexpected error like null pointer exception
						if(pubList.containsKey(rowSRT.pub.get(i)))
							if(tasksMatching(pubList.get(rowSRT.pub.get(i)).tasks,combineTasks) == 1)
							{
							//if(tasksMatching(rowSRT.tasks,rowPRT.tasks) == 1)
								commonRowPRT.pub.add(rowSRT.pub.get(i));
							}
					}
					//start: "if full compatible publication found"
					if(commonRowPRT.pub.size() > 0)
					{
						//Copy rowPRT.tasks until rowSRT.tasks.size to commonTasks
						for(int i=0;i<rowSRT.tasks.size();i++)
							commonTasks.add(rowPRT.tasks.get(i));
						//copy commonTasks to commonRow.tasks
						for(int i=0;i<commonTasks.size();i++)
							commonRowPRT.tasks.add(commonTasks.get(i));
					
						//remove commonTasks from rowPRT.tasks until rowSRT.tasks.size
						for(int i=0;i<rowSRT.tasks.size();i++)
							rowPRT.tasks.remove(0);
						
						//start: "innermost if-else"
						//checking for further processing of rowPRT
						if(rowPRT.tasks.size() > 0)
						{
							//keep sub reference with the matching part
							commonRowPRT.sub.add(rowPRT.sub.get(0));
							//take next tasks index to follow the remaining part of rowPRT
							commonRowPRT.followKeys.add(rowPRT.tasks.get(0));
							//in the corresponding subscription of subList, 
							//initialize the rTaskIndex for the unmatched tasks of rowPRT
							subList.get(rowPRT.sub.get(0)).rTaskIndex = rowPRT.tasks.get(0);
							//in the corresponding subscription of subList, 
							//assign matching publication index to pubIndex field of subList
							subList.get(rowPRT.sub.get(0)).pubIndex.addAll(commonRowPRT.pub);
							//if it's a neighbor sub then delete matched tasks
							if(!subList.get(rowPRT.sub.get(0)).resBroker.equals("NULL"))
							{
								//remove commonTasks from rowPRT.tasks until rowSRT.tasks.size
								for(int i=0;i<pubList.get(commonRowPRT.pub.get(0)).tasks.size();i++)
									subList.get(rowPRT.sub.get(0)).tasks.remove(0);
								subList.get(rowPRT.sub.get(0)).rTaskIndex = "NULL";
								subList.get(rowPRT.sub.get(0)).pubIndex.clear();
							}
							//empty the commonTasks because it already assigned to commonRowPRT
							//which will be inserted into PRT in the next line 
							commonTasks.clear();
							//START: parameter list for outQueueMsgWritingPRT() method
							String paramPubIndex = commonRowPRT.pub.get(0);
							String paramSubIndex = rowPRT.sub.get(0);
							ArrayList<String> paramTasks = new ArrayList<String>();
							for(int m = 0;m<commonRowPRT.tasks.size();m++)
								paramTasks.add(commonRowPRT.tasks.get(m));
							//END: parameter list for outQueueMsgWritingPRT() method
							
							//insert the matching part of subscription into PRT
							insertToPRT(commonRowPRT);
							//for forwarding call outQueueMsgWritingPRT() to write into outQueue
							outQueueMsgWritingPRT(paramPubIndex, paramSubIndex, paramTasks);							//after that no need to execute further the remaining instructions of method, 
							//unmatched part of subscription will be processed 
							//by calling matchWithPRT(rowPRT,upOnly) first, then with SRT
							matchWithPRT(rowPRT,upOnly);
							//after that no need to execute further the remaining  
							//instructions of method, so exit from the method
							return;
						}
						//no need to process rowPRT further, 
						//because rowPRT.tasks.size == rowSRT.tasks.size  
						else
						{
							//assign the subscription index of rowPRT to commonRowPRT.sub
							commonRowPRT.sub=rowPRT.sub;
							//in the corresponding subscription of subList clear the rTaskIndex,
							//because rowPRT is matched completely
							subList.get(rowPRT.sub.get(0)).rTaskIndex = "NULL";
							//in the corresponding subscription of subList 
							//assign matching publication index to pubIndex of subList
							subList.get(rowPRT.sub.get(0)).pubIndex.addAll(commonRowPRT.pub);
							if(!subList.get(rowPRT.sub.get(0)).resBroker.equals("NULL"))
								subList.remove(rowPRT.sub.get(0));
							//empty the commonTasks because it already assigned to commonRowPRT
							//which will be inserted into PRT in the next line 
							commonTasks.clear();
							//START: parameter list for outQueueMsgWritingPRT() method
							String paramPubIndex = commonRowPRT.pub.get(0);
							String paramSubIndex = rowPRT.sub.get(0);
							ArrayList<String> paramTasks = new ArrayList<String>();
							for(int m = 0;m<commonRowPRT.tasks.size();m++)
								paramTasks.add(commonRowPRT.tasks.get(m));
							//END: parameter list for outQueueMsgWritingPRT() method
							
							//insert the subscription into PRT which has matching publication
							insertToPRT(commonRowPRT);
							//for forwarding call outQueueMsgWritingPRT() to write into outQueue
							outQueueMsgWritingPRT(paramPubIndex, paramSubIndex, paramTasks);							//after that no need to execute further the remaining instructions of method, 
							//so exit from the method
							return;	
						}
						//end: "innermost if-else"
						//break;
					}//end: "if full compatible publication found"
				}//end: first if condition inside while
				
				//start: second if condition inside while loop
				//now checking for the next while loop processing, 
				//if rowPRT have more tasks than rowSRT
				if(rowPRT.tasks.size() > rowSRT.tasks.size())
				{
					int flag = 0;
					//checking for the compatibility of rowSRT.followKeys for the next processing 
					//of rowPRT 
					for(int i=0;i<rowSRT.followKeys.size();i++)
					{
						if(rowSRT.followKeys.get(i).equals(rowPRT.tasks.get(rowSRT.tasks.size())))
						{
							flag=1; //that means next matching index found in the SRT
						}
					}
					//if next matching SRT index found
					if(flag==1)
					{
						//copy the matching rowPRT.tasks to commonTasks up to rowSRT.tasks.size
						for(int i=0;i<rowSRT.tasks.size();i++)
						{
							commonTasks.add(rowPRT.tasks.get(i));
						}
						//remove the matching rowPRT.tasks up to rowSRT.tasks.size
						for(int i=0;i<rowSRT.tasks.size();i++)
						{
							rowPRT.tasks.remove(0);
						}
						//next matching index of SRT will be checked with the remaining rowPRT
						rowSRT =  (RowSRT)srt.get(rowPRT.tasks.get(0));
						continue;
					}
				}//end: second if condition inside while
				break;
			}//end of while loop
			
			//if there is no complete matching with publication found in the SRT 
			//but partial matching found or there is no matching found at all in the SRT
			if(commonTasks.size()>0) //that means partial matching found
			{
				for(int i=0;i<rowPRT.tasks.size();i++)
					commonTasks.add(rowPRT.tasks.get(i));
				rowPRT.tasks=commonTasks;
				//START: parameter list for outQueueMsgWritingPRT() method
				String paramPubIndex = "NULL";
				String paramSubIndex = rowPRT.sub.get(0);
				ArrayList<String> paramTasks = new ArrayList<String>();
				for(int m = 0;m<rowPRT.tasks.size();m++)
					paramTasks.add(rowPRT.tasks.get(m));
				//END: parameter list for outQueueMsgWritingPRT() method
				
				//If PRT only need to update (already contains this part, don't need to insert again),
				//so return the method if upOnly==true
				if(upOnly)
				{
					//forward this unmatched part to neighbor for search
					forward4SearchSub(rowPRT);
					return;
				}					
				//insert unmatched subscription into PRT
				insertToPRT(rowPRT);
				//for forwarding call outQueueMsgWritingPRT() to write into outQueue
				outQueueMsgWritingPRT(paramPubIndex, paramSubIndex, paramTasks);//unmatched
				//forward this unmatched part to neighbor for search
				forward4SearchSub(rowPRT);
			}
			else //that means no matching found at all
			{
				//START: parameter list for outQueueMsgWritingPRT() method
				String paramPubIndex = "NULL";
				String paramSubIndex = rowPRT.sub.get(0);
				ArrayList<String> paramTasks = new ArrayList<String>();
				for(int m = 0;m<rowPRT.tasks.size();m++)
					paramTasks.add(rowPRT.tasks.get(m));
				//END: parameter list for outQueueMsgWritingPRT() method
				
				//If PRT only need to update (already contains this part, don't need to insert again),
				//so return the method if upOnly==true
				if(upOnly)
				{
					//forward this unmatched part to neighbor for search
					forward4SearchSub(rowPRT);
					return;
				}					
				//insert unmatched subscription into PRT
				insertToPRT(rowPRT);
				//for forwarding call outQueueMsgWritingPRT() to write into outQueue
				outQueueMsgWritingPRT(paramPubIndex, paramSubIndex, paramTasks);//unmatched
				//forward this unmatched part to neighbor for search
				forward4SearchSub(rowPRT);
			}
		}//end: main if condition 
		// Start: main else
		//that means, there is no matching in the SRT, main if condition is false
		else 
		{
			//START: parameter list for outQueueMsgWritingPRT() method
			String paramPubIndex = "NULL";
			String paramSubIndex = rowPRT.sub.get(0);
			ArrayList<String> paramTasks = new ArrayList<String>();
			for(int m = 0;m<rowPRT.tasks.size();m++)
				paramTasks.add(rowPRT.tasks.get(m));
			//END: parameter list for outQueueMsgWritingPRT() method
			
			//If PRT only need to update (already contains this part, don't need to insert again),
			//so return the method if upOnly==true
			if(upOnly)
			{
				//forward this unmatched part to neighbor for search
				forward4SearchSub(rowPRT);
				return;
			}				
			//insert unmatched subscription into PRT
			insertToPRT(rowPRT);
			//for forwarding call outQueueMsgWritingPRT() to write into outQueue
			outQueueMsgWritingPRT(paramPubIndex, paramSubIndex, paramTasks);//unmatched
			//forward this unmatched part to neighbor for search
			forward4SearchSub(rowPRT);
		} //end: main else
	}//end of method matchWithSRT()
	//END: matchWithSRT(RowPRT,boolean)**********************************************
	
	//START: matchWithPRT(RowPRT,boolean)********************************************
	public static synchronized void matchWithPRT(RowPRT newRowPRT, boolean upOnly)
	{
		String indexTask = newRowPRT.tasks.get(0);
		//main if condition
		if(prt.containsKey(indexTask))
		{
			RowPRT oldRowPRT = (RowPRT)prt.get(indexTask);
			RowPRT commonRowPRT = new RowPRT();
			ArrayList<String> commonTasks = new ArrayList<String>();
			//for checking the size of the newRowPRT which have to => than oldRowPRT,
			//otherwise there is no matching in the PRT exists for this newRowPRT
			if(oldRowPRT.tasks.size() <= newRowPRT.tasks.size())
			while(tasksMatching(oldRowPRT.tasks, newRowPRT.tasks) == 1)
			{
				//start: first if condition inside while loop
				if(oldRowPRT.pub.size() > 0)
				{
					ArrayList<String> combineTasks = new ArrayList<String>();
					if(commonTasks.size()>0)
					{
						for(int j=0;j<commonTasks.size();j++)
							combineTasks.add(commonTasks.get(j));
					}
					for(int k=0;k<newRowPRT.tasks.size();k++)
						combineTasks.add(newRowPRT.tasks.get(k));
					//final checking of compatibility with publication
					for(int i=0;i<oldRowPRT.pub.size();i++)
					{
						//for avoiding unexpected error like null pointer exception
						if(pubList.containsKey(oldRowPRT.pub.get(i)))
							if(tasksMatching(pubList.get(oldRowPRT.pub.get(i)).tasks,combineTasks) == 1)
							{
								//assign the matched pub index to commmonRowPRT
								commonRowPRT.pub.add(oldRowPRT.pub.get(i));
							}
					}
					//start: "if full compatible publication found"
					if(commonRowPRT.pub.size() > 0)
					{
						//Copy tasks from newRowPRT.tasks until oldRowPRT.tasks.size 
						//to commonTasks
						for(int i=0;i<oldRowPRT.tasks.size();i++)
							commonTasks.add(newRowPRT.tasks.get(i));
						//copy commonTasks to commonRow.tasks
						for(int i=0;i<commonTasks.size();i++)
							commonRowPRT.tasks.add(commonTasks.get(i));

						//remove tasks from newRowPRT.tasks until oldRowPRT.tasks.size
						for(int i=0;i<oldRowPRT.tasks.size();i++)
						{
							//index used here is 0, because every time arrayList elements 
							//shifted to left after deleting any prior element
							newRowPRT.tasks.remove(0);  
						}
						//start of "innermost if-else"
						//checking for further processing of newRowPRT is required 
						if(newRowPRT.tasks.size() > 0)
						{
							//keep sub reference with the matching part
							commonRowPRT.sub.add(newRowPRT.sub.get(0));
							//take next tasks index to follow the newRowPRT
							commonRowPRT.followKeys.add(newRowPRT.tasks.get(0));
							//in the corresponding subscription of subList, 
							//initialize the rTaskIndex for the unmatched tasks
							subList.get(newRowPRT.sub.get(0)).rTaskIndex = newRowPRT.tasks.get(0);
							//in the corresponding subscription of subList, 
							//initialize the pub by matched publication index
							subList.get(newRowPRT.sub.get(0)).pubIndex.addAll(commonRowPRT.pub);
							//if it's a neighbor sub then delete matched tasks
							if(!subList.get(newRowPRT.sub.get(0)).resBroker.equals("NULL"))
							{
								//remove commonTasks from rowPRT.tasks until rowSRT.tasks.size
								for(int i=0;i<pubList.get(commonRowPRT.pub.get(0)).tasks.size();i++)
									subList.get(newRowPRT.sub.get(0)).tasks.remove(0);
								subList.get(newRowPRT.sub.get(0)).rTaskIndex = "NULL";
								subList.get(newRowPRT.sub.get(0)).pubIndex.clear();
							}
							//empty the commonTasks because it already assigned to commonRowPRT
							//which will be inserted into PRT in the next line 
							commonTasks.clear();
							//START: parameter list for outQueueMsgWritingPRT() method
							String paramPubIndex = commonRowPRT.pub.get(0);
							String paramSubIndex = newRowPRT.sub.get(0);
							ArrayList<String> paramTasks = new ArrayList<String>();
							for(int m = 0;m<commonRowPRT.tasks.size();m++)
								paramTasks.add(commonRowPRT.tasks.get(m));
							//END: parameter list for outQueueMsgWritingPRT() method
							
							//insert the matching part of subscription into PRT
							insertToPRT(commonRowPRT);
							//for forwarding call outQueueMsgWritingPRT() to write into outQueue
							outQueueMsgWritingPRT(paramPubIndex, paramSubIndex, paramTasks);
							//unmatched part of subscription will be processed 
							//by calling matchWithPRT(newRowPRT,upOnly) again
							matchWithPRT(newRowPRT,upOnly);
							//after that no need to execute further the remaining instructions of method, 
							//so exit from the method
							return;
						}
						//no need to process newRowPRT further, 
						//because newRowPRT.tasks.size == oldRowPRT.tasks.size  
						else
						{
							//assign the subscription index of newRowPRT to commonRowPRT.sub
							commonRowPRT.sub=newRowPRT.sub;
							//in the corresponding subscription, clear the rTaskIndex 
							subList.get(newRowPRT.sub.get(0)).rTaskIndex = "NULL";
							//in the corresponding subscription, assign matching publication index to pub
							subList.get(newRowPRT.sub.get(0)).pubIndex.addAll(commonRowPRT.pub);
							//if it's a neighbor sub then delete it from subList as complete match found
							if(!subList.get(newRowPRT.sub.get(0)).resBroker.equals("NULL"))
								subList.remove(newRowPRT.sub.get(0));
							//empty the commonTasks because it already assigned to commonRowPRT
							//which will be inserted into PRT in the next line 
							commonTasks.clear();
							//START: parameter list for outQueueMsgWritingPRT() method  
							String paramPubIndex = commonRowPRT.pub.get(0);
							String paramSubIndex = newRowPRT.sub.get(0);
							ArrayList<String> paramTasks = new ArrayList<String>();
							for(int m = 0;m<commonRowPRT.tasks.size();m++)
								paramTasks.add(commonRowPRT.tasks.get(m));
							//END: parameter list for outQueueMsgWritingPRT() method
							
							//insert the subscription into PRT which has matching publication
							insertToPRT(commonRowPRT);
							//for forwarding call outQueueMsgWritingPRT() to write into outQueue
							outQueueMsgWritingPRT(paramPubIndex, paramSubIndex, paramTasks);							//writeOutqueue4FrowardPRT("B2",newRowPRT,commonRowPRT.pub.get(0),commonRowPRT.tasks);
							//after that no need to execute further the remaining instructions of method, 
							//so exit from the method
							return;	
						}
						//end: "innermost if-else"
						//break;
					}//end: "if full compatible publication found"
				}//end: first if condition inside while
				
				//start: second if condition inside while loop
				//now checking for the next while loop processing,
				//if newRowPRT have more tasks than oldRowPRT
				if(newRowPRT.tasks.size() > oldRowPRT.tasks.size())
				{
					int flag = 0;
					//checking for the compatibility of oldRowPRT.followKeys for the next processing 
					for(int i=0;i<oldRowPRT.followKeys.size();i++)
					{
						if(oldRowPRT.followKeys.get(i).equals(newRowPRT.tasks.get(oldRowPRT.tasks.size())))
						{
							flag=1; //next matching index found in PRT
						}
					}
					//if next matching index found in PRT
					if(flag==1)
					{
						//copy the matching newRowPRT.tasks to commonTasks up to oldRowPRT.tasks.size
						for(int i=0;i<oldRowPRT.tasks.size();i++)
						{
							commonTasks.add(newRowPRT.tasks.get(i));
						}
						//remove the matching newRowPRT.tasks up to oldRowPRT.tasks.size
						for(int i=0;i<oldRowPRT.tasks.size();i++)
						{
							newRowPRT.tasks.remove(0);
						}
						//now next matching index of PRT will be checked with the remaining newRowPRT
						oldRowPRT =  (RowPRT)prt.get(newRowPRT.tasks.get(0));
						continue;
					}
				}//end: second if condition inside while
				break;
			}//end of while loop
			
			//if there is no complete matching with publication in the PRT but only partial matching
			//or no matching at all in the PRT then call matchWithSRT() 
			//whether SRT has any complete publication matching with newRowPRT
			if(commonTasks.size()>0) //that means have some partial matching
			{
				for(int i=0;i<newRowPRT.tasks.size();i++)
					commonTasks.add(newRowPRT.tasks.get(i));
				newRowPRT.tasks=commonTasks;
				//insertToPRT(newRowPRT);
				//search matching in the SRT because there is no match in PRT
				//matchWithSRT(newRowPRT);
				matchWithSRT(newRowPRT,upOnly);
			}
			else //no matching at all so far in the PRT
			{
				//insertToPRT(newRowPRT);
				//search matching in the SRT because there is no match in PRT
				//matchWithSRT(newRowPRT);
				matchWithSRT(newRowPRT,upOnly);
			}
		}//end: main if condition 
		//Start: main else
		else //that means main if condition false, so there is no matching found in the PRT
		{
			//insertToPRT(newRowPRT);
			//search matching in the SRT because there is no match in PRT
			//matchWithSRT(newRowPRT);
			matchWithSRT(newRowPRT,upOnly);
		}
	}//end of method matchWithPRT()	
	//END: matchWithPRT(RowPRT,boolean)**********************************************
	
	//Start: tasksMatching(ArrayList,ArrayList)*********************************************
	public static int tasksMatching(ArrayList<String> tasksSRT, ArrayList<String> tasksPRT)
	{
		if(tasksSRT.size() <= tasksPRT.size())
		{
			for(int j = 0;j < tasksSRT.size();j++)
			{
				if(tasksSRT.get(j).equals(tasksPRT.get(j)))
				{
					continue;
				}
				return 0;
			}
			return 1;
		}
		else
			return 0;
	}
	//END: tasksMatching(ArrayList,ArrayList)***********************************************
	
	//START: insertToPRT()******************************************************
	//this method insert row into PRT table efficiently
	public static synchronized void insertToPRT(RowPRT newRow)
	{
		String indexTask=newRow.tasks.get(0);
		if(prt.containsKey(indexTask))
		{
			ArrayList<String> commonTasks = new ArrayList<String>();
			ArrayList<String> newTasks = new ArrayList<String>();
			ArrayList<String> oldTasks = new ArrayList<String>();
			RowPRT oldRow=(RowPRT)prt.get(indexTask);//existing row in PRT
			RowPRT commonRow = new RowPRT();
			
			//taking the matching part from both
			for(int i=0;i<oldRow.tasks.size()&& i<newRow.tasks.size();i++)
			{
				if(oldRow.tasks.get(i).equals(newRow.tasks.get(i)))
				{
					commonTasks.add(oldRow.tasks.get(i));
					continue;
				}
				break;
			}
			//assign the common tasks part to the commonRow
			commonRow.tasks=commonTasks;
			//remove the common tasks from the new and existing row, 
			//so that both contains only the unmatched part of them
			if(commonTasks.size()<oldRow.tasks.size())
			{
				for(int i=commonTasks.size();i<oldRow.tasks.size();i++)
				{
					oldTasks.add(oldRow.tasks.get(i));
				}
				oldRow.tasks=oldTasks;
				//check whether commonRow has any followKeys
				if(commonRow.followKeys.size()>0)
				{
					//for avoiding duplication
					if(commonRow.followKeys.contains(oldRow.tasks.get(0))==false)//if not contain than add
						commonRow.followKeys.add(oldRow.tasks.get(0));
				}
				else
					commonRow.followKeys.add(oldRow.tasks.get(0));
				//prepare row for insert into PRT
				RowPRT insertRowPRT = prepareRow4InsertToPRT(oldRow);
				insertToPRT(insertRowPRT);
				//insertToPRT(oldRow);
			}
			else if(commonTasks.size()==oldRow.tasks.size())
			{
				commonRow.followKeys=oldRow.followKeys;
				commonRow.sub=oldRow.sub;
				commonRow.pub=oldRow.pub;
				commonRow.sentToBroker=oldRow.sentToBroker;
			}
			if(commonTasks.size()<newRow.tasks.size())
			{
				for(int i=commonTasks.size();i<newRow.tasks.size();i++)
				{
					newTasks.add(newRow.tasks.get(i));
				}
				newRow.tasks=newTasks;
				//check whether commonRow has any followKeys
				if(commonRow.followKeys.size()>0)
				{
					//for avoiding duplication
					if(commonRow.followKeys.contains(newRow.tasks.get(0))==false)//if not contain than add
						commonRow.followKeys.add(newRow.tasks.get(0));
				}
				else
					commonRow.followKeys.add(newRow.tasks.get(0));
				//prepare row for insert into PRT
				RowPRT insertRowPRT = prepareRow4InsertToPRT(newRow);
				insertToPRT(insertRowPRT);
				//insertToPRT(newRow);
			}
			else if(commonTasks.size()==newRow.tasks.size())
			{
				if(newRow.sub.size()>0)//for avoiding null pointer exception
				{
					//for avoiding duplication
					if(commonRow.sub.contains(newRow.sub.get(0))==false)//if not contain than add
						//newRow only has one ServiceName, so assign only the first index of sub
						commonRow.sub.add(newRow.sub.get(0));
				}
				if(newRow.followKeys.size()>0)//for avoiding null pointer exception
				{
					//for avoiding duplication
					if(commonRow.followKeys.contains(newRow.followKeys.get(0))==false)
						//newRow at most might have one followKeys, 
						//so assign only the first index of followKeys
						commonRow.followKeys.add(newRow.followKeys.get(0));
				}
				if(newRow.pub.size()>0)//for avoiding null pointer exception
				{
					//for avoiding duplication
					if(commonRow.pub.contains(newRow.pub.get(0))==false)
						//newRow at most might have one pub, but could have more than one in real life 
						//so assign by using loop for being in safe side
						for(int i=0;i<newRow.pub.size();i++)
							commonRow.pub.add(newRow.pub.get(i));
				}
				if(newRow.sentToBroker.size()>0)//for avoiding null pointer exception
				{
					//for avoiding duplication
					if(commonRow.sentToBroker.contains(newRow.sentToBroker.get(0))==false)
						//newRow at most might have one sentToBroker, but could have more than one in real life 
						//so assign by using loop for being in safe side
						for(int i=0;i<newRow.sentToBroker.size();i++)
							commonRow.sentToBroker.add(newRow.sentToBroker.get(i));
				}
			}
			//remove the existing row
			prt.remove(indexTask);
			//insert the commonRow into SRT
			prt.put(commonRow.tasks.get(0), commonRow);
		}
		else
		{
			prt.put(indexTask, newRow);
			//forward this new entry to next neighbors
		}	
	}//END: insertToPRT()
	//END: insertToPRT()******************************************************

	//START: insertToSRT(rowSRT, pubIndex)******************************************************
	//this method insert row into SRT table efficiently
	public static synchronized void insertToSRT(RowSRT newRow, String pubIndex)
	{
		String indexTask=newRow.tasks.get(0);
		//START: main if condition inside method
		if(srt.containsKey(indexTask))
		{
			isFullRowSRT = 0;
			ArrayList<String> commonTasks = new ArrayList<String>();
			ArrayList<String> newTasks = new ArrayList<String>();
			ArrayList<String> oldTasks = new ArrayList<String>();
			RowSRT oldRow=(RowSRT)srt.get(indexTask);
			RowSRT commonRow = new RowSRT();
			
			//taking the matching part from both
			for(int i=0;i<oldRow.tasks.size()&& i<newRow.tasks.size();i++)
			{
				if(oldRow.tasks.get(i).equals(newRow.tasks.get(i)))
				{
					commonTasks.add(oldRow.tasks.get(i));
					continue;
				}
				break;
			}
			//assign the common tasks part to the commonRow
			commonRow.tasks=commonTasks;
			//remove the common tasks from the new and existing row, 
			//so that both contains only the unmatched part of them
			if(commonTasks.size()<oldRow.tasks.size())
			{
				for(int i=commonTasks.size();i<oldRow.tasks.size();i++)
				{
					oldTasks.add(oldRow.tasks.get(i));
				}
				oldRow.tasks=oldTasks;
				//check whether commonRow has any followKeys
				if(commonRow.followKeys.size()>0)
				{
					//for avoiding duplication
					if(commonRow.followKeys.contains(oldRow.tasks.get(0))==false)//if not contain than add
						commonRow.followKeys.add(oldRow.tasks.get(0));
				}
				else
					commonRow.followKeys.add(oldRow.tasks.get(0));
				insertToSRT(oldRow, pubIndex);
				//assign the index of this row to the commonRow's followKeys 
				//commonRow.followKeys.add(oldRow.tasks.get(0));
			}
			else if(commonTasks.size()==oldRow.tasks.size())
			{
				commonRow.followKeys=oldRow.followKeys;
				commonRow.pub=oldRow.pub;
				commonRow.sentToBroker=oldRow.sentToBroker;
			}
			if(commonTasks.size()<newRow.tasks.size())
			{
				for(int i=commonTasks.size();i<newRow.tasks.size();i++)
				{
					newTasks.add(newRow.tasks.get(i));
				}
				newRow.tasks=newTasks;
				//check whether commonRow has any followKeys
				if(commonRow.followKeys.size()>0)
				{
					//for avoiding duplication
					if(commonRow.followKeys.contains(newRow.tasks.get(0))==false)//if not contain than add
						commonRow.followKeys.add(newRow.tasks.get(0));
				}
				else
					commonRow.followKeys.add(newRow.tasks.get(0));
				insertToSRT(newRow, pubIndex);
				//assign the index of this row to the commonRow's followKeys 
				//commonRow.followKeys.add(newRow.tasks.get(0));
			}
			else if(commonTasks.size()==newRow.tasks.size())
			{
				//newRow only has the ServiceName, so assign only the first index of pub
				//commonRow.pub.add(newRow.pub.get(0));
				if(newRow.pub.size()>0)//for avoiding null pointer exception
				{
					//for avoiding duplication
					if(commonRow.pub.contains(newRow.pub.get(0))==false)//if not contain than add
						//newRow only has one ServiceName, so assign only the first index of sub
						commonRow.pub.add(newRow.pub.get(0));
				}
				//if newRow contains sentToBroker than add
				if(newRow.sentToBroker.size()>0)//for avoiding null pointer exception
				{
					if(commonRow.sentToBroker.size()>0)
					{
						//for avoiding duplication
						if(commonRow.sentToBroker.contains(newRow.sentToBroker.get(0))==false)//if not contain than add
							//newRow only has one sentToBroker, so assign only the first index of sentToBroker
							commonRow.sentToBroker.add(newRow.sentToBroker.get(0));
					}
					else
						commonRow.sentToBroker.add(newRow.sentToBroker.get(0));
				}
				//this case seems to never happen but still written for security reason 
				if(newRow.followKeys.size()>0)//for avoiding null pointer exception
				{
					if(commonRow.followKeys.size()>0)
					{
						//for avoiding duplication
						if(commonRow.followKeys.contains(newRow.followKeys.get(0))==false)//if not contain than add
							//newRow usually should not contain any followKeys
							commonRow.followKeys.add(newRow.followKeys.get(0));
					}
					else
						commonRow.followKeys.add(newRow.followKeys.get(0));
				}
			}
			//remove the existing row
			srt.remove(indexTask);
			//insert the commonRow into SRT
			srt.put(commonRow.tasks.get(0), commonRow);
			
			//START: for forwarding call outQueueMsgWritingSRT() to write into outQueue
			ArrayList<String> forwardTasks = new ArrayList<String>();
			for(int z=0;z<commonRow.tasks.size();z++)
				forwardTasks.add(commonRow.tasks.get(z));
			outQueueMsgWritingSRT(pubIndex, forwardTasks);
			//END: for forwarding call outQueueMsgWritingSRT() to write into outQueue
			
			isFullRowSRT = 1;//set back to it's initial value
		}//END: main if condition inside method
		else
		{
			srt.put(indexTask, newRow);
			//START: for forwarding write into outQueue
			if(isFullRowSRT==1)
			{
				//START: for forwarding call outQueueMsgWritingSRT() to write into outQueue
				ArrayList<String> forwardTasks = new ArrayList<String>();
				for(int z=0;z<newRow.tasks.size();z++)
					forwardTasks.add(newRow.tasks.get(z));
				outQueueMsgWritingSRT(pubIndex, forwardTasks);
				//END: for forwarding call outQueueMsgWritingSRT() to write into outQueue
			}
			//END: for forwarding write into outQueue
		}	
	}//END: method insertToSRT(rowSRT, pubIndex)	
	//END: insertToSRT(rowSRT, pubIndex)********************************************************
			
	//START: prepareRow4InsertToPRT(rowPRT)*****************************************************
	//this method prepare row for PRT for avoiding unexpected bugs
	public static RowPRT prepareRow4InsertToPRT(RowPRT rp)
	{
		RowPRT insertRowPRT = new RowPRT();
		if(rp.sub.size()>0)
			for(int i=0;i<rp.sub.size();i++)
				insertRowPRT.sub.add(rp.sub.get(i));
		if(rp.pub.size()>0)
			for(int i=0;i<rp.pub.size();i++)
				insertRowPRT.pub.add(rp.pub.get(i));
		if(rp.tasks.size()>0)
			for(int i=0;i<rp.tasks.size();i++)
				insertRowPRT.tasks.add(rp.tasks.get(i));
		if(rp.sentToBroker.size()>0)
			for(int i=0;i<rp.sentToBroker.size();i++)
				insertRowPRT.sentToBroker.add(rp.sentToBroker.get(i));
		if(rp.followKeys.size()>0)
			for(int i=0;i<rp.followKeys.size();i++)
				insertRowPRT.followKeys.add(rp.followKeys.get(i));
		return insertRowPRT;
	}
	//END: prepareRow4InsertToPRT(rowPRT)*****************************************************

	//START: prepareRow4InsertToSRT(rowSRT)*****************************************************
	//this method prepare row for SRT for avoiding unexpected bugs
	public static RowSRT prepareRow4InsertToSRT(RowSRT rs)
	{
		RowSRT insertRowSRT = new RowSRT();
		if(rs.pub.size()>0)
			for(int i=0;i<rs.pub.size();i++)
				insertRowSRT.pub.add(rs.pub.get(i));
		if(rs.tasks.size()>0)
			for(int i=0;i<rs.tasks.size();i++)
				insertRowSRT.tasks.add(rs.tasks.get(i));
		if(rs.sentToBroker.size()>0)
			for(int i=0;i<rs.sentToBroker.size();i++)
				insertRowSRT.sentToBroker.add(rs.sentToBroker.get(i));
		if(rs.followKeys.size()>0)
			for(int i=0;i<rs.followKeys.size();i++)
				insertRowSRT.followKeys.add(rs.followKeys.get(i));
		return insertRowSRT;
	}
	//END: prepareRow4InsertToSRT(rowSRT)*****************************************************	
	
	//START: displaySRT()*******************************************************
	//this method display the contents of SRT table
	public static synchronized void displaySRT()
	{
		// Get a set of the entries 
		Set<Entry<String, RowSRT>> set = srt.entrySet(); 
		// Get an iterator 
		Iterator<Entry<String, RowSRT>> i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) 
		{ 
			Map.Entry<String, RowSRT> me = (Map.Entry<String, RowSRT>)i.next(); 
			System.out.print(me.getKey() + ": "); 
			RowSRT temp = (RowSRT)me.getValue();
			temp.displayContents();
		}
	}
	//END: displaySRT()*******************************************************
	
	//START: displayPRT()*******************************************************
	public static synchronized void displayPRT()
	{
		// Get a set of the entries 
		Set<Entry<String, RowPRT>> set = prt.entrySet(); 
		// Get an iterator 
		Iterator<Entry<String, RowPRT>> i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) 
		{ 
			Map.Entry<String, RowPRT> me = (Map.Entry<String, RowPRT>)i.next(); 
			System.out.print(me.getKey() + ": "); 
			RowPRT temp = (RowPRT)me.getValue();
			temp.displayContents();
		}
	}//END: displayPRT()
	//END: displayPRT()********************************************************
		
	//START: displayPubList()**************************************************
	//this method display the contents of pubList table
	public static synchronized void displayPubList()
	{
		// Get a set of the entries 
		Set<Entry<String, RowPubList>> set = pubList.entrySet(); 
		// Get an iterator 
		Iterator<Entry<String, RowPubList>> i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) 
		{ 
			Map.Entry<String, RowPubList> me = (Map.Entry<String, RowPubList>)i.next(); 
			System.out.print(me.getKey() + ": "); 
			RowPubList temp = (RowPubList)me.getValue();
			ArrayList<String> tasks = (ArrayList<String>)temp.tasks;
			String resBroker=temp.resBroker;
			for(int j=0;j<tasks.size();j++)
				System.out.print("-->"+tasks.get(j));
			System.out.print("\t");
			System.out.print(resBroker);
			System.out.println();
		}
	}
	//END: displayPubList()*****************************************************
	
	//START: displaySubList()***************************************************
	//this method display the contents of subList table
	public static synchronized void displaySubList()
	{
		// Get a set of the entries 
		Set<Entry<String, RowSubList>> set = subList.entrySet(); 
		// Get an iterator 
		Iterator<Entry<String, RowSubList>> i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) 
		{ 
			Map.Entry<String, RowSubList> me = (Map.Entry<String, RowSubList>)i.next(); 
			System.out.print(me.getKey() + ": "); 
			RowSubList temp = (RowSubList)me.getValue();
			ArrayList<String> tasks = (ArrayList<String>)temp.tasks;
			ArrayList<String> pubIndex = (ArrayList<String>)temp.pubIndex;
			String rTaskIndex=temp.rTaskIndex;
			String resBroker=temp.resBroker;
			for(int j=0;j<tasks.size();j++)
				System.out.print("-->"+tasks.get(j));
			System.out.print("\t");
			if(pubIndex.size()>0)
				for(int j=0;j<pubIndex.size();j++)
					System.out.print("-"+pubIndex.get(j));
			else
				System.out.print("NULL");
			System.out.print("\t");
			System.out.print(rTaskIndex);
			System.out.print("\t");
			System.out.print(resBroker);
			System.out.println();
		}
	}
	//END: displaySubList()*****************************************************
	
	//START: displayAllTable()*****************************************************
	public static synchronized void displayAllTable()
	{
		System.out.println("");
		System.out.println("**********::Contents of Tables::**********");
		System.out.println("SRT Table: "+srt.keySet());
		displaySRT();
		System.out.println("Publication Table: "+pubList.keySet());
		displayPubList();
		System.out.println("PRT Table: "+prt.keySet());
		displayPRT();
		System.out.println("Subscription Table: "+subList.keySet());
		displaySubList();
	}
	//END: displayAllTable()*****************************************************
	
	//START: insertToPubList(pub,tasks)*****************************************************
	public static synchronized void insertToPubList(String pub, ArrayList<String> tasks)
	{
		RowPubList rowPubList = new RowPubList();
		for(int z=0;z<tasks.size();z++)
			rowPubList.tasks.add(tasks.get(z));
		pubList.put(pub, rowPubList);
	}
	//END: insertToPubList(pub,tasks)*****************************************************
	
	//START: insertToPubList(pub,tasks,resBroker)*****************************************************
	public static synchronized void insertToPubList(String pub, ArrayList<String> tasks, String resBroker)
	{
		RowPubList rowPubList = new RowPubList();
		for(int z=0;z<tasks.size();z++)
			rowPubList.tasks.add(tasks.get(z));
		rowPubList.resBroker = resBroker;
		pubList.put(pub, rowPubList);
	}
	//END: insertToPubList(pub,tasks,resBroker)*****************************************************
	
	//START: insertToSubList(sub,tasks)*****************************************************
	public static synchronized void insertToSubList(String sub, ArrayList<String> tasks)
	{
		RowSubList rowSubList = new RowSubList();
		for(int z=0;z<tasks.size();z++)
			rowSubList.tasks.add(tasks.get(z));
		subList.put(sub, rowSubList);
	}
	//END: insertToSubList(sub,tasks)*****************************************************
	
	//START: insertToSubList(sub,tasks,resBroker)*****************************************************
	public static synchronized void insertToSubList(String sub, ArrayList<String> tasks, String resBroker)
	{
		RowSubList rowSubList = new RowSubList();
		for(int z=0;z<tasks.size();z++)
			rowSubList.tasks.add(tasks.get(z));
		rowSubList.resBroker = resBroker;
		subList.put(sub, rowSubList);
	}
	//END: insertToSubList(sub,tasks,resBroker)*****************************************************
	
	//START: inQueueMsgExecutionSRT()*****************************************************
	public static synchronized void inQueueMsgExecutionSRT(String msg)
	{
		String msgArray[] = msg.split(",");
		String msgSentToBroker = msgArray[1];
		String msgPubIndex = msgArray[2];
		//own publication so no need to execute this msg
		if(pubList.containsKey(msgPubIndex))		
			return;
		//already executed this msg, so no need to execute again
		if(pubList4Check.containsKey(msgPubIndex))
			return;
		RowSRT rs = new RowSRT();
		rs.sentToBroker.add(msgSentToBroker);
		for(int k=3;k<msgArray.length;k++)
			rs.tasks.add(msgArray[k]);
		pubList4Check.put(msgPubIndex,msgSentToBroker);
		insertToSRT(rs,msgPubIndex);
		//display all the tables contents together
		displayAllTable();
	}
	//END: inQueueMsgExecutionSRT()*****************************************************
	
	//START: inQueueMsgExecutionPRT()*****************************************************
	public static synchronized void inQueueMsgExecutionPRT(String msg)
	{
		//check whether this msg need to execute or not
		if(inQueueMsgCheckPRT(msg))
			return;//no need to execute this msg
		String msgArray[] = msg.split(",");//store whole msg in msgArray
		String msgSentToBroker = msgArray[1];//store sentToBroker in msgSentToBroker
		String msgPubIndex = msgArray[2];//store pubIndex in msgPubIndex
		String msgSubIndex = msgArray[3];//store subIndex in msgSubIndex
		ArrayList<String> msgTasks = new ArrayList<String>();//Store tasks in msgTasks
		for(int k=4;k<msgArray.length;k++)
			msgTasks.add(msgArray[k]);
		RowPRT rp = new RowPRT();//create rp to insert into PRT
		rp.sentToBroker.add(msgSentToBroker);
		for(int k=0;k<msgTasks.size();k++)
			rp.tasks.add(msgTasks.get(k));
		//START: if: Matching
		if(!msgPubIndex.equals("NULL"))
		{
			//insert the matched publication into it's own pubList
			if(!pubList.containsKey(msgPubIndex))
				insertToPubList(msgPubIndex, msgTasks, msgSentToBroker);
		
			rp.pub.add(msgPubIndex);
			if(subList.containsKey(msgSubIndex))
			{
				//START: if: own sub
				if(subList.get(msgSubIndex).resBroker.equals("NULL"))
				{
					subList.get(msgSubIndex).pubIndex.add(msgPubIndex);
					if(subList.get(msgSubIndex).rTaskIndex.equals("NULL"))//no matching yet
					{
						if(subList.get(msgSubIndex).tasks.size()>msgTasks.size())
							subList.get(msgSubIndex).rTaskIndex=subList.get(msgSubIndex).tasks.get(msgTasks.size());
						else
							subList.get(msgSubIndex).rTaskIndex = "NULL";//subscription complete 
					}
					else//already partially matched
					{
						int position=0;//get the position of remaining tasks index
						for(int j=0; j<subList.get(msgSubIndex).pubIndex.size();j++)
							position += pubList.get(subList.get(msgSubIndex).pubIndex.get(j)).tasks.size();
						if((subList.get(msgSubIndex).tasks.size()-position) > 0)
							subList.get(msgSubIndex).rTaskIndex = subList.get(msgSubIndex).tasks.get(position);
						else
							subList.get(msgSubIndex).rTaskIndex = "NULL"; //subscription complete
					}
				}//END: if: own sub
				//START: else if: neighbor's sub
				else if(!subList.get(msgSubIndex).resBroker.equals("NULL"))
				{
					//subList.get(msgSubIndex).pubIndex.add(msgPubIndex);
					if(subList.get(msgSubIndex).tasks.size()>msgTasks.size())
					{
						for(int j=0; j<msgTasks.size();j++)
							subList.get(msgSubIndex).tasks.remove(0);
					}
					else
						subList.remove(msgSubIndex);//subscription complete, so remove from subList as it's neighbor's sub
				}//END: else if: neighbor's sub
			}
			//insert into prt table
			insertToPRT(rp);
		}//END: if: Matched
		else if(msgPubIndex.equals("NULL"))
		{
			rp.sub.add(msgSubIndex);
			//insert the unmatched subscription into it's own subList
			insertToSubList(msgSubIndex, msgTasks, msgSentToBroker);
			//insert into prt table
			insertToPRT(rp);
		}
		//display all the tables contents together
		displayAllTable();
		//write this msg to outQueue to forward to it's own neighbors 
		outQueueMsgWritingPRT(msgPubIndex, msgSubIndex, msgTasks);
	}//END: method inQueueMsgExecutionPRT()
	//END: inQueueMsgExecutionPRT()*****************************************************
	
	//START: inQueueMsgExecutionSEARCH()*****************************************************
	public static synchronized void inQueueMsgExecutionSEARCH(String msg)
	{
		String msgArray[] = msg.split(",");
		String msgSentToBroker = msgArray[1];
		String msgSubIndex = msgArray[3];
		RowPRT rp = new RowPRT();
		rp.sentToBroker.add(msgSentToBroker);
		rp.sub.add(msgSubIndex);
		for(int i=0;i<subList.get(msgSubIndex).tasks.size();i++)
			rp.tasks.add(subList.get(msgSubIndex).tasks.get(i));
		matchWithSRT(rp, true);
		//display all the tables contents together
		displayAllTable();
	}
	//END: inQueueMsgExecutionSEARCH()*****************************************************
	
	//START: outQueueMsgWritingSRT()*****************************************************
	//this method writing the SRT message for the outQueue
	public static void outQueueMsgWritingSRT(String pubIndex, ArrayList<String> tasks)
	{
		String msg = "SRT" + "," + brokerName + "," + pubIndex;
		for(int i=0;i<tasks.size();i++)
			msg = msg + "," + tasks.get(i);
		outQueue.add(msg);
	}
	//END: outQueueMsgWritingSRT()*****************************************************
	
	//START: outQueueMsgWritingPRT()*****************************************************
	//this method writing the PRT message for the outQueue
	public static void outQueueMsgWritingPRT(String pubIndex, String subIndex, ArrayList<String> tasks)
	{
		String msg = "PRT" + "," + brokerName + "," + pubIndex + "," + subIndex;
		for(int i=0;i<tasks.size();i++)
			msg = msg + "," + tasks.get(i);
		outQueue.add(msg);
	}
	//END: outQueueMsgWritingPRT()*****************************************************
	
	//START: outQueueMsgWritingSEARCH()*****************************************************
	//this method writing the SEARCH message for the outQueue
	public static void outQueueMsgWritingSEARCH(String neighborBrokerName, String subIndex)
	{
		String msg = "SEARCH" + "," + brokerName + "," + neighborBrokerName + "," + subIndex;
		outQueue.add(msg);
	}
	//END: outQueueMsgWritingSEARCH()*****************************************************
	
	//START: outQueueMsgForwardSRT()*****************************************************
	//this method forward the SRT message to all it's neighbors
	public static void outQueueMsgForwardSRT(String msg)
	{
		// Get a set of the entries 
		Set<Entry<String, Neighbors>> set = neighbors.entrySet(); 
		// Get an iterator 
		Iterator<Entry<String, Neighbors>> i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) 
		{
			Map.Entry<String, Neighbors> me = (Map.Entry<String, Neighbors>)i.next(); 
			neighbors.get(me.getKey()).os.println(msg);
		}
	}
	//END: outQueueMsgForwardSRT()******************************************************
	
	//START: outQueueMsgForwardPRT()*****************************************************
	//this method forward the PRT message to all it's neighbors
	public static void outQueueMsgForwardPRT(String msg)
	{
		// Get a set of the entries 
		Set<Entry<String, Neighbors>> set = neighbors.entrySet(); 
		// Get an iterator 
		Iterator<Entry<String, Neighbors>> i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) 
		{
			Map.Entry<String, Neighbors> me = (Map.Entry<String, Neighbors>)i.next(); 
			neighbors.get(me.getKey()).os.println(msg);
		}
	}
	//END: outQueueMsgForwardPRT()*****************************************************
	
	//START: outQueueMsgForwardSEARCH()*****************************************************
	//this method forward the SEARCH message to the neighbor
	public static void outQueueMsgForwardSEARCH(String msg)
	{
		String neighborBrokerName = msg.split(",")[2];
		neighbors.get(neighborBrokerName).os.println(msg);
	}
	//END: outQueueMsgForwardSEARCH()*****************************************************
	
	//START: inQueueMsgCheckPRT()*****************************************************
	//this method check the PRT message for executing or not
	public static synchronized boolean inQueueMsgCheckPRT(String msg)
	{
		String msgArray[] = msg.split(",");//store whole msg in msgArray
		//String msgSentToBroker = msgArray[1];//store sentToBroker in msgSentToBroker
		String msgPubIndex = msgArray[2];//store pubIndex in msgPubIndex
		String msgSubIndex = msgArray[3];//store subIndex in msgSubIndex
		ArrayList<String> msgTasks = new ArrayList<String>();//Store tasks in msgTasks
		for(int i=4;i<msgArray.length;i++)
			msgTasks.add(msgArray[i]);
		//START: if: unmatched msg of PRT
		if(msgPubIndex.equals("NULL"))
		{
			if(subList.containsKey(msgSubIndex))
				return true;//no need to execute
		}//START: if: unmatched msg of PRT
		//START: else if: matched msg of PRT
		else if(pubList.containsKey(msgPubIndex))
		{
			//START: if: subList contains msgSubIndex
			if(subList.containsKey(msgSubIndex))
			{
				//own sub with complete matching
				if(subList.get(msgSubIndex).rTaskIndex.equals("NULL") && subList.get(msgSubIndex).pubIndex.size()>0)
					return true;//no need to execute
				else if(!subList.get(msgSubIndex).rTaskIndex.equals("NULL"))//own sub
				{
					int position=0;//get the position of remaining tasks index
					for(int j=0; j<subList.get(msgSubIndex).pubIndex.size();j++)
						position += pubList.get(subList.get(msgSubIndex).pubIndex.get(j)).tasks.size();
					ArrayList<String> tempTasks = new ArrayList<String>();
					for(int k=position;k<subList.get(msgSubIndex).tasks.size();k++)
						tempTasks.add(subList.get(msgSubIndex).tasks.get(k));
					if(tasksMatching(msgTasks, tempTasks)==0)
						return true;//no need to execute
				}//END: own sub
				//START: else: neighbor sub or maybe own sub in special case
				else
				{
					if(tasksMatching(msgTasks, subList.get(msgSubIndex).tasks)==0)
						return true;//no need to execute 
				}//END: else: neighbor sub or maybe own sub in special case
			}//END: if: subList contains msgSubIndex
			//START: else: subList doesn't contain msgSubIndex
			//that means already matched completely and removed from the subList as it's a neighbors sub
			else
			{
				return true;//no need to execute
			}//END: else: subList doesn't contain msgSubIndex
		}//END: else if: matched msg of PRT
		return false;//need to execute
	}
	//END: inQueueMsgCheckPRT()*****************************************************

}//END: class BrokerTwo