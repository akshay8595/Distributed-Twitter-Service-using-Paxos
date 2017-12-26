import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;

public class AcceptorStableStorage {

    private String filename = "acceptor.json";
    /*
    Easier to reuse in the other code
     */
    private HashMap<Integer,AcceptorVariables> acceptorVariables = new HashMap<>();

    public AcceptorStableStorage(){
        this.filename = "acceptor.json";
        acceptorVariables = new HashMap<>();
    }

    public AcceptorStableStorage(String filename, HashMap<Integer,AcceptorVariables> acceptorVariables) {
        this.filename = filename;
        this.acceptorVariables = acceptorVariables;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public HashMap<Integer,AcceptorVariables> getAcceptorVariables() {
        return acceptorVariables;
    }

    public void setAcceptorVariables(HashMap<Integer,AcceptorVariables> acceptorVariables) {
        this.acceptorVariables = acceptorVariables;
    }

    public synchronized String getPath(String filename) {
        String workingDir = Paths.get(".").toAbsolutePath().normalize().toString();
        String path = workingDir + File.separator + filename;
        return path;
    }

    public synchronized void saveAcceptorStableStorage() {
        BufferedWriter writer = null;

        Gson g = new Gson();
        String jLog = g.toJson(acceptorVariables);

        try {
            writer = new BufferedWriter(new FileWriter(getPath(filename)));
            writer.write(jLog);
            writer.close();
        } catch (IOException ex) {
            System.out.println("Could not save acceptor variables! " + ex);
        }
    }

    public synchronized void loadAcceptorStableStorage() {
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
            System.err.println("Could not load acceptor variables! " + ex.getMessage());
        }

        if(json != ""){
            Gson g = new Gson();
            Type setType = new TypeToken<HashMap<Integer, AcceptorVariables>>() {
            }.getType();
            acceptorVariables = new HashMap<Integer, AcceptorVariables>(g.fromJson(json, setType));
        }
    }

    public synchronized void printAcceptorStableStorage() {
        for (Integer slotNumber: acceptorVariables.keySet()) {
            System.out.println("Slot Number " + slotNumber + " AccNum " + acceptorVariables.get(slotNumber).getAccNum() + " AccVal " + acceptorVariables.get(slotNumber).getAccVal());
        }
    }

    //Sorting log with custom comparator
    public synchronized List<AcceptorVariables> getAcceptorStableStorage() {
        List<AcceptorVariables> acceptorVariableList = (List<AcceptorVariables>) acceptorVariables.values();
        Collections.sort(acceptorVariableList, new AcceptorVariablesComparator());
        return acceptorVariableList;
    }

    public synchronized void addToAcceptorStableStorage(AcceptorVariables acceptorVariable) {
        if(acceptorVariables.containsKey(acceptorVariable.getSlotNumber())){
            acceptorVariables.remove(acceptorVariable.getSlotNumber());
        }
        acceptorVariables.put((Integer)acceptorVariable.getSlotNumber(),acceptorVariable);
    }


    public static void main(String[] args) {
        AcceptorVariables a1 = new AcceptorVariables(1,"Hi there",1,1);
        AcceptorVariables a2 = new AcceptorVariables(2,"Booo",2,1);
        AcceptorVariables a3 = new AcceptorVariables(3,"Booo1",2,4);


        AcceptorStableStorage acceptorStableStorage = new AcceptorStableStorage();
        acceptorStableStorage.loadAcceptorStableStorage();
        acceptorStableStorage.addToAcceptorStableStorage(a1);
        acceptorStableStorage.addToAcceptorStableStorage(a2);
        acceptorStableStorage.addToAcceptorStableStorage(a3);

        //l.saveAcceptorStableStorage("", "log.json");
        //l.loadAcceptorStableStorage("", "log.json");
        //Event t = new Event(Event.OperationTypes.VIEW, 1, "node_1", "message1", 89747L);
        //l.removeFromEventLog(t);
        acceptorStableStorage.saveAcceptorStableStorage();
        acceptorStableStorage.printAcceptorStableStorage();
    }
}
