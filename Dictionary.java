/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Ananya
 */
public class Dictionary {

    private Map<String, Set<String>> blocked_byUsers;
    private String filename = "mydictionary.json";

    public Dictionary() {
        if (this.blocked_byUsers == null) {
            this.blocked_byUsers = new HashMap<>();
        }
    }

    public synchronized String getPath(String filename) {
        String workingDir = Paths.get(".").toAbsolutePath().normalize().toString();
        String path = workingDir + File.separator + filename;
        return path;
    }

    public synchronized void addToDict(String blocked, String byUser) {
        if (blocked != byUser) {
            if (blocked_byUsers.containsKey(blocked)) {
                Set<String> byUsers = blocked_byUsers.get(blocked);
                if (!byUsers.contains(byUser)) {
                    byUsers.add(byUser);
                    blocked_byUsers.put(blocked, byUsers);
                } else {
                    System.out.println("This user has already blocked the follower!");
                }
            } else {
                Set<String> byUsers = new HashSet<>();
                byUsers.add(byUser);
                blocked_byUsers.put(blocked, byUsers);
            }
            System.out.println(blocked.toString() + " was added to your blocked list.");
        }
            else {
    System.out.println("You cannot block yourself!");
    }
    }

    
    

    public synchronized void removeFromDict(String blocked, String byUser) {
        if (blocked_byUsers.containsKey(blocked)) {
            Set<String> byUsers = blocked_byUsers.get(blocked);
            if (byUsers.contains(byUser)) {
                byUsers.remove(byUser);
                blocked_byUsers.put(blocked, byUsers);
            } else {
                System.out.println("This user wasn't blocking the follower!");
            }
        }
    }

    public synchronized boolean amIBlocked(String blocked, String byUser) {
        if (blocked_byUsers.containsKey(blocked)) {
            if (blocked_byUsers.get(blocked).contains(byUser)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isInDict(String blocked) {
        if (blocked_byUsers.containsKey(blocked)) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized void printDict() {
        for (Map.Entry<String, Set<String>> entry : blocked_byUsers.entrySet()) {
            String blocked = entry.getKey();
            Set<String> byUsers = entry.getValue();
            for (String user : byUsers) {
                System.out.println(user + " is blocking " + blocked);
            }
        }
    }

    public synchronized void saveDict() {
        BufferedWriter writer = null;

        Gson g = new Gson();
        String jMap = g.toJson(blocked_byUsers);

        try {
            writer = new BufferedWriter(new FileWriter(getPath(filename)));
            writer.write(jMap);
            writer.close();
        } catch (IOException ex) {
            System.err.println("Could not save dictionary! " + ex.getMessage());
        }
    }

    public synchronized void loadDict() {
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
            System.out.println("Could not load dictionary! " + ex);
        }

        if (json != "") {
            Gson g = new Gson();
            Type setType = new TypeToken<Map<String, Set<String>>>() {
            }.getType();
            blocked_byUsers = new HashMap<>(g.fromJson(json, setType));
        }
    }

    public synchronized Map<String, Set<String>> getDictionary() {
        return blocked_byUsers;
    }

    public synchronized void setDictionary(Map<String, Set<String>> blocked_byUser) {
        this.blocked_byUsers = blocked_byUser;
    }
}
