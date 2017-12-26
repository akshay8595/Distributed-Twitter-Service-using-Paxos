
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import com.amazonaws.util.EC2MetadataUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.nio.file.Paths;

import java.time.Instant;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;
import java.io.File;
import java.util.Collections;

public class User {

    String ec2IP;
    Integer userId;
    String name;
    
    //Need client name-ID mapping for blocked user bit.
    HashSet<UserClientConnector> connectedServers = new HashSet<UserClientConnector>();
    ConcurrentHashMap<Integer, String[]> userDetails = new ConcurrentHashMap<>();
    CopyOnWriteArrayList<UserConnector> connectedUsers = new CopyOnWriteArrayList<>();
    HashMap<String, Integer> userIPUserID = new HashMap<String, Integer>();
    HashSet<String> unconnectedServers = new HashSet<>();
    ServerSocket serverSocket;
    int numClients = 4;
    final ExecutorService serverListeningPool = Executors.newFixedThreadPool(10);
    final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
    //WuuBernstein initial conditions
    Dictionary dictionary;
    int eventCounter = 0;
    NodeTable nodeTable;
    EventLog log;

    /*
    Initialise proposal number to UserID
    Initialise proposal increment to the size of the users
     */
    int proposalNumber;
    int proposalIncrement;
    //Will be reset in accept?
    int majority = 0;
    int majority_ack = 0;

    String tweetToSend = "";

    /*
    The acceptor variables
    MaxPrepare changes in the accept(n,v)
     */
    int maxPrepare = 0;
    int accNum = 0;
    String accVal = "junk";
    HashMap<Integer,String> acceptedVals = new HashMap<Integer, String>();

    public User(String name,
            String ec2IP,
            int userId,
            Dictionary dictionary,
            NodeTable nodeTable,
            EventLog log,
            int eventCounter) {
        this.name = name;
        this.ec2IP = ec2IP;
        this.userId = userId;
        this.dictionary = dictionary;
        this.nodeTable = nodeTable;
        this.log = log;
        this.eventCounter = eventCounter;
        this.proposalNumber = 0;
        this.proposalIncrement = 0;
        
    }

    public ExecutorService getServerListeningPool() {
        return serverListeningPool;
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

    public CopyOnWriteArrayList<UserConnector> getConnectedUsers() {
        return connectedUsers;
    }

    public void setConnectedUsers(CopyOnWriteArrayList<UserConnector> connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public NodeTable getNodeTable() {
        return nodeTable;
    }

    public void setNodeTable(NodeTable nodeTable) {
        this.nodeTable = nodeTable;
    }

    public HashSet<UserClientConnector> getConnectedServers() {
        return connectedServers;
    }

    public void setConnectedServers(HashSet<UserClientConnector> connectedServers) {
        this.connectedServers = connectedServers;
    }

    public HashSet<String> getUnconnectedServers() {
        return unconnectedServers;
    }

    public void setUnconnectedServers(HashSet<String> unconnectedServers) {
        this.unconnectedServers = unconnectedServers;
    }

    public String getTweetToSend() {
        return tweetToSend;
    }

    public void setTweetToSend(String tweetToSend) {
        this.tweetToSend = tweetToSend;
    }

    public User() {
        name = new String();
        ec2IP = "";
        userId = 1;
        this.dictionary = new Dictionary();
        this.log = new EventLog();
        this.nodeTable = new NodeTable();
        this.eventCounter = 0;
        this.serverSocket = null;
        this.proposalNumber = 0;
        this.proposalIncrement = 0;
    }

    public ExecutorService getClientProcessingPool() {
        return clientProcessingPool;
    }

    /*
    Will be called from sendTweet to send tweet to a particular client
     */
    public UserConnector getThreadConnectedToClient(Integer userId) {
        for (UserConnector uc : connectedUsers) {
            if (uc.getClientID() == userId) {
                return uc;
            }
        }
        return null;
    }
    
        
     /*
    Need a client for every other site that has come up
    The client threads spawned will connect to the servers, and listen for input
     */
    public void startUserClients() {
        //Creating a thread pool for N users I can connect to
        //final ExecutorService serverListeningPool = Executors.newFixedThreadPool(10);
        User user = this;

        //To unblock the other main operation
        Runnable clientTask = new Runnable() {
            @Override
            public void run() {
                System.out.println("Connecting to servers...");

                for (int serverId : userDetails.keySet()) {
                    //Don't connect to own server, as a client
                    if(serverId != userId){
                        String serverIPAddress = userDetails.get(serverId)[1];

                        //Add an if condition to check if it is the current user, then don't connect
                        UserClientConnector userClientConnector = new UserClientConnector(user, user.userId, serverIPAddress, user.getEc2IP());
                        connectedServers.add(userClientConnector);
                        serverListeningPool.submit(userClientConnector);
                    }
                }

                while (true) {
                    //Users are added to the unconnected servers only after 4 retries so this won't be tried too often
                    while (unconnectedServers.iterator().hasNext()) {
                        String serverIpAddress = unconnectedServers.iterator().next();
                        //Add an if condition to check if it is the current user, then don't connect
                        //On a successful connect the user will be removed
                        UserClientConnector userClientConnector = new UserClientConnector(user, user.userId, serverIpAddress, user.getEc2IP());
                        connectedServers.add(userClientConnector);
                        serverListeningPool.submit(userClientConnector);
                        unconnectedServers.remove(serverIpAddress);
                    }
                }
            }
        };
        Thread serverThread = new Thread(clientTask);
        serverThread.start();
    }

    /*
    Spawning incoming client threads that can establish connection with the server thread
     */
    public void startUserServer() {
        //Creating a thread pool for N users I can connect to
        //final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
        User user = this;

        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                try {
                    if (System.getProperty("cPort") != null) {
                        System.out.println("Using client port " + System.getProperty("cPort"));
                        serverSocket = new ServerSocket(Integer.valueOf(System.getProperty("cPort")));
                    } else {
                        serverSocket = new ServerSocket(9001);
                    }
                    System.out.println("Accepting connections...");
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("I recognise a client" + clientSocket.getInetAddress() + " port : " + clientSocket.getPort());
                        String ipAddress = clientSocket.getInetAddress().getHostAddress();
                        String clientName = "";
                        Integer clientId = 0;

                        //Clients are identified by their IP address, as we have this stored in the config file
                        if (userIPUserID.containsKey(ipAddress)) {
                            clientId = userIPUserID.get(ipAddress);
                            clientName = userDetails.get(clientId)[0];
                        }

                        UserConnector userConnector = new UserConnector(clientSocket, user, clientName, ipAddress, clientId);
                        //userConnector.start();
                        connectedUsers.add(userConnector);
                        clientProcessingPool.execute(userConnector);
//                        Future<?> future = clientProcessingPool.submit(userConnector);
//                        try {
//                            future.get();
//                        } catch (InterruptedException e) {
//                            System.err.println("Client Processing pool error. " + e);
//                        } catch (ExecutionException e) {
//                            System.err.println("Client " + clientName + " is disconnected. " + e);
//                        }
                    }
                } catch (IOException e) {
                    System.err.println("Unable to process client request " + e);                   
                }
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }

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
    Reads from the file, and updates the User name, IP, ID
     */
    public void init() {
//        EC2MetadataUtils metadataUtils = new EC2MetadataUtils();
//        System.out.println(metadataUtils.getNetworkInterfaces().get(0).getPublicHostname());
//        ec2IP = metadataUtils.getNetworkInterfaces().get(0).getPublicIPv4s().get(0);
        //metadataUtils.getNetworkInterfaces()

        createFile(getPath("mydictionary.json"));
        createFile(getPath("nodeTable.json"));
        createFile(getPath("mylog.json"));
        String line = "";

        //Load all the datastructures here
        log.loadEventLog();
        dictionary.loadDict();

        //Loading user details into hashmap
        BufferedReader reader = null;

        try {
            if (System.getProperty("ipList") != null) {
                System.out.println("Using ips " + System.getProperty("ipList"));
                reader = new BufferedReader(new FileReader(getPath(System.getProperty("ipList"))));
            } else {
                reader = new BufferedReader(new FileReader(getPath("IP_list.txt")));
            }

            while ((line = reader.readLine()) != null) {
                String[] userLine = line.split(" ");
                userDetails.put(Integer.valueOf(userLine[0]), new String[]{userLine[1], userLine[2]});
                userIPUserID.put(userLine[2], Integer.valueOf(userLine[0]));
            }

            if(userIPUserID.containsKey(ec2IP)){
                userId = userIPUserID.get(ec2IP);
                System.out.println("Starting the service for : " + userId + " IP " + ec2IP);
            }

            /*
            Setting the initial proposal number and increment
             */
            proposalNumber = userId;
            proposalIncrement = userDetails.size();

//            //Can comment this out
//            for (Integer key : userDetails.keySet()) {
//                System.out.println("User: " + key + "Name: " + userDetails.get(key)[0] + "IP: " + userDetails.get(key)[1]);
//            }

            //Set the Ti to hold as many sites as in the userDetails file
            nodeTable = new NodeTable(userDetails.size());
            nodeTable.loadNodeTable();
            //set counter to nodetable[i][i], this is especially needed in case of a crash
            eventCounter = nodeTable.getNodeTable()[getUserId() - 1][getUserId() - 1];
        } catch (IOException e) {
            System.err.println("Could not open File: " + e);
        }

        //Connect to the servers, like establish multiple connections
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEc2IP() {
        return ec2IP;
    }

    public void setEc2IP(String ec2IP) {
        this.ec2IP = ec2IP;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public int getNumClients() {
        return numClients;
    }

    public void setNumClients(int numClients) {
        this.numClients = numClients;
    }

    public int getEventCounter() {
        return eventCounter;
    }

    public void setEventCounter(int eventCounter) {
        this.eventCounter = eventCounter;
    }

    public EventLog getLog() {
        return log;
    }

    public void setLog(EventLog log) {
        this.log = log;
    }

    /*
    Save the log, node table and dictionary to recover from a crash
    Need to change this to include the saving of accNum, accVal; log
     */
    public void saveToSolidState() {
        log.saveEventLog();
        nodeTable.saveNodeTable();
        dictionary.saveDict();
    }

    /*
    Wuu Bernstein functions implementation begins
     */
    public boolean hasRecord(int[][] nodeTable, Event eventRecord, int nodeK) {
        if (nodeTable[nodeK - 1][eventRecord.getNodeId() - 1] >= eventRecord.getEventCounter()) {
            return true;
        }
        return false;
    }

    public void blockUser(int nodeID) {
        eventCounter += 1;
        nodeTable.getNodeTable()[userId - 1][nodeID - 1] = eventCounter;
        String blockedFollower = userDetails.get(nodeID)[0];
        String userName = userDetails.get(userId)[0];
        dictionary.addToDict(blockedFollower, userName);
        Event blockEvent = new Event();
        blockEvent.setNodeId(userId);
        blockEvent.setContents(blockedFollower);
        blockEvent.setNodeName(userName);
        blockEvent.setEventCounter(eventCounter);
        blockEvent.setTimestamp(Instant.now().toString());
        blockEvent.setOpType(Event.OperationTypes.BLOCK);
        log.addToEventLog(blockEvent);
        saveToSolidState();
    }

    /*
    Simulates the delete operation, wherein a (user,follower) pair is removed from the dictionary
     */
    public void unblockUser(int nodeID) {
        eventCounter += 1;
        nodeTable.getNodeTable()[userId - 1][nodeID - 1] = eventCounter;
        String blockedFollower = userDetails.get(nodeID)[0];
        String userName = userDetails.get(userId)[0];
        dictionary.removeFromDict(blockedFollower, userName);
        Event blockEvent = new Event();
        blockEvent.setNodeId(userId);
        blockEvent.setNodeName(userName);
        blockEvent.setContents(blockedFollower);
        blockEvent.setEventCounter(eventCounter);
        blockEvent.setTimestamp(Instant.now().toString());
        blockEvent.setOpType(Event.OperationTypes.UNBLOCK);
        log.addToEventLog(blockEvent);
        saveToSolidState();
    }

    /*
    This function gets called by a server's wait thread
    The received tweet from User pi is written to log
    Also dictionary is updated
    Also the log is updated
    The message is read from the server's datainput stream buffer
     */
    public void receiveTweet(String tweet) {
        /*
        {"message":"Hey you","partialLog":"[{\"opType\":\"SEND\",\"eventCounter\":1,\"contents\":\"Hey you\",\"timestamp\":\"\",\"nodeId\":1}]",
        "nodeTable":"[[0,0,0],[0,0,0],[0,0,0]]"}
         */
        System.out.println(" In receive of User - Message receive in process for " + tweet);
        //Converting it back to a JSON string
        //tweet = tweet.replace("\n","");
        Gson gson = new Gson();
        Type messageType = new TypeToken<Message>() {
        }.getType();
        System.out.println("I enter here in receive");

        Message messsageReceived = gson.fromJson(tweet, messageType);
        int[][] receivedNodeTable = messsageReceived.getNodeTable().getNodeTable();
        EventLog partialLog = messsageReceived.getPartialLog();
        Integer sentUserId = messsageReceived.getUserID();
        EventLog newEventLog = new EventLog();
        Set<Event> newEvents = new HashSet<Event>();
        System.out.println("User ID : " + sentUserId);
        //Extracting events from the received partial log, that are not already in j
        for (Event events : partialLog.getLog()) {
            if (!hasRecord(nodeTable.getNodeTable(), events, userId)) {
                newEvents.add(events);
            }
        }

        System.out.println("Events length " + newEvents.size());

        //Add block events to dictionary, only if an unblock doesn't exist
        for (Event events : newEvents) {
            if (events.getOpType() == Event.OperationTypes.BLOCK) {
                //Add the user, follower combination if there is no unblock operation
                if (!checkIfFollowerUnblockedAfterBlock(newEvents, events.getContents(), events.getNodeName())) {
                    dictionary.addToDict(events.getContents(), events.getNodeName());
                }
            }
            //Partial log will never have combination of block, unblock, block; since truncation happens
            else if(events.getOpType() == Event.OperationTypes.UNBLOCK){
                dictionary.removeFromDict(events.getContents(),events.getNodeName());
            }
        }
        dictionary.saveDict();
        int[][] myNodeTable = nodeTable.getNodeTable();
        //Updating j's view of the world, with as much i knows.
        for (int i = 0; i < userDetails.size(); i++) {
            myNodeTable[userId - 1][i] = Math.max(myNodeTable[userId - 1][i], receivedNodeTable[sentUserId - 1][i]);
        }

        for (int k = 0; k < userDetails.size(); k++) {
            for (int l = 0; l < userDetails.size(); l++) {
                myNodeTable[k][l] = Math.max(myNodeTable[k][l], receivedNodeTable[k][l]);
            }
        }

        nodeTable.setNodeTable(myNodeTable);
        nodeTable.saveNodeTable();

        //Merge my log with the new events log
        log.addMultipleEvents(newEvents);
        //Remove the events that are present in j's log which other processes know about.
        //Don't read from disk here, as it is a union of two lists
        EventLog logDiskReplace = new EventLog();

        for (Event event : log.getLog()) {
            //Don't truncate sends
            if(event.getOpType() == Event.OperationTypes.SEND){
                logDiskReplace.addToEventLog(event);
            }
            else{
                //Otherwise it results in Index out of bound, as hasRecord starts from 1
                for (int j = 1; j <= userDetails.size(); j++) {
                    if (!hasRecord(myNodeTable, event, j)) {
                        logDiskReplace.addToEventLog(event);
                    }
                }
            }
        }

        log.setLog(logDiskReplace.getLog());
        System.out.println("Size of cumulative log " + log.getLog().size());
        log.saveEventLog();
        System.out.println(" I am the server and I have received your " + messsageReceived.getMessage());
    }

    /*
    Add to dictionary only if there isn't an unblock operation for the same user, follower pair.
     */
    public synchronized boolean checkIfFollowerUnblockedAfterBlock(Set<Event> allEvents, String blockedUser, String blockedByUser) {
        for (Event event : allEvents) {
            if (event.getOpType().equals(Event.OperationTypes.UNBLOCK)) {
                if (event.getNodeName().equals(blockedByUser) && event.getContents().equals(blockedUser)) {
                    return true;
                }
            }
        }
        return false;

    }

    public void sendTweet(String tweet) {
        Event sendEvent = new Event();
        eventCounter += 1;
        nodeTable.getNodeTable()[userId - 1][userId - 1] = eventCounter;
        proposalNumber += proposalIncrement;
        nodeTable.setNodeTable(nodeTable.getNodeTable());
        sendEvent.setNodeId(userId);
        sendEvent.setNodeName(userDetails.get(userId)[0]);
        sendEvent.setContents(tweet);
        sendEvent.setEventCounter(eventCounter);
        sendEvent.setTimestamp(Instant.now().toString());
        sendEvent.setOpType(Event.OperationTypes.SEND);
        log.addToEventLog(sendEvent);
        saveToSolidState();        
        for (int i = 0; i < connectedUsers.size(); i++) {
            Set<Event> partialEvents = new HashSet<Event>();
            int clientId = connectedUsers.get(i).getClientID();

            /*
            Don't send data to my own machine, send to every other machine connected to me
            If client is blocked by user then don't send tweets
             */
            if (clientId != userId 
                    && !dictionary.amIBlocked(userDetails.get(clientId)[0], userDetails.get(userId)[0])) {

                //Build partial log of all messages that the node j hasn't seen
                for (Event event : log.getLog()) {
                    if (!hasRecord(nodeTable.getNodeTable(), event, clientId)) {
                        partialEvents.add(event);
                    }
                }

                /*
                  Build a json that can be sent over the network as a string
                  Eg: {"message":"Hey","nodeTable":[[0,1],[2,3]],"NP":log}
                 */
                //Fetch thread assigned to this user, and send message on this node
                UserConnector clientThread = connectedUsers.get(i);
                if (clientThread != null) {
                    EventLog partialLog = new EventLog();
                    Message messagePayload = new Message();
                    messagePayload.setMessage(tweet);
                    messagePayload.setNodeTable(nodeTable);
                    partialLog.addMultipleEvents(partialEvents);
                    messagePayload.setPartialLog(partialLog);
                    messagePayload.setUserID(userId);
                    Gson g = new Gson();
                    String messageString = g.toJson(messagePayload);
                    System.out.println("Message " + messageString);
                    //Din needs a \n
                    //ServerConnectionThread.sendTweetToUser(messageString);
                    //Change this to just tweet for testing
                    clientThread.sendTweetToUser(messageString);
                    System.out.println(proposalNumber + " Sending a tweet to : " + clientThread.getClientName() + " on " + clientThread.getIpAddress());
                }
            }
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

    public void sendTweetPaxos(String tweet,MessageType messageType,UserParticipantType participantType) {
        //Need to increment eventCounter in commit phase
        //eventCounter += 1;
        saveToSolidState();
        for (int i = 0; i < connectedUsers.size(); i++) {
            int clientId = connectedUsers.get(i).getClientID();

            /*
            Don't send data to my own machine, send to every other machine connected to me
            Need to clarify if blocked participants can act as acceptors as well
            If client is blocked by user then don't send tweets
            && !dictionary.amIBlocked(userDetails.get(clientId)[0], userDetails.get(userId)[0])
             */
            if (clientId != userId) {
                /*
                  Build a json that can be sent over the network as a string
                  Eg: {"message":"Hey","nodeTable":[[0,1],[2,3]],"NP":log}
                 */
                //Fetch thread assigned to this user, and send message on this node
                UserConnector clientThread = connectedUsers.get(i);
                if (clientThread != null) {
                    Message messagePayload = new Message();
                    //This is used by default in the commit phase
                    messagePayload.setMessage(tweet);
                    messagePayload.setUserID(userId);
                    messagePayload.setMessageType(messageType);
                    messagePayload.setUserParticipantType(participantType);

                    if(messageType == MessageType.PROMISE && participantType == UserParticipantType.ACCEPTOR){
                        boolean valid = tweet.indexOf("Valid") !=-1? true: false;

                        if(valid){
                            tweet = tweet.replace("Valid ","");
                            String[] tweetSplits = tweet.split(",");
                            int accNum = Integer.parseInt(tweetSplits[0].split(":")[1]);
                            String accVal = tweetSplits[1].split(":")[1];
                            messagePayload.setAccNum(accNum);
                            messagePayload.setAccVal(accVal);
                            messagePayload.setAcceptProposal(true);
                            System.out.println("In send of promise phase for valid scenario");
                        }
                        else{
                            messagePayload.setAcceptProposal(false);
                            messagePayload.setAccVal("");
                            messagePayload.setAccNum(0);
                        }

                    }

                    if(messageType == MessageType.ACCEPT && participantType == UserParticipantType.PROPOSER){
                        //Uncomment later on, when there is no majority_promise after a timeout
                        tweet = tweet.replace("Valid ","");
                        String[] tweetSplits = tweet.split(",");
                        int recvNum = Integer.parseInt(tweetSplits[0].split(":")[1]);
                        String recvVal = tweetSplits[1].split(":")[1];
                        messagePayload.setRecvProposal(recvNum);
                        messagePayload.setRecvVal(recvVal);
                    }

                    if(messageType == MessageType.ACK && participantType == UserParticipantType.ACCEPTOR){
                        tweet = tweet.replace("Valid ","");
                        String[] tweetSplits = tweet.split(",");
                        boolean sendAck = Boolean.parseBoolean(tweetSplits[0].split(":")[1]);
                        int accNum = Integer.parseInt(tweetSplits[1].split(":")[1]);
                        String accVal = tweetSplits[2].split(":")[1];
                        messagePayload.setSendAck(sendAck);
                        messagePayload.setAccNum(accNum);
                        messagePayload.setAccVal(accVal);
                    }

                    messagePayload.setUserID(userId);
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
            int proposalNumber = Integer.parseInt(messageRecv);
            System.out.println("Sending a prepare request to Acceptor " + proposalNumber);

            prepareTweet(proposalNumber, senderType);
        }
        else if(receivedMessageType == MessageType.PROMISE){
            System.out.println("In the receive phase of promise");
            promiseTweet(messsageReceived.getAccNum(),messsageReceived.getAccVal(),messsageReceived.getAcceptProposal(),senderType);
        }
        else if(receivedMessageType == MessageType.ACCEPT){
            System.out.println("In the receive phase of accept");
            acceptTweet(messsageReceived.getRecvProposal(),messsageReceived.getRecvVal(),senderType);
        }
        else if(receivedMessageType == MessageType.ACK){
            System.out.println("In the receive phase of ack");
            ackTweet(messsageReceived.getSendAck(),messsageReceived.getAccNum(),messsageReceived.getAccVal(),senderType);
        }
        else if(receivedMessageType == MessageType.COMMIT) {
            System.out.println("In the receive phase of commit");
            commit(messsageReceived.getUserID(),messageRecv,senderType);
        }

    }

    /*
    Runs the prepare phase of the tweet
     */
    public void prepareTweet(int proposalNumber, UserParticipantType participantType){
        /*
        Proposer sends value
        proposalNumber += proposalIncrement;
        Need to increment this in Paxos
         */
        System.out.println("In the prepare phase as " + participantType);

        if(participantType == UserParticipantType.PROPOSER){
            //Increment only your proposal number
            this.proposalNumber += this.proposalIncrement;
            String proposalNumberStr = Integer.toString(this.proposalNumber);
            System.out.println("Sending a prepare request " + participantType);
            //It sends a message payload and need to decipher from there in the second portion
            sendTweetPaxos(proposalNumberStr,MessageType.PREPARE, UserParticipantType.PROPOSER);
            //Write a sendTweetPaxos, build a message

        }
        else{
            //Receive a prepare message, check with maxPrepare, receive essentially just channelises it
            //This function returns
            boolean acceptProposal = false;
            if(proposalNumber >= maxPrepare) {
                //call the promise function
                acceptProposal = true;
                maxPrepare = proposalNumber;
                System.out.println(" I can accept your proposal " + proposalNumber + " , " + maxPrepare);
            }
            else
            {
                System.out.println("I cannot accept your proposal " + proposalNumber + " , " + maxPrepare);
            }
            /*
            Call the promise function irrespective of whether you can accept the proposal or not
            Just to let the proposer know if they have a chance
             */
            promiseTweet(accNum, accVal, acceptProposal,UserParticipantType.ACCEPTOR);

        }

    }

    /*
    Acceptor - sends message to client with accNum, accVal if proposal number is greater
    Proposer - on the other hand updates its majority_promise, as it receives promises
     */
    public void promiseTweet(int accNum, String accVal, boolean acceptProposal, UserParticipantType participantType){
        if(participantType == UserParticipantType.ACCEPTOR){
            System.out.println("In promise phase" + acceptProposal);
            //This gets set by the prepare function at the acceptor's end
            if(acceptProposal){
                String condensed = "Valid accNum:" + accNum + ",accVal:" + accVal;
                sendTweetPaxos(condensed,MessageType.PROMISE,UserParticipantType.ACCEPTOR);
            }
            else{
                sendTweetPaxos("I cannot accept your proposal", MessageType.PROMISE, UserParticipantType.ACCEPTOR);
            }
        }
        else
        {
            if(acceptProposal){
                majority += 1;
                acceptedVals.put(accNum,accVal);

                System.out.println("The acceptor has replied with an accNum of " + accNum + " accVal " + accVal);
            }

            //Irrespective of acceptProposal or not, you keep checking for majority_promise
             /*
                If majority_promise then call accept
                Need to have a false accept to declare majority_promise cannot be reached
             */
             //Change this to >
            if(majority >= (userDetails.size()/2)){
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
                //Forwarding this to the accept function of Proposer
                acceptTweet(proposalNumber,proposedVal,UserParticipantType.PROPOSER);
            }
        }

    }
    /*
    Accept for proposer - send the values of n, v to all acceptors
    Accept for acceptor - receive vals of n, v check against max Prepare and reset the accNum and accVal
     */
    public void acceptTweet(int n, String messageString,UserParticipantType participantType){
        if(participantType == UserParticipantType.PROPOSER){
            String condensed = "n:"+n+",v:"+messageString;
            sendTweetPaxos(condensed,MessageType.ACCEPT,UserParticipantType.PROPOSER);
            System.out.println("In the accept phase of proposer");
        }
        else{
            //Change this to greater than
            boolean acceptTweet = false;
            if(n >= maxPrepare){
                System.out.println("Acceptor is able to accept the tweet " + messageString);
                accNum = n;
                accVal = messageString;
                maxPrepare = n;
                acceptTweet = true;
            }
            else{
                System.out.println("Acceptor is unable to accept the tweet " + messageString + " as its maxPrepare is higher than " + n);
            }
            //Call the ack function here
            ackTweet(acceptTweet,accNum,accVal,UserParticipantType.ACCEPTOR);
        }


    }
    /*
    Acceptor - sends an ack if it has accepted the proposal
    Proposer - on receiving a majority_promise runs a commit
     */
    public void ackTweet(boolean accepted_ack, int accNum, String accVal, UserParticipantType userParticipantType){
        if(userParticipantType == UserParticipantType.ACCEPTOR){
            String condensed = "accepted:"+accepted_ack+",accNum:"+accNum+",accVal:"+accVal;
            //Set sendAck based on accepted
            sendTweetPaxos(condensed,MessageType.ACK,UserParticipantType.ACCEPTOR);
        }
        else{
            if(accepted_ack){
                System.out.println("I have received an acknowledgement ");
                majority_ack+=1;
            }
            else{
                System.out.println("I was declined an ack");
            }
            //Change this to >
            //If majority_promise has been reached, send commits.
            //Might need to move this out, to keep checking with timeouts
            if(majority_ack >= (userDetails.size()/2)){
                System.out.println("Received majority_promise of acknowledgements, sending out a commit ");

                commit(userId, getTweetToSend(),UserParticipantType.PROPOSER);
            }

        }

    }
    /*
    Acceptor - writes the received Tweet into log
    Proposer - sends the log entry to acceptor, and writes to own log
     */
    public void commit(int senderId, String tweet, UserParticipantType userParticipantType){
        if(userParticipantType == UserParticipantType.PROPOSER){
            /*
                Write to your own log, so build the event object here
                The placeholder will also be here
            */
            //Save to own log
            Event sendEvent = new Event();
            sendEvent.setNodeId(senderId);
            sendEvent.setNodeName(userDetails.get(senderId)[0]);
            sendEvent.setContents(tweet);
            sendEvent.setTimestamp(Instant.now().toString());
            //This will get set from tweet parsing
            sendEvent.setOpType(Event.OperationTypes.SEND);
            log.addToEventLog(sendEvent);
            saveToSolidState();
            sendTweetPaxos(tweet,MessageType.COMMIT,userParticipantType);
            System.out.println("Sending tweet " + tweet + " to be committed to all acceptors");
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
            log.addToEventLog(recvEvent);
            saveToSolidState();
        }
    }
    /*
    Simulate a run of Synod, where proposer tries to get their value into the log
     */
    public void runSynod(String tweet){
        prepareTweet(this.proposalNumber,UserParticipantType.PROPOSER);
        //Accept is to be called, only if there is a majority_promise
    }

    public static void main(String[] args) {
        User u1 = new User();
        u1.init();

        //Spawn clients
        u1.startUserServer();
        u1.startUserClients();
        
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
                    for(UserClientConnector clientConnector:u1.getConnectedServers()){
                        System.out.println(clientConnector.getServerIPAddress());
                    }
                    System.out.println("connected clients");
                    for(UserConnector clientConnector:u1.getConnectedUsers()){
                        System.out.println(clientConnector.getIpAddress());
                    }
                    break;
                }
                case 6: {
                    System.out.println("Bye bye!");
                    //Close all the thread pools
                    u1.getClientProcessingPool().shutdown();
                    u1.getServerListeningPool().shutdown();
                    System.exit(0);
                    break;
                }

                default:
                    System.out.println("Error! Please enter menu items 1-6.");
            }
        }
    }
}
