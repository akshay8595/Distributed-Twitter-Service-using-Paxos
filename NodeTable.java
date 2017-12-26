
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Paths;

public class NodeTable {

    int no_of_sites;
    int[][] nodeTable = new int[no_of_sites][no_of_sites];
    private String filename = "nodeTable.json";

    public NodeTable(int no_of_sites) {
        this.no_of_sites = no_of_sites;
        this.nodeTable = new int[no_of_sites][no_of_sites];
    }

    public NodeTable() {
        this.no_of_sites = 4;
        this.nodeTable = new int[no_of_sites][no_of_sites];

    }

    //Initialise the nodetable to contain 0 in every field, until
    public void init() {
        for (int i = 0; i < no_of_sites; i++) {
            for (int j = 0; j < no_of_sites; j++) {
                nodeTable[i][j] = 0;
            }
        }
    }

    public String getPath(String filename) {
        String workingDir = Paths.get(".").toAbsolutePath().normalize().toString();
        String path = workingDir + File.separator + filename;
        return path;
    }

    public void saveNodeTable() {
        BufferedWriter writer = null;

        Gson g = new Gson();
        String nodeLog = g.toJson(nodeTable);

        try {
            writer = new BufferedWriter(new FileWriter(getPath(filename)));
            writer.write(nodeLog);
            writer.close();
        } catch (IOException ex) {
            System.out.println("Could not save table to file! " + ex);
        }
    }

    public void loadNodeTable() {
        BufferedReader reader = null;
        String line = "";
        String json = "";

        try {
            reader = new BufferedReader(new FileReader(getPath(filename)));
            while ((line = reader.readLine()) != null) {
                json += line;
            }
            reader.close();
            //System.out.println(json);
        } catch (IOException ex) {
            System.err.println("Could not load node table! " + ex.getMessage());
        }

        Gson g = new Gson();

        if (json != "") {
            nodeTable = g.fromJson(json, int[][].class);
            //System.out.println("Node table : " + nodeTable.length);
        }
    }

    public int getNo_of_sites() {

        return no_of_sites;
    }

    public void setNo_of_sites(int no_of_sites) {
        this.no_of_sites = no_of_sites;
    }

    public int[][] getNodeTable() {
        return nodeTable;
    }

    public void setNodeTable(int[][] nodeTable) {
        this.nodeTable = nodeTable;

    }

    public static void main(String[] args) {
        System.out.println("Writing to log");
        NodeTable n1 = new NodeTable();
        n1.init();
        n1.saveNodeTable();
        n1.loadNodeTable();
    }
}
