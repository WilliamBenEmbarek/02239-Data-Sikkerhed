import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;

public class Server implements Print {

    private LinkedList<JobInterface> printQueue = new LinkedList<JobInterface>();
    private boolean running = false;
    private HashMap<String, String> config = new HashMap<String, String>();
    private int currentID;
    final String passwordPath = "passwords.txt";

    public Server() throws NoSuchAlgorithmException {
    }

    public static void main(String[] args) {
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

    private boolean authenticate(String username, String password) throws IOException, NoSuchAlgorithmException {
        String line;
        String[] credentials;
        String passwordDigest;
        
        BufferedReader br = new BufferedReader(new FileReader(passwordPath));
        while ((line = br.readLine()) != null) {
            credentials = line.split(",");
            if (username == credentials[0]) {
                passwordDigest = bytesToBase64(hash(password, base64ToString(credentials[2])));
                if (passwordDigest == credentials[1]) {
                    System.out.println("User successfully verified.");
                    br.close();
                    return true;
                } else {
                    System.out.println("Wrong password.");
                    br.close();
                    return false;
                }
            } else {
                System.out.println("User does not exist.");
                br.close();
                return false;
            }
        }
        br.close();
        return false;
    }

    private static byte[] hash(String password, String salt) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt.getBytes(StandardCharsets.UTF_8));
        final byte[] sha3_256bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return sha3_256bytes;
    }

    private static String bytesToBase64(byte[] bytes) {
        String encodedString = Base64.getEncoder().encodeToString(bytes);
        return encodedString;
    }

    private static String base64ToString(String string) {
        byte[] decodedBytes = Base64.getDecoder().decode(string);
        String decodedString = new String(decodedBytes);
        return decodedString;
    }

    public boolean print(String filename, String printer){
        JobInterface newJob = new Job(currentID,filename);
        queue().add(newJob);
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
