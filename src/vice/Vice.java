package vice;

import data.FID;
import data.FileHandler;
import data.LockMode;
import data.Parameter;
import interfaces.VenusInterface;
import interfaces.ViceInterface;
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

    private HashMap<Integer, VenusInterface> clientMap;
    private AtomicInteger uniquifier;

    protected Vice() throws RemoteException {
        FileSystemUtil.initRootDir();
        clientMap = new HashMap<Integer, VenusInterface>();
        uniquifier = new AtomicInteger(FileSystemUtil.getStartUniquifier());
    }

    @Override
    public void register(String rmi) {
        try {
            VenusInterface venus = (VenusInterface) Naming.lookup(rmi);
            int userId = rmi.hashCode();
            clientMap.put(userId, venus);
            Log.getInstance().i("userId:%d register", userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public FileHandler fetch(FID fid, int userId) throws RemoteException {
        Log.getInstance().i("userId:%d fetch:%s", userId, fid);
        return FileSystemUtil.readFile(fid, Parameter.VICE_DIR);
    }

    @Override
    public void store(FID fid, FileHandler handler, int userId) throws RemoteException {
        // TODO check callback promise
        Log.getInstance().i("userId:%d store:%s", userId, fid);
        FileSystemUtil.writeFile(fid, handler, Parameter.VICE_DIR);
    }

    @Override
    public FID create(int userId) throws RemoteException {
        FID fid = FileSystemUtil.createFile(userId, uniquifier.getAndIncrement(), false);
        Log.getInstance().i("userId:%d create file %s", userId, fid);
        return fid;
    }

    @Override
    public FID makeDir(int userId) throws RemoteException {
        FID fid = FileSystemUtil.createFile(userId, uniquifier.getAndIncrement(), true);
        Log.getInstance().i("userId:%d make directory %s", userId, fid);
        return fid;
    }

    @Override
    public void remove(FID fid, int userId) throws RemoteException {

    }

    @Override
    public void setLock(FID fid, LockMode mode, int userId) throws RemoteException {

    }

    @Override
    public void releaseLock(FID fid, int userId) throws RemoteException {

    }

    @Override
    public void removeCallback(FID fid, int userId) throws RemoteException {

    }

}
