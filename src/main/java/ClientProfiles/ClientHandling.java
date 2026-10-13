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
import java.util.List;

public class ClientHandling
{
    @Getter @Setter
    private String name;
    private List<ServerThread> groupList;
    private USER_TYPE status;


    public ClientHandling(String name)
    {
        this.name = name;
        groupList = new ArrayList<>();

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

    public void createGroup()
    {
        //TODO: Handle
    }
}
