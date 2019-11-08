import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.Random;
import java.util.UUID;

public class Server implements Print {

    private static BufferedReader br;
    private LinkedList<Job> printQueue = new LinkedList<Job>();
    private boolean running = false;
    private HashMap<String, String> config = new HashMap<String, String>();
    private int currentID;
    final static String passwordPath = "src/passwords.txt";
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
            br = new BufferedReader(new FileReader(passwordPath));
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
        BufferedReader br = new BufferedReader(new FileReader(passwordPath));
        while ((line = br.readLine()) != null) {
            credentials = line.split(",");
            if (username.equals(credentials[0])) {
                passwordDigest = bytesToBase64(hash(password, base64ToString(credentials[2])));
                if (passwordDigest.equals(credentials[1])) {
                    System.out.println("User successfully verified.");
                    br.close();
                    String token = UUID.randomUUID().toString().replace("-","");
                    tokenMap.put(token,username);
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
    
    private static boolean createUser(String username, String password) throws IOException, NoSuchAlgorithmException {
        String salt = generateSalt(16);
        String passwordDigestBase64 = bytesToBase64(hash(password, salt));
        BufferedWriter  bw = new BufferedWriter(new FileWriter(passwordPath, true));
        bw.write(username + "," + passwordDigestBase64 + "," + stringToBase64(salt));
        bw.newLine();
        bw.close();
        return true;
    }

    public static String generateSalt(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(Integer.toHexString(random.nextInt()));
        }
        return sb.toString();
    }

    private static byte[] hash(String password, String salt) throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes(StandardCharsets.UTF_8));
        final byte[] sha3_256bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
        return sha3_256bytes;
    }

    private static String bytesToBase64(byte[] bytes) {
        String encodedString = Base64.getEncoder().encodeToString(bytes);
        return encodedString;
    }

    private static String stringToBase64(String string) {
        String encodedString = Base64.getEncoder().encodeToString(string.getBytes());
        return encodedString;
    }

    private static String base64ToString(String string) {
        byte[] decodedBytes = Base64.getDecoder().decode(string);
        String decodedString = new String(decodedBytes);
        return decodedString;
    }

    public boolean print(String filename, String printer, String token) throws AuthenticationException {
        if (running) {
            if (tokenMap.containsKey(token)) {
                System.out.println("Command : Print by User : " + tokenMap.get(token));
                Job newJob = new Job(currentID, filename);
                printQueue.add(newJob);
                currentID++;
                return true;
            } else {
                throw new AuthenticationException("User is not authenticated");
            }
        } else {
            return false;
        }
    }

    public LinkedList<Job> queue(String token) throws AuthenticationException {
        if (running) {
            if (tokenMap.containsKey(token)) {
                System.out.println("Command : List Queue by User : " + tokenMap.get(token));
                return printQueue;
            } else {
                throw new AuthenticationException("User is not authenticated");
            }
        } else {
            return new LinkedList<Job>();
        }
    }
    public boolean topQueue(int job, String token) throws AuthenticationException {
        if (running) {
            if (tokenMap.containsKey(token)) {
                System.out.println("Command : topQueue by User : " + tokenMap.get(token));
                Job tempJob = new Job(job, "");
                int index = printQueue.indexOf(tempJob);
                if (index != -1) {
                    tempJob = printQueue.get(index);
                    printQueue.remove(index);
                    return printQueue.offerFirst(tempJob);
                } else {
                    return false;
                }
            } else {
                throw new AuthenticationException("User is not authenticated");
            }
        } else {
            return false;
        }
    }
    public boolean start(String token) throws AuthenticationException {
        if (tokenMap.containsKey(token)) {
            System.out.println("Command : Start by User : " + tokenMap.get(token));
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
        if (tokenMap.containsKey(token)) {
            System.out.println("Command : Stop by User : " + tokenMap.get(token));
            if (running) {
                running = false;
                tokenMap.clear();
                printQueue.clear();
                return true;
            } else {
                return false;
            }
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }
    public boolean restart(String token) throws AuthenticationException {
        if (tokenMap.containsKey(token)) {
            System.out.println("Command : Restart by User : " + tokenMap.get(token));
            if (running) {
                running = false;
                printQueue.clear();
                tokenMap.clear();
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
        if (tokenMap.containsKey(token)) {
            System.out.println("Command : Status by User : " + tokenMap.get(token));
            StringBuilder sb = new StringBuilder();
            if (running) {
                sb.append("Server Status : Running \n");
            } else {
                sb.append("Server Status: Off \n");
            }
            if (running) {
                sb.append("Print Queue length: \n").append(printQueue.size());
            }
            return sb.toString();
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }
    public String readConfig(String parameter, String token) throws AuthenticationException {
        if (running) {
            if (tokenMap.containsKey(token)) {
                System.out.println("Command : Read Config by User : " + tokenMap.get(token));
                return config.get(parameter);
            } else {
                throw new AuthenticationException("User is not authenticated");
            }
        } else {
            return "";
        }
    }
    public boolean setConfig(String parameter, String value, String token) throws AuthenticationException {
        if (running) {
            if (tokenMap.containsKey(token)) {
                System.out.println("Command : Set Config by User : " + tokenMap.get(token));
                config.put(parameter, value);
                return true;
            } else {
                throw new AuthenticationException("User is not authenticated");
            }
        } else {
            return false;
        }
    }
}
