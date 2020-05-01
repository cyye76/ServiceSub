
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Algorithm 
{
	//two global ArrayList for decomposition
	static ArrayList<Node> nodeList =new ArrayList<Node>();
	static ArrayList<ArrayList<Node>> fsmList= new ArrayList<ArrayList<Node>>();

	//for routing 
	static HashMap<String,RowSRT> srt;	//SRT table
	static HashMap<String,RowPRT> prt;	//PRT table
	static HashMap<String,ArrayList<String>> pubList;	//Publication table
	static HashMap<String,RowSubList> subList;	//Subscription table

	public static void main(String[] args) 
	{
		//start of decomposition
		/*Node s1 = new Node(1,2);
		Node s2 = new Node(2,1);
		Node s3 = new Node(3,1);
		Node s4 = new Node(4,1);
		s1.setNext(0, s2, "t1");
		s1.setNext(1,s3,"t2");
		s2.setNext(0, s4, "t3");
		s3.setNext(0, s4, "t4");
		s4.setNext(0, s4, "NULL");
		s4.setIsFinal(true);*/
		
		Node s1 = new Node(1,1);
		Node s2 = new Node(2,1);
		Node s3 = new Node(3,1);
		Node s4 = new Node(4,2);
		Node s5 = new Node(5,1);
		s1.setNext(0, s2, "t1");
		s2.setNext(0,s3,"t2");
		s3.setNext(0,s4,"t3");
		s4.setNext(0, s5, "t4");
		s4.setNext(1, s2, "t5",3);
		s5.setNext(0, s5, "NULL");
		s5.setIsFinal(true);
		
		//call decomposition() for decomposing the FSM to split into list of sequential FSM
		decomposition(s1);
		for (int i = 0; i < fsmList.size(); i++)
		{
			ArrayList<Node> singleFSM = (ArrayList<Node>)fsmList.get(i);
			//call this method for printing single FSM
			displayFSM(singleFSM);	
		}
		//end of decomposition in main
		
		srt = new HashMap<String,RowSRT>();
		prt = new HashMap<String,RowPRT>();
		pubList= new HashMap<String,ArrayList<String>>();
		subList = new HashMap<String,RowSubList>();
		//first row to insert in the SRT
		RowSRT rowSRT1=new RowSRT();
		rowSRT1.setTasks("t1");
		rowSRT1.setTasks("t2");
		rowSRT1.setTasks("t3");
		rowSRT1.setPub("P1");
		//rowSRT1.setSentToBroker("B3");
		pubList.put(rowSRT1.pub.get(0), rowSRT1.tasks);
		//insertToSRT(rowSRT1);
		newPubMatchWithPRT(rowSRT1);
		//second row to insert in the SRT
		RowSRT rowSRT2=new RowSRT();
		rowSRT2.setTasks("t4");
		rowSRT2.setTasks("t5");
		rowSRT2.setTasks("t6");
		rowSRT2.setPub("P2");
		//rowSRT2.setSentToBroker("B3");
		pubList.put(rowSRT2.pub.get(0), rowSRT2.tasks);
		//insertToSRT(rowSRT2);
		newPubMatchWithPRT(rowSRT2);
		//third row to insert in the SRT
		RowSRT rowSRT3=new RowSRT();
		rowSRT3.setTasks("t8");
		//rowSRT3.setTasks("t11");
		//rowSRT3.setTasks("t4");
		rowSRT3.setPub("P3");
		//rowSRT3.setSentToBroker("B3");
		pubList.put(rowSRT3.pub.get(0), rowSRT3.tasks);
		insertToSRT(rowSRT3);
		
		//printing SRT
		System.out.print("SRT Table:  ");
		System.out.println(srt.keySet());
		displaySRT();
		//printing PubList
		System.out.print("Publication List:  ");
		System.out.println(pubList.keySet());
		displayPubList();
		
		//first row to insert in the PRT
		RowPRT rowPRT1=new RowPRT();
		rowPRT1.setTasks("t4");
		rowPRT1.setTasks("t5");
		rowPRT1.setTasks("t6");
		rowPRT1.setTasks("t9");
		rowPRT1.setSub("S1");
		//rowPRT1.setSentToBroker("B3");
		RowSubList rowSubList1 = new RowSubList();
		for(int z=0;z<rowPRT1.tasks.size();z++)
			rowSubList1.tasks.add(rowPRT1.tasks.get(z));
		subList.put(rowPRT1.sub.get(0), rowSubList1);
		//insertToPRT(rowPRT1);
		matchWithPRT(rowPRT1);
		//Second row to insert in the PRT
		RowPRT rowPRT2=new RowPRT();
		rowPRT2.setTasks("t7");
		rowPRT2.setTasks("t1");
		rowPRT2.setTasks("t2");
		rowPRT2.setTasks("t3");
		//rowPRT2.setTasks("t7");
		//rowPRT2.setTasks("t4");
		//rowPRT2.setTasks("t5");
		//rowPRT2.setTasks("t6");
		//rowPRT2.setTasks("t7");
		rowPRT2.setTasks("t8");
		rowPRT2.setSub("S2");
		RowSubList rowSubList2 = new RowSubList();
		for(int y=0;y<rowPRT2.tasks.size();y++)
			rowSubList2.tasks.add(rowPRT2.tasks.get(y));
		subList.put(rowPRT2.sub.get(0), rowSubList2);
		//insertToPRT(rowPRT2);
		matchWithPRT(rowPRT2);
		
		//Third row to insert in the PRT
		RowPRT rowPRT3=new RowPRT();
		rowPRT3.setTasks("t1");
		rowPRT3.setTasks("t2");
		rowPRT3.setTasks("t3");
		rowPRT3.setTasks("t10");
		rowPRT3.setTasks("t11");
		rowPRT3.setTasks("t1");
		rowPRT3.setTasks("t2");
		rowPRT3.setTasks("t3");
		rowPRT3.setSub("S3");
		RowSubList rowSubList3 = new RowSubList();
		for(int y=0;y<rowPRT3.tasks.size();y++)
			rowSubList3.tasks.add(rowPRT3.tasks.get(y));
		subList.put(rowPRT3.sub.get(0), rowSubList3);
		//insertToPRT(rowPRT3);
		//matchWithSRT(rowPRT3);
		matchWithPRT(rowPRT3);
		
		//Fourth row to insert in the PRT
		RowPRT rowPRT4=new RowPRT();
		rowPRT4.setTasks("t10");
		rowPRT4.setTasks("t11");
		rowPRT4.setTasks("t1");
		rowPRT4.setTasks("t2");
		rowPRT4.setTasks("t3");
		rowPRT4.setTasks("t7");
		rowPRT4.setSub("S4");
		RowSubList rowSubList4 = new RowSubList();
		for(int w=0;w<rowPRT4.tasks.size();w++)
			rowSubList4.tasks.add(rowPRT4.tasks.get(w));
		subList.put(rowPRT4.sub.get(0), rowSubList4);
		//insertToPRT(rowPRT4);
		matchWithPRT(rowPRT4);
		
		//fourth row to insert in the SRT
		RowSRT rowSRT4=new RowSRT();
		rowSRT4.setTasks("t10");
		rowSRT4.setTasks("t11");
		//rowSRT4.setTasks("t7");
		rowSRT4.setPub("P4");
		//rowSRT4.setSentToBroker("B3");
		pubList.put(rowSRT4.pub.get(0), rowSRT4.tasks);
		//insertToSRT(rowSRT4);
		newPubMatchWithPRT(rowSRT4);
		
		//printing SRT 2nd time
		System.out.print("SRT Table:  ");
		System.out.println(srt.keySet());
		displaySRT();
		//printing PubList
		System.out.print("Publication List:  ");
		System.out.println(pubList.keySet());
		displayPubList();
		
		//Printing PRT
		System.out.print("PRT Table:  ");
		System.out.println(prt.keySet());
		displayPRT();
		//Printing SubList
		System.out.print("Subscription List:  ");
		System.out.println(subList.keySet());
		displaySubList();
	}//end of main() method
	
	
	//START: newPubMatchWithPRT()******************************************************
	//this method check PRT for the matching if new Publication added in SRT
	public static void newPubMatchWithPRT(RowSRT rowSRT)
	{
		String indexTask = rowSRT.tasks.get(0);
		ArrayList<String> backupTasksOfNewPUB = new ArrayList<String>();
		for(int x=0;x<rowSRT.tasks.size();x++)
			backupTasksOfNewPUB.add(rowSRT.tasks.get(x));
		//insert new publication into the SRT first
		insertToSRT(rowSRT);
		//START: main if condition inside method
		if(prt.containsKey(indexTask))
		{
			RowPRT rowPRT = (RowPRT)prt.get(indexTask);
			RowPRT commonRowPRT = new RowPRT();
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
										position += pubList.get(subList.get(rowPRT.sub.get(i)).pubIndex.get(j)).size();
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
												position += pubList.get(subList.get(rowPRT.sub.get(i)).pubIndex.get(j)).size();
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
	//END: checkPRTwithNewPub()********************************************************
	
	//START: matchWithSRT(RowPRT,boolean)********************************************
	public static void matchWithSRT(RowPRT rowPRT, boolean upOnly)
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
							if(tasksMatching(pubList.get(rowSRT.pub.get(i)),combineTasks) == 1)
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
							commonTasks.clear();
							//insert the matching part of subscription into PRT
							insertToPRT(commonRowPRT);
							//unmatched part of subscription will be processed 
							//by calling matchWithSRT(rowPRT)
							//matchWithSRT(rowPRT);
							
							//unmatched part of subscription will be processed 
							//by calling matchWithPRT(rowPRT) first, then with SRT
							//matchWithPRT(rowPRT);
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
							commonTasks.clear();
							//insert the subscription into PRT which has matching publication
							insertToPRT(commonRowPRT);
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
				//if rowPRT have more tasks than rowSRT
				if(rowPRT.tasks.size() > rowSRT.tasks.size())
				{
					int flag = 0;
					//checking for the compatibility of rowSRT.followKeys for the next processing 
					//of rowPRT 
					for(int i=0;i<rowSRT.followKeys.size();i++)
					{
						if(rowSRT.followKeys.get(i) == rowPRT.tasks.get(rowSRT.tasks.size()))
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
				//If PRT only need to update (already contains this part, don't need to insert again),
				//so return the method if upOnly==true
				if(upOnly)
					return;
				insertToPRT(rowPRT);
			}
			else //that means no matching found at all
			{
				//If PRT only need to update (already contains this part, don't need to insert again),
				//so return the method if upOnly==true
				if(upOnly)
					return;
				insertToPRT(rowPRT);
			}
		}//end: main if condition 
		// Start: main else
		//that means, there is no matching in the SRT, main if condition is false
		else 
		{
			//If PRT only need to update (already contains this part, don't need to insert again),
			//so return the method if upOnly==true
			if(upOnly)
				return;
			insertToPRT(rowPRT);
		} //end: main else
	}//end of method matchWithSRT()
	//END: matchWithSRT(RowPRT,boolean)**********************************************
	
	//START: matchWithPRT(RowPRT,boolean)********************************************
	public static void matchWithPRT(RowPRT newRowPRT, boolean upOnly)
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
							if(tasksMatching(pubList.get(oldRowPRT.pub.get(i)),combineTasks) == 1)
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
							//empty the commonTasks because it already assigned to commonRowPRT
							//which will be inserted into PRT in the next line 
							commonTasks.clear();
							//insert the matching part of subscription into PRT
							insertToPRT(commonRowPRT);
							//unmatched part of subscription will be processed 
							//by calling matchWithPRT(newRowPRT) again
							//matchWithPRT(newRowPRT);
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
							commonTasks.clear();
							//insert the subscription into PRT which has matching publication
							insertToPRT(commonRowPRT);
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
						if(oldRowPRT.followKeys.get(i) == newRowPRT.tasks.get(oldRowPRT.tasks.size()))
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
	
	//this method search matching in the PRT for the new Subscription
	public static void matchWithPRT(RowPRT newRowPRT)
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
							if(tasksMatching(pubList.get(oldRowPRT.pub.get(i)),combineTasks) == 1)
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
							//empty the commonTasks because it already assigned to commonRowPRT
							//which will be inserted into PRT in the next line 
							commonTasks.clear();
							//insert the matching part of subscription into PRT
							insertToPRT(commonRowPRT);
							//unmatched part of subscription will be processed 
							//by calling matchWithPRT(newRowPRT) again
							matchWithPRT(newRowPRT);
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
							commonTasks.clear();
							//insert the subscription into PRT which has matching publication
							insertToPRT(commonRowPRT);
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
						if(oldRowPRT.followKeys.get(i) == newRowPRT.tasks.get(oldRowPRT.tasks.size()))
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
				matchWithSRT(newRowPRT);
			}
			else //no matching at all so far in the PRT
			{
				//insertToPRT(newRowPRT);
				//search matching in the SRT because there is no match in PRT
				matchWithSRT(newRowPRT);
			}
		}//end: main if condition 
		//Start: main else
		else //that means main if condition false, so there is no matching found in the PRT
		{
			//insertToPRT(newRowPRT);
			//search matching in the SRT because there is no match in PRT
			matchWithSRT(newRowPRT);
		}
	}//end of method matchWithPRT()
	
	//this method search matching in the SRT for the new Subscription
	public static void matchWithSRT(RowPRT rowPRT)
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
							if(tasksMatching(pubList.get(rowSRT.pub.get(i)),combineTasks) == 1)
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
							commonTasks.clear();
							//insert the matching part of subscription into PRT
							insertToPRT(commonRowPRT);
							//unmatched part of subscription will be processed 
							//by calling matchWithSRT(rowPRT)
							//matchWithSRT(rowPRT);
							
							//unmatched part of subscription will be processed 
							//by calling matchWithPRT(rowPRT) first, then with SRT
							matchWithPRT(rowPRT);
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
							commonTasks.clear();
							//insert the subscription into PRT which has matching publication
							insertToPRT(commonRowPRT);
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
				//if rowPRT have more tasks than rowSRT
				if(rowPRT.tasks.size() > rowSRT.tasks.size())
				{
					int flag = 0;
					//checking for the compatibility of rowSRT.followKeys for the next processing 
					//of rowPRT 
					for(int i=0;i<rowSRT.followKeys.size();i++)
					{
						if(rowSRT.followKeys.get(i) == rowPRT.tasks.get(rowSRT.tasks.size()))
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
				insertToPRT(rowPRT);
			}
			else //that means no matching found at all
			{
				insertToPRT(rowPRT);
			}
		}//end: main if condition 
		// Start: main else
		//that means, there is no matching in the SRT, main if condition is false
		else 
		{
			insertToPRT(rowPRT);
		} //end: main else
	}//end of method matchWithSRT()
	
	//this method match between two ArrayList of tasks
	public static int tasksMatching(ArrayList<String> tasksSRT, ArrayList<String> tasksPRT)
	{
		if(tasksSRT.size() <= tasksPRT.size())
		{
			for(int j = 0;j < tasksSRT.size();j++)
			{
				if(tasksSRT.get(j)==tasksPRT.get(j))
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
	
	//this method insert row into PRT table efficiently
	public static void insertToPRT(RowPRT newRow)
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
				if(oldRow.tasks.get(i)==newRow.tasks.get(i))
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
				//for avoiding duplication
				if(commonRow.followKeys.contains(oldRow.tasks.get(0))==false)//if not contain than add
					commonRow.followKeys.add(oldRow.tasks.get(0));
				insertToPRT(oldRow);
				//assign the index of this row to the commonRow's followKeys 
				//commonRow.followKeys.add(oldRow.tasks.get(0));
			}
			else if(commonTasks.size()==oldRow.tasks.size())
			{
				commonRow.followKeys=oldRow.followKeys;
				commonRow.sub=oldRow.sub;
				commonRow.pub=oldRow.pub;
			}
			if(commonTasks.size()<newRow.tasks.size())
			{
				for(int i=commonTasks.size();i<newRow.tasks.size();i++)
				{
					newTasks.add(newRow.tasks.get(i));
				}
				newRow.tasks=newTasks;
				//for avoiding duplication
				if(commonRow.followKeys.contains(newRow.tasks.get(0))==false)//if not contain than add
					commonRow.followKeys.add(newRow.tasks.get(0));
				insertToPRT(newRow);
				//assign the index of this row to the commonRow's followKeys 
				//commonRow.followKeys.add(newRow.tasks.get(0));
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
			
	}
	
	//this method display the contents of PRT table
	public static void displayPRT()
	{
		// Get a set of the entries 
		Set set = prt.entrySet(); 
		// Get an iterator 
		Iterator i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) { 
		Map.Entry me = (Map.Entry)i.next(); 
		System.out.print(me.getKey() + ": "); 
		RowPRT temp = (RowPRT)me.getValue();
		temp.displayContents();
	}
}
	
	//this method insert row into SRT table efficiently
	public static void insertToSRT(RowSRT newRow)
	{
		String indexTask=newRow.tasks.get(0);
		//System.out.println(indexTask);
		if(srt.containsKey(indexTask))
		{
			ArrayList<String> commonTasks = new ArrayList<String>();
			ArrayList<String> newTasks = new ArrayList<String>();
			ArrayList<String> oldTasks = new ArrayList<String>();
			RowSRT oldRow=(RowSRT)srt.get(indexTask);
			RowSRT commonRow = new RowSRT();
			
			//taking the matching part from both
			for(int i=0;i<oldRow.tasks.size()&& i<newRow.tasks.size();i++)
			{
				if(oldRow.tasks.get(i)==newRow.tasks.get(i))
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
				//for avoiding duplication
				if(commonRow.followKeys.contains(oldRow.tasks.get(0))==false)//if not contain than add
					commonRow.followKeys.add(oldRow.tasks.get(0));
				insertToSRT(oldRow);
				//assign the index of this row to the commonRow's followKeys 
				//commonRow.followKeys.add(oldRow.tasks.get(0));
			}
			else if(commonTasks.size()==oldRow.tasks.size())
			{
				commonRow.followKeys=oldRow.followKeys;
				commonRow.pub=oldRow.pub;
			}
			if(commonTasks.size()<newRow.tasks.size())
			{
				for(int i=commonTasks.size();i<newRow.tasks.size();i++)
				{
					newTasks.add(newRow.tasks.get(i));
				}
				newRow.tasks=newTasks;
				//for avoiding duplication
				if(commonRow.followKeys.contains(newRow.tasks.get(0))==false)//if not contain than add
					commonRow.followKeys.add(newRow.tasks.get(0));
				insertToSRT(newRow);
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
				//this case seems to never happen but still written for security reason 
				if(newRow.followKeys.size()>0)//for avoiding null pointer exception
				{
					//for avoiding duplication
					if(commonRow.followKeys.contains(newRow.followKeys.get(0))==false)//if not contain than add
						//newRow usually should not contain any followKeys
						commonRow.followKeys.add(newRow.followKeys.get(0));
				}
			}
			//remove the existing row
			srt.remove(indexTask);
			//insert the commonRow into SRT
			srt.put(commonRow.tasks.get(0), commonRow);
		}
		else
		{
			srt.put(indexTask, newRow);
			//forward this new entry to next neighbors
		}
			
	}
	
	//this method display the contents of SRT table
	public static void displaySRT()
	{
		// Get a set of the entries 
		Set set = srt.entrySet(); 
		// Get an iterator 
		Iterator i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) 
		{ 
			Map.Entry me = (Map.Entry)i.next(); 
			System.out.print(me.getKey() + ": "); 
			RowSRT temp = (RowSRT)me.getValue();
			temp.displayContents();
		}
	}
	
	//this method display the contents of pubList table
	public static void displayPubList()
	{
		// Get a set of the entries 
		Set set = pubList.entrySet(); 
		// Get an iterator 
		Iterator i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) 
		{ 
			Map.Entry me = (Map.Entry)i.next(); 
			System.out.print(me.getKey() + ": "); 
			ArrayList<String> temp = (ArrayList<String>)me.getValue();
			for(int j=0;j<temp.size();j++)
				System.out.print("-->"+temp.get(j));
			System.out.println();
		}
	}
	
	//this method display the contents of subList table
	public static void displaySubList()
	{
		// Get a set of the entries 
		Set set = subList.entrySet(); 
		// Get an iterator 
		Iterator i = set.iterator(); 
		// Display elements 
		while(i.hasNext()) 
		{ 
			Map.Entry me = (Map.Entry)i.next(); 
			System.out.print(me.getKey() + ": "); 
			RowSubList temp = (RowSubList)me.getValue();
			ArrayList<String> tasks = (ArrayList<String>)temp.tasks;
			ArrayList<String> pubIndex = (ArrayList<String>)temp.pubIndex;
			String rTaskIndex=temp.rTaskIndex;
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
			System.out.println();
		}
	}

	//decomposition part 
	//start of decomposition algorithm
	public static void decomposition(Node start)
	{
		if(start.getIsFinal()==false)
		{
			nodeList.add(start);
			for(int i=0;i<start.next.length;i++)
			{
				if(start.iterate[i] >= 1)
				{
					start.iterate[i]--;
					//nodeList.add(start);
					decomposition(start.next[i]);
				}
				else if(start.iterate[i] == 0)
					continue;
				else if(start.iterate[i]== -1)
				{
					decomposition(start.next[i]);
				}
			}
		}
		else 
		{
			nodeList.add(start);
			//copy the nodeList into another ArrayList
			ArrayList<Node> fsm = new ArrayList<Node>(nodeList);
			fsmList.add(fsm);
		}
		nodeList.remove(nodeList.size()-1);
	}//end of decomposition algorithm
	
	//for printing sequential single FSM
	//start of displayFSM()
	public static void displayFSM(ArrayList<Node> singleFSM)
	{
		for (int j=0;j<singleFSM.size()-1;j++)
		{
			System.out.print("-->");
			System.out.print("S" + singleFSM.get(j).getKey());
			for(int k=0;k<singleFSM.get(j).next.length;k++)
			{
				//identify the task of this state to go to the next state
				if(singleFSM.get(j).next[k].getKey()==singleFSM.get(j+1).getKey())
				{
					System.out.print("--"+singleFSM.get(j).task[k]);
					break;
				}
			}
		}
		System.out.print("-->S" + singleFSM.get(singleFSM.size()-1).getKey()+"\n");
	}//end of displayFSM()
	//end of decomposition part
		
}//end of class Algorithm
