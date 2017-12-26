import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.Socket;

public class client2 extends Thread{

	static Socket clientSocket = null;
	static DataInputStream inp = null;
	static DataOutputStream out = null;
	static BufferedReader input = null;
	public static boolean closed=false;
	client2(Socket getSock)
	{
		clientSocket = getSock;
	}
	public void run()
	{
		int port;
		try
		{
		//	clientSocket = new Socket("localhost", 8887);
			inp = new DataInputStream(clientSocket.getInputStream());
			input = new BufferedReader(new InputStreamReader(inp));
			out = new DataOutputStream(clientSocket.getOutputStream());
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
		while(true)
		{
			try
			{
				String text;
				text = inp.readUTF();
				Gson gson = new Gson();
				Type messageType = new TypeToken<Message>() {
				}.getType();
				System.out.println("I enter here in receive");

				Message messsageReceived = gson.fromJson(text, messageType);

				String messageRecv = messsageReceived.getMessage();
				System.out.println("Client 2 is here " + messageRecv);
			}
			catch(IOException e)
			{
				closed = true;
//				UserPaxos.flag_beta=0;
//				System.out.println(UserPaxos.flag_beta);
				System.out.println("Server is down for Client 2");
				break;
			}

		}
		System.out.println("Thread is dead");
	}
}
