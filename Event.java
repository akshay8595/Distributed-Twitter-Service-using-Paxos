/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Comparator;
/**
 *
 * @author Ananya
 */
public class Event implements Comparable<Event> {

    private OperationTypes opType;
    private Integer eventCounter;
    private String contents;
    private String timestamp;
    private Integer nodeId;
    private String nodeName;

    private Integer slotNumber;

    public enum OperationTypes {
        SEND, BLOCK, UNBLOCK, VIEW, RECEIVE;
    };

    public Event(){
        this.opType = OperationTypes.SEND;
        this.eventCounter = 0;
        this.contents = "";
        this.timestamp = "";
        this.nodeId = 0;
        this.slotNumber = 0;

    }

    public Event(OperationTypes opType, Integer eventCounter, int nodeId, String nodeName, String contents, String timestamp) {
        this.eventCounter = eventCounter;
        this.timestamp = timestamp;
        this.contents = contents;
        this.opType = opType;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
    }

    @Override
    public synchronized int compareTo(Event event) {
        return this.getKey().compareTo(event.getKey());
    }

    @Override
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof Event)) {
            return false;
        } else {
            Event event = (Event) obj;
            return this.getKey().equals(event.getKey());
        }
    }

    @Override
    public synchronized int hashCode() {
        return this.getKey().hashCode();
    }

    @Override
    public synchronized String toString() {
        return "event(" + opType + ", " + eventCounter + ", "
                + nodeId + ", " + nodeName + ", " + contents + ", "
                + timestamp + ")";
    }
    /*
    Gets called in a compare
     */
    public synchronized String getKey() {
        return nodeId + "_" + contents;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }

    public synchronized String getTimestamp() {
        return timestamp;
    }

    public synchronized void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public synchronized Integer getNodeId() {
        return nodeId;
    }

    public synchronized void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public synchronized String getNodeName() {
        return nodeName;
    }

    public synchronized void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public synchronized OperationTypes getOpType() {
        return opType;
    }

    public synchronized void setOpType(OperationTypes opType) {
        this.opType = opType;
    }

    public synchronized String getContents() {
        return contents;
    }

    public synchronized void setContents(String contents) {
        this.contents = contents;
    }

    public synchronized Integer getEventCounter() {
        return eventCounter;
    }

    public synchronized void setEventCounter(Integer eventCounter) {
        this.eventCounter = eventCounter;
    }
}

//Custom comparator that sort based on Event Counter.
//If counter is equal, use timestamp.
class EventComparator implements Comparator<Event> {

    @Override
    public synchronized int compare(Event e1, Event e2) {
        return e1.getTimestamp().compareTo(e2.getTimestamp());
    }
}
