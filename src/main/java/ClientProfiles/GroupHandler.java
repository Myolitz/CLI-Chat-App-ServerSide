package ClientProfiles;

import ServerHandling.ServerThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupHandler
{
    protected Map<String, ArrayList<String>> groupMemberList;
    protected String groupName;
    protected ArrayList<String> groupMembers;
    private ServerThread userThread;
    private String groupOwner;

    public GroupHandler(String groupName, ServerThread inputThread) throws IOException {
        this.groupMemberList = new HashMap<>();
        this.groupMembers = new ArrayList<>();
        this.groupName = groupName;
        this.userThread = inputThread;
        this.groupOwner = inputThread.getUser().getName();

        this.groupMembers.add(groupOwner);
    }

    public void addMembers(String member, String destGroup) throws IOException {
        for (ServerThread x : userThread.getConnections())
        {
            if (x.getUser().getName().equalsIgnoreCase((member)))
            {
                this.userThread.getUser().getGroupList().get(destGroup).groupMembers.add(member);

                x.sendMessage("You have been added to " + this.groupName + " by " + this.groupOwner);
                x.getUser().addGroup(this.groupName, this);

                userThread.sendMessage("You have successfully added " + member + " to " + this.groupName);
                return;
            }
        }
        userThread.sendMessage("User not found, try again");
    }

    public ArrayList<String> getGroupMembersList(String destGroup)
    {
        return groupMemberList.get(destGroup);
    }

    public ArrayList<String> getGroupMems()
    {
        return this.groupMembers;
    }

    public String getGroupName()
    {
        return this.groupName;
    }
}