package interfaces;

import data.FID;
import data.FileHandler;
import data.Lock;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Server Vice Interface
 * <p/>
 * Created by Dawnwords on 2014/5/4.
 */
public interface ViceInterface extends Remote {
    /**
     * Basic 8 Components of Vice Service Interface
     */
    FileHandler fetch(FID fid, long userid) throws RemoteException;

    void store(FID fid, FileHandler handler, long userid) throws RemoteException;

    FID create(long userid) throws RemoteException;

    FID makeDir(FID parent, long userid) throws RemoteException;

    void remove(FID fid, long userid) throws RemoteException;

    boolean setLock(FID fid, Lock.LockMode mode, long userid) throws RemoteException;

    void releaseLock(FID fid, long userid) throws RemoteException;

    void removeCallback(FID fid, long userid) throws RemoteException;

    /**
     * For Server Callback
     */
    void register(String rmi) throws RemoteException;
}

