/** Class: COSC 2327
 *  Date of Creation: 10/04/26
 * @author Jesus Cardenas
 * @operating-system Endeavour OS (Linux)
 */

package ClientProfiles;

import ServerHandling.Enums.USER_TYPE;
import ServerHandling.ServerThread;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandling
{
    private @Getter @Setter String name;
    private @Getter @Setter String oldName;
    private Map<String, GroupHandler> groupList;
    private USER_TYPE status;
    private GroupHandler groupHandler;


    public ClientHandling(String name)
    {
        this.groupList = new HashMap<>();
        this.name = name;

        //Might not be needed if I directly handle server cmds through a dedicated server thread and not a pseudo-user
        if (name.equalsIgnoreCase("admin"))
        {
            status = USER_TYPE.ADMIN;
        }
        else
        {
            status = USER_TYPE.STANDARD;
        }
    }

    public void addGroup(String groupName, GroupHandler newGroup)
    {
        groupList.put(groupName, newGroup);
    }

    public void setName(String newName)
    {
        this.oldName = name;
        name = newName;
    }

    public String getOldName()
    {
        return oldName;
    }

    public GroupHandler getGroup(String groupName)
    {
        return groupList.get(groupName);
    }

    public Map<String, GroupHandler> getGroupList()
    {
        return groupList;
    }
    // LOMBOK is apparently not available so yeah.
    public String getName()
    {
        return this.name;
    }
}
