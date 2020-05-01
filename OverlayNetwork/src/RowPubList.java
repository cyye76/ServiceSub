import java.util.ArrayList;

public class RowPubList 
{
	ArrayList<String> tasks;
	String resBroker;
	public RowPubList()
	{
		this.tasks = new ArrayList<String>();
		this.resBroker= new String("NULL");
	}
	public RowPubList(ArrayList<String> tasks)
	{
		this.tasks = tasks;
		this.resBroker= new String("NULL");
	}
}