import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLOutput;
import java.util.LinkedList;
import java.util.Scanner;

public class Client {

    private Client() {}

    public static void main(String[] args) {

        String host = "localhost";
        try {
            Scanner kb = new Scanner(System.in);
            Registry registry = LocateRegistry.getRegistry(host,6969);
            Print stub = (Print) registry.lookup("server");
            System.out.println("Welcome to the PrintServer3000");
            String currentStatus = stub.status();
            System.out.println("Current server status is : " + currentStatus);
            while(true) {
                System.out.println("What would you like to do? (Type help for help): ");
                String input = kb.nextLine();
                switch (input) {
                    case "start":
                        if (stub.start()) {
                            System.out.println("Print server started");
                        } else {
                            System.out.println("Error : Server already started");
                        }
                        break;
                    case "stop":
                        if (stub.stop()) {
                            System.out.println("Print server stopped");
                        } else {
                            System.out.println("Error : Server not running");
                        }
                        break;
                    case "restart":
                        if (stub.restart()) {
                            System.out.println("Print server restarted");
                        } else {
                            System.out.println("Error : Server not running");
                        }
                        break;
                    case "status":
                        System.out.println("Current server status is : " + stub.status());
                        break;
                    case "queue":
                        LinkedList<JobInterface> queue = stub.queue();
                        for (JobInterface item : queue) {
                            System.out.println(item.toString());
                        }
                        break;
                    case "topQueue":
                        int jobNumber = kb.nextInt();
                        if (stub.topQueue(jobNumber)){
                            System.out.println("Successfully moved job:" + jobNumber + "to top of queue");
                        } else {
                            System.out.println("Error : Failed to move job to top of queue");
                        }
                        break;
                    case "print":
                        System.out.println("What is the filename : ");
                        String fileName = kb.nextLine();
                        System.out.println("What printer do you want to print to : ");
                        String printer = kb.nextLine();
                        stub.print(fileName, printer);
                        break;
                    case "readConfig":
                        System.out.println("What parameter do you want to read? : ");
                        String parameter = kb.nextLine();
                        System.out.println(stub.readConfig(parameter));
                        break;
                    case "setConfig":
                        System.out.println("What parameter do you want to set : ");
                        parameter = kb.nextLine();
                        System.out.println("What value do you want to set it to?");
                        String value = kb.nextLine();
                        stub.setConfig(parameter,value);
                        break;
                    case "help":
                        help();
                        break;
                }
            }


        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void help() {
        System.out.println("The following commands are available : ");
        System.out.println("start : Start the print server");
        System.out.println("stop : Stop the print server");
        System.out.println("restart: Restart the server");
        System.out.println("status : Show current print server status");
        System.out.println("queue : Show current print queue");
        System.out.println("topQueue : Move the specified job to the top of the job queue");
        System.out.println("print : print the file with the name filename to the printer with the name printer");
        System.out.println("readConfig : read the configuration for the specified parameter");
        System.out.println("setConfig : set the configuration for the specified parameter to the specified value");
    }
}