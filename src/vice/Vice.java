package vice;

import data.FID;
import data.FileHandler;
import data.Lock;
import data.Parameter;
import interfaces.VenusInterface;
import interfaces.ViceInterface;
import util.DataTypeUtil;
import util.FileSystemUtil;
import util.Log;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Dawnwords on 2014/5/4.
 */
public class Vice extends UnicastRemoteObject implements ViceInterface {

    private Map<Long, VenusInterface> clientMap;
    private Queue<Lock> lockQueue;
    private AtomicInteger uniquifier;

    protected Vice() throws RemoteException {
        FileSystemUtil.initRootDir(Parameter.VICE_DIR);
        FileSystemUtil.createFile(Parameter.ROOT_FID, Parameter.ROOT_FID);
        clientMap = new ConcurrentHashMap<Long, VenusInterface>();
        uniquifier = new AtomicInteger(FileSystemUtil.getStartUniquifier());
        lockQueue = new ConcurrentLinkedQueue<Lock>();
    }

    @Override
    public void register(String rmi) {
        try {
            VenusInterface venus = (VenusInterface) Naming.lookup(rmi);
            long userId = DataTypeUtil.longHash(rmi);
            clientMap.put(userId, venus);
            Log.getInstance().i("userId:%x register", userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public FileHandler fetch(FID fid, long userId) throws RemoteException {
        Log.getInstance().i("userId:%x fetch:%s", userId, fid);
        FileSystemUtil.addCallbackPromise(fid, userId);
        FileHandler fileHandler = FileSystemUtil.readFile(fid, Parameter.VICE_DIR);
        if (fileHandler != null) {
            fileHandler.encrypt();
        }
        return fileHandler;
    }

    @Override
    public void store(FID fid, FileHandler handler, long userId) throws RemoteException {
        Log.getInstance().i("userId:%x store:%s", userId, fid);
        handler.decrypt();
        FileSystemUtil.writeFile(fid, handler, Parameter.VICE_DIR);
    }

    @Override
    public FID create(long userId) throws RemoteException {
        FID fid = new FID(userId, uniquifier.getAndIncrement());
        FileSystemUtil.createFile(fid, Parameter.NULL_FID);
        Log.getInstance().i("userId:%x create file %s", userId, fid);
        return fid;
    }

    @Override
    public FID makeDir(FID parent, long userId) throws RemoteException {
        FID fid = new FID(userId, -uniquifier.getAndIncrement());
        FileSystemUtil.createFile(fid, parent);
        Log.getInstance().i("userId:%x make directory %s", userId, fid);
        return fid;
    }

    @Override
    public void remove(FID fid, long userId) throws RemoteException {
        Log.getInstance().i("userId:%x remove %s", userId, fid);
        removeR(fid, userId);
    }


    @Override
    public boolean setLock(FID fid, Lock.LockMode mode, long userId) throws RemoteException {
        Iterator<Lock> iterator = lockQueue.iterator();
        while (iterator.hasNext()) {
            Lock lock = iterator.next();
            if (lock.isExpire()) {
                iterator.remove();
                Log.getInstance().i("lock expire:userId:%x fid:%s", userId, fid);
            } else if (lock.isLocked(fid, mode)) {
                return false;
            }
        }
        lockQueue.add(new Lock(userId, fid, mode));
        Log.getInstance().i("userId:%x %s lock file %s", userId, mode, fid);
        return true;
    }

    @Override
    public void releaseLock(FID fid, long userId) throws RemoteException {
        Iterator<Lock> iterator = lockQueue.iterator();
        while (iterator.hasNext()) {
            Lock lock = iterator.next();
            if (lock.isExpire()) {
                iterator.remove();
                Log.getInstance().i("lock expire:userId:%x fid:%s", userId, fid);
            } else if (lock.sameLock(fid, userId)) {
                iterator.remove();
                Log.getInstance().i("userId:%x release lock %s", userId, fid);
            }
        }
    }

    @Override
    public void removeCallback(FID fid, long userId) throws RemoteException {
        Log.getInstance().i("userId:%x remove callback %s", userId, fid);
        List<Long> callbackList = FileSystemUtil.getCallbackPromiseList(fid);
        for (long cp : callbackList) {
            if (cp != userId) {
                Log.getInstance().i("break callback: userid:%x, fid:%s", cp, fid);
                clientMap.get(cp).breakCallBack(fid);
            }
        }
    }

    private void removeR(FID fid, long userId) throws RemoteException {
        if (FileSystemUtil.fileExist(fid, Parameter.VICE_DIR)) {
            if (fid.isDirectory()) {
                FileHandler handler = FileSystemUtil.readFile(fid, Parameter.VICE_DIR);
                if (handler != null) {
                    for (FID child : FileSystemUtil.getNameFIDMap(handler).values()) {
                        removeR(child, userId);
                    }
                }
            }
            removeCallback(fid, userId);
            FileSystemUtil.removeFile(fid, Parameter.VICE_DIR);
        }
    }

}
