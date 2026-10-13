/** Class: COSC 2327
 *  LAST EDITED: 9/24/26
 * @author Jesus Cardenas
 * @operating-system Endeavour OS (Linux)
 */
package ServerHandling;

//Libraries
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerSide
{
    //Objects
    protected DataOutputStream serverToClient;
    protected DataInputStream clientToServer;
    protected List<ServerThread> activeConn;

    //Variables
    private int networkPort = 12345;
    protected ServerSocket srvrSocket;
    private Socket clientSocket;
    private boolean isDone;

    ///For Debug Purposes
    int numClientMessages;
    String userString;

    public ServerSide()
    {
        isDone = false;
        try
        {
            activeConn = new ArrayList<>();
            srvrSocket = new ServerSocket(networkPort);

        } catch (IOException e)
        {
            System.out.println("IO Exception caught");
        }
    }

    public void start()
    {
            try
            {
                System.out.println("Server started and listening on port: " + networkPort);

                while (!isDone)
                {
                    //Connection on
                    updateConns();
                    clientSocket = srvrSocket.accept();
                    System.out.println("Connection Accepted");

                    ServerThread userThread = new ServerThread(clientSocket, activeConn);
                    activeConn.add(userThread);
                    userThread.start();
                }

            } catch (IOException e) {
                System.out.println("Unable to form proper connection!");
            } catch (NullPointerException e)
            {
                System.out.println("A user has failed to connect!");
            }

            System.out.println("Goodbye from the server side!");
        }

    public void broadcast(String msg) throws IOException
        {
            for (ServerThread x : activeConn)
            {
                x.sendMessage(msg);
            }
        }

        public void updateConns()
        {
            for (ServerThread x : activeConn)
            {
                x.updateList(activeConn);
            }
        }

}

