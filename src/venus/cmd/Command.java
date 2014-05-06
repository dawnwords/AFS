package venus.cmd;

import venus.Venus;

/**
 * Created by Dawnwords on 2014/5/6.
 */
public interface Command {
    void processCommand(Venus venus, String[] arg);

    boolean checkArgs(String[] args);

    String getArgFormat();
}
