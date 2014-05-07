package interfaces;

import data.FID;
import data.FileHandler;
import data.LockMode;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Dawnwords on 2014/5/4.
 */
public interface ViceInterface extends Remote {
    /**
     * Basic 8 Components of Vice Service Interface
     */
    FileHandler fetch(FID fid, long userid) throws RemoteException;

    void store(FID fid, FileHandler handler, long userid) throws RemoteException;

    FID create(long userid) throws RemoteException;

    FID makeDir(long userid) throws RemoteException;

    void remove(FID fid, long userid) throws RemoteException;

    void setLock(FID fid, LockMode mode, long userid) throws RemoteException;

    void releaseLock(FID fid, long userid) throws RemoteException;

    void removeCallback(FID fid, long userid) throws RemoteException;

    /**
     * For Server Callback
     */
    void register(String rmi) throws RemoteException;
}

