/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 *
 * @author Ananya
 */
public class Message {

    private Integer userID;
    /*
    Need to be removed for Paxos
     */
    private String message;
    private EventLog partialLog;
    private NodeTable nodeTable;
    private MessageType messageType;

    //Used in the prepare phase
    private int proposalNumber;

    private int accNum;
    private String accVal;
    //Used in promise phase of the acceptor
    private boolean acceptProposal;

    private boolean sendAck;

    //Variables used in the accept phase
    private int recvProposal;
    private String recvVal;


    //Will be used to obtain accNum and accVal
    private int slotNumber;


    private UserParticipantType userParticipantType;

    public Message() {
        this.userID = 0;
        this.message = "";
        this.partialLog = new EventLog();
        this.nodeTable = new NodeTable();
        this.userParticipantType = UserParticipantType.ACCEPTOR;
        this.acceptProposal = false;
        this.accNum = 0;
        this.accVal = "";
        this.sendAck = false;
    }

    public Message(Integer userID, String message, EventLog partialLog, NodeTable nodeTable,MessageType messageType, UserParticipantType userParticipantType, int accNum, String accVal, boolean acceptProposal) {
        this.userID = userID;
        this.message = message;
        this.partialLog = partialLog;
        this.nodeTable = nodeTable;
        this.messageType = messageType;
        this.userParticipantType = userParticipantType;
        this.accVal = accVal;
        this.accNum = accNum;
        this.acceptProposal = acceptProposal;
    }

    public synchronized Integer getUserID() {
        return userID;
    }

    public synchronized void setUserID(Integer userID) {
        this.userID = userID;
    }

    public synchronized String getMessage() {
        return message;
    }

    public synchronized void setMessage(String message) {
        this.message = message;
    }

    public int getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(int proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    public synchronized EventLog getPartialLog() {
        return partialLog;
    }

    public synchronized void setPartialLog(EventLog partialLog) {
        this.partialLog = partialLog;
    }

    public synchronized NodeTable getNodeTable() {
        return nodeTable;
    }

    public synchronized void setNodeTable(NodeTable nodeTable) {
        this.nodeTable = nodeTable;
    }

    public boolean getSendAck() {
        return sendAck;
    }

    public void setSendAck(boolean sendAck) {
        this.sendAck = sendAck;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public UserParticipantType getUserParticipantType() {
        return userParticipantType;
    }

    public void setUserParticipantType(UserParticipantType userParticipantType) {
        this.userParticipantType = userParticipantType;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
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

    public boolean getAcceptProposal() {
        return acceptProposal;
    }

    public void setAcceptProposal(boolean acceptProposal) {
        this.acceptProposal = acceptProposal;
    }

    public int getRecvProposal() {
        return recvProposal;
    }

    public void setRecvProposal(int recvProposal) {
        this.recvProposal = recvProposal;
    }

    public String getRecvVal() {
        return recvVal;
    }

    public void setRecvVal(String recvVal) {
        this.recvVal = recvVal;
    }

    public synchronized static void main(String[] args) {
        Gson g = new Gson();
        Type messageType = new TypeToken<Message>() {
        }.getType();

        Event p = new Event(Event.OperationTypes.VIEW, 2, 4, "d", "messageALL", "2017-10-13T06:18:17.568Z");
        Event c = new Event(Event.OperationTypes.SEND, 3, 5, "e", "message3", "2017-10-13T06:18:17.569Z");
        Event z = new Event(Event.OperationTypes.BLOCK, 4, 6, "f", "node_3", "2017-10-13T06:18:17.569Z");

        EventLog partialLog = new EventLog();
        partialLog.addToEventLog(z);
        partialLog.addToEventLog(c);
        partialLog.addToEventLog(p);

        NodeTable table = new NodeTable();
        table.init();
        Integer userid = 1;
        String tweet = "Hey! check out my message!!!";

        //send json file
        Message messageToSend = new Message(userid, tweet, partialLog, table,MessageType.ACCEPT,UserParticipantType.ACCEPTOR,0,"",false);
        String jsonToSend = g.toJson(messageToSend);
        String jsonExample = "{\"userID”:2,”message\":\"Hey you\",\"partialLog\":{\"log\":[{\"opType\":\"SEND\",\"eventCounter\":1,\"contents\":\"Hey you\",\"timestamp\":\"2017-10-14T03:24:32.986Z\",\"nodeId\":1,\"nodeName\":\"us-east-1\"},{\"opType\":\"SEND\",\"eventCounter\":2,\"contents\":\"Hey you\",\"timestamp\":\"2017-10-14T03:24:42.296Z\",\"nodeId\":1,\"nodeName\":\"us-east-1\"}],\"filename\":\"mylog.json\"},\"nodeTable\":{\"no_of_sites\":4,\"nodeTable\":[[2,0,0],[0,0,0],[0,0,0]],\"filename\":\"nodeTable.json\"}}";
        System.out.println("JSON To send " + jsonToSend.toString() + "message " + jsonToSend.contains("\n"));

        //send() it over internet...
        //receive() json file at the other end
        String jsonFromFile = jsonToSend;
        Message messsageReceived = g.fromJson(jsonFromFile, messageType);

        System.out.println("Tweet: " + messsageReceived.getMessage()
                + "\nPartial Log:" + messsageReceived.getUserID());
        messsageReceived.getPartialLog().printEventLog();

        System.out.println("Node Table: " + messsageReceived.getNodeTable().toString());

    }
}
