/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Ananya
 */
public class EventLog {

    private Set<Event> log;
    private String filename = "mylog.json";

    public EventLog() {
        if (this.log == null) {
            this.log = new HashSet<Event>();
        }
    }

    public synchronized void addMultipleEvents(EventLog newlog) {
        for (Event ev : newlog.getLog()) {
            this.addToEventLog(ev);
        }
    }

    public synchronized void addMultipleEvents(Set<Event> events) {
        for (Event ev : events) {
            this.addToEventLog(ev);
        }
    }

    public synchronized void addToEventLog(Event event) {
        if (!log.contains(event)) {
            //Log truncation
            if (event.getOpType().equals(Event.OperationTypes.BLOCK)) {
                Iterator<Event> it = log.iterator();
                while(it.hasNext()) {
                    Event past = it.next();
                    if (past.getOpType().equals(event.getOpType())
                            && past.getContents().equals(event.getContents())
                            && past.getNodeName().equals(event.getNodeName())) {
                        it.remove();
                    } else if (past.getOpType().equals(Event.OperationTypes.UNBLOCK)
                            && past.getContents().equals(event.getContents())
                            && past.getNodeName().equals(event.getNodeName())) {
                        it.remove();
                    }
                }
            } else if (event.getOpType().equals(Event.OperationTypes.UNBLOCK)) {
                Iterator<Event> it = log.iterator();
                while(it.hasNext()) {
                    Event past = it.next();
                    if (past.getOpType().equals(event.getOpType())
                            && past.getContents().equals(event.getContents())
                            && past.getNodeName().equals(event.getNodeName())) {
                        it.remove();
                    }
                }
            }

            log.add(event);
        } else {
            //System.out.println("Event already in log!");
        }
    }

    public synchronized void removeFromEventLog(Event event) {
        if (log.contains(event)) {
            log.remove(event);
        } else {
            System.out.println("Event is not in log!");
        }
    }

    public synchronized void saveEventLog() {
        BufferedWriter writer = null;

        Gson g = new Gson();
        String jLog = g.toJson(log);

        try {
            writer = new BufferedWriter(new FileWriter(getPath(filename)));
            writer.write(jLog);
            writer.close();
        } catch (IOException ex) {
            System.out.println("Could not save log! " + ex);
        }
    }

    public synchronized void loadEventLog() {
        BufferedReader reader = null;
        String line = "";
        String json = "";

        try {
            reader = new BufferedReader(new FileReader(getPath(filename)));
            while ((line = reader.readLine()) != null) {
                json += line;
            }
            reader.close();
        } catch (IOException ex) {
            System.err.println("Could not load event log! " + ex.getMessage());
        }

        if(json != ""){
            Gson g = new Gson();
            Type setType = new TypeToken<Set<Event>>() {
            }.getType();
            log = new HashSet<>(g.fromJson(json, setType));
        }        
    }

    public synchronized void printEventLog() {
        for (Event event : log) {
            System.out.println(event.toString());
        }
    }

    //Sorting log with custom comparator
    public synchronized List<Event> getSortedLog() {
        List<Event> events = new ArrayList<>(log);
        Collections.sort(events, new EventComparator());
        return events;
    }

    public synchronized Set<Event> getLog() {
        return log;
    }

    public synchronized void setLog(Set<Event> log) {
        this.log = log;
    }

    public synchronized String getPath(String filename) {
        String workingDir = Paths.get(".").toAbsolutePath().normalize().toString();
        String path = workingDir + File.separator + filename;
        return path;
    }

    public static void main(String[] args) {
        //public Event(OperationTypes opType, Integer eventCounter, int nodeId, String nodeName,String contents, Long timestamp) {
        Event e = new Event(Event.OperationTypes.VIEW, 1, 1, "a", "message1", "2017-10-13T06:18:17.565Z");
        Event k = new Event(Event.OperationTypes.BLOCK, 2, 2, "b", "c", "2017-10-13T06:18:17.566Z");
        Event o = new Event(Event.OperationTypes.BLOCK, 6, 3, "b", "c", "2017-10-13T06:18:17.567Z");
        Event p = new Event(Event.OperationTypes.VIEW, 2, 4, "d", "messageALL", "2017-10-13T06:18:17.568Z");
        Event c = new Event(Event.OperationTypes.SEND, 3, 5, "e", "message3", "2017-10-13T06:18:17.569Z");
        Event z = new Event(Event.OperationTypes.BLOCK, 4, 6, "f", "node_3", "2017-10-13T06:18:17.569Z");

        EventLog l = new EventLog();

        l.addToEventLog(p);
        l.addToEventLog(o);
        l.addToEventLog(z);
        l.addToEventLog(e);
        l.addToEventLog(k);
        l.addToEventLog(c);
        l.loadEventLog();

        //l.saveEventLog("", "log.json");
        //l.loadEventLog("", "log.json");
        //Event t = new Event(Event.OperationTypes.VIEW, 1, "node_1", "message1", 89747L);
        //l.removeFromEventLog(t);
        l.printEventLog();
        l.saveEventLog();

        System.out.println(l.getSortedLog());

        Set<Event> partial = new HashSet<>();//that comes from the socket        
        l.getLog().addAll(partial);
        Type mapType = new TypeToken<Set<Event>>() {
        }.getType();
        
        System.out.println("2017-10-13T06:18:17.569Z".compareTo("2017-10-14T06:18:17.569Z"));

        //System.out.println(Instant.now().toEpochSecond() * 1000);
    }
}
