package clients;

import babel.Babel;
import babel.notification.INotificationConsumer;
import babel.notification.ProtocolNotification;
import floodbcast.FloodBCast;
import floodbcast.delivers.FloodBCastDeliver;
import floodbcast.requests.FloodBCastRequest;
import hyparview.HyParViewMembership;
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

public class InteractiveClient implements INotificationConsumer {

	public static final String EXIT = "exit";
	public static final String SUBSCRIBE = "subscribe";
	public static final String PUBLISH = "publish";
	public static final String UNSUBSCRIBE = "unsubscribe";

	public InteractiveClient(String[] args) throws Exception {

		Babel babel = Babel.getInstance();
		Properties configProps = babel.loadConfig("network_config.properties", args);
		INetwork net = babel.getNetworkInstance();

		//Define protocols
		HyParViewMembership membership = new HyParViewMembership(net);
		membership.init(configProps);

		FloodBCast bCast = new FloodBCast(net);
		bCast.init(configProps);
		
	    PublishSubscribe ps = new PublishSubscribe(net);
	    ps.init(configProps);
	    ps.wait();


		//Register protocols
		babel.registerProtocol(membership);
		babel.registerProtocol(bCast);
		babel.registerProtocol(ps);

		//subscribe to notifications
		ps.subscribeNotification(PSDeliver.NOTIFICATION_ID, this);

		//start babel runtime
		babel.start();

		//Application Logic
		Scanner in = new Scanner(System.in);
		String command = (in.nextLine()).toLowerCase();
		while (!command.equalsIgnoreCase(EXIT)) {
			if (!command.equals("")) {
				String[] cmd = command.split(" ");
				switch (cmd[0]) {
				case SUBSCRIBE: 
					PSSubscribeRequest pssubReq = new PSSubscribeRequest(cmd[1].getBytes(StandardCharsets.UTF_8));
					ps.deliverRequest(pssubReq);
					break;
				case UNSUBSCRIBE: ;
					PSUnsubscribeRequest psunsubReq = new PSUnsubscribeRequest(cmd[1].getBytes(StandardCharsets.UTF_8));
					ps.deliverRequest(psunsubReq);
					break;
				case PUBLISH: ;
					PSPublishRequest pspubReq = new PSPublishRequest(cmd[1].getBytes(StandardCharsets.UTF_8), cmd[2].getBytes(StandardCharsets.UTF_8));
					ps.deliverRequest(pspubReq);
				break;
				case EXIT:
					System.exit(1);
				default:
					break;
				}
			}
			command = in.nextLine();
		}
	}

	public static void main(String[] args) throws Exception {
		InteractiveClient m = new InteractiveClient(args);

	}

	static {
		System.setProperty("log4j.configurationFile", "log4j.xml");
	}

	@Override
	public void deliverNotification(ProtocolNotification notification) {
		FloodBCastDeliver deliver = (FloodBCastDeliver) notification;
		System.out.println("Received event: Topic: " +  new String(deliver.getTopic(), StandardCharsets.UTF_8) + " Message: " + new String(deliver.getMessage(), StandardCharsets.UTF_8));
	}
}
