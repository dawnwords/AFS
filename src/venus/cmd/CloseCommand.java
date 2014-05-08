package venus.cmd;

import venus.Venus;

/**
 * close Command Handler
 * <p/>
 * Created by Dawnwords on 2014/5/8.
 */
public class CloseCommand implements Command {
    @Override
    public void processCommand(Venus venus, String[] arg) {
        venus.close();
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
