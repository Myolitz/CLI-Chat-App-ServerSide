import ServerHandling.ServerSide;

/**
 * Class: COSC 2327
 * Date of Creation: September 28th, 2026
 * @author Jesus Cardenas
 * @operating-system Endeavour OS (Linux)
 *
 * CONSIDERED FINAL, ONLY UPDATES WILL BE COMMENTS OR SLIGHT LOGIC FIXES
 */

/*
    Whole purpose of this is to prevent having to use the `static` keyword
    in method calls, probably unneeded, but I'm used to leaving functionality in
    NOT-main methods unless absolutely necessary.
 */

public class ServerStart
{
    public static void main(String[] args)
    {
        ServerSide server = new ServerSide();

        // Calling `server.run()` would also work but since Thread implements Runnable then `.start()` works all the same
        server.start();
    }
}
