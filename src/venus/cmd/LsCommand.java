package venus.cmd;

import venus.Venus;

/**
 * Created by Dawnwords on 2014/5/7.
 */
public class LsCommand implements Command {
    @Override
    public void processCommand(Venus venus, String[] arg) {
        String[] files = venus.listFile();
        for (String file : files) {
            System.out.println(file);
        }
    }

    @Override
    public boolean checkArgs(String[] args) {
        return true;
    }

    @Override
    public String getArgFormat() {
        return null;
    }
}
