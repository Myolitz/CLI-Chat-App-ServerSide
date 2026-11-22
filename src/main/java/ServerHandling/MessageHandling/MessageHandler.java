/** Class: COSC 2327
 *  Date of Creation: 11/19/26
 * @author Jesus Cardenas
 * @operating-system Endeavour OS (Linux)
 */

package ServerHandling.MessageHandling;

import ServerHandling.Enums.USER_MSG_TYPE;

import java.util.HashMap;
import java.util.Map;

public class MessageHandler
{
    HashMap<String, USER_MSG_TYPE> messageMappings;

    public MessageHandler()
    {
        messageMappings = new HashMap<>
                (Map.of(
                        "/nick", USER_MSG_TYPE.COSMETIC,
                        "/priv", USER_MSG_TYPE.PRIVATE,
                        "/group", USER_MSG_TYPE.GROUP,
                        "/add", USER_MSG_TYPE.GROUP
                ));
    }

    public USER_MSG_TYPE messageType(String userIn)
    {
        String[] userArgs = userIn.split(" ");

        // Debug Printing
        //System.out.println("The start of the user's String is: " + userArgs[0]);

        return messageMappings.getOrDefault(userArgs[0], USER_MSG_TYPE.GLOBAL);
    }

    //TODO Handle Group Commands Better

    /*
        Maybe do regex splitting to remove hard-coding calls in `ServerThread?`

        Bunch of Psuedocode, tech debt is already high enough lol to begin testing this

        public X messageSplitting(String userIn, ServerThread inputThread)
        {
            split userIn into 3

            if /GROUP == COMMAND_TYPE = userIn[0]

            check userIn[1] for CREATE or GROUPNAME

            if CREATE then userIn[2] is GROUPNAME

            else if userIn[1] is GROUPNAME then userIn[2] is MESSAGE

            else if /ADD == COMMAND_TYPE = userIn[0]

            TARGET_USER = userIn[1]

            GROUPNAME = userIn[2]

            return these variables to then be sorted through in SERVERTHREAD
        }
     */
}
