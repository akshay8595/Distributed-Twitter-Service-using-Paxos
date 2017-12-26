import java.io.*;
import java.net.Socket;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class UserClientConnector implements Runnable {
    Socket clientsocket;

    //Needs reference to all the user methods
    private User user;
    private DataInputStream din;
    private java.io.DataOutputStream dout;
    private Integer clientID;
    private String serverIPAddress;
    private String clientIPAddress;
    private static int DEFAULT_SERVER_PORT = 9001;
    int no_of_reconnects=0;


    public UserClientConnector() {
        this.din = null;
        this.dout = null;
        this.serverIPAddress = "";
        this.clientIPAddress = "";
        this.user = new User();
    }

    public UserClientConnector(User user, Integer clientID, String serverIPAddress, String clientIPAddress) {
        this.user = user;
        this.clientID = clientID;
        this.serverIPAddress = serverIPAddress;
        this.clientIPAddress = clientIPAddress;
    }

    public Socket getClientsocket() {
        return clientsocket;
    }

    public void setClientsocket(Socket clientsocket) {
        this.clientsocket = clientsocket;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DataInputStream getDin() {
        return din;
    }

    public void setDin(DataInputStream din) {
        this.din = din;
    }

    public DataOutputStream getDout() {
        return dout;
    }

    public void setDout(DataOutputStream dout) {
        this.dout = dout;
    }

    public Integer getClientID() {
        return clientID;
    }

    public void setClientID(Integer clientID) {
        this.clientID = clientID;
    }

    public String getServerIPAddress() {
        return serverIPAddress;
    }

    public void setServerIPAddress(String serverIPAddress) {
        this.serverIPAddress = serverIPAddress;
    }

    public String getClientIPAddress() {
        return clientIPAddress;
    }

    public void setClientIPAddress(String clientIPAddress) {
        this.clientIPAddress = clientIPAddress;
    }

    public static int getDefaultServerPort() {
        return DEFAULT_SERVER_PORT;
    }

    public static void setDefaultServerPort(int defaultServerPort) {
        DEFAULT_SERVER_PORT = defaultServerPort;
    }

    public boolean hostAvailabilityCheck(Socket clientSocket) {
        try (Socket s = new Socket(serverIPAddress, DEFAULT_SERVER_PORT)) {
            return true;
        } catch (IOException ex) {
        /* ignore */
        }
        return false;
    }


    private void createClient(String serverIPAddress)throws IOException
    {
        if (System.getProperty("sPort") != null) {
            clientsocket = new Socket(serverIPAddress, Integer.valueOf(System.getProperty("sPort")));
        } else {
            clientsocket = new Socket(serverIPAddress, 9001);
        }
        //Client is capable of send`ing data
        dout = new DataOutputStream(clientsocket.getOutputStream());
        din = new DataInputStream(clientsocket.getInputStream());

        BufferedReader bin;
        bin = new BufferedReader(new InputStreamReader(System.in));
        //Create an input stream here, that calls receive?
        System.out.println("Connected to server " + serverIPAddress);
        //client has successfully connected to server, so at this point it is safe to remove the server
        if(user.getUnconnectedServers().contains(serverIPAddress)){
            System.out.println("After reconnect " + user.getConnectedServers().size());
            user.getUnconnectedServers().remove(serverIPAddress);
        }
        user.getConnectedServers().add(this);
    }

    //Keep retrying for a period of 4*1 = 4 mins
    private boolean reconnect(String serverIPAddress,int no_of_reconnects){
        user.getConnectedServers().remove(this);

        while(true)
        {
            try {
                no_of_reconnects+=1;
                if(no_of_reconnects > 4){
                    System.out.println("Reattempt failed to connect to server, maximum number of attempts tried for " + serverIPAddress);
                    break;
                }
                //Sleep for a min
                Thread.sleep(1000);
                System.out.println("Finished sleeping");
                //If successful, but if unsuccessful createClient will throw an exception
                createClient(serverIPAddress);
                return true;
            } catch(InterruptedException ie) {
                System.out.println("Server reconnect attempt failed " + ie.getMessage());
                ie.printStackTrace();

            } catch (UnknownHostException e) {
                System.out.println("Server reconnect attempt failed " + e.getMessage());

                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Server reconnect attempt failed " + e.getMessage());

                e.printStackTrace();
            }
        }
        //If the while loop was successful, there would have been a return(so do the exception closing here)
        try {
            clientsocket.close();
            din.close();
            dout.close();
            user.getUnconnectedServers().add(serverIPAddress);
            user.getConnectedServers().remove(this);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    private boolean listenForData(){
        try {
            while (true){
                BufferedReader bin=new BufferedReader(new InputStreamReader(din));
                //This causes the first character to be skipped
//                if(din.read() == -1){
//                    System.out.println("Server " + serverIPAddress + " has gone down ");
//                    user.getUnconnectedServers().add(serverIPAddress);
//                    user.getConnectedServers().remove(this);
//                    break;
//                }
                String line = "";
                if (bin.ready() && (line = bin.readLine()) != null) {
                    System.out.println("Received message from " + serverIPAddress);
                    String response = line;
                    //System.out.printf("Received broadcast: %s\n", response);
                    user.receiveTweetPaxos(response);
                }

            }

        } catch(EOFException e) {
            System.out.println("Server " + serverIPAddress + " has gone down ");
            user.getUnconnectedServers().add(serverIPAddress);
            user.getConnectedServers().remove(this);
            return false;
        }
        catch (IOException e) {
            System.out.println("IO Exception in client" + serverIPAddress);
            user.getUnconnectedServers().add(serverIPAddress);
            user.getConnectedServers().remove(this);
            return false;
        }
        //return false;
    }

    @Override
    public void run() {

        try {
            createClient(serverIPAddress);

            while (true) {
                //Client is listening for data from server
                BufferedReader bin=new BufferedReader(new InputStreamReader(din));

//               if(din.read() == -1){
//                    System.out.println("Server " + serverIPAddress + " has gone down ");
//                    user.getUnconnectedServers().add(serverIPAddress);
//                    user.getConnectedServers().remove(this);
//                    break;
//                }

//                if(din.available() > 0)
//                {
//                    System.out.println("Received message from : " + serverIPAddress);
//                    //int length = din.readInt();
//                    //System.out.println("JSON length for : " + length);
//                    String response = din.readUTF();
//                    if(response != ""){
//                        user.receiveTweet(response);
//                    }
//                }

                String line = "";
                if (bin.ready() && (line = bin.readLine()) != null) {
                    System.out.println("Received message from : " + serverIPAddress);
                    String response = line;
                    //System.out.printf("Received broadcast: %s\n", response);
                    user.receiveTweetPaxos(response);
                }

//                if (bin.readLine() != null) {
//                    System.out.println("Received message from : " + serverIPAddress);
//                    String response = bin.readLine();
//                    //System.out.printf("Received broadcast: %s\n", response);
//                    user.receiveTweet(response);
//                }
            }
        } catch(ConnectException exception){
            System.err.println(" Client " + clientIPAddress + " unable to connect to " + serverIPAddress);
            if(reconnect(serverIPAddress,no_of_reconnects))
            {
                System.out.println("Listening on socket after reconnect to " + serverIPAddress);
                boolean result = listenForData();
                if(!result){
                    try {
                        clientsocket.close();
                        din.close();
                        dout.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                }
            }

        } catch(SocketException sockEx) {
            System.err.println(" Client " + clientIPAddress + " unable to connect to " + serverIPAddress + " socketex ");
            if(reconnect(serverIPAddress,no_of_reconnects))
            {
                System.out.println("Listening on socket after reconnect to " + serverIPAddress);
                boolean result = listenForData();
                if(!result){
                    try {
                        clientsocket.close();
                        din.close();
                        dout.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                }
            }
        } catch(EOFException ie){
            System.err.println(" Client " + clientIPAddress + " unable to connect to " + serverIPAddress + " eof ");
            try {
                clientsocket.close();
                din.close();
                dout.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            try {
                clientsocket.close();
                din.close();
                dout.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
