import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;

public interface Print extends Remote {
    boolean print(String filename, String printer) throws RemoteException;
    ;

    LinkedList<JobInterface> queue() throws RemoteException;
    ;

    boolean topQueue(int job) throws RemoteException;
    ;

    boolean start() throws RemoteException;
    ;

    boolean stop() throws RemoteException;
    ;

    boolean restart() throws RemoteException;
    ;

    String status() throws RemoteException;
    ;

    String readConfig(String parameter) throws RemoteException;
    ;

    void setConfig(String parameter, String value) throws RemoteException;
    ;
}
