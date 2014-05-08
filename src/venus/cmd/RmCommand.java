package venus.cmd;

import venus.Venus;

/**
 * rm Command Handler
 * <p/>
 * Created by Dawnwords on 2014/5/7.
 */
public class RmCommand implements Command {
    @Override
    public void processCommand(Venus venus, String[] arg) {
        if (!venus.remove(arg[0])) {
            System.out.println("No such file or directory");
        }
    }

    @Override
    public boolean checkArgs(String[] args) {
        return args.length == 1;
    }

    @Override
    public String getArgFormat() {
        return "%filename|%dirname";
    }
}
