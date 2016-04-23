package com.company;

import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Random;
import java.util.Stack;

/**
 * Created by ahsan on 4/23/2016.
 */
public class PeerTwo {
    static PeerDHT peer = null;
    public static void main(String args[])
    {
        PeerAddress bootStrapServer = null;
        try
        {
            Random r = new Random(43L);
            peer = new PeerBuilderDHT(new PeerBuilder(new Number160(r)).ports(4001).behindFirewall().start()).start();
            if(!(args[0].isEmpty()))
                bootStrapServer = new PeerAddress(Number160.ZERO, InetAddress.getByName(args[0]), 4000, 4001);
            else
                bootStrapServer = new PeerAddress(Number160.ZERO, InetAddress.getLocalHost(), 4000, 4001);

            FutureDiscover fd = peer.peer().discover().peerAddress(bootStrapServer).start();
            System.out.println("** About to wait...");
            fd.awaitUninterruptibly();
            if (fd.isSuccess()) {
                System.out.println("*** My Address is... " + fd.peerAddress());
            } else {
                System.out.println("*** Failed... " + fd.failedReason());
            }

            bootStrapServer = fd.reporter();
            FutureBootstrap bootstrap = peer.peer().bootstrap().peerAddress(bootStrapServer).start();
            bootstrap.awaitUninterruptibly();
            if (!bootstrap.isSuccess()) {
                System.out.println("*** Could not Join...!");
            } else {
                System.out.println("*** Joined Successfully...");

            }
            System.out.println("Write something to network...");
            while (true)
            {
                //System.out.println("Write something to network...");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String anyData = br.readLine();
                if(!anyData.trim().isEmpty())
                {
                    int r2 = new Random().nextInt();
                    System.out.println("Storing DHT address (" + r2 + ") in DHT");

                    //txtConsole.setText(txtConsole.getText() + "\n" + message);
                    peer.add(new Number160(12345)).data(new Data(r2)).start().awaitUninterruptibly();
                    System.out.println("Adding your text to Network...");
                    //key = r2;
                    peer.put(new Number160(r2)).data(new Data(anyData)).start().awaitUninterruptibly();
                    System.out.println("added...");
                }

            }
        }
        catch (Exception exception)
        {
            System.out.println(exception.getMessage());
        }
    }

}
