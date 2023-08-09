package rice.BanditRouting.BanditClient;

import rice.p2p.scribe.ScribeClient;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.Topic;
import rice.environment.logging.Logger;
import rice.BanditRouting.ScribeImpl;
import rice.BanditRouting.UpdateContent;
import rice.p2p.commonapi.*;

/**
 * @(#) MiniProjectClient.java
 *
 * This interface represents a client using the Scribe system.
 *
 * @version $Id: MiniProjectClient.java 2022-10-11 7:18:28Z ccw $
 * @author Cheng-Wei Ching
 */
public interface BanditClient extends ScribeClient {

    /**
     * This method is called when the client receives update message 
     * for a topic which this client is interested in. 
     * @param topic The topic the message was sent to
     * @param content The content which was sent
     */
    public void updateData(Topic topic, UpdateContent content);

    /**
     * Send the update message to the parent node.
     * @param topic The topic the message is going to send to
     * @param cotent The content needed to be sent
     */
    public void sendUpdate(Topic topic, ScribeContent content);

    /**
     * @param topic The topic the client is interested in
     * @return Whether or not the cient is the root of the topic
     */
    public boolean isRoot(Topic topic);

    /**
     * @param topic The topic the client is interested in 
     * @return NodeHandle of the parent node
     */
    public NodeHandle getParent(Topic topic);

    /**
     * @param topic The topic the client is interested in
     * @return Array of NodeHandles of the child nodes
     */
    public NodeHandle[] getChildren(Topic topic);

    /**
     * @return NodeHandle of the client
     */
    public NodeHandle getLocalNodeHandle();

    /**
     * @return Endpoint of the client
     */
    public Endpoint getEndpoint();

    /**
     * @return ScribeImpl of the client
     */
    public ScribeImpl getScribeImpl();

    /**
     * @return Logger of the client
     */
    public Logger getLogger();

    /**
     * @return Topic the client is interested in
     */
    public Topic getTopic();

    /**
     * @return Id of the client
     */
    public Id getId();


}
