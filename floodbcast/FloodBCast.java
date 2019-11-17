package floodbcast;

import babel.exceptions.DestinationProtocolDoesNotExist;
import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolMessageHandler;
import babel.handlers.ProtocolReplyHandler;
import babel.handlers.ProtocolRequestHandler;
import babel.handlers.ProtocolTimerHandler;
import babel.protocol.GenericProtocol;
import babel.protocol.event.ProtocolMessage;
import babel.requestreply.ProtocolReply;
import babel.requestreply.ProtocolRequest;
import babel.timer.ProtocolTimer;
import floodbcast.delivers.FloodBCastDeliver;
import floodbcast.messages.FloodBCastProtocolMessage;
import floodbcast.requests.FloodBCastRequest;
import floodbcast.timers.AntiEntropyTimer;
import floodbcast.messages.AntiEntropyMessage;
import hyparview.HyParViewMembership;
import hyparview.replys.HyParViewMembershipReply;
import hyparview.requests.HyParViewMembershipRequest;
import hyparview.timers.ShuffleTimer;
import network.Host;
import network.INetwork;

import java.util.*;

public class FloodBCast extends GenericProtocol {

    public final static short PROTOCOL_ID = 300;
    public final static String PROTOCOL_NAME = "Flood BCast";

    //Parameters
    private int fanout;

    //Protocol State
    private Map<UUID,FloodBCastProtocolMessage> delivered;
    private Set<Host> currentAV;
    private Map<UUID, FloodBCastProtocolMessage> pending;

    public FloodBCast(INetwork net) throws HandlerRegistrationException {
        super("FloodBCast", PROTOCOL_ID, net);

        //Register Events

        //Requests
        registerRequestHandler(FloodBCastRequest.REQUEST_ID, uponFloodBCastRequest);

        //Replies
        registerReplyHandler(HyParViewMembershipReply.REPLY_ID, uponHyParViewMembershipReply);

        //Notifications Produced
        registerNotification(FloodBCastDeliver.NOTIFICATION_ID, FloodBCastDeliver.NOTIFICATION_NAME);

        //Messages
        registerMessageHandler(FloodBCastProtocolMessage.MSG_CODE, uponBcastProtocolMessage, FloodBCastProtocolMessage.serializer);

        //Timers
        registerTimerHandler(AntiEntropyTimer.TimerCode, uponAntiEntropyTimer);

    }

    private ProtocolTimerHandler uponAntiEntropyTimer = new ProtocolTimerHandler() {

        @Override
        public void uponTimer(ProtocolTimer protocolTimer) {
            antyEntropy();
        }
    };

    public void antyEntropy() {
        if (!currentAV.isEmpty()) {
            Host h = randomNode(currentAV);
            Set<UUID> knownMessages= new HashSet<>();
                knownMessages.addAll(delivered.keySet());
            AntiEntropyMessage msg = new AntiEntropyMessage(knownMessages);
            sendMessage(msg, h);
        }

    }

    private ProtocolMessageHandler uponAntyEntropyMessage = new ProtocolMessageHandler() {
        public void receive(ProtocolMessage m) {
            AntiEntropyMessage msg = (AntiEntropyMessage) m;
            for(UUID d :delivered.keySet())
                if(!msg.getKnownMessages().contains(d))
                    sendMessage(delivered.get(d),msg.getFrom());


        }
    };


    @Override
    public void init(Properties props) {
        //Load parameters
        fanout = Short.parseShort(props.getProperty("fanout", "3"));

        //Initialize State
        this.delivered = new HashMap<>();
        this.pending = new HashMap<>();
        this.currentAV= new TreeSet<>();

        //setup timers
        UUID shuffleTimerUUID = setupPeriodicTimer(new ShuffleTimer(), 1000, 30000);
    }


    private ProtocolRequestHandler uponFloodBCastRequest = new ProtocolRequestHandler() {

        @Override
        public void uponRequest(ProtocolRequest r) {
            //Create Message
            FloodBCastRequest req = (FloodBCastRequest) r;
            FloodBCastProtocolMessage message = new FloodBCastProtocolMessage(myself, req.getTopic(), req.getMessage());

            FloodBCastDeliver deliver = new FloodBCastDeliver(message.getPayload(), message.getTopic());
            triggerNotification(deliver);
            delivered.put(message.getMessageId(),message);
            pending.put(message.getMessageId(), message);
            //Get which peers will send the message to
            getPeers(message.getMessageId());
        }
    };

    //orange
    private ProtocolReplyHandler uponHyParViewMembershipReply = new ProtocolReplyHandler() {

        @Override
        public void uponReply(ProtocolReply reply) {

            HyParViewMembershipReply rep = (HyParViewMembershipReply) reply;

            //Send message to sample.
            //FloodBCastProtocolMessage msg = pending.remove(rep.getRequestID());
            currentAV.clear();
            currentAV.addAll(rep.getPeers());
            removeOneElem(currentAV);
            for (FloodBCastProtocolMessage p : pending.values()) {
                FloodBCastProtocolMessage msg = p;
                for (Host h : currentAV) {
                    if (!h.equals(p.getOwner())) {
                        sendMessage(msg, h);
                    }
                }
                pending.clear();
            }
        }
    };

    private ProtocolMessageHandler uponBcastProtocolMessage = new ProtocolMessageHandler() {


        @Override
        public void receive(ProtocolMessage m) {
            FloodBCastProtocolMessage msg = (FloodBCastProtocolMessage) m;

            //check if message was already observed, ignore if yes
            //System.out.println(delivered.contains(msg.getMessageId()));
            if (!delivered.keySet().contains(msg.getMessageId())) {
                //Deliver message
                delivered.put(msg.getMessageId(),msg);
                FloodBCastDeliver deliver = new FloodBCastDeliver(msg.getPayload(), msg.getTopic());
                triggerNotification(deliver);
                pending.put(msg.getMessageId(), msg);
                //Create Request for peers (in the membership)
                getPeers(msg.getMessageId());
            }
        }
    };

    private void getPeers(UUID sender) {
        HyParViewMembershipRequest reqPeers = new HyParViewMembershipRequest(sender);
        reqPeers.setDestination(HyParViewMembership.PROTOCOL_ID);
        try {
            sendRequest(reqPeers);
        } catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
            destinationProtocolDoesNotExist.printStackTrace();
            System.exit(1);
        }
    }

    private Set<Host> removeOneElem(Set<Host> set) {
        int rand = new Random().nextInt(set.size());
        int counter = 0;
        for (Host h : set) {
            if (counter == rand) {
                set.remove(h);
                break;
            }
            counter++;
        }
        return set;

    }

    private Host randomNode(Set<Host> s) {
        int number = new Random().nextInt(s.size());
        int count = 0;
        Host aux = null;
        for (Host h : s) {
            if (number == count) {
                aux = h;
                break;
            }
            count++;
        }
        return aux;
    }
}
