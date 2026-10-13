package ServerHandling;

import ClientProfiles.ClientHandling;
import ServerHandling.Enums.USER_TYPE;
import ServerHandling.Enums.CHAT_TYPE;
import ServerHandling.Enums.SYS_MSG_TYPE;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.util.List;
import java.net.Socket;

public class ServerThread extends Thread
{
    // Class Variables
    private Socket clientSocket;
    private DataOutputStream serverToClient;
    private DataInputStream clientToServer;
    private List<ServerThread> connections;
    private ClientHandling user;
    private String destUser;
    protected String userString = "";
    protected String[] userArgs;
    private String userConf;

    public ServerThread(Socket client, List<ServerThread> connections)
    {
        this.connections = connections;
        this.clientSocket = client;
    }

    @Override
    public void run()
    {
        try {
            this.serverToClient = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            this.clientToServer = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

            this.user = new ClientHandling(clientToServer.readUTF());

            System.out.println("User " + this.user.getName() + " has successfully connected");

            serverToClient.writeUTF("Welcome to the chat, please remember our rules!");
            serverToClient.flush();

            broadcastSystemMessage(SYS_MSG_TYPE.JOIN);

            while (!userString.equalsIgnoreCase("Quit"))
            {
                userString = clientToServer.readUTF();
                System.out.println(this.user.getName() + ": " + userString);

//                serverToClient.writeUTF("Reflecting the message of " + userString);
//                serverToClient.flush();

                broadcastMessage(userString);

//                if (userString.startsWith("/nick "))
//                {
//                    userArgs = userString.split(" ", 2);
//                    if (userArgs.length == 2)
//                    {
//                        serverToClient.writeUTF("Are you wanting to switch from " + this.user.getName() + " to " + userArgs[1] + " ?");
//                        serverToClient.flush();
//
//                        userString = clientToServer.readUTF();
//                        if (userString.equalsIgnoreCase("yes"))
//                        {
//                            broadcastMessage(this.user.getName() + " has changed their name to " + userArgs[1]);
//                            this.user.setName(userArgs[1]);
//
//                        }
//                    }
//                }
//                else if (userString.startsWith("/priv"))
//                {
//                    userArgs = userString.split(" ", 3);
//                    if (userArgs.length == 3)
//                    {
//                        privateMessage(userArgs[2], userArgs[1]);
//                    }
//                }
//                else if (userString.startsWith("/group"))
//                {
//                    //Handle group
//                }
//                else {
//                    broadcastMessage(userString);
//                }

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
                System.out.println("Broadcasting to: " + x.user.getName());
                x.sendMessage(msg);
            }
        } catch (IOException e) {
            System.out.println("Encountered an error when broadcasting!");
        }
    }

    public void broadcastSystemMessage(SYS_MSG_TYPE type) throws IOException {
        for (ServerThread x : connections)
        {
            switch (type)
            {
                case JOIN ->
                {
                    x.serverToClient.writeUTF("[" + USER_TYPE.SERVER + "]: " + this.user.getName() + " has entered " + CHAT_TYPE.GLOBAL + " chat! Welcome them in!");
                    x.serverToClient.flush();
                }
                case LEAVE ->
                {
                    x.serverToClient.writeUTF("[" + USER_TYPE.SERVER + "] " + this.user.getName() + " has left the chat");
                    x.serverToClient.flush();
                }
            }

        }
    }

    public void privateMessage(String msg, String destUser) throws IOException {
        for (ServerThread x : connections)
        {
            if (x.user.getName().equalsIgnoreCase(destUser))
            {
                x.serverToClient.writeUTF("[" + CHAT_TYPE.PRIVATE + "]" + this.user.getName() + ": " + msg);
                x.serverToClient.flush();
            }
        }
    }

    public void groupMessage(String msg, String destGroup)
    {

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
}
