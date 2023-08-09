package rice.BanditRouting;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.NodeHandleSet;
import rice.p2p.scribe.ScribeContent;

public class BroadcastContent implements ScribeContent {

    String content;
    NodeHandleSet neighboring_set;

    public BroadcastContent(String content) {
        this.content = content;
    }

    public BroadcastContent(String content, NodeHandleSet neighboring_set) {
        this.content = content;
        this.neighboring_set = neighboring_set;
    }

    public String getContent() {
        return this.content;
    }



    public NodeHandleSet getNeighboringSet() {
        return this.neighboring_set;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setNeighboringSet(NodeHandleSet neighboring_set) {
        this.neighboring_set = neighboring_set;
    }

    public String toString() {
        return content;
    }
    
}
