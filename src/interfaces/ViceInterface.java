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
    FileHandler fetch(FID fid, int userId) throws RemoteException;

    void store(FID fid, FileHandler handler, int userId) throws RemoteException;

    FID create(int userId) throws RemoteException;

    FID makeDir(int userId) throws RemoteException;

    void remove(FID fid, int userId) throws RemoteException;

    void setLock(FID fid, LockMode mode, int userId) throws RemoteException;

    void releaseLock(FID fid, int userId) throws RemoteException;

    void removeCallback(FID fid, int userId) throws RemoteException;

    /**
     * For Server Callback
     */
    void register(String rmi) throws RemoteException;
}

