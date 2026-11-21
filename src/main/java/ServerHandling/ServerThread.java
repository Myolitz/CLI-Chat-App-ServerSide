/** Class: COSC 2327
 *  Date of Creation: 10/02/26
 * @author Jesus Cardenas
 * @operating-system Endeavour OS (Linux)
 */

package ServerHandling;

//Objects/Enums
import ClientProfiles.ClientHandling;
import ClientProfiles.GroupHandler;
import ServerHandling.Enums.USER_TYPE;
import ServerHandling.Enums.CHAT_TYPE;
import ServerHandling.Enums.SYS_MSG_TYPE;

//Libraries
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.util.List;
import java.net.Socket;

import ServerHandling.MessageHandling.MessageHandler;
import ServerUtils.ChatDictionaries;


public class ServerThread extends Thread
{
    // Class Variables
    private Socket clientSocket;
    private DataOutputStream serverToClient;
    private DataInputStream clientToServer;
    private List<ServerThread> connections;
    private ClientHandling user;
    private MessageHandler msgHandler;
    private ChatDictionaries dict;

    private String destUser;
    protected String userString = "";
    protected String[] userArgs;
    private String userConf;

    public ServerThread(Socket client, List<ServerThread> connections)
    {
        this.connections = connections;
        this.clientSocket = client;
        msgHandler = new MessageHandler();
        dict = new ChatDictionaries();
    }

    public ServerThread(USER_TYPE type)
    {
        //TODO HANDLE CREATION OF ADMIN/SYSTEM THREAD TO READ SYSTEM.IN
    }

    @Override
    public void run()
    {
        try {
            /*
                Creates the IO socket streams between clients and the server before announcing the connection
                to any other existing connections.

                TODO:
                Maybe swap serverToClient to printWriter to allow for automatic flushing?
                Its working as-is so maybe not.
             */
            this.serverToClient = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            this.clientToServer = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

            // Creates a User Profile with a nickname that serves as ID if anything.
            // TODO: Private and Group Messaging
            this.user = new ClientHandling(clientToServer.readUTF());

            // Server broadcasting, lets every other use know who else has joined. Maybe add timestamp?
            System.out.println("User " + this.user.getName() + " has successfully connected");
            serverToClient.writeUTF("Welcome to the chat, please remember our rules!");
            serverToClient.flush();
            broadcastSystemMessage(SYS_MSG_TYPE.JOIN);

            while (!userString.equalsIgnoreCase("Quit"))
            {
                userString = clientToServer.readUTF();
                switch (msgHandler.messageType(userString))
                {
                    case GLOBAL -> // "[message]"
                    {
                        broadcastMessage(userString);
                    }
                    case PRIVATE -> // "/priv [destUser] [message]"
                    {
                        privateMessage(userString);
                    }
                    case GROUP -> // "/group [groupName] [message]", "/group create [groupName]", "/add [targetUser] [groupName]
                    {
                        groupMessage(userString);
                    }
                    case COSMETIC -> // "/nick [newNickName]
                    {
                        changeName(userString);
                    }
                    default ->
                    {
                        serverToClient.writeUTF("There was en error in processing your message, Please Re-send!");
                    }
                }

                if (userString.equalsIgnoreCase("Quit"))
                {
                    broadcastSystemMessage(SYS_MSG_TYPE.LEAVE);
                    connections.remove(this);
                    break;
                }
                else
                {
                    System.out.println(this.user.getName() + ": " + userString);
                }



            }
        } catch (IOException e) {
            try {
                broadcastSystemMessage(SYS_MSG_TYPE.LEAVE);
                clientSocket.close();
            } catch (IOException ex) {
                System.out.println(user.getName() + " has disconnected forcefully");;
            }
        }

    }

    public void broadcastMessage(String msg) {
        try {
            for (ServerThread x : connections)
            {
                // Debug print statement
                // System.out.println("Broadcasting to: " + x.user.getName());
                x.serverToClient.writeUTF("[" + CHAT_TYPE.GLOBAL + "] " + user.getName() + ": " + msg);
                x.serverToClient.flush();
            }
        } catch (IOException e) {
            System.out.println("Encountered an error when broadcasting!");
        }
    }

    // Tells all active connections about the new one once a nickname is set as well as tells when they leave.
    public void broadcastSystemMessage(SYS_MSG_TYPE type) throws IOException {
        for (ServerThread x : connections)
        {
            switch (type)
            {
                case JOIN ->
                {
                    x.serverToClient.writeUTF(dict.getAdminMaps().get(USER_TYPE.SERVER)
                            + this.user.getName() + " has entered " + CHAT_TYPE.GLOBAL + " chat! Welcome them in!");

                    x.serverToClient.flush();
                }
                case LEAVE ->
                {
                    x.serverToClient.writeUTF(dict.getAdminMaps().get(USER_TYPE.SERVER)
                            + this.user.getName() + " has left the chat");

                    x.serverToClient.flush();
                }
                case NICK_CHANGE ->
                {
                    x.serverToClient.writeUTF(dict.getAdminMaps().get(USER_TYPE.SERVER)
                            + this.user.getOldName() + " has changed their name to " + this.user.getName());

                    x.serverToClient.flush();
                }
            }

        }
    }

    public void privateMessage(String msg) throws IOException {

        userArgs = msg.split(" ", 3);
        for (ServerThread x : connections)
        {
            if (x.user.getName().equalsIgnoreCase(userArgs[1]))
            {
                x.serverToClient.writeUTF(dict.getChatMaps().get(CHAT_TYPE.PRIVATE) + this.user.getName() + ": " + userArgs[2]);
                x.serverToClient.flush();
            }
        }
    }

    public void groupMessage(String msg) throws IOException {
        userArgs = msg.split(" ", 3);

        if (userArgs[1].equalsIgnoreCase("create"))
        {
            this.user.addGroup(new GroupHandler(userArgs[2], this));
        } else if (userArgs[1].equalsIgnoreCase("add"))
        {
            //groupHandler.addMembers(userArgs[3], userArgs[4]);
        }
        else
        {
//            for (String x : groupHandler.getGroupMembers(userArgs[1]))
//            {
//
//            }
        }
    }

    public void changeName(String userString) throws IOException {
        String[] userArgs = userString.split(" ", 2);
        if (userArgs.length == 2)
        {
            this.user.setName(userArgs[1]);

            serverToClient.writeUTF("Are you sure you want to change your nickname to: " + userArgs[1] + " ? [Y/N]");
            serverToClient.flush();

            if (clientToServer.readUTF().equalsIgnoreCase("Y"))
            {
                broadcastSystemMessage(SYS_MSG_TYPE.NICK_CHANGE);
            }
            else
            {
                serverToClient.writeUTF("Command Canceled");
                serverToClient.flush();
            }
        }
    }

    public void sendMessage(String msg) throws IOException
    {
        serverToClient.writeUTF("[" + CHAT_TYPE.GLOBAL + "] " + user.getName() + ": " + msg);
        serverToClient.flush();
    }

    public void updateList(List<ServerThread> activeConn)
    {
        this.connections = activeConn;
    }

    public ClientHandling getUser()
    {
        return this.user;
    }

    public List<ServerThread> getConnections()
    {
        return connections;
    }
}
