package babel.demos;

import babel.Babel;
import network.INetwork;

import babel.demos.protocols.globalmembership.GlobalMembership;
import babel.notification.INotificationConsumer;
import babel.notification.ProtocolNotification;

import java.util.Map;
import java.util.Properties;

public class SimpleGlobalMembershipDemo implements INotificationConsumer {

    public SimpleGlobalMembershipDemo(String[] args) throws Exception {
        Babel babel = Babel.getInstance();
        Properties configProps = babel.loadConfig("network_config.properties", args);
        INetwork net = babel.getNetworkInstance();

        System.out.println(net.myHost());

        GlobalMembership membership = new GlobalMembership(net);
        membership.init(configProps);
        babel.registerProtocol(membership);

        Map<String,Short> notifications = membership.producedNotifications();

        for(Short s: notifications.values())
            membership.subscribeNotification(s, this);

        babel.start();
    }

    public static void main(String[] args) throws Exception {
        SimpleGlobalMembershipDemo m = new SimpleGlobalMembershipDemo(args);
    }


    @Override
    public void deliverNotification(ProtocolNotification notification) {
        System.out.println(notification);
    }
}
