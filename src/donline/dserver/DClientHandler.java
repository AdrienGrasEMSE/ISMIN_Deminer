// Package declaration
package donline.dserver;

// Import
import donline.DConnexionHandler;
import donline.DPing;
import donline.DReader;
import donline.DWritter;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Class Client Handler
 * 
 * @author AdrienG
 * @version 0.0
 * 
 * 
 * Class wich can hold a client by sending and receiving message
 */
public class DClientHandler implements DConnexionHandler{

    /**
     * Attributes
     */
    private         Thread          service;
    private final   Socket          socket;
    private final   String          uuid;
    private final   DServer         server;
    private final   DPing           pingService;
    private         boolean         connected   = true;
    private         boolean         serverOwner = false;
    private         boolean         ready       = false;
    private         String          playerName  = "";


    /**
     * Write stream
     */
    private final   DWritter        writter;
    private final   Queue<String>   writeQueue  = new LinkedList<>();


    /**
     * Read stream
     */
    private final   DReader         reader;
    private final   Queue<String>   readQueue   = new LinkedList<>();




    /**
     * Constructor
     * 
     * @param uuid
     * @param socket
     * @param server
     */
    public DClientHandler(String uuid, Socket socket, DServer server) {

        // Getting attributes
        this.uuid   = uuid;
        this.socket = socket;
        this.server = server;


        // Setting up writter and reader
        reader  = new DReader   (socket, readQueue);
        writter = new DWritter  (socket);


        // Check writter and reader status
        if (!writter.isReady() || !reader.isReady()) {

            // Disconnect client
            this.connected = false;

        }


        // Starting services
        this.pingService    = new DPing(uuid, this);
        this.service        = new Thread(this);
        this.service.start();

    }




    /**
     * Getter : to check client ownership
     * 
     * @return
     */
    public boolean isServerOwner() {
        return serverOwner;
    }




    /**
     * To change client ownership
     * 
     * @param serverOwner
     */
    public void setOwnership(boolean serverOwner) {
        this.serverOwner = serverOwner;
    }




    /**
     * Getter : to get the id of the client
     * 
     * @return uuid
     */
    public String getUUID() {
        return uuid;
    }




    /**
     * Method that add a server request that will be sent to the client
     * 
     * @param request
     */
    @Override
    public void addRequest(String request) {

        // Adding the request to the queue
        synchronized (writeQueue) {
            writeQueue.add(request);
        }

    }




    /**
     * Setter : to set the player name after the client hello
     * 
     * @param name
     */
    public void setPlayerName(String name) {
        playerName = name;
    }




    /**
     * Getter : to get the player pseudo
     * 
     * @return playerName
     */
    public String getPlayerName() {
        return playerName;
    }




    /**
     * Start ping service
     */
    public void startPinging() {
        pingService.start();
    }




    /**
     * Valid ping response
     */
    public void pingReceived() {
        pingService.answerPing();
    }




    /**
     * Stop the client handler
     */
    @Override
    public void shutDown() {

        // Sending all data before closing all
        synchronized (writeQueue) {
            while (!writeQueue.isEmpty()) {}
        }


        // Disconnecting
        connected = false;
    }




    /**
     * Closing all
     */
    public void disconnect() {

        // TODO : info
        System.out.println("Disconnecting " + this.uuid);


        // Trying to stop all properly
        if (reader.stop() && writter.stop()) {

            // Trying to close the socket
            try {

                // Closing socket
                socket.close();

            } catch (IOException e) {

                // TODO : handle this one (omg)
                System.out.println(e);

            }

        }


        // Shutting down service
        this.service = null;
        this.pingService.stop();


        // Removing the handler from the server
        server.removeHandler(uuid);

    }

    


    /**
     * Setter : to change the client state
     * 
     * @param ready
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }




    /**
     * Getter : to check if the client is ready or not
     * 
     * @return ready
     */
    public boolean getReady() {
        return this.ready;
    }




    /**
     * Thread method
     * 
     * Critical thread : 10ms
     */
    @Override
    public void run () {

        // Stop condition
        while (service != null) {

            // Get the start time of the loop iteration
            long startTime = System.currentTimeMillis();


            // While connected
            if (connected) {

                // Cheking client request
                synchronized (readQueue) {
                    if (!readQueue.isEmpty()) {

                        // Reading data
                        synchronized (server) {
                            server.addRequest(readQueue.poll());
                        }
                        
                    }
                }


                // Cheking server request
                synchronized (writeQueue) {
                    if (!writeQueue.isEmpty()) {

                        // Sending data
                        writter.write(writeQueue.poll());
                        
                    }

                }


            } else {

                // Disconnect
                this.disconnect();

            }



            // THREAD LIMITER
            // ====================================================================================
            
            // Calculate how long the operations took
            long elapsedTime = System.currentTimeMillis() - startTime;


            // Calculate the remaining time to sleep
            long sleepTime = 10 - elapsedTime;


            // If there is still time left in the 10ms window, sleep
            if (sleepTime > 0) {
                try {

                    // Pause
                    Thread.sleep(sleepTime);


                } catch (InterruptedException e) {

                    // Handle the exception
                    e.printStackTrace();

                }
            }
                      
        }

    }

}
