package vice;

/**
 * Created by Dawnwords on 2014/5/6.
 */
public class Log {
    private static Log ourInstance = new Log();

    public static Log getInstance() {
        return ourInstance;
    }

    private Log() {
    }

    public void i(String format, Object... args) {
        Object[] timeArgs = new Object[args.length + 2];
        System.arraycopy(args, 0, timeArgs, 2, args.length);
        timeArgs[0] = System.currentTimeMillis();
        timeArgs[1] = timeArgs[0];
        System.out.printf("[INFO %tF %tT]" + format + "\n", timeArgs);
    }
}
