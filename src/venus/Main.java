package venus;

import data.Parameter;
import interfaces.ViceInterface;
import util.FileSystemUtil;
import venus.cmd.Command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Created by Dawnwords on 2014/5/4.
 */
public class Main {
    public static void main(String[] args) {
        String venusRMI = "rmi://%s:%d/venus";
        int venusRMIPort = 0;
        try {
            venusRMIPort = Integer.parseInt(args[1]);
            venusRMI = String.format(venusRMI, args[0], venusRMIPort);
        } catch (Exception e) {
            System.err.println("Arg Format Error: should be(%ip %port)");
            System.exit(-1);
        }
        Parameter.VENUS_DIR = String.format(Parameter.VENUS_DIR, venusRMIPort);

        Venus venus = null;
        try {
            ViceInterface vice = (ViceInterface) Naming.lookup(Parameter.RMI_URL);
            venus = new Venus(vice, venusRMI);
            LocateRegistry.createRegistry(venusRMIPort);
            Naming.rebind(venusRMI, venus);
            System.out.println("Venus Started!");

            vice.register(venusRMI);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(venus, line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileSystemUtil.close(reader);
        }
    }

    private static void processLine(Venus venus, String line) {
        String[] tokens = line.split(" ");
        if (tokens.length > 0 && tokens[0].length() > 0) {
            String cmd = tokens[0];
            String[] arguments = new String[tokens.length - 1];
            System.arraycopy(tokens, 1, arguments, 0, arguments.length);

            try {
                cmd = Character.toUpperCase(cmd.charAt(0)) + cmd.substring(1);
                Class cmdClass = Class.forName("venus.cmd." + cmd + "Command");
                Command command = (Command) cmdClass.newInstance();
                if (command.checkArgs(arguments)) {
                    command.processCommand(venus, arguments);
                } else {
                    System.err.printf("Argument Error: should be(%s)\n", command.getArgFormat());
                }
            } catch (Throwable e) {
                e.printStackTrace();
                System.err.println("No Such Command");
            }
        } else {
            System.err.println("Format Error: should be(%venus.cmd %arg1 %arg2 ...)");
        }
    }

}
