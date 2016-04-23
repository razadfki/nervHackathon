package com.company;

import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import sun.rmi.runtime.Log;

import java.net.ConnectException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Random;

public class Main {

    public static void main(String[] args) {

        boolean serverFlag = true;
        int keyStore = 12345;

        try{
            //do something
            PeerDHT peer = null;
            try {
                Random r = new Random(42L);
                Bindings b = new Bindings();
                b.addAddress(InetAddress.getLocalHost());
                // b.addAddress(InetAddress.getByAddress(addr));
                System.out.println("Starting distributed back-end...");
                peer = new PeerBuilderDHT(new PeerBuilder(new Number160(r)).bindings(b).ports(4000).start()).start();
                System.out.println("Started and listening now at " + peer.peerAddress().inetAddress().getHostAddress());
                //createTable();
                FutureGet fget;
                while(serverFlag) {
                    Thread.sleep(5000);
                    FutureGet fg = peer.get(new Number160(keyStore)).all().start();
                    fg.awaitUninterruptibly();
                    int size = fg.dataMap().size();
                    //System.out.println("size: " + size);


                    StringBuffer getMessage = new StringBuffer();
                    Iterator<Data> iterator = fg.dataMap().values().iterator();
                    //peer.remove(new Number160(key)).start().awaitUninterruptibly();
                    while (iterator.hasNext()) {
                        Data d = iterator.next();
                        fget = peer.get(new Number160(((Integer) d.object()).intValue())).start();
                        fget.awaitUninterruptibly();
                        if(fget.data()!=null)
                        {
                            System.out.println("size: " + size);

                            //TODO: Write in DataBase using JDBC Driver
                            dumpData(fget.data().object().toString());
                            //TODO: Delete uncomment here
                            System.out.println("got: " + (getMessage.append(fget.data().object().toString()).toString()));
                            peer.remove(new Number160(keyStore)).all().start();
                        }
                    }
                }

            } finally {
                if(peer!=null)
                    peer.shutdown();
            }
        }
        catch (Exception ex){}
    }

    private static void dumpData(String message)
    {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = getConnection();
            c.setAutoCommit(false);

            stmt = c.createStatement();
            String sql = "INSERT INTO dumpsensordata (SensorData) "
                    + "VALUES ( '" + message+ "');";

            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        }
    }

    private static Connection getConnection(){
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/Hackthon",
                            "postgres", "postgresql");
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            return null;
        }
    }
    private static void createTable()
    {
        Connection c = null;
        Statement stmt = null;
        try {
            c = getConnection();
            stmt = c.createStatement();
            String sql = "CREATE TABLE DumpSensorData " +
                    "(Id INT PRIMARY KEY     ," +
                    " SensorData TEXT     ;";
            c.setAutoCommit(false);
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
            System.out.println("Table created...");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }

    }

}
