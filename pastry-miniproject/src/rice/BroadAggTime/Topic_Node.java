package rice.BroadAggTime;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.Array;
import java.util.*;

import rice.*;
import rice.environment.Environment;
import rice.environment.logging.Logger;
import rice.environment.params.Parameters;
import rice.p2p.commonapi.*;
import rice.p2p.commonapi.rawserialization.*;
import rice.p2p.scribe.*;
import rice.p2p.scribe.javaserialized.JavaScribeContentDeserializer;
import rice.p2p.scribe.maintenance.MaintainableScribe;
import rice.p2p.scribe.maintenance.ScribeMaintenancePolicy;
import rice.p2p.scribe.messaging.*;
import rice.p2p.scribe.rawserialization.*;

public class Topic_Node {
    private List<Topic> children;
    private Topic topic;

    public Topic_Node(Topic parent) {
        
        topic = parent;
        children = new ArrayList<Topic>();
    }
    
    public void addChild(Topic topic){
        children.add(topic);
    }

    public void removeChild(Topic topic) {
        int index = 0;
        for (Topic t : children) {
            if (t.equals(topic)) {
                children.remove(index);
                return;
            }
            index += 1;
        }
    }
    
    public List<Topic> getChildren() {
        return children;
    }

    public Topic getTopic() {
        return topic;
    }

    public String toString() {
        return children.toString();
    }
}
