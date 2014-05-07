package vice;

import data.FID;
import data.FileHandler;
import data.LockMode;
import data.Parameter;
import interfaces.VenusInterface;
import interfaces.ViceInterface;
import util.DataTypeUtil;
import util.FileSystemUtil;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Dawnwords on 2014/5/4.
 */
public class Vice extends UnicastRemoteObject implements ViceInterface {

    private HashMap<Long, VenusInterface> clientMap;
    private AtomicInteger uniquifier;

    protected Vice() throws RemoteException {
        FileSystemUtil.initRootDir();
        clientMap = new HashMap<Long, VenusInterface>();
        uniquifier = new AtomicInteger(FileSystemUtil.getStartUniquifier());
    }

    @Override
    public void register(String rmi) {
        try {
            VenusInterface venus = (VenusInterface) Naming.lookup(rmi);
            long userId = DataTypeUtil.longHash(rmi);
            clientMap.put(userId, venus);
            Log.getInstance().i("userId:%d register", userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public FileHandler fetch(FID fid, long userId) throws RemoteException {
        Log.getInstance().i("userId:%d fetch:%s", userId, fid);
        return FileSystemUtil.readFile(fid, Parameter.VICE_DIR);
    }

    @Override
    public void store(FID fid, FileHandler handler, long userId) throws RemoteException {
        // TODO check callback promise
        Log.getInstance().i("userId:%d store:%s", userId, fid);
        FileSystemUtil.writeFile(fid, handler, Parameter.VICE_DIR);
    }

    @Override
    public FID create(long userId) throws RemoteException {
        FID fid = FileSystemUtil.createFile(userId, uniquifier.getAndIncrement(), false);
        Log.getInstance().i("userId:%d create file %s", userId, fid);
        return fid;
    }

    @Override
    public FID makeDir(long userId) throws RemoteException {
        FID fid = FileSystemUtil.createFile(userId, uniquifier.getAndIncrement(), true);
        Log.getInstance().i("userId:%d make directory %s", userId, fid);
        return fid;
    }

    @Override
    public void remove(FID fid, long userId) throws RemoteException {

    }

    @Override
    public void setLock(FID fid, LockMode mode, long userId) throws RemoteException {

    }

    @Override
    public void releaseLock(FID fid, long userId) throws RemoteException {

    }

    @Override
    public void removeCallback(FID fid, long userId) throws RemoteException {

    }

}
