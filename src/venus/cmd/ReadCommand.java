package venus.cmd;

import venus.Venus;

/**
 * Created by Dawnwords on 2014/5/8.
 */
public class ReadCommand implements Command {
    @Override
    public void processCommand(Venus venus, String[] arg) {
        String read = venus.read();
        if (read != null) {
            System.out.println(read);
        } else {
            System.err.print("Open before reading");
        }
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
