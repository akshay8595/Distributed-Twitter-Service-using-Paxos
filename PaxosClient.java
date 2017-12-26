import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.Socket;

public class PaxosClient extends Thread {

	Socket clientSocket = null;
	DataInputStream inp = null;
	DataOutputStream out = null;
	BufferedReader input = null;
	UserPaxos u1 = null;

	String serverIPAddress = "";

	boolean closed=false;
	/*
	Used to access the crash flag for the associated server it is connected to
	 */
	int connectedServerID = 0;
	//	public static boolean reset = false;
	PaxosClient(UserPaxos u1, Socket getSoc, int connectedServerID, String serverIPAddress)
	{
		this.u1 = u1;
		this.clientSocket = getSoc;
		this.connectedServerID = connectedServerID;
		this.serverIPAddress = serverIPAddress;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public DataInputStream getInp() {
		return inp;
	}

	public void setInp(DataInputStream inp) {
		this.inp = inp;
	}

	public DataOutputStream getOut() {
		return out;
	}

	public void setOut(DataOutputStream out) {
		this.out = out;
	}

	public BufferedReader getInput() {
		return input;
	}

	public void setInput(BufferedReader input) {
		this.input = input;
	}

	public UserPaxos getU1() {
		return u1;
	}

	public void setU1(UserPaxos u1) {
		this.u1 = u1;
	}

	public String getServerIPAddress() {
		return serverIPAddress;
	}

	public void setServerIPAddress(String serverIPAddress) {
		this.serverIPAddress = serverIPAddress;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public int getConnectedServerID() {
		return connectedServerID;
	}

	public void setConnectedServerID(int connectedServerID) {
		this.connectedServerID = connectedServerID;
	}

	public void run()
	{
		try
		{
	//		clientSocket = new Socket("localhost", 8887);
			inp = new DataInputStream(clientSocket.getInputStream());
			input = new BufferedReader(new InputStreamReader(inp));
			out = new DataOutputStream(clientSocket.getOutputStream());
		}
		catch(IOException e)
		{

			//	System.out.println(e);
		}

		while(true)
		{
			try
			{
				String text;
				text = inp.readUTF();
				//Forward received message to the receipt call
				u1.receiveTweetPaxos(text);
				System.out.println("Message received " + text + " from " + u1.ec2IP);
			}
			catch(IOException e)
			{
				//	reset=true;
				closed = true;
				u1.crashFlags.put(connectedServerID,0);
				System.out.println(u1.crashFlags.get(connectedServerID));
				System.out.println("Server is down for Client " + clientSocket.getInetAddress().getHostAddress());
				break;
			}
		}

		System.out.println("Thread is dead");
	}

}



