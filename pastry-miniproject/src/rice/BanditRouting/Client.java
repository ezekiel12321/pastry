package rice.BanditRouting;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import rice.BanditRouting.BanditClient.*;
import rice.environment.logging.Logger;
import rice.p2p.commonapi.CancellableTask;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.NodeHandleSet;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.Topic;
import rice.p2p.scribe.rawserialization.JavaSerializedScribeContent;
import rice.p2p.scribe.rawserialization.RawScribeContent;
import rice.pastry.commonapi.PastryIdFactory;

public class Client implements BanditClient {

    protected String file_path;
    protected HashMap<String, Integer> receivedWordMap = new HashMap<String, Integer>();
    protected HashMap<NodeHandle, Double> Q_value = new HashMap<NodeHandle, Double>();
    protected HashMap<NodeHandle, Double> UCB_value = new HashMap<NodeHandle, Double>();
    protected HashMap<NodeHandle, Integer> action_selected = new HashMap<NodeHandle, Integer>();
    protected int time_step;
    CancellableTask publishTask;
    protected ScribeImpl scribe;
    protected Topic topic;
    protected Logger logger;
    protected int latencyMean;
    protected NodeHandle rootNodeHandle;

    public Client(Node node, int maxLatency) {
        this.scribe = new ScribeImpl(node, "instance");
        this.topic = new Topic(new PastryIdFactory(node.getEnvironment()), "SimpleAggr");
        this.scribe.setClient(this);
        this.logger = node.getEnvironment().getLogManager().getLogger(BanditClient.class, null);
        this.time_step = 0;
        this.rootNodeHandle = null;
        setLatencyMean(maxLatency);

    }

    private void setLatencyMean(int maxLatency) {
        Random rand = new Random();
        this.latencyMean = rand.nextInt(maxLatency) + 1;
    }

    /**
     * Subscribes to myTopic.
     */
    public void subscribe() {
        this.scribe.subscribe(this.topic, this); 
    }

    /**
     * Starts the publish task.
     */
    public void startPublishTask() {
        BroadcastContent content = new BroadcastContent("Publish BroadcastMessage to child nodes");
        publishTask = this.getEndpoint().scheduleMessage(new BroadcastMessage(this.scribe.getEndpoint().getLocalNodeHandle(),this.topic, (ScribeContent)content), 1000, 1500000);    
    }

    /**
     * Publish root's neighbor sets to all the nodes in the same tree.
     */
    public void startPublishTask_neighboring_set() {
        // first get the neighboring set of root node.
        NodeHandleSet neighboring_set = this.getEndpoint().neighborSet(999);
        // add itself into the neighboring set since we need to consider which one is better.
        neighboring_set.putHandle(this.getLocalNodeHandle());
        BroadcastContent content = new BroadcastContent("Publish BroadcastMessage to child nodes", neighboring_set);
        publishTask = this.getEndpoint().scheduleMessage(new BroadcastMessage(this.scribe.getEndpoint().getLocalNodeHandle(),this.topic, (ScribeContent)content), 1000, 1500000);    
    }


    public void sendMulticast(Message message) {
        this.logger.log("Publish BroadcastMessage to child nodes for topic"+this.topic+" at "+System.currentTimeMillis());
        this.scribe.publish(((BroadcastMessage)message).getTopic(), ((BroadcastMessage)message).getContent()); 
    }

    @Override
    public boolean anycast(Topic topic, ScribeContent content) {
        return false;
    }

    @Override
    public void deliver(Topic topic, ScribeContent content) {
        if (content instanceof BroadcastContent) {
            // non-neighboring set nodes should start finding new routing paths to the root.
            NodeHandleSet _temp_neighboring_set = ((BroadcastContent)content).getNeighboringSet();
            if(_temp_neighboring_set.getHandle(this.getId()) == null) {
                this.logger.log("Node: " + this.getId() + " is NOT in neighboring set. Start finding alternative routing paths.");
                // if time step = 0, initialize action_selected, and Q values to be 0.
                if (this.time_step == 0) init_ucb_algorithm(_temp_neighboring_set);
                NodeHandle selected_arm = selectActionBasedOnUCB();
                UpdateContent updateContent = new UpdateContent(this.getLocalNodeHandle(), selected_arm, this.topic);
                sendUpdate(topic, selected_arm, updateContent);

                // we want to check routing latency to the root.
                if (selected_arm != this.rootNodeHandle && this.rootNodeHandle != null) {
                    UpdateContent updateContenttoRoot = new UpdateContent(this.getLocalNodeHandle(), this.rootNodeHandle, this.topic);
                    sendUpdate(topic, this.rootNodeHandle, updateContenttoRoot);
                }
            }
            else {
                // if neighboring set nodes, then they init a hashmap to know themselves are neighboring set nodes.
                if (this.time_step == 0) init_ucb_algorithm(_temp_neighboring_set);
                this.time_step += 1;
                this.logger.log("Node: " + this.getId() + " is IN neighboring set. Drop the broadcast content.");
            }
        }
        // Two cases: 1. forwarding nodes receive updatedata, 2. final neighboring sets receives updatedata
        // In the first case, forwarding nodes add latency values to the contenct and send it to next node.
        // In the second case, neighboring sets send direct messages back to the source nodes so that they can update UCB values.
        if (content instanceof UpdateContent) {
            UpdateContent _content = (UpdateContent) content;
            this.updateData(_content.getTopic(), _content);
        }

        if (content instanceof ResponseContent) {
            this.logger.log("Node: " + this.getId() + " received ResponseContent from node "+ ((ResponseContent)content).getSource().getId());
            ResponseContent _content = (ResponseContent) content;
            boolean ifWriteToLog = true;
            updateUCB(_content, ifWriteToLog);
        }


    }

    public void sendBackUpdate(Topic topic, NodeHandle destination, ScribeContent content) {
        this.sendBackUpdate(topic, content instanceof RawScribeContent ? (RawScribeContent)content : new JavaSerializedScribeContent(content), destination);
    }

    public void sendBackUpdate(Topic topic, RawScribeContent content, NodeHandle nh) {
        this.logger.log("Node: " + this.getId()
                    + " sends back ResponseContent \"" + content 
                    + "\" to node with id: " + nh.getId().toStringFull());
        // by specifying hint in route function, the message would send back to the source node directly.
        this.getEndpoint().route(null, 
                                new ResponseMessage(this.getLocalNodeHandle(), topic, content), 
                                nh);
    }



    @Override
    public void sendUpdate(Topic topic, ScribeContent content) {
        this.sendUpdate(topic, null, content instanceof RawScribeContent ? (RawScribeContent)content : new JavaSerializedScribeContent(content));
    }

    public void sendUpdate(Topic topic, NodeHandle destination, ScribeContent content) {
        this.sendUpdate(topic, destination, content instanceof RawScribeContent ? (RawScribeContent)content : new JavaSerializedScribeContent(content));
    }

    public void sendUpdate(Topic topic, NodeHandle nh, RawScribeContent content) {
        if (nh == null) {
            this.logger.log("Node: " + this.getId()
                        + " sends UpdateContent \"" + content 
                        + "\" to parent node: " + this.getParent(topic));
            this.getEndpoint().route(null, 
                                    new UpdateMessage(this.getLocalNodeHandle(), topic, content), 
                                    this.getParent(topic));
        }
        else {
            this.logger.log("Node: " + this.getId()
                        + " sends UpdateContent \"" + content 
                        + "\" to node with id: " + nh.getId().toStringFull());
            this.getEndpoint().route(nh.getId(), 
                                    new UpdateMessage(this.getLocalNodeHandle(), topic, content), null);
        }
    }

    @Override
    public void childAdded(Topic topic, NodeHandle child) {
        
    }

    @Override
    public void childRemoved(Topic topic, NodeHandle child) {
        this.logger.log("Child: "+child.getId()+" removed.");
    }

    @Override
    public void subscribeFailed(Topic topic) {
        this.logger.log("Failed to subscribe Topic: "+topic.getId());
    }

    @Override
    public Topic getTopic() {
        return this.topic;
    }

    @Override
    public ScribeImpl getScribeImpl() {
        return this.scribe;
    }

    @Override
    public void updateData(Topic topic, UpdateContent content){
        NodeHandle nh = content.getDestination();
        Id destination_id = nh.getId();
        // first, if this node finds itself in the neighboring set and destination of the updatemessage is itself, 
        // send direct message back to the source node
        if(this.getId() == destination_id && this.Q_value.containsKey(this.getLocalNodeHandle())) {
            this.logger.log("Neighboring set node received a message from node: " + content.getSource().getId().toStringFull());
            content.addLatency(gaussianRandom((double)latencyMean, 0.1));
            ResponseContent _content = new ResponseContent(this.getLocalNodeHandle(), content.getSource(), topic, content.getLatency());
            _content.setIfRoot(this.isRoot(topic));
            sendBackUpdate(topic, content.getSource(), _content);
        }
        // second, if this node finds itself in the neighboring set but not destination of the updatemessage, 
        // it might be the forwarder node for this updatemessage. Then, add some latency and forward it to next hop.
        else if (this.getId() != destination_id && this.Q_value.containsKey(this.getLocalNodeHandle())) {
            this.logger.log("Neighboring set node received a message from node: " + content.getSource().getId().toStringFull()
                            + ". This node is not the destination of the message. Start forwarding this message.");
            content.addLatency(gaussianRandom((double)latencyMean, 0.1));
            sendUpdate(topic, nh, content);
        }
        // third, if this node doesn't find itself in the neighboring set neither destination of the updatemessage,
        // it is the forwarder node for this updatemessage. Then, add some latency and forward it to next hop.
        else if (this.getId() != destination_id && !this.Q_value.containsKey(this.getLocalNodeHandle())) {
            this.logger.log("Non-neighboring set node received a message from node: " + content.getSource().getId().toStringFull()
                            + ". Start forwarding this message.");
            content.addLatency(gaussianRandom((double)latencyMean, 0.1));
            sendUpdate(topic, nh, content);
        }
        else {
            try {
                throw new Exception("This node is the destination but not in neighboring set. Something went wrong");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isRoot(Topic topic) {
        return this.scribe.isRoot(this.topic);

    }

    @Override
    public NodeHandle getParent(Topic topic) {
        return this.scribe.getParent(this.topic); 
    }

    @Override
    public NodeHandle[] getChildren(Topic topic) {
        return this.scribe.getChildren(this.topic);
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public Id getId() {
        return this.scribe.getId();
    }

    @Override
    public NodeHandle getLocalNodeHandle() {
        return this.scribe.getEndpoint().getLocalNodeHandle();
    }

    @Override
    public Endpoint getEndpoint() {
        return this.scribe.getEndpoint();
    }


    private void init_ucb_algorithm(NodeHandleSet neighboring_set) {
        this.logger.log("Init UCB values.");
        int size = neighboring_set.size();
        for(int i = 0; i < size; i++) {
            NodeHandle nh = neighboring_set.getHandle(i);
            this.Q_value.put(nh, 0.);
            this.action_selected.put(nh, 1);
            this.UCB_value.put(nh, Double.MAX_VALUE);
        }
    }

    private NodeHandle selectActionBasedOnUCB() {
        NodeHandle largestKey = null;
        double largestValue = Double.NEGATIVE_INFINITY;

        for (Map.Entry<NodeHandle, Double> entry : UCB_value.entrySet()) {
            NodeHandle key = entry.getKey();
            double value = entry.getValue();

            if (value > largestValue) {
                largestKey = key;
                largestValue = value;
            }
        }
        return largestKey;
    }

    private double gaussianRandom(double mean, double stdDev) {
        Random rand = new Random();
        return mean + rand.nextGaussian() * stdDev;
    }

    private void updateUCB(ResponseContent content, boolean ifWriteToLog) {
        NodeHandle source_neighboring_node = content.getSource();
        double latency = content.getLatency();
        if (content.getIfRoot()) {
            this.logger.log("[Root]: Latency at Time step " + this.time_step + " is "+ String.format("%.2f", latency));
            this.rootNodeHandle = content.getSource();
        }
        
        else this.logger.log("Latency at Time step " + this.time_step + " is "+ String.format("%.2f", latency));
        int currentValue = this.action_selected.get(source_neighboring_node);
        this.action_selected.put(source_neighboring_node, currentValue+1);
        double currentQValue = this.Q_value.get(source_neighboring_node);
        this.Q_value.put(source_neighboring_node, (currentQValue+latency)/(currentValue+1));
        this.time_step += 1;


        // get new Q values to update UCB. Please note that *-1 is because we want to select lowerst latency. 
        for (NodeHandle key: this.UCB_value.keySet()) {
            double logT = Math.log((double)this.time_step);
            // if the key is equal source node, then update new Q values.
            if (key.equals(source_neighboring_node)) {
                double newQValue = (currentQValue+latency)/(currentValue+1)*-1.;
                int newActionNumber = currentValue+1;
                double ucbValue = newQValue + 1.0 * Math.sqrt(logT/ newActionNumber);
                this.UCB_value.put(source_neighboring_node, ucbValue);

                DecimalFormat df = new DecimalFormat("#.####");
                this.UCB_value.put(source_neighboring_node, Double.parseDouble(df.format(ucbValue)));
            }
            // else Q values stay the same but increases exploration values.
            else {
                if (this.UCB_value.get(key) < Double.MAX_VALUE) {
                    double oldQValue = this.Q_value.get(key)*-1.;
                    double oldActionNumber = this.action_selected.get(key);
                    double ucbValue = oldQValue + 1.0 * Math.sqrt(logT/ oldActionNumber);
                    this.UCB_value.put(key, ucbValue);
                    DecimalFormat df = new DecimalFormat("#.####");
                    this.UCB_value.put(key, Double.parseDouble(df.format(ucbValue)));
                }

            }
        }
        

        if (ifWriteToLog) {
            this.logger.log("==================================");
            this.logger.log("UCB Values:");
            this.logger.log(this.UCB_value.toString());
            this.logger.log("==================================");
            this.logger.log("Times of arms selected:");
            this.logger.log(this.action_selected.values().toString());
            this.logger.log("==================================");
        }
        this.logger.log("Finished updating UCB values for " +this.UCB_value.size()+ " neighboring set nodes plus root.");
    }


}

