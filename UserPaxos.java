import java.io.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.net.InetAddress;

public class UserPaxos {

    String ec2IP;


    private ServerSocket server=null;
    int threadnumber = 0 ;

    /*
    Solid state variables
     */
    Dictionary dictionary;
    EventLog log;

    /*
    Data Structures for connection
     */
    HashMap<Integer,Integer> crashFlags = new HashMap<>();
    //Contains mapping of ID against <IP,name>
    ConcurrentHashMap<Integer, String[]> userDetails = new ConcurrentHashMap<>();
    /*
    This will be populated from the acceptorsStableStorage
     */
   HashMap<Integer,AcceptorVariables> accNumAccVals = new HashMap<>();

   public UserPaxos(String ec2IP, ServerSocket server, int threadnumber, Dictionary dictionary, EventLog log, HashMap<Integer, Integer> crashFlags,
                    ConcurrentHashMap<Integer, String[]> userDetails, HashMap<String, Integer> userIPUserID,
                    ArrayList<ServerConnectionThread> connectedUsers, HashSet<PaxosClient> connectedServers, Integer userId,
                    String userName, int proposalNumber, int proposalIncrement, int majority_promise, int majority_ack,
                    String tweetToSend, int maxPrepare, int accNum, String accVal, HashMap<Integer, String> acceptedVals) {
        this.ec2IP = ec2IP;
        this.server = server;
        this.threadnumber = threadnumber;
        this.dictionary = dictionary;
        this.log = log;
        this.crashFlags = crashFlags;
        this.userDetails = userDetails;
        this.userIPUserID = userIPUserID;
        this.connectedUsers = connectedUsers;
        this.connectedServers = connectedServers;
        this.userId = userId;
        this.userName = userName;
        this.proposalNumber = proposalNumber;
        this.proposalIncrement = proposalIncrement;
        this.majority_promise = majority_promise;
        this.majority_ack = majority_ack;
        this.tweetToSend = tweetToSend;
        this.maxPrepare = maxPrepare;
        this.accNum = accNum;
        this.accVal = accVal;
        this.acceptedVals = acceptedVals;
    }

    public UserPaxos(){
        ec2IP = "";
        userId = 1;
        this.dictionary = new Dictionary();
        this.log = new EventLog();
        this.proposalNumber = 0;
        this.proposalIncrement = 0;

    }

    HashMap<String, Integer> userIPUserID = new HashMap<String, Integer>();
    private ArrayList<ServerConnectionThread> connectedUsers = new ArrayList<ServerConnectionThread>();
    HashSet<PaxosClient> connectedServers = new HashSet<PaxosClient>();
    Integer userId = 1;
    String userName;

    /*
    Initialise proposal number to UserID
    Initialise proposal increment to the size of the users
    */
    int proposalNumber;
    int proposalIncrement;
    //Will be reset in accept?
    int majority_promise = 0;
    int majority_ack = 0;

    String tweetToSend = "";

    /*
    The acceptor variables
    MaxPrepare changes in the accept(n,v)
     */
    int maxPrepare = 0;
    int accNum = 0;
    String accVal = "junk";

    int slotNumber = 0;
    HashMap<Integer, String> acceptedVals = new HashMap<>();
    AcceptorStableStorage acceptorStableStorage = new AcceptorStableStorage();
    /*
    The proposer's leader variable is set in commit phase
     */
    boolean leader = false;
    /*
    Will be reset after a majority_promise is reached for promise phase at proposer
     */
    int no_of_received_responses_promise = 0;

    int no_of_received_responses_ack = 0;
    /*
    The getter and setter methods for instance variables
     */
    public String getEc2IP() {
        return ec2IP;
    }

    public void setEc2IP(String ec2IP) {
        this.ec2IP = ec2IP;
    }

    public ServerSocket getServer() {
        return server;
    }

    public void setServer(ServerSocket server) {
        this.server = server;
    }

    public int getThreadnumber() {
        return threadnumber;
    }

    public void setThreadnumber(int threadnumber) {
        this.threadnumber = threadnumber;
    }

    public ArrayList<ServerConnectionThread> getConnectedUsers() {
        return connectedUsers;
    }

    public void setConnectedUsers(ArrayList<ServerConnectionThread> connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    public HashMap<Integer, Integer> getCrashFlags() {
        return crashFlags;
    }

    public void setCrashFlags(HashMap<Integer, Integer> crashFlags) {
        this.crashFlags = crashFlags;
    }

    public ConcurrentHashMap<Integer, String[]> getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(ConcurrentHashMap<Integer, String[]> userDetails) {
        this.userDetails = userDetails;
    }

    public HashMap<String, Integer> getUserIPUserID() {
        return userIPUserID;
    }

    public void setUserIPUserID(HashMap<String, Integer> userIPUserID) {
        this.userIPUserID = userIPUserID;
    }

    public HashSet<PaxosClient> getConnectedServers() {
        return connectedServers;
    }

    public void setConnectedServers(HashSet<PaxosClient> connectedServers) {
        this.connectedServers = connectedServers;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(int proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    public int getProposalIncrement() {
        return proposalIncrement;
    }

    public void setProposalIncrement(int proposalIncrement) {
        this.proposalIncrement = proposalIncrement;
    }

    public int getMajority_promise() {
        return majority_promise;
    }

    public void setMajority_promise(int majority_promise) {
        this.majority_promise = majority_promise;
    }

    public int getMajority_ack() {
        return majority_ack;
    }

    public void setMajority_ack(int majority_ack) {
        this.majority_ack = majority_ack;
    }

    public String getTweetToSend() {
        return tweetToSend;
    }

    public void setTweetToSend(String tweetToSend) {
        this.tweetToSend = tweetToSend;
    }

    public int getMaxPrepare() {
        return maxPrepare;
    }

    public void setMaxPrepare(int maxPrepare) {
        this.maxPrepare = maxPrepare;
    }

    public int getAccNum() {
        return accNum;
    }

    public void setAccNum(int accNum) {
        this.accNum = accNum;
    }

    public String getAccVal() {
        return accVal;
    }

    public void setAccVal(String accVal) {
        this.accVal = accVal;
    }

    public HashMap<Integer, String> getAcceptedVals() {
        return acceptedVals;
    }

    public void setAcceptedVals(HashMap<Integer, String> acceptedVals) {
        this.acceptedVals = acceptedVals;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public EventLog getLog() {
        return log;
    }

    public void setLog(EventLog log) {
        this.log = log;
    }

    public HashMap<Integer, AcceptorVariables> getAccNumAccVals() {
        return accNumAccVals;
    }

    public void setAccNumAccVals(HashMap<Integer, AcceptorVariables> accNumAccVals) {
        this.accNumAccVals = accNumAccVals;
    }

    public int getNo_of_received_responses_promise() {
        return no_of_received_responses_promise;
    }

    public void setNo_of_received_responses_promise(int no_of_received_responses_promise) {
        this.no_of_received_responses_promise = no_of_received_responses_promise;
    }

    public int getNo_of_received_responses_ack() {
        return no_of_received_responses_ack;
    }

    public void setNo_of_received_responses_ack(int no_of_received_responses_ack) {
        this.no_of_received_responses_ack = no_of_received_responses_ack;
    }

    /*
    The slot number is the size of the log, it increments in prepare phase
     */
    public int getSlotNumber() {
        return this.slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public AcceptorStableStorage getAcceptorStableStorage() {
        return acceptorStableStorage;
    }

    public void setAcceptorStableStorage(AcceptorStableStorage acceptorStableStorage) {
        this.acceptorStableStorage = acceptorStableStorage;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    /*
    Returns file path to access file
     */
    public synchronized String getPath(String filename) {
        String workingDir = Paths.get(".").toAbsolutePath().normalize().toString();
        String path = workingDir + File.separator + filename;
        return path;
    }

    public synchronized void createFile(String filename) {
        File file = new File(filename);
        boolean b = false;
        /*
         * exists() method tests whether the file or directory denoted by this
         * abstract pathname exists or not accordingly it will return TRUE /
         * FALSE.
         */
        if (!file.exists()) {
            /*
           * createNewFile() method is used to creates a new, empty file
           * mentioned by given abstract pathname if and only if a file with
           * this name does not exist in given abstract pathname.
             */
            try {
                b = file.createNewFile();
                if (b) {
                    System.out.println("Empty File successfully created");
                }
                else {
                    System.out.println("File exists");
                }
            } catch (IOException e) {
                System.err.println("Could not create file. " + e);
            }
        }

    }

    /*
    Forwarding point where the various phases of Paxos, utilise this function to send out the data to all connected clients
    Inherently summons the thread and sends out the data on that thread
    Proposers broadcast, and acceptors only send it to the proposer who responded
     */
    public void sendTweetPaxos(String tweet, MessageType messageType, UserParticipantType participantType, Integer proposerID) {

        for (int i = 0; i < connectedUsers.size(); i++) {
            int clientId = connectedUsers.get(i).getClientID();

            if(participantType == UserParticipantType.ACCEPTOR){
                if(clientId == proposerID){
                    /*
                    Don't send to a proposer who is down
                     */
                    if(crashFlags.get(proposerID) != 0){
                        ServerConnectionThread clientThread = connectedUsers.get(i);
                        if (clientThread != null){
                            Message messagePayload = new Message();
                            //This is used by default in the commit phase
                            messagePayload.setMessage(tweet);
                            messagePayload.setUserID(userId);
                            messagePayload.setMessageType(messageType);
                            messagePayload.setUserParticipantType(participantType);

                            if (messageType == MessageType.PROMISE && participantType == UserParticipantType.ACCEPTOR) {
                                boolean valid = tweet.indexOf("Valid") != -1 ? true : false;

                                if (valid) {
                                    String alteredTweet = tweet.replace("Valid ", "");
                                    String[] tweetSplits = alteredTweet.split(",");
                                    int accNum = Integer.parseInt(tweetSplits[0].split(":")[1]);
                                    String accVal = tweetSplits[1].split(":")[1];
                                    int slotNumber = Integer.parseInt(tweetSplits[2].split(":")[1]);
                                    messagePayload.setAccNum(accNum);
                                    messagePayload.setAccVal(accVal);
                                    messagePayload.setSlotNumber(slotNumber);
                                    messagePayload.setAcceptProposal(true);
                                    System.out.println("In send of promise phase for valid scenario");
                                } else {
                                    String[] tweetSplits = tweet.split(",");
                                    int slotNumber = Integer.parseInt(tweetSplits[1].split(":")[1]);
                                    messagePayload.setAcceptProposal(false);
                                    messagePayload.setAccVal("junk");
                                    messagePayload.setSlotNumber(slotNumber);
                                    messagePayload.setAccNum(0);
                                }

                            }

                            if (messageType == MessageType.ACK && participantType == UserParticipantType.ACCEPTOR) {
                                String alteredTweet = tweet.replace("Valid ", "");
                                String[] tweetSplits = alteredTweet.split(",");
                                boolean sendAck = Boolean.parseBoolean(tweetSplits[0].split(":")[1]);
                                int accNum = Integer.parseInt(tweetSplits[1].split(":")[1]);
                                String accVal = tweetSplits[2].split(":")[1];
                                int slotNumber = Integer.parseInt(tweetSplits[3].split(":")[1]);
                                messagePayload.setSendAck(sendAck);
                                messagePayload.setAccNum(accNum);
                                messagePayload.setAccVal(accVal);
                                messagePayload.setSlotNumber(slotNumber);
                            }

                            Gson g = new Gson();
                            String messageString = g.toJson(messagePayload);
                            //Din needs a \n
                            //ServerConnectionThread.sendTweetToUser(messageString);
                            //Change this to just tweet for testing
                            clientThread.sendTweetToUser(messageString);
                            System.out.println(proposalNumber + " Sending a tweet to proposer : " + clientThread.getClientName() + " on " + clientThread.getIpAddress());
                            break;
                        }
                    }
                    else{
                        System.out.println("The proposer who spoke to me " + userDetails.get(proposerID)[0] + " is down ");
                    }

                }
                else{
                    //Continue until you find the relevant ID
                    continue;
                }
            }
            else{
                /*
                Proposers broadcast message to all acceptors
                Blocked participants can act as acceptors as well
                If client is blocked by user then don't send tweets
                && !dictionary.amIBlocked(userDetails.get(clientId)[0], userDetails.get(userId)[0])
                Don't send message to crashed sites
                */
                if (crashFlags.get(clientId) != 0) {
                /*
                  Build a json that can be sent over the network as a string
                  Eg: {"message":"Hey","nodeTable":[[0,1],[2,3]],"NP":log}
                 */
                    //Fetch thread assigned to this user, and send message on this node
                    ServerConnectionThread clientThread = connectedUsers.get(i);
                    if (clientThread != null) {
                        Message messagePayload = new Message();
                        //This is used by default in the commit phase
                        messagePayload.setMessage(tweet);
                        messagePayload.setUserID(userId);
                        messagePayload.setMessageType(messageType);
                        messagePayload.setUserParticipantType(participantType);

                        if (messageType == MessageType.PREPARE && participantType == UserParticipantType.PROPOSER) {
                            String alteredTweet = tweet.replace("Valid ", "");
                            String[] tweetSplits = alteredTweet.split(",");
                            int proposalNumber = Integer.parseInt(tweetSplits[0].split(":")[1]);
                            int slotNumber = Integer.parseInt(tweetSplits[1].split(":")[1]);
                            messagePayload.setProposalNumber(proposalNumber);
                            messagePayload.setSlotNumber(slotNumber);
                        }


                        if (messageType == MessageType.ACCEPT && participantType == UserParticipantType.PROPOSER) {
                            //Uncomment later on, when there is no majority_promise after a timeout
                            String alteredTweet = tweet.replace("Valid ", "");
                            String[] tweetSplits = alteredTweet.split(",");
                            int recvNum = Integer.parseInt(tweetSplits[0].split(":")[1]);
                            String recvVal = tweetSplits[1].split(":")[1];
                            int slotNumber = Integer.parseInt(tweetSplits[2].split(":")[1]);
                            messagePayload.setRecvProposal(recvNum);
                            messagePayload.setRecvVal(recvVal);
                            messagePayload.setSlotNumber(slotNumber);
                        }



                        if (messageType == MessageType.COMMIT && participantType == UserParticipantType.PROPOSER) {
                            String alteredTweet = tweet.replace("Valid ", "");
                            String[] tweetSplits = alteredTweet.split(",");
                            String logWritable = tweetSplits[0].split(":")[1];
                            int slotNumber = Integer.parseInt(tweetSplits[1].split(":")[1]);
                            messagePayload.setMessage(logWritable);
                            messagePayload.setSlotNumber(slotNumber);
                        }

                        Gson g = new Gson();
                        String messageString = g.toJson(messagePayload);
                        //Din needs a \n
                        //ServerConnectionThread.sendTweetToUser(messageString);
                        //Change this to just tweet for testing
                        clientThread.sendTweetToUser(messageString);
                        System.out.println(proposalNumber + " Sending a tweet to : " + clientThread.getClientName() + " on " + clientThread.getIpAddress());
                    }
                }
            }


        }
    }

    /*
    This function is the entry point from the client, which can either be from the proposer or acceptor end
    Based on the message Payload, channelise the function to either a prepare, promise, accept, ack or commit phase
     */
    public void receiveTweetPaxos(String tweet){
        System.out.println(" In receive of User - Message receive in process for ");
        //Converting it back to a JSON string
        //tweet = tweet.replace("\n","");
        Gson gson = new Gson();
        Type messageType = new TypeToken<Message>() {
        }.getType();
        System.out.println("I enter here in receive");

        Message messsageReceived = gson.fromJson(tweet, messageType);
        MessageType receivedMessageType = messsageReceived.getMessageType();
        UserParticipantType senderType = messsageReceived.getUserParticipantType();
        String messageRecv = messsageReceived.getMessage();
        //If sender was Proposer, then acceptor should take action
        if(senderType == UserParticipantType.PROPOSER){
            senderType = UserParticipantType.ACCEPTOR;
        }
        else{
            senderType = UserParticipantType.PROPOSER;
        }
        /*
        Message type dictates what function to call
         */

        if(receivedMessageType == MessageType.PREPARE){
            int proposalNumber = messsageReceived.getProposalNumber();
            int slotNumber = messsageReceived.getSlotNumber();
            int proposerId = messsageReceived.getUserID();
            System.out.println("Sending a prepare request to Acceptor " + proposalNumber + " for slot " + slotNumber);
            prepareTweet(proposalNumber, senderType,slotNumber,proposerId);
        }
        else if(receivedMessageType == MessageType.PROMISE){
            System.out.println("In the receive phase of promise");
            //Receipt of promise is a proposer, so proposerID is 0
            promiseTweet(messsageReceived.getAccNum(),messsageReceived.getAccVal(),messsageReceived.getAcceptProposal(),senderType, messsageReceived.getSlotNumber(),messsageReceived.getUserID() );
        }
        else if(receivedMessageType == MessageType.ACCEPT){
            System.out.println("In the receive phase of accept");
            acceptTweet(messsageReceived.getRecvProposal(),messsageReceived.getRecvVal(),senderType,messsageReceived.getSlotNumber(),messsageReceived.getUserID() );
        }
        else if(receivedMessageType == MessageType.ACK){
            System.out.println("In the receive phase of ack");
            ackTweet(messsageReceived.getSendAck(),messsageReceived.getAccNum(),messsageReceived.getAccVal(),senderType,messsageReceived.getSlotNumber(),messsageReceived.getUserID());
        }
        else if(receivedMessageType == MessageType.COMMIT) {
            System.out.println("In the receive phase of commit");
            commit(messsageReceived.getUserID(),messageRecv,senderType,messsageReceived.getSlotNumber(),messsageReceived.getUserID());
        }

    }

    /*
    Save the log, node table and dictionary to recover from a crash
    Need to change this to include the saving of accNum, accVal; log
     */
    public void saveToSolidState() {
        log.saveEventLog();
        dictionary.saveDict();
        acceptorStableStorage.saveAcceptorStableStorage();
    }

    /*
    This function will return the AcceptorVariable at the slot number
    If not present, it will create a slot and add default values
     */
    public AcceptorVariables getAcceptorVarForSlot(int slotNumber){
        if(acceptorStableStorage.getAcceptorVariables().containsKey(slotNumber)){
            return acceptorStableStorage.getAcceptorVariables().get(slotNumber);
        }
        else{
            /*
            Add it to the stable storage
            And save it as well
             */
            AcceptorVariables acceptorVariable = new AcceptorVariables();
            acceptorVariable.setSlotNumber(slotNumber);
            acceptorStableStorage.addToAcceptorStableStorage(acceptorVariable);
            acceptorStableStorage.saveAcceptorStableStorage();
            return acceptorVariable;
        }

    }
    /*
    Get the number of entries in log
     */
    public int getLogLength(){
        return log.getLog().size();
    }
    /*
   Runs the prepare phase of the tweet
   Acceptor only replies back to the proposer that spoke to it
    */
    public void prepareTweet(int proposalNumber, UserParticipantType participantType, int slotNumber, int proposerID){
        /*
        Proposer sends value
        proposalNumber += proposalIncrement;
        Need to increment this in Paxos
         */
        System.out.println("In the prepare phase as " + participantType);

        if(participantType == UserParticipantType.PROPOSER){
            //Increment only your proposal number
            this.proposalNumber += this.userDetails.size();
            //Increment the slotNumber
            setSlotNumber(getLogLength() + 1);
            //Proposer uses his slot number
            String proposalNumberStr = "pNum:"+Integer.toString(this.proposalNumber) + ",slot:" + Integer.toString(getSlotNumber());
            //Augment and send the slot number
            System.out.println("Sending a prepare request " + participantType);
            //It sends a message payload and need to decipher from there in the second portion, proposer broadcasts message to all acceptors
            sendTweetPaxos(proposalNumberStr,MessageType.PREPARE, UserParticipantType.PROPOSER, 0);
            //Write a sendTweetPaxos, build a message

        }
        else{
            //Receive a prepare message, check with maxPrepare, receive essentially just channelises it
            //This function returns
            boolean acceptProposal = false;
            //Retrieve acceptorVals for this slot
            AcceptorVariables acceptorVariable = getAcceptorVarForSlot(slotNumber);
            int accNumAcceptor = acceptorVariable.getAccNum();
            String accValAcceptor = acceptorVariable.getAccVal();
            int maxPrepareAcceptor = acceptorVariable.getMaxPrepare();
            if(proposalNumber > maxPrepareAcceptor) {
                //call the promise function
                acceptProposal = true;
                maxPrepareAcceptor = proposalNumber;
                acceptorVariable.setMaxPrepare(maxPrepareAcceptor);
                //Add the changed acceptorVariable
                acceptorStableStorage.addToAcceptorStableStorage(acceptorVariable);
                acceptorStableStorage.saveAcceptorStableStorage();
                System.out.println(" I can accept your proposal " + proposalNumber + " , " + maxPrepareAcceptor);
            }
            else
            {
                System.out.println("I cannot accept your proposal " + proposalNumber + " , " + maxPrepareAcceptor);
            }
            /*
            Call the promise function irrespective of whether you can accept the proposal or not
            Just to let the proposer know if they have a chance
            Slot number always gets passed from proposer
            ProposerID is the receiverID
             */
            promiseTweet(accNumAcceptor, accValAcceptor, acceptProposal,UserParticipantType.ACCEPTOR, slotNumber, proposerID);

        }

    }


    /*
    Return number of alive sites
     */
    public int getAliveCount(){
        int number_of_noncrash = 0;
        for(int no_crash: crashFlags.values()){
            if(no_crash == 1){
                number_of_noncrash += 1;
            }
        }
        return number_of_noncrash;
    }

    /*
    Acceptor - sends message to client with accNum, accVal if proposal number is greater
    Proposer - on the other hand updates its majority_promise, as it receives promises
     */
    public void promiseTweet(int accNum, String accVal, boolean acceptProposal, UserParticipantType participantType, int slotNumber, int proposerID){
        if(participantType == UserParticipantType.ACCEPTOR){
            //Get the acceptorVariable values for slot
            System.out.println("In promise phase" + acceptProposal);
            //This gets set by the prepare function at the acceptor's end
            if(acceptProposal){
                String condensed = "Valid accNum:" + accNum + ",accVal:" + accVal + ",slotNum:" + slotNumber;
                sendTweetPaxos(condensed,MessageType.PROMISE,UserParticipantType.ACCEPTOR, proposerID);
            }
            else{
                sendTweetPaxos("I cannot accept your proposal,slotNum:" + slotNumber, MessageType.PROMISE, UserParticipantType.ACCEPTOR, proposerID);
            }
        }
        else
        {
            if(acceptProposal){
                majority_promise += 1;
                acceptedVals.put(accNum,accVal);
                System.out.println("The acceptor has replied with an accNum of " + accNum + " accVal " + accVal);
            }

            no_of_received_responses_promise += 1;
            int number_of_noncrash = getAliveCount();

            if(no_of_received_responses_promise == number_of_noncrash){
                //Irrespective of acceptProposal or not, you keep checking for majority_promise
                 /*
                    If majority_promise then call accept
                    Need to have a false accept to declare majority_promise cannot be reached
                 */
                //Change this to >
                if(majority_promise > (int)(userDetails.size()/2)){
                    System.out.println("Received majority_promise of promises, sending out an accept ");
                    //To get the largest accVal
                    int maxAccNum = Collections.max(acceptedVals.keySet());
                    String proposedVal = "";
                    Collection<String> allStrings = acceptedVals.values();
                    //If all nulls then you can propose own value
                    boolean containsNull = true;
                    for(String acceptedString: allStrings){
                        //A non-empty value
                        if(!acceptedString.equalsIgnoreCase("junk")){
                            proposedVal = acceptedVals.get(maxAccNum);
                            containsNull = false;
                            break;
                        }
                    }
                    //If all the acceptors have not seen anything for the slot yet, then proposer can send its own value
                    if(containsNull){
                        proposedVal = getTweetToSend();
                        setTweetToSend(proposedVal);
                    }
                    //Forwarding this to the accept function of Proposer, proposer broadcasts accept to all acceptors
                    acceptTweet(proposalNumber,proposedVal,UserParticipantType.PROPOSER,slotNumber,0 );
                }
                else{
                    System.out.println("Retrying a prepare, no majority_promise votes received from the alive acceptors  ");
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runSynod(getTweetToSend());
                }
                //Reset the number of responses received after a run of promise
                no_of_received_responses_promise = 0;
                majority_promise = 0;
                //Reset the acceptedVals
                setAcceptedVals(new HashMap<Integer, String>());
            }
        }

    }
    /*
    Accept for proposer - send the values of n, v to all acceptors
    Accept for acceptor - receive vals of n, v check against max Prepare and reset the accNum and accVal
     */
    public void acceptTweet(int n, String messageString, UserParticipantType participantType, int slotNumber, int proposerID){
        if(participantType == UserParticipantType.PROPOSER){
            /*
            So the slot number and other values are forwarded from promiseTweet phase of proposer
             */
            String condensed = "n:"+n+",v:"+messageString+",slotNum:"+slotNumber;
            //Proposer broadcasts message, so proposerID is irrelevant in this case
            sendTweetPaxos(condensed,MessageType.ACCEPT,UserParticipantType.PROPOSER,0 );
            System.out.println("In the accept phase of proposer");
        }
        else{
            //Change this to greater than
            boolean acceptTweet = false;
            //Get the maxPrepare and the other vals for this slot
            AcceptorVariables acceptorVariable = acceptorStableStorage.getAcceptorVariables().get(slotNumber);

            if(n >= acceptorVariable.getMaxPrepare()){
                System.out.println("Acceptor is able to accept the tweet " + messageString);
                acceptorVariable.setAccNum(n);
                acceptorVariable.setAccVal(messageString);
                acceptorVariable.setMaxPrepare(n);
                acceptorStableStorage.addToAcceptorStableStorage(acceptorVariable);
                acceptorStableStorage.saveAcceptorStableStorage();
                acceptTweet = true;
            }
            else{
                System.out.println("Acceptor is unable to accept the tweet " + messageString + " as its maxPrepare is higher than " + n);
            }
            //Call the ack function here, irrespective of whether you accept or not
            ackTweet(acceptTweet,acceptorVariable.getAccNum(),acceptorVariable.getAccVal(),UserParticipantType.ACCEPTOR,slotNumber, proposerID);
        }


    }
    /*
    Acceptor - sends an ack if it has accepted the proposal
    Proposer - on receiving a majority_promise runs a commit
     */
    public void ackTweet(boolean accepted_ack, int accNum, String accVal, UserParticipantType userParticipantType, int slotNumber, int proposerID){
        if(userParticipantType == UserParticipantType.ACCEPTOR){
            String condensed = "accepted:"+accepted_ack+",accNum:"+accNum+",accVal:"+accVal+",slotNum:"+slotNumber;
            //Set sendAck based on accepted to the proposer who sent an accept
            //ProposerID will come from the accept phase
            sendTweetPaxos(condensed,MessageType.ACK,UserParticipantType.ACCEPTOR, proposerID);
        }
        else{
            if(accepted_ack){
                System.out.println("I have received an acknowledgement ");
                majority_ack+=1;
            }
            else{
                System.out.println("I was declined an ack ");
            }
            no_of_received_responses_ack += 1;
            //Change this to >
            //If majority_promise has been reached, send commits.
            //Might need to move this out, to keep checking with timeouts
            int alive_sites = getAliveCount();

            if(no_of_received_responses_ack == alive_sites){
                //Own ack is also taken care here
                if(majority_ack > (int)(userDetails.size()/2)){
                    System.out.println("Received majority_promise of acknowledgements, sending out a commit ");
                    //Send it out to all acceptors
                    commit(userId, getTweetToSend(),UserParticipantType.PROPOSER , slotNumber,0);
                }
                else
                {
                    System.out.println("Retrying the synod phase again, failed to receive majority_promise from acceptors ");
                    /*
                    Retry if the value failed
                     */
                    runSynod(tweetToSend);
                }
                /*
                Reset variables after all sites have been checked
                 */
                no_of_received_responses_ack = 0;
                majority_ack = 0;

            }

        }

    }
    /*
    Acceptor - writes the received Tweet into log
    Proposer - sends the log entry to all acceptors, including self
     */
    public void commit(int senderId, String tweet, UserParticipantType userParticipantType, int slotNumber, int proposerID){
        if(userParticipantType == UserParticipantType.PROPOSER){
            String commitable = "Message:"+tweet+",slotNumb:" + slotNumber;
            //The person who sends the message sets the leader to true
            leader = true;
            System.out.println("Sending tweet " + tweet + " to be committed to all acceptors");
            //Irrelevant here, need to broadcast to everyone
            sendTweetPaxos(commitable,MessageType.COMMIT,userParticipantType,0);
        }
        else{
            /*
            Write the content to log
             */
            //Save to own log
            System.out.println("Writing the tweet to log " + tweet);
            Event recvEvent = new Event();
            recvEvent.setNodeId(senderId);
            recvEvent.setNodeName(userDetails.get(senderId)[0]);
            recvEvent.setContents(tweet);
            recvEvent.setTimestamp(Instant.now().toString());
            //This will get set from tweet parsing
            recvEvent.setOpType(Event.OperationTypes.SEND);
            recvEvent.setSlotNumber(slotNumber);
            log.addToEventLog(recvEvent);
            //The acceptor knows it has received a tweet, hence it sets to false
            leader = false;
            saveToSolidState();
        }
    }

    /*
    Simulate a run of Synod, where proposer tries to get their value into the log
     */
    public void runSynod(String tweet){
        //Accept is to be called, only if there is a majority_promise
        if(leader){
            System.out.println("I am the leader " + userId + " I skip the prepare and promise ");
            this.proposalNumber += this.userDetails.size();
            acceptTweet(this.proposalNumber,tweet,UserParticipantType.PROPOSER,getLogLength() + 1,0 );
        }
        else{
            prepareTweet(this.proposalNumber,UserParticipantType.PROPOSER,getLogLength(),0 );
        }

    }

    /*
    Just prints out the log to the console
    Tweets are shown in timestamp order

     */
    public void viewTimeline() {
        List<Event> events = log.getSortedLog();
        List<String> tweetsList = new ArrayList<String>();

        for (Event event : events) {
            /*
            If current machine is blocked by sender of tweet, then don't show the tweet
             */
            if (event.getOpType() == Event.OperationTypes.SEND
                    && !dictionary.amIBlocked(userDetails.get(userId)[0], event.getNodeName())) {
                tweetsList.add(event.getTimestamp() + " : " + event.getNodeName() + " says " + event.getContents());

            }
        }

        for (String tweet : tweetsList) {
            System.out.println(tweet);
        }
    }

    public void blockUser(int nodeID) {
        //Need to call runSynod here
        String blockedFollower = userDetails.get(nodeID)[0];
        String userName = userDetails.get(userId)[0];
        dictionary.addToDict(blockedFollower, userName);
        Event blockEvent = new Event();
        blockEvent.setNodeId(userId);
        blockEvent.setContents(blockedFollower);
        blockEvent.setNodeName(userName);
        blockEvent.setTimestamp(Instant.now().toString());
        blockEvent.setOpType(Event.OperationTypes.BLOCK);
        log.addToEventLog(blockEvent);
        saveToSolidState();
    }

    /*
    Simulates the delete operation, wherein a (user,follower) pair is removed from the dictionary
     */
    public void unblockUser(int nodeID) {
        //Need to call runSynod here
        String blockedFollower = userDetails.get(nodeID)[0];
        String userName = userDetails.get(userId)[0];
        dictionary.removeFromDict(blockedFollower, userName);
        Event blockEvent = new Event();
        blockEvent.setNodeId(userId);
        blockEvent.setNodeName(userName);
        blockEvent.setContents(blockedFollower);
        blockEvent.setTimestamp(Instant.now().toString());
        blockEvent.setOpType(Event.OperationTypes.UNBLOCK);
        log.addToEventLog(blockEvent);
        saveToSolidState();
    }

    public static void main(String args[])
    {
        UserPaxos u1 = new UserPaxos();
        u1.init();
        u1.serverThread();
        u1.clientConnect();
        Scanner sc = new Scanner(System.in);
        String tweet;
        System.out.println("Flow of program continuing");

        boolean menudriven = true;
        //Trying to simulate the broadcast scenario
        while (menudriven) {
            System.out.println("Enter 1 to Send Message");
            //Send message needs to call Synod
            System.out.println("Enter 2 to View Timeline");
            System.out.println("Enter 3 to View Dictionary");
            System.out.println("Enter 4 to Block Someone");
            System.out.println("Enter 5 to UnBlock Someone");
            System.out.println("Enter 7 to View client and servers");
            System.out.println("Enter 6 to Quit\n");
            Scanner in = new Scanner(System.in);
            int input = in.nextInt();

            switch (input) {
                case 1: {
                    System.out.println("Enter the message you would like to send:");
                    in.nextLine();
                    u1.setTweetToSend(in.nextLine());
                    //u1.tweetToSend = in.nextLine();
                    u1.runSynod(u1.getTweetToSend());
                    break;
                }

                case 2: {
                    u1.viewTimeline();
                    System.out.println("\n");
                    break;
                }

                case 3: {
                    u1.dictionary.printDict();
                    System.out.println("\n");
                    break;
                }

                case 4: {
                    System.out.println("Who would you like to BLOCK?");

                    for (Integer key: u1.getUserDetails().keySet()) {
                        System.out.println("\tUser: " + key + "\tName: " + u1.getUserDetails().get(key)[0]);
                    }
                    System.out.println("Enter userID to block:\n");
                    int idToBlock = in.nextInt();
                    u1.blockUser(idToBlock);
                    System.out.println("User has been blocked!\n");
                    break;
                }

                case 5: {
                    System.out.println("Who would you like to UNBLOCK?");
                    for (Integer key: u1.getUserDetails().keySet()) {
                        System.out.println("\tUser: " + key + "\tName: " + u1.getUserDetails().get(key)[0]);
                    }
                    System.out.println("Enter userID to unblock:");
                    int idToUnblock = in.nextInt();
                    u1.unblockUser(idToUnblock);
                    System.out.println("User has been unblocked!\n");
                    break;
                }

                case 7:{
                    System.out.println("connected servers");
                    for(PaxosClient clientConnector:u1.getConnectedServers()){
                        System.out.println(clientConnector.getServerIPAddress());
                    }
                    System.out.println("connected clients");
                    for(ServerConnectionThread clientConnector:u1.getConnectedUsers()){
                        System.out.println(clientConnector.getIpAddress());
                    }
                    break;
                }
                case 6: {
                    System.out.println("Bye bye!");
                    //Close all the thread pools

                    System.exit(0);
                    break;
                }

                default:
                    System.out.println("Error! Please enter menu items 1-6.");
            }
        }

    }
    public void init()
    {
        //Need to add create files for the accNum and accVal, maxPrepare vals as well
        createFile(getPath("mylog.json"));
        createFile(getPath("mydictionary.json"));
        createFile(getPath("acceptor.json"));

        //Loading user details into hashmap
        BufferedReader reader = null;
        String line = "";

        //Load all the datastructures here
        log.loadEventLog();
        dictionary.loadDict();
        acceptorStableStorage.loadAcceptorStableStorage();
        
        try {
            reader = new BufferedReader(new FileReader(getPath("IP_list.txt")));
            while ((line = reader.readLine()) != null) {
                String[] userLine = line.split(" ");
                userDetails.put(Integer.valueOf(userLine[0]), new String[]{userLine[1], userLine[2]});
                userIPUserID.put(userLine[2], Integer.valueOf(userLine[0]));
                crashFlags.put(Integer.valueOf(userLine[0]),0);
            }

            InetAddress localhost = InetAddress.getLocalHost();
            /*
            Will later on use EC2 SDK here
             */
            //EC2MetadataUtils metadataUtils = new EC2MetadataUtils();
//        System.out.println(metadataUtils.getNetworkInterfaces().get(0).getPublicHostname());
//        ec2IP = metadataUtils.getNetworkInterfaces().get(0).getPublicIPv4s().get(0);

            ec2IP = (localhost.getHostAddress()).trim();
            System.out.println("System IP Address : " +
                    ec2IP);

            if(userIPUserID.containsKey(ec2IP)){
                userId = userIPUserID.get(ec2IP);
                this.proposalNumber = userId;
                System.out.println("Starting the service for : " + userId + " IP " + ec2IP);
            }

            /*Testing purpose*/
            for(Integer id: userDetails.keySet()){
                System.out.println("ID : " + id + " IP : " + userDetails.get(id)[1]);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }



        /*read from the file */
        try
        {
            /*
            Also give an IP
             */
            server = new ServerSocket(8887);
            System.out.println("Server Started");
            connectedUsers = new ArrayList<ServerConnectionThread>();

        }
        catch(IOException e)
        {
            System.out.println(e);
        }

    }

    public void serverThread()
    {
        UserPaxos u1 = this;
        Thread t = new Thread()
        {
            public void run(){
                while(true)
                {

                    try{
                        Socket serverSocket = server.accept();
                        String ipAddress = serverSocket.getInetAddress().getHostAddress();
                        String clientName = "";
                        Integer clientId = 0;

                        //Clients are identified by their IP address, as we have this stored in the config file
                        if (userIPUserID.containsKey(ipAddress)) {
                            clientId = userIPUserID.get(ipAddress);
                            clientName = userDetails.get(clientId)[0];
                        }

                        //UserConnector userConnector = new UserConnector(serverSocket, u1, clientName, ipAddress, clientId);
                        /*Add serverIP bit here*/
                        connectedUsers.add(new ServerConnectionThread(serverSocket, u1, clientName, ipAddress, clientId));
                    }
                    catch(IOException e)
                    {
                        System.out.println(e);
                    }

                }

            }
        };
        t.start();
    }
    /*
    Runs client thread to constantly connect to other servers
     */
    public void clientConnect()
    {
        UserPaxos user = this;

        Thread client = new Thread()
        {
            @SuppressWarnings("deprecation")
            public void run()
            {
                while(true)
                {
                    /*
                    Need to use a Hashmap, demarcated by userIds
                    Write a for loop for the hashmap
                     */
                    for (int serverId : userDetails.keySet()){
                        /*
                        Don't connect to self
                         */
                        if(crashFlags.get(serverId) == 0){
                            int currentConnectFlag = crashFlags.get(serverId);
                            if(currentConnectFlag ==0)
                            {
                                try
                                {
                                    System.out.println("Trying to connect to " + serverId);
                                    String serverIPAddress = userDetails.get(serverId)[1];
                                    Socket clientSocket = new Socket(serverIPAddress,8887);
                                    currentConnectFlag =1;
                                    crashFlags.put(serverId,1);
                                    System.out.println("Connecting to " + serverId + " established");
                                    //Creating a client that can connect to  a server
                                    PaxosClient cl = new PaxosClient(user,clientSocket,serverId,serverIPAddress);
                                    cl.start();
                                    connectedServers.add(cl);
                                    threadnumber+=1;

                                }
                                catch(IOException e)
                                {
                                    System.out.println("Connection failed " + e.getMessage());
                                }
                            }
                        }

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }

            }
        };
        client.start();
    }

}

class ServerConnectionThread
{
    /*
    This needs a backreference to call the sen
     */
    UserPaxos user;

    Socket listen;
    DataInputStream inp;
    DataOutputStream out;
    String clientName;
    String ipAddress;
    Integer clientID;

    ServerConnectionThread(Socket hear, UserPaxos user, String clientName, String ipAddress, Integer clientID)
    {
        listen = hear;
        try
        {
            inp = new DataInputStream(listen.getInputStream());
            out = new DataOutputStream(listen.getOutputStream());
            this.user = user;
            this.ipAddress = ipAddress;
            this.clientName = clientName;
            this.clientID = clientID;
            System.out.println(" Details " + this.clientName);
        }
        catch(IOException e)
        {
            System.out.println("Infinite loop");
            System.out.println(e);
        }
    }
    public UserPaxos getUser() {
        return user;
    }

    public void setUser(UserPaxos user) {
        this.user = user;
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
    public void write(String message)
    {
        try
        {
            Message message1 = new Message();
            message1.setMessage(message);
            Gson g = new Gson();
            String tweetObject = g.toJson(message1);
            System.out.println(tweetObject);
            out.writeUTF(tweetObject);
            out.flush();
        }
        catch(IOException e)
        {
            //   System.out.println(e);
        }

    }
    /*
    Will be called by the Paxos send function
    It only receives data and sends it to user
     */
    public void sendTweetToUser(String tweet) {
        try
        {
            System.out.println("Sending message" + tweet);
            out.writeUTF(tweet);
            out.flush();
        }
        catch(IOException e)
        {
            //   System.out.println(e);
        }
    }
}
