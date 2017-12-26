
import java.io.*;
import java.net.ServerSocket;

/*
Accepting connections
 */
import java.net.*;
public class UserConnector implements Runnable {
    Socket clientsocket;
    //Needs reference to all the user methods
    User user;
    DataInputStream din;
    java.io.DataOutputStream dout;
    String clientName;
    String ipAddress;
    Integer clientID;

    public UserConnector(Socket clientsocket, User user, String clientName, String ipAddress,Integer clientID) {
        //super("ServerConnectionThread");
        this.clientsocket = clientsocket;
        this.user = user;
        this.ipAddress = ipAddress;
        this.clientName = clientName;
        this.clientID = clientID;
        //Create a listener that only accepts incoming connections and adds to it
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

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getClientID() {
        return clientID;
    }

    public void setClientID(Integer clientID) {
        this.clientID = clientID;
    }


    public void sendTweetToUser(String tweet) {
        //String serverContent = user.sendTweet(tweet);
        PrintWriter out =
                new PrintWriter(dout, true);
        //out.println(tweet);
        out.println(tweet);
        out.flush();
    }

    //Send tweet to all connected clients of user?
    public void sendTweetToAllClients(String tweet) {
        for(int i = 0; i < user.connectedUsers.size(); i++)
        {
            UserConnector uc = user.connectedUsers.get(i);
            uc.sendTweetToUser(tweet);
        }
    }

    @Override
    public void run() {
        //Opening data input and output streams from client
        try {
            System.out.println("Potential client " + clientName + "attempting to connect");
            din = new DataInputStream(clientsocket.getInputStream());
            dout= new java.io.DataOutputStream(clientsocket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(din));

//            while(true){
//                //Doesn't affect the accepting thread
//                while(din != null && din.read() != -1 && din.available() == 0){
//                    try {
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                //Server is no longer listening so this can be ignored
////                String incomingTweet = din.readUTF();
////                //Server is essentially listening for incoming data from client's input stream
////                user.receiveTweet(incomingTweet);
//
//            }

        }
        catch(EOFException e){
           System.out.println("Server Client " + clientName + " disconnected because of EOF exception" );
           //Remove yourself from connected clients of the user
           user.connectedUsers.remove(this);
            try {
                clientsocket.close();
                din.close();
                dout.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        catch (IOException e) {
            System.out.println("Server Client " + clientName + " disconnected because of IO Exception" );

            try {
                din.close();
                dout.close();
                clientsocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            e.printStackTrace();
        }
    }
}
