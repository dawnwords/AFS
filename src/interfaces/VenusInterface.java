package interfaces;

import data.FID;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Client Venus Interface
 * <p/>
 * Created by Dawnwords on 2014/5/6.
 */
public interface VenusInterface extends Remote {
    void breakCallBack(FID fid) throws RemoteException;
}
