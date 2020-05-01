import java.util.ArrayList;

public class RowSubList 
{
	ArrayList<String> tasks;
	ArrayList<String> pubIndex;
	String rTaskIndex;
	String resBroker;
	public RowSubList()
	{
		this.tasks = new ArrayList<String>();
		this.pubIndex= new ArrayList<String>();
		this.rTaskIndex= new String("NULL");
		this.resBroker= new String("NULL");
	}
	public RowSubList(ArrayList<String> tasks)
	{
		this.tasks = tasks;
		this.pubIndex= new ArrayList<String>();
		this.rTaskIndex= new String("NULL");
		this.resBroker= new String("NULL");
	}
}
