package venus.cmd;

import venus.Venus;

/**
 * Created by Dawnwords on 2014/5/6.
 */
public class ExitCommand implements Command {

    @Override
    public void processCommand(Venus venus, String[] arg) {
        System.exit(0);
    }

    @Override
    public boolean checkArgs(String[] args) {
        return true;
    }

    @Override
    public String getArgFormat() {
        return "";
    }
}
