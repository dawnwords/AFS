package venus.cmd;

import venus.Venus;

/**
 * Created by Dawnwords on 2014/5/7.
 */
public class CdCommand implements Command {
    @Override
    public void processCommand(Venus venus, String[] arg) {
        if (!venus.changeDir(arg[0])) {
            System.out.println("No Such Directory");
        }
    }

    @Override
    public boolean checkArgs(String[] args) {
        return args.length == 1;
    }

    @Override
    public String getArgFormat() {
        return "%dirname";
    }
}
