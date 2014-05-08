package venus.cmd;

import data.Lock;
import venus.Venus;

/**
 * open Command Handler
 * <p/>
 * Created by Dawnwords on 2014/5/8.
 */
public class OpenCommand implements Command {
    private static final String READ_ONLY = "-r";
    private static final String WRITE = "-w";

    @Override
    public void processCommand(Venus venus, String[] arg) {
        Lock.LockMode mode = READ_ONLY.equals(arg[0]) ? Lock.LockMode.SHARED : Lock.LockMode.EXCLUSIVE;
        Venus.OpenError error = venus.open(arg[1], mode);
        if (error != Venus.OpenError.SUCCESS) {
            System.out.println(error.getErrorMsg());
        }
    }

    @Override
    public boolean checkArgs(String[] args) {
        return args.length == 2 && (READ_ONLY.equals(args[0]) || WRITE.equals(args[0]));
    }

    @Override
    public String getArgFormat() {
        return "[-r|-w] %filename";
    }
}
