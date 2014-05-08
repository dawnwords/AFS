package venus.cmd;

import venus.Venus;

/**
 * Created by Dawnwords on 2014/5/8.
 */
public class WriteCommand implements Command {
    private static final String APPEND = "-a";
    private static final String NEW = "-n";

    @Override
    public void processCommand(Venus venus, String[] arg) {
        String write = "";
        for (int i = 1; i < arg.length; i++) {
            write += arg[i] + " ";
        }
        if (!venus.write(write.trim(), APPEND.equals(arg[0]))) {
            System.err.println("Cannot write before opening -w");
        }
    }

    @Override
    public boolean checkArgs(String[] args) {
        return args.length > 1 && (APPEND.equals(args[0]) || NEW.equals(args[0]));
    }

    @Override
    public String getArgFormat() {
        return "[-a|-n] %content";
    }
}
