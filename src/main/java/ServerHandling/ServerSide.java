/** Class: COSC 2327
 *  Date of Creation: 9/24/26
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
    final private int networkPort = 12345;
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
                    /*
                        TODO:
                        Create server-based ServerThread to read Server Console `System.in` to handle `isDone` changing rather than hard-shutoff
                     */

                    // Maintains Server "ON" State, updating connections as needed to prevent NPE or IOE
                    updateConns();
                    clientSocket = srvrSocket.accept();
                    System.out.println("Connection Accepted");

                    // Creates a new instance of the `ServerThread` class, passing the reference to the
                    // existing list and whatever socket has been accepted
                    ServerThread userThread = new ServerThread(clientSocket, activeConn);
                    activeConn.add(userThread);
                    userThread.start();
                }

            } catch (IOException e) {
                System.out.println("Unable to form proper connection!");
            } catch (NullPointerException e) {
                System.out.println("A user has failed to connect!");
            }
            System.out.println("Goodbye from the server side!");
        }

    // A bit of a redundant method as I could not figure out how to call it from within ServerThread
    public void broadcast(String msg) throws IOException
        {
            for (ServerThread x : activeConn)
            {
                x.sendMessage(msg);
            }
        }
    // Actively updates the connection list for every thread, checking to see if the thread is even alive.
    // If its not, it removes it from the list to prevent broadcasting NPE/IOE
    public void updateConns()
    {
        for (ServerThread x : activeConn) {
            if (x.isAlive()) {
                continue;
            } else {
                activeConn.remove(x);
            }
            x.updateList(activeConn);
        }
    }
}

