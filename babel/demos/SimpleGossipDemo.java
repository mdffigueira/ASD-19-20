package babel.demos;

import babel.Babel;
import babel.demos.protocols.besteffortbcast.GossipBCast;
import babel.notification.INotificationConsumer;
import babel.notification.ProtocolNotification;
import network.INetwork;
import babel.demos.protocols.besteffortbcast.notifications.BCastDeliver;
import babel.demos.protocols.besteffortbcast.requests.BCastRequest;
import babel.demos.protocols.globalmembership.GlobalMembership;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;

public class SimpleGossipDemo implements INotificationConsumer {

    public SimpleGossipDemo(String[] args) throws Exception {
        Babel babel = Babel.getInstance();
        Properties configProps = babel.loadConfig("network_config.properties", args);
        INetwork net = babel.getNetworkInstance();

        //Define protocols
        GlobalMembership membership = new GlobalMembership(net);
        membership.init(configProps);

        GossipBCast bCast = new GossipBCast(net);
        bCast.init(configProps);

        //Register protocols
        babel.registerProtocol(membership);
        babel.registerProtocol(bCast);

        //subscribe to notifications
        bCast.subscribeNotification(BCastDeliver.NOTIFICATION_ID, this);

        //start babel runtime
        babel.start();

        //Application Logic
        Random r = new Random();
        int sequenceNumber = 1;

        while(true) {
            Thread.sleep(1000);

            if(r.nextDouble() < 0.5) {
                byte[] msg = (net.myHost().toString() + " " + sequenceNumber).getBytes(StandardCharsets.UTF_8);
                BCastRequest bcastrequest = new BCastRequest(msg);
                bCast.deliverRequest(bcastrequest);
                sequenceNumber++;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SimpleGossipDemo m = new SimpleGossipDemo(args);
    }


    @Override
    public void deliverNotification(ProtocolNotification notification) {
        BCastDeliver deliver = (BCastDeliver) notification;
        System.out.println("Received: " + new String(deliver.getMessage(), StandardCharsets.UTF_8));
    }
}
