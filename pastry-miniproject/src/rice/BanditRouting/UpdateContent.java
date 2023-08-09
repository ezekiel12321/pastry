package rice.BanditRouting;

import java.util.HashMap;
import java.util.Vector;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.Topic;


public class UpdateContent implements ScribeContent{

    protected NodeHandle source;

    protected NodeHandle destination;

    protected Topic topic;

    protected double latency;

    // protected Vector idVec = new Vector();

    public UpdateContent(NodeHandle source, NodeHandle destination, Topic topic) {
        this.source = source;
        this.topic = topic;
        this.destination = destination;
        this.latency = 0;
        // this.idVec = idVec;
    }


    public Topic getTopic() {
        return this.topic;
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
