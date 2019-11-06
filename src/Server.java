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
import java.util.UUID;

public class Server implements Print {

    private LinkedList<JobInterface> printQueue = new LinkedList<JobInterface>();
    private boolean running = false;
    private HashMap<String, String> config = new HashMap<String, String>();
    private int currentID;
    final String passwordPath = "/home/willbenem/Uni/02239-Data-Security/RMI-lab/src/passwords.txt";
    private HashMap<String,String> tokenMap = new HashMap<String,String>();
    public Server() throws NoSuchAlgorithmException {
    }

    public static void main(String[] args) {
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

    public String authenticate(String username, String password) throws IOException, NoSuchAlgorithmException, AuthenticationException {
        String line;
        String[] credentials;
        String passwordDigest;
        if (tokenMap.containsKey(username)) { // Yes potential timing attack here :P
            return tokenMap.get(username);
        }
        BufferedReader br = new BufferedReader(new FileReader(passwordPath));
        while ((line = br.readLine()) != null) {
            credentials = line.split(",");
            if (username.equals(credentials[0])) {
                passwordDigest = bytesToBase64(hash(password, base64ToString(credentials[2])));
                if (passwordDigest.equals(credentials[1])) {
                    System.out.println("User successfully verified.");
                    br.close();
                    String token = UUID.randomUUID().toString().replace("-","");
                    tokenMap.put(username,token);
                    return token;
                } else {
                    System.out.println("Wrong password.");
                    br.close();
                    throw new AuthenticationException("Wrong Username or Password");
                }
            }
        }
        System.out.println("User does not exist.");
        br.close();
        throw new AuthenticationException("Wrong Username or Password");
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

    public boolean print(String filename, String printer, String token) throws AuthenticationException {
        if (tokenMap.containsValue(token)) {
            JobInterface newJob = new Job(currentID, filename);
            printQueue.add(newJob);
            currentID++;
            return true;
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }

    public LinkedList<JobInterface> queue(String token) throws AuthenticationException {
        if (tokenMap.containsValue(token)) {
            return printQueue;
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }
    public boolean topQueue(int job, String token) throws AuthenticationException {
        if (tokenMap.containsValue(token)) {
            JobInterface tempJobInterface = new Job(job, "");
            int index = printQueue.indexOf(tempJobInterface);
            if (index != -1) {
                tempJobInterface = printQueue.get(index);
                printQueue.remove(index);
                return printQueue.offerFirst(tempJobInterface);
            } else {
                return false;
            }
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }
    public boolean start(String token) throws AuthenticationException {
        if (tokenMap.containsValue(token)) {
            if (!running) {
                running = true;
                return true;
            } else {
                return false;
            }
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }
    public boolean stop(String token) throws AuthenticationException {
        if (tokenMap.containsValue(token)) {
            if (running) {
                running = false;
                return true;
            } else {
                return false;
            }
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }
    public boolean restart(String token) throws AuthenticationException {
        if (tokenMap.containsValue(token)) {
            if (running) {
                running = false;
                printQueue.clear();
                running = true;
                return true;
            } else {
                return false;
            }
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }
    public String status(String token) throws AuthenticationException {
        if (tokenMap.containsValue(token)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Status: ").append(running);
            if (running) {
                sb.append("Print Queue length:").append(printQueue.size());
            }
            return sb.toString();
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }
    public String readConfig(String parameter, String token) throws AuthenticationException {
        if (tokenMap.containsValue(token)) {
            return config.get(parameter);
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }
    public void setConfig(String parameter, String value, String token) throws AuthenticationException {
        if (tokenMap.containsValue(token)) {
            config.put(parameter, value);
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }
}
