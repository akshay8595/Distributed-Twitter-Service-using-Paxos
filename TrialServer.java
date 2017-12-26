import java.io.*;
import java.net.*;

public class TrialServer {
    public static void main(String[] args){
        try{
            ServerSocket ss = new ServerSocket(9001);
            System.out.println("Client need");
            Socket s=ss.accept();//establish connection
            DataInputStream dis = new DataInputStream(s.getInputStream());
            System.out.println("Client says :" + dis.readUTF());
            ss.close();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}
