public class Node 
{
	int key;
	boolean isFinal;
	Node next[];
	String task[];
	//String condition[];
	int iterate[];
	
	//constructor
	public Node(int key, int nNextNode)
	{
		this.key=key;
		this.isFinal=false;
		this.next = new Node[nNextNode];
		this.task = new String[nNextNode];
		this.iterate = new int[nNextNode];

		for (int i=0;i<nNextNode;i++)
		{
			this.next[i]=this.next[i];
			this.task[i]="null";
			this.iterate[i]=-1;
		}
	}
	//getter method of key
	public int getKey()
	{
		return this.key;
	}
	//setter method for next
	public void setNext(int index, Node next, String task)
	{
		this.next[index]=next;
		this.task[index]=task;
	}
	//setter method for next
	public void setNext(int index, Node next, String task,int iterate)
	{
		this.next[index]=next;
		this.task[index]=task;
		this.iterate[index]=iterate;
	}
	//set isFianl attribute true if this is a final node
	public void setIsFinal(boolean isFinal)
	{
		this.isFinal=isFinal;
	}
	//getter for isFinal attribute
	boolean getIsFinal()
	{
		return this.isFinal;
	}
}
