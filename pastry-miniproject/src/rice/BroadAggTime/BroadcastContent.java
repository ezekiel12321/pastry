package rice.BroadAggTime;

import java.util.Vector;

import rice.p2p.commonapi.Id;
import rice.p2p.scribe.ScribeContent;

public class BroadcastContent implements ScribeContent {

    String content;
	byte[] bytes;

    public BroadcastContent(String content) {
        this.content = content;
    }

    public BroadcastContent(byte[] bytes) {
        this.bytes = bytes;
    }

    public BroadcastContent(String content, byte[] bytes) {
        this.content = content;
        this.bytes = bytes;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {
        return content;
    }
    
}
