package ClientProfiles;

import ServerHandling.ServerThread;

import java.util.ArrayList;
import java.util.Map;

public class GroupHandler
{
    protected Map<String, ArrayList<String>> groupList;
    protected String groupName;
    protected ArrayList<String> groupMembers;
    private ServerThread userThread;
    private String groupOwner;

    public GroupHandler(String groupName, ServerThread inputThread)
    {
        this.userThread = inputThread;
        this.groupOwner = inputThread.getUser().getName();
    }

    public void addMembers(String member, String destGroup)
    {
        for (ServerThread x : userThread.getConnections())
        {
            if (x.getUser().getName().equalsIgnoreCase(member))
            {
                this.groupMembers.add(member);
            }
        }
    }

    /**
     * Queries `groupList`
     *
     * @param destGroup user-requested group, uses
     * @return groupMembers
     */
    public ArrayList<String> getGroupMembers(String destGroup)
    {
        return groupList.get(destGroup);
    }
}