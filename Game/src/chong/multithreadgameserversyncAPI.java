/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chong;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

/**
 *
 * @author Chong Chu
 */
public class multithreadgameserversyncAPI {
    
    public interface UpdateData{
        public void update(String responseLine);
    }
    
    // The server socket.
    private static ServerSocket serverSocket = null;
    // The client socket.
    private static Socket clientSocket = null;

    // This game server can accept up to maxClientsCount clients' connections.    
    private static final int maxClientsCount = 50;
    private static final clientThread[] threads = new clientThread[maxClientsCount];
    private static final int[] clientID= new int[maxClientsCount];
    //
    //need to split out
    private static final int[] clientSubmitValue=new int[maxClientsCount];
    private static final boolean[] clientValueChanged=new boolean[maxClientsCount];
    private static final int[] clientStatus=new int[maxClientsCount];
    private SumOfNumServer sms;
    
    public multithreadgameserversyncAPI(SumOfNumServer sms){
        this.sms=sms;
        for(int i=0;i<maxClientsCount;i++){
            this.clientID[i]=-1;
            this.clientSubmitValue[i]=-1;
            this.clientValueChanged[i]=false;
            this.clientStatus[i]=0;//by default they are running
        }
    }
    
    public void connect(int portNumber){
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }
        this.sms.update("Server started!\n");
        /*
        * Create a client socket for each connection and pass it to a new client
        * thread.
        */
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new clientThread(clientSocket, threads,clientID,clientSubmitValue,clientValueChanged,sms)).start();
                        break;
                    }
                }
                if (i == maxClientsCount) {
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    } 
    
    public void broadCastAll(String content){
        for(int i=0;i<maxClientsCount;i++){
            if(threads[i]!=null)
                threads[i].printContent(content);
        }
    }
    
    
    public void sendCommandToSpecificClients(int[] wakeCliendIDs, int size, String content){
        for(int i=0;i<size;i++){
            int id=wakeCliendIDs[i];
            for(int j=0;j<maxClientsCount;j++){
                if((threads[j]!=null) && (clientID[j]==id))
                    threads[j].printContent(content);
            }
            
        }
    }
    
    public boolean allNumbersChangesInGroup(int[] wakeCliendIDs, int size){
        boolean bAllChange=true;
        int[] clientPos=new int[size];
        for(int i=0;i<size;i++){
            int id=wakeCliendIDs[i];
            for(int j=0;j<maxClientsCount;j++){
                if(clientID[j]==id){
                    clientPos[i]=j;
                    if(this.clientValueChanged[j]==false){
                        bAllChange=false;
                        return false;
                    }
                }
            }
        }
        
        synchronized (this) {
            for(int i=0;i<size;i++){
                this.clientValueChanged[clientPos[i]]=false;
            }
        }
        
        return bAllChange;
    }
    
    public boolean validateGoal(int[] wakeCliendIDs, int size, int goal){
        
        int sum=0;
        for(int i=0;i<size;i++){
            int id=wakeCliendIDs[i];
            for(int j=0;j<maxClientsCount;j++){
                if(threads[j]!=null && clientID[j]==id && clientStatus[j]==0){
                    sum+=clientSubmitValue[j];
                }
            }
        }
        
        if(sum==goal)
            return true;
        else
            return false;
    }
}


/*
 * The game client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the game, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcast the incoming messages to all clients and
 * routes the private message to the particular client. When a client leaves the
 * game this thread informs also all the clients about that and terminates.
 */
class clientThread extends Thread {
    private String clientName = null;
    private DataInputStream is = null;
    private PrintStream os = null;
    private Socket clientSocket = null;
    private final clientThread[] threads;
    private final int[] clientID;
    private final int[] clientSubmitValue;
    private final boolean[] clientValueChanged;
    private int maxClientsCount;
    private SumOfNumServer sms;

    
    public clientThread(Socket clientSocket, clientThread[] threads, int[] clientID, int[] clientSubmitValue, 
                        boolean[] clientValueChanged, SumOfNumServer sms) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
        this.clientID=clientID;
        this.clientSubmitValue=clientSubmitValue;
        this.clientValueChanged=clientValueChanged;
        this.sms=sms;
    }
    
    public void printContent(String content){     
        os.println(content);
    }
    
    public void run() {
        int maxClientsCount = this.maxClientsCount;
        clientThread[] threads = this.threads;
        int[] clientID=this.clientID;
        int[] clientSubmitValue=this.clientSubmitValue;
        boolean[] clientValueChanged=this.clientValueChanged;
        //DataInputStream is=this.is;
        //PrintStream os =this.os;
        try {
            /*
             * Create input and output streams for this client.
             */
            is = new DataInputStream(clientSocket.getInputStream());
            os = new PrintStream(clientSocket.getOutputStream());
            String name;
            while (true) {
                //os.println("Enter your ID.");
                name = is.readLine().trim();
                sms.update(name+" is connected!\n");
                if (name.indexOf('@') == -1) {
                    break;
                } else {
                    os.println("The name should not contain '@' character.");
                }
            }
            
            synchronized (this) {
                int id=Integer.parseInt(name);
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == this) {
                        clientID[i]=id;
                    }
                }
            }
            
            /* Start the conversation.*/
            while (true) {
                //os.println("Enter a number:");
                String line = is.readLine().trim();    
                sms.update(name+" entered "+line+"\n");
                if (line.startsWith("/quit")) {
                    break;
                }     
                
                synchronized (this) {
                    int value=Integer.parseInt(line);
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] == this) {
                            clientSubmitValue[i]=value;
                            clientValueChanged[i]=true;
                        }
                    }
                }
            }
            
            /*
             * Clean up. Set the current thread variable to null so that a new client
             * could be accepted by the server.
             */
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                        clientID[i]=-1;
                        clientSubmitValue[i]=-1;
                        clientValueChanged[i]=false;
                    }
                }
            }
            /*
             * Close the output stream, close the input stream, close the socket.
             */
            is.close();
            os.close();
            clientSocket.close();
        } catch (IOException e) {
            
        }
    }
}
