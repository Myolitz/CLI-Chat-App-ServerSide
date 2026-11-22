/** Class: COSC 2327
 *  Date of Creation: 10/02/26
 * @author Jesus Cardenas
 * @operating-system Endeavour OS (Linux)
 */

package ServerHandling;

//Objects/Enums
import ClientProfiles.ClientHandling;
import ClientProfiles.GroupHandler;
import ServerHandling.Enums.USER_MSG_TYPE;
import ServerHandling.Enums.USER_TYPE;
import ServerHandling.Enums.CHAT_TYPE;
import ServerHandling.Enums.SYS_MSG_TYPE;

//Libraries
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.util.ArrayList;
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

                HANDLE SAME-NAME USERS
             */
            this.serverToClient = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            this.clientToServer = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

            // Creates a User Profile with a nickname that serves as ID if anything.
            this.user = new ClientHandling(clientToServer.readUTF());

            /*
                Server broadcasting, lets every other use know who else has joined.

                TODO: Timestamps maybe?
             */
            System.out.println("User " + this.user.getName() + " has successfully connected");
            serverToClient.writeUTF("Welcome to the chat, please remember our rules!");
            serverToClient.flush();

            broadcastSystemMessage(SYS_MSG_TYPE.JOIN);

            /*
                Simple Loop that uses MessageHandler to simplify method calling until the User Quits manually or forcefully.
             */
            while (!userString.equalsIgnoreCase("Quit"))
            {
                userString = clientToServer.readUTF();
                switch (msgHandler.messageType(userString))
                {
                    case GLOBAL ->
                    {
                        broadcastMessage(userString);
                    }
                    case PRIVATE ->
                    {
                        privateMessage(userString);
                    }
                    case GROUP ->
                    {
                        groupMessage(userString);
                    }
                    case COSMETIC ->
                    {
                        changeName(userString);
                    }
                    default ->
                    {
                        serverToClient.writeUTF("There was en error in processing your message, Please Re-send!");
                    }
                }

                // Prints whatever is inputted into server log for the sake of admin or w/e
                System.out.println(this.user.getName() + ": " + userString);
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

    /*
        The Simplest method, simply does an O(n) check on every active connection it can see
     */
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

    /*
        Follows the methodology of broadcastMessage but for SYSTEM level messages only
     */
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

    /*
        Not overly complex, same O(n) check as broadcasting but immediately exits upon finding the user

        If nothing is ever found, it simply outputs a "not found" message
     */
    public void privateMessage(String msg) throws IOException {

        userArgs = msg.split(" ", 3);
        for (ServerThread x : connections)
        {
            if (x.user.getName().equalsIgnoreCase(userArgs[1]))
            {
                x.serverToClient.writeUTF(dict.getChatMaps().get(CHAT_TYPE.PRIVATE) + this.user.getName() + ": " + userArgs[2]);
                x.serverToClient.flush();
                return;
            }
        }
        sendMessage(userArgs[1] + " was not found or is offline");
    }

    /*

     */
    public void groupMessage(String msg) throws IOException {
        userArgs = msg.split(" ", 3);

        if (userArgs[1].equalsIgnoreCase("create")) // `/group create [groupName]`
        {
            if (userArgs[2].length() >= 10)
            {
                while (userArgs[2].length() >= 10)
                {
                    sendMessage("Group Name too long, try again! (<10 characters)!");
                    userArgs[2] = clientToServer.readUTF();
                }
            }

            this.user.addGroup(userArgs[2], new GroupHandler(userArgs[2], this));

            this.serverToClient.writeUTF(dict.getAdminMaps().get(USER_TYPE.SERVER) + "You have successfully created " + userArgs[2]);
            this.serverToClient.flush();
            return;

        } else if (userArgs[0].equalsIgnoreCase("/add")) // "/add [targetUser] [groupName]"
        {
            this.user.getGroup(userArgs[2]).addMembers(userArgs[1], userArgs[2]);
        }
        else // "/group [groupName] [message]"
        {
            if (this.user.getGroupList().isEmpty())
            {
                serverToClient.writeUTF(dict.getAdminMaps().get(USER_TYPE.SERVER) + " You are not part of any groups!");
                serverToClient.flush();
            }
            else if (this.user.getGroupList().get(userArgs[1]) == null)
            {
                serverToClient.writeUTF(dict.getAdminMaps().get(USER_TYPE.SERVER) + " You are not part of or don't have access to " + userArgs[1]);
                serverToClient.flush();

                serverToClient.writeUTF(dict.getAdminMaps().get(USER_TYPE.SERVER) + " You only have access to the following groups, not counting global chat\n");

                for (String x : this.user.getGroupList().keySet())
                {
                    serverToClient.writeUTF(x);
                    serverToClient.flush();
                }
            }
            else
            {
                for (ServerThread x : connections)
                {
                    if (x.getUser() == null)
                    {
                        continue;
                    }
                    if (x.getUser().getGroup(userArgs[1]) != null)
                    {
                        for (String y : this.user.getGroup(userArgs[1]).getGroupMems())
                        {
                            if (y.equalsIgnoreCase(x.getUser().getName()))
                            {
                                x.serverToClient.writeUTF("[" + userArgs[1] + "] " + this.user.getName() + ": " + userArgs[2]);
                                x.serverToClient.flush();
                            }
                        }
                    }
//                    else if (x.getUser().getGroup(userArgs[1]) == null)
//                    {
//                        serverToClient.writeUTF(dict.getAdminMaps().get(USER_TYPE.SERVER) + " You do have access or are not part of " + userArgs[1]);
//                        serverToClient.flush();
//                    }
                }
            }
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
        serverToClient.writeUTF("[" + USER_TYPE.SERVER + "] " + msg);
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
