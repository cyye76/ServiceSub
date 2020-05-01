

import java.util.ArrayList;

public class RowPRT 
{
	ArrayList<String> tasks;
 	ArrayList<String> sentToBroker;
 	ArrayList<String> followKeys;
 	ArrayList<String> sub;
 	ArrayList<String> pub;
 	//constructor
 	public RowPRT()
 	{
 		this.tasks=new ArrayList<String>();
 		this.sentToBroker=new ArrayList<String>();
 		this.followKeys=new ArrayList<String>();
 		this.sub=new ArrayList<String>();
 		this.pub=new ArrayList<String>();
 	}
 	public void setTasks(String tasks)
 	{
 		this.tasks.add(tasks);
 	}
 	public void setSentToBroker(String sentToBroker)
 	{
 		this.sentToBroker.add(sentToBroker);
 	}
 	public void setFollowKeys(String followKeys)
 	{
 		this.followKeys.add(followKeys);
 	}
 	public void setSub(String sub)
 	{
 		this.sub.add(sub);
 	}
 	public void setPub(String pub)
 	{
 		this.pub.add(pub);
 	}
 	//for printing all the contents- task, sentToBroker, followKeys
 	public void displayContents()
 	{
 		//for printing tasks
 		if(this.tasks.size()>0)
 			for(int i=0;i<this.tasks.size();i++)
 				System.out.print("-->"+this.tasks.get(i));
 		else
 			System.out.print("NULL");
 		//for printing sentToBroker
 		System.out.print("\t");
 		if(this.sentToBroker.size()>0)
 			for(int i=0;i<this.sentToBroker.size();i++)
 				System.out.print(this.sentToBroker.get(i));
 		else
 			System.out.print("NULL");
 		//for printing followKeys
 		System.out.print("\t");
 		if(this.followKeys.size()>0)
 			for(int i=0;i<this.followKeys.size();i++)
 				System.out.print(this.followKeys.get(i));
 		else
 			System.out.print("NULL");
 		//for printing sub
 		System.out.print("\t");
 		if(this.sub.size()>0)
 			for(int i=0;i<this.sub.size();i++)
 				System.out.print(this.sub.get(i));
 		else
 			System.out.print("NULL");
 		//for printing pub
 		System.out.print("\t");
 		if(this.pub.size()>0)
 			for(int i=0;i<this.pub.size();i++)
 				System.out.print(this.pub.get(i));
 		else
 			System.out.print("NULL");
 		System.out.println();
 	}
}
