import java.io.BufferedReader;

public class InMsgHandler4B2 extends Thread
{
	String nbIndex;//neighbor index
	InMsgHandler4B2(String nbIndex)
	{ 
		this.nbIndex = nbIndex; 
	}
	public void run( )
	{
		try
		{
			BufferedReader br = new BufferedReader(BrokerTwo.neighbors.get(nbIndex).is);
			String msg;
			while(true)
			{	
				if(br.ready())//if next msg is ready for receiving
				{
					msg = br.readLine();
					BrokerTwo.inQueue.add(msg);
				}
				else
					Thread.sleep(1000);	
			}			
		}
		catch(Exception e){e.printStackTrace( );}
	}
}