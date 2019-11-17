package dissemination;

import java.util.TreeSet;

import dht.Node;

public class Topic {

	Node responsibleNode;
	TreeSet<Node> nodes;

	public Topic(Node resp) {
		this.responsibleNode = resp;
		this.nodes = new TreeSet<Node>();
	}

	public Topic(Node resp, TreeSet<Node> nodes) {
		this.responsibleNode = resp;
		this.nodes = nodes;
	}

	public Node getResponsible() {
		return responsibleNode;
	}

	public TreeSet<Node> getNodes(){
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

	public void setResponsible(Node nodeID) {
		responsibleNode = nodeID;
	}


}
