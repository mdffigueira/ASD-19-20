package dissemination;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.UUID;

import dht.Node;

public class Topic {

    private Node upStream;
    private TreeSet<Node> nodes;
    private HashMap<Node, UUID> timers;

    public Topic(Node resp) {
        this.upStream = resp;
        this.nodes = new TreeSet<Node>();
        this.timers = new HashMap<Node, UUID>();
    }

    public Topic(Node resp, TreeSet<Node> nodes) {
        this.upStream = resp;
        this.nodes = nodes;
    }

    public void addTimer(Node node, UUID timer) {
        timers.put(node, timer);
    }

    public UUID getTimer(Node node) {
        return timers.get(node);
    }

    public Node getUpStream() {
        return upStream;
    }

    public TreeSet<Node> getNodes() {
        return nodes;
    }

    public boolean nodeExists(Node nodeID) {
        return nodes.contains(nodeID);
    }

    public void addNode(Node nodeID) {
        nodes.add(nodeID);
    }

    public int removeNode(Node nodeID) {
        nodes.remove(nodeID);
        return nodes.size();
    }

    public void setUpStream(Node nodeID) {
        upStream = nodeID;
    }


}
