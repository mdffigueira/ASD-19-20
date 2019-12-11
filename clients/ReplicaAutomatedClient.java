package clients;

import babel.Babel;
import babel.notification.INotificationConsumer;
import babel.notification.ProtocolNotification;
import dissemination.Dissemination;
import floodbcast.FloodBCast;
import floodbcast.delivers.FloodBCastDeliver;
import floodbcast.requests.FloodBCastRequest;
import hyparview.HyParViewMembership;
import multipaxos.MultiPaxos;
import network.INetwork;
import publishsubscribe.PublishSubscribe;
import publishsubscribe.delivers.PSDeliver;
import publishsubscribe.requests.PSPublishRequest;
import publishsubscribe.requests.PSSubscribeRequest;
import publishsubscribe.requests.PSUnsubscribeRequest;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class ReplicaAutomatedClient implements INotificationConsumer {

    public static final String EXIT = "exit";
    public static final String SUBSCRIBE = "subscribe";
    public static final String PUBLISH = "publish";
    public static final String UNSUBSCRIBE = "unsubscribe";
    public static final String[] topics = {"ASD", "E", "A", "MELHOR", "CADEIRA", "DE", "SEMPRE"};
    public static final String[] message = {"qrqwrqwe", "adasdadadasqweq", "asdasdasdasdawq", "iniojmiqj", "aqnmicaxm", "mioqwn ieounqw", "nipasndaips"};

    public ReplicaAutomatedClient(String[] args) throws Exception {
        Babel babel = Babel.getInstance();
        Properties configProps = babel.loadConfig("network_config.properties", args);
        INetwork net = babel.getNetworkInstance();

        //Define protocols
        // HyParViewMembership hyParV = new HyParViewMembership(net);

        MultiPaxos mp = new MultiPaxos(net);
        mp.init(configProps);
        babel.registerProtocol(mp);
        PublishSubscribe ps = new PublishSubscribe(net);
        ps.init(configProps);
        // hyParV.init(configProps);

        //FloodBCast bCast = new FloodBCast(net);
        //bCast.init(configProps);

        // PublishSubscribe ps = new PublishSubscribe(net);
        //ps.init(configProps);


        //Register protocols

        babel.registerProtocol(ps);
        // babel.registerProtocol(hyParV);
        // babel.registerProtocol(bCast);
        //   babel.registerProtocol(ps);

        //subscribe to notifications
        //   ps.subscribeNotification(PSDeliver.NOTIFICATION_ID, this);

        //start babel runtime
        babel.start();

        //Application Logic
        //subscribe

        while (true) {
//
//            int ns1 = new Random().nextInt(topics.length);
//            PSSubscribeRequest pssubReq1 = new PSSubscribeRequest(topics[ns1].getBytes(StandardCharsets.UTF_8));
//            //  ps.deliverRequest(pssubReq1);
//            int ns2 = ns1;
//            while (ns2 == ns1) {
//                ns2 = new Random().nextInt(topics.length);
//            }
//            PSSubscribeRequest pssubReq2 = new PSSubscribeRequest(topics[ns2].getBytes(StandardCharsets.UTF_8));
//            //ps.deliverRequest(pssubReq2);
//            //unsubscribe
//            PSUnsubscribeRequest psunsubReq1 = new PSUnsubscribeRequest(topics[ns1].getBytes(StandardCharsets.UTF_8));
//            // ps.deliverRequest(psunsubReq1);
//            //publish
//            int np3 = new Random().nextInt(topics.length);
//            PSPublishRequest pspubReq = new PSPublishRequest(topics[np3].getBytes(StandardCharsets.UTF_8), message[np3].getBytes(StandardCharsets.UTF_8));
//            //ps.deliverRequest(pspubReq);
//            Thread.sleep(1000);
        }


    }


    public static void main(String[] args) throws Exception {
        ReplicaAutomatedClient m = new ReplicaAutomatedClient(args);

    }

    static {
        System.setProperty("log4j.configurationFile", "log4j.xml");
    }

    @Override
    public void deliverNotification(ProtocolNotification notification) {
        FloodBCastDeliver deliver = (FloodBCastDeliver) notification;
        System.out.println("Received event: Topic: " + new String(deliver.getTopic(), StandardCharsets.UTF_8) + " Message: " + new String(deliver.getMessage(), StandardCharsets.UTF_8));
    }
}