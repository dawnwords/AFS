package venus.cmd;

import venus.Venus;

/**
 * Command Handler Interface
 * <p/>
 * Created by Dawnwords on 2014/5/6.
 */
public interface Command {
    /**
     * Process the command with given arguments
     *
     * @param venus venus instance
     * @param arg   arguments
     */
    void processCommand(Venus venus, String[] arg);

    /**
     * Check argument
     *
     * @param args arguments
     * @return true if the format of arguments is correct
     */
    boolean checkArgs(String[] args);

    /**
     * @return correct format of arguments
     */
    String getArgFormat();
}
