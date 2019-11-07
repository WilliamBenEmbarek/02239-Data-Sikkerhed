import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

public interface Print extends Remote {
    boolean print(String filename, String printer, String token) throws RemoteException, AuthenticationException;

    String authenticate(String username, String password) throws IOException, NoSuchAlgorithmException, AuthenticationException;

    LinkedList<Job> queue(String token) throws RemoteException, AuthenticationException;

    boolean topQueue(int job, String token) throws RemoteException, AuthenticationException;

    boolean start(String token) throws RemoteException, AuthenticationException;

    boolean stop(String token) throws RemoteException, AuthenticationException;

    boolean restart(String token) throws RemoteException, AuthenticationException;

    String status(String token) throws RemoteException, AuthenticationException;

    String readConfig(String parameter, String token) throws RemoteException, AuthenticationException;

    boolean setConfig(String parameter, String value, String token) throws RemoteException, AuthenticationException;
}
