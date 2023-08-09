package rice.BanditRouting;

import java.util.HashMap;
import java.util.Vector;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.Topic;


public class ResponseContent implements ScribeContent{

    protected NodeHandle source;

    protected NodeHandle destination;

    protected Topic topic;

    protected double latency;

    protected boolean ifRoot;

    // protected Vector idVec = new Vector();

    public ResponseContent(NodeHandle source, NodeHandle destination, Topic topic, double latency) {
        this.source = source;
        this.topic = topic;
        this.destination = destination;
        this.latency = latency;
        this.ifRoot = false;
    }


    public Topic getTopic() {
        return this.topic;
    }

    public boolean getIfRoot() {
        return this.ifRoot;
    }

    public void setIfRoot(boolean ifRoot) {
        this.ifRoot = ifRoot;
    }

    public void setSource(NodeHandle source) {
        this.source = source;
    }

    public void setDestination(NodeHandle destination) {
        this.destination = destination;
    }

    public double getLatency() {
        return this.latency;
    }

    public void setLatency(double latency) {
        this.latency = latency;
    }

    public void addLatency(double latency) {
        this.latency += latency;
    }

    public NodeHandle getSource() {
        return this.source;
    }

    public NodeHandle getDestination() {
        return this.destination;
    }

    // public void addId(Id newId) {
    //     if(!this.idVec.contains(newId)) this.idVec.add(newId);
    // }

    // public Integer getNumIds() {
    //     return this.idVec.size();
    // }

    // public Vector getIdVector() {
    //     return this.idVec;
    // }

    // public String toString() {
    //     return idVec.toString();
    // }

    public String toString() {
        return String.valueOf(this.latency);
    }

}
