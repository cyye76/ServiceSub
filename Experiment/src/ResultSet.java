
public class ResultSet 
{
	long startTime;
	long endTime;
	public ResultSet()
	{
		this.startTime = 0;
		this.endTime = 0;
	}
	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}
	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}
	public long getStartTime()
	{
		return startTime;
	}
	public long getEndTime()
	{
		return endTime;
	}
	public long getTotalTime()
	{
		return endTime - startTime;
	}
}//END: class ResultSet