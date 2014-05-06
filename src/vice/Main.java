package vice;

import data.Parameter;
import interfaces.ViceInterface;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Created by Dawnwords on 2014/5/4.
 */
public class Main {

    public static void main(String[] args) {
        try {
            ViceInterface vice = new Vice();
            LocateRegistry.createRegistry(Parameter.PORT);
            Naming.rebind(Parameter.RMI_URL, vice);
            System.out.println("Vice Started!");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
