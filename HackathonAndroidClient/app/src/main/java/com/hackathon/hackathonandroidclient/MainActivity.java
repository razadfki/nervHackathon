package com.hackathon.hackathonandroidclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import java.net.InetAddress;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static PeerDHT peer = null;
    int keyStore = 12345;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScrollView mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mScrollView.fullScroll(View.FOCUS_DOWN);
    }

    public void Connect(View view)
    {
        try
        {
            final String txtIp = ((EditText)findViewById(R.id.txtIp)).getText().toString();
            Random r = new Random(43L);
            peer = new PeerBuilderDHT(new PeerBuilder(new Number160(r)).ports(4001).behindFirewall().start()).start();
            PeerAddress bootStrapServer = new PeerAddress(Number160.ZERO, InetAddress.getByName(txtIp), 4000, 4000);
            FutureDiscover fd = peer.peer().discover().peerAddress(bootStrapServer).start();
            System.out.println("About to wait...");
            fd.awaitUninterruptibly();
            if (fd.isSuccess()) {
                System.out.println("*** FOUND THAT MY OUTSIDE ADDRESS IS " + fd.peerAddress());
            } else {
                System.out.println("*** FAILED " + fd.failedReason());
            }
            bootStrapServer = fd.reporter();
            FutureBootstrap bootstrap = peer.peer().bootstrap().peerAddress(bootStrapServer).start();
            bootstrap.awaitUninterruptibly();
            if (!bootstrap.isSuccess()) {
                System.out.println("*** COULD NOT BOOTSTRAP!");
            } else {
                System.out.println("*** SUCCESSFUL BOOTSTRAP");
            }
        }
        catch (Exception ex)
        {
            Log.e("Connection: " , ex.getMessage());
        }

        }

        public void sendToNetwork(View view)
        {
            try
            {
                EditText msg = (EditText)findViewById(R.id.txtMsg);
                String msgToSend = msg.getText().toString();
                int r2 = new Random().nextInt();

                TextView txtConsol = (TextView)findViewById(R.id.txtConsol);
                txtConsol.append("Storing DHT address (" + r2 + ") in DHT");
                txtConsol.append("Adding (" + msgToSend + ") to DHT");

                peer.add(new Number160(keyStore)).data(new Data(r2)).start().awaitUninterruptibly();

                peer.put(new Number160(r2)).data(new Data(msgToSend)).start().awaitUninterruptibly();
                msg.setText("");
            }
            catch (Exception ex)
            {
                Log.e("Sending: ",ex.getMessage());
            }
        }

}
