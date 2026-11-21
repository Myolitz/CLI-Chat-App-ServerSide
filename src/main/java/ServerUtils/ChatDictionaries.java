package ServerUtils;

import ServerHandling.Enums.CHAT_TYPE;
import ServerHandling.Enums.USER_TYPE;

import java.util.Map;

public class ChatDictionaries
{
    protected Map<CHAT_TYPE, String> chatMaps =
            Map.of(
                    CHAT_TYPE.GLOBAL, "[" + CHAT_TYPE.GLOBAL + "] ",
                    CHAT_TYPE.PRIVATE, "[" + CHAT_TYPE.PRIVATE + "] ",
                    CHAT_TYPE.GROUP, "[" + CHAT_TYPE.GROUP + "] "
            );

    protected Map<USER_TYPE, String> adminMaps =
            Map.of(
                    USER_TYPE.ADMIN, "[" + USER_TYPE.ADMIN + "] ",
                    USER_TYPE.SERVER, "[" + USER_TYPE.SERVER + "] "
            );



    public ChatDictionaries(){}

    public Map<CHAT_TYPE, String> getChatMaps() {
        return chatMaps;
    }

    public Map<USER_TYPE, String> getAdminMaps() {
        return adminMaps;
    }
}
