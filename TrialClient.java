import java.io.*;
import  java.net.*;

public class TrialClient {
    static DataOutputStream dis;
    static DataInputStream din;
    public static void main(String[] args){
        Socket s= null;
        try {
            //ec2-18-220-213-186.us-east-2.compute.amazonaws.com
            s = new Socket("127.0.0.1",9001);
            //Client is capable of sending data
            dis = new DataOutputStream(s.getOutputStream());
            din = new DataInputStream(s.getInputStream());

            BufferedReader bin;
            bin=new BufferedReader(new InputStreamReader(System.in));
            //Create an input stream here, that calls receive?

            while(true){
//                System.out.println("<Sending:> ");
//                String strin=bin.readLine();
//                dis.writeUTF(strin);
//                dis.flush();

                if(din.available() == 0)
                {
                    String response = din.readUTF();
                    System.out.printf("Received broadcast: %s\n", response);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
