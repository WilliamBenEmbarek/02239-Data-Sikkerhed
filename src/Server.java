import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;

import static javax.xml.crypto.dsig.DigestMethod.SHA3_256;

public class Server implements Print {

    private LinkedList<JobInterface> printQueue = new LinkedList<JobInterface>();
    private boolean running= false;
    private HashMap<String, String> config = new HashMap<String,String>();
    private int currentID;
    final MessageDigest digest = MessageDigest.getInstance(SHA3_256);
    final String passwordPath = "passwords.pwd";


    public Server() throws NoSuchAlgorithmException {
    }

    public static void main (String[] args) {
        // TODO AUTHENTICATION
        try {
            Server obj = new Server();
            Print stub = (Print) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(6969);
            registry.rebind("server", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public boolean print(String filename, String printer){
        JobInterface newJobInterface = new Job(currentID,filename);
        currentID++;
        return true;
    }
    public LinkedList<JobInterface> queue(){
        return printQueue;
    }
    public boolean topQueue(int job){
        JobInterface tempJobInterface = new Job(job,"");
        int index = printQueue.indexOf(tempJobInterface);
        if (index != -1) {
            tempJobInterface = printQueue.get(index);
            printQueue.remove(index);
            return printQueue.offerFirst(tempJobInterface);
        } else {
            return false;
        }
    }
    public boolean start(){
        if (!running) {
            running = true;
            return true;
        } else {
            return false;
        }
    }
    public boolean stop(){
        if (running) {
            running = false;
            return true;
        } else {
            return false;
        }
    }
    public boolean restart(){
        if (running) {
            running = false;
            printQueue.clear();
            running = true;
            return true;
        } else {
            return false;
        }
    }
    public String status(){
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(running);
        if (running) {
            sb.append("Print Queue length:").append(printQueue.size());
        }
        return sb.toString();
    }
    public String readConfig(String parameter){
        return config.get(parameter);
    }
    public void setConfig(String parameter, String value){
        config.put(parameter,value);
    }
}
