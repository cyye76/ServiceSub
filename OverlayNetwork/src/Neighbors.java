import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;


public class Neighbors 
{
	Socket clientSocket;
	InputStreamReader is;
	PrintStream os;
	public Neighbors(Socket clientSocket, InputStreamReader is, PrintStream os)
	{
		this.clientSocket = clientSocket;
		this.is = is;
		this.os = os;
	}
}
