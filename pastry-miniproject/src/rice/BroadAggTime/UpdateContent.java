package rice.BroadAggTime;

import java.util.Vector;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.Topic;


public class UpdateContent implements ScribeContent{

    protected NodeHandle source;

    protected Topic topic;

    protected Vector idVec = new Vector();

    protected Long kill_time = 2000000000000L;

	byte[] bytes;

    // protected Long kill_time = 1964996891221;

    public UpdateContent(NodeHandle source, Topic topic) {
        this.source = source;
        this.topic = topic;
        // this.idVec = idVec;
    }

    public UpdateContent(NodeHandle source, Topic topic, byte[] bytes) {
        this.source = source;
        this.topic = topic;
        this.bytes = bytes;
        // this.idVec = idVec;
    }

    public Topic getTopic() {
        return this.topic;
    }

    public void setSource(NodeHandle source) {
        this.source = source;
    }

    public NodeHandle getSource() {
        return this.source;
    }

    public void addId(String fullId) {
        if(!this.idVec.contains(fullId)) this.idVec.add(fullId);
    }

    public long getKillTime() {
        return this.kill_time;
    }

    public void setKillTime(long new_kill_time) {
        this.kill_time = (this.kill_time >= new_kill_time) ? new_kill_time : this.kill_time;
    }

    public Integer getNumIds() {
        return this.idVec.size();
    }

    public Vector getIdVector() {
        return this.idVec;
    }

    public void clearBytes() {
        this.bytes = null;
    }

    public String toString() {
        return idVec.toString();
    }
}
