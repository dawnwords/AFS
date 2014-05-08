package data;

/**
 * Created by Dawnwords on 2014/5/4.
 */
public class Lock {
    private long userId;
    private long requestTime;
    private FID fid;
    private LockMode mode;

    public static enum LockMode {
        SHARED, EXCLUSIVE;
    }

    public Lock(long userId, FID fid, LockMode mode) {
        this.userId = userId;
        this.fid = fid;
        this.mode = mode;
        this.requestTime = System.currentTimeMillis();
    }

    public long getUserId() {
        return userId;
    }

    public LockMode getMode() {
        return mode;
    }

    public boolean isExpire() {
        return System.currentTimeMillis() - requestTime > Parameter.LOCK_EXPIRE_TIME;
    }

    public boolean isLocked(FID fid, LockMode mode) {
        return this.fid.equals(fid) && (mode == LockMode.EXCLUSIVE || this.mode == LockMode.EXCLUSIVE);
    }

    public boolean sameLock(FID fid, long userId) {
        return this.fid.equals(fid) && this.userId == userId;
    }
}
