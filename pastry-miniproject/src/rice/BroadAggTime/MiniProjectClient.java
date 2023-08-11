package rice.BroadAggTime;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;


import rice.environment.Environment;
import rice.environment.logging.Logger;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.CancellableTask;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.scribe.ScribeClient;
import rice.p2p.scribe.ScribeContent;
// import rice.p2p.scribe.ScribeImpl;
import rice.p2p.scribe.Topic;
import rice.p2p.scribe.rawserialization.JavaSerializedScribeContent;
import rice.p2p.scribe.rawserialization.RawScribeContent;
import rice.pastry.commonapi.PastryIdFactory;

public class MiniProjectClient implements ScribeClient {

    CancellableTask publishTask;
    protected ScribeImpl scribe;
    protected Topic topic;
    protected Logger logger;
    protected long kill_time = 2000000000000L;
    public Vector ids = new Vector();
    protected String filePath;
    // protected String filePath = "9MB.log";

    public MiniProjectClient(Node node) {
        this.scribe = new ScribeImpl(node, "instance");
        this.topic = new Topic(new PastryIdFactory(node.getEnvironment()), "SimpleAggr");
        this.scribe.setClient(this);
        this.logger = node.getEnvironment().getLogManager().getLogger(MiniProjectClient.class, null);
    }

    public MiniProjectClient(Node node, String modelSize) {
        this.scribe = new ScribeImpl(node, "instance");
        this.topic = new Topic(new PastryIdFactory(node.getEnvironment()), "SimpleAggr");
        this.scribe.setClient(this);
        this.logger = node.getEnvironment().getLogManager().getLogger(MiniProjectClient.class, null);
        this.filePath = modelSize;
    }

    /**
     * Subscribes to myTopic.
     */
    public void subscribe() {
        getScribeImpl().subscribe(this.topic, this);
    }
    
    
    /**
     * Written by Ezekiel for subTopic feature
     */
    public void testSubTopic(Topic prev) {
        getScribeImpl().subLink(prev, this.topic);
        //this will create some random topic that all of the client's topic will start subsribing to. 
    }


    /**
     * Starts the publish task.
     */
    public void startPublishTask() {
		try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            BroadcastContent content = new BroadcastContent(bytes);
            publishTask = getScribeImpl().getEndpoint().scheduleMessage(new BroadcastMessage(getScribeImpl().getEndpoint().getLocalNodeHandle(),this.topic, (ScribeContent)content), 500, 5000000);     
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // BroadcastContent content = new BroadcastContent("Publish BroadcastMessage to child nodes");
        // publishTask = getScribeImpl().getEndpoint().scheduleMessage(new BroadcastMessage(getScribeImpl().getEndpoint().getLocalNodeHandle(),this.topic, (ScribeContent)content), 500, 5000000);    
    }

    // public void startPublishTask(String filePath) {
	// 	try {
    //         byte[] bytes = Files.readAllBytes(Paths.get(filePath));
    //         BroadcastContent content = new BroadcastContent(bytes);
    //         publishTask = getScribeImpl().getEndpoint().scheduleMessage(new BroadcastMessage(getScribeImpl().getEndpoint().getLocalNodeHandle(),this.topic, (ScribeContent)content), 500, 5000000);    
    //     } catch (IOException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }
    //     // BroadcastContent content = new BroadcastContent("Publish BroadcastMessage to child nodes");
    //     // publishTask = getScribeImpl().getEndpoint().scheduleMessage(new BroadcastMessage(getScribeImpl().getEndpoint().getLocalNodeHandle(),this.topic, (ScribeContent)content), 500, 5000000);    
    // }

    public void sendMulticast(Message message, boolean clear_ids) {
        // System.out.println("Node "+this.scribe.getEndpoint().getLocalNodeHandle()+" broadcasting ");
        if (clear_ids) this.ids.clear();
        // this.ids.clear();
        this.logger.log("Publish BroadcastMessage to child nodes for topic " + this.topic+" at " + System.currentTimeMillis());
        // System.out.println("=================================================================");
        getScribeImpl().publish(((BroadcastMessage)message).getTopic(), ((BroadcastMessage)message).getContent()); 
        // seqNum++;
    }


    @Override
    public boolean anycast(Topic topic, ScribeContent content) {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public void deliver(Topic topic, ScribeContent content) {
        if(content instanceof BroadcastContent) {
            if(!this.getScribeImpl().isRoot(topic)) {
                this.logger.log("Node: " + getScribeImpl().getId().toStringFull()
                                + " received BroadcastContent \"" + content
                                + "\" from the root at "
                                + System.currentTimeMillis());
                try {
                    // Random rand = new Random();
                    // this.getScribeImpl().getEnvironment().getTimeSource().sleep(rand.nextInt(1000, 3000));
                    byte[] bytes = Files.readAllBytes(Paths.get(filePath));
                    UpdateContent upcontent = new UpdateContent(getScribeImpl().getEndpoint().getLocalNodeHandle(), topic, bytes);
                    upcontent.addId(getScribeImpl().getId().toStringFull());
                    ids = upcontent.getIdVector();
                    sendUpdate(topic, upcontent);
                    upcontent.clearBytes();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // UpdateContent upcontent = new UpdateContent(getScribeImpl().getEndpoint().getLocalNodeHandle(), topic);
                // upcontent.addId(getScribeImpl().getId().toStringFull());
                // ids = upcontent.getIdVector();
                // sendUpdate(topic, upcontent);
            }
        }
        if(content instanceof UpdateContent) {
            UpdateContent _content = (UpdateContent)content;
            updateData(_content.getTopic(), _content);
        }
    }
    //it is better find check leaf set and children => done
    public void updateData(Topic topic, UpdateContent content) {
        if(getScribeImpl().isRoot(topic)) {
            content.addId(getScribeImpl().getId().toStringFull());
            // try {
            //     content = updateKillTime(topic, content);
            // } catch (FileNotFoundException e) {
            //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            // }
            Vector _temp = content.getIdVector();
            // this.kill_time = (content.getKillTime() > this.kill_time) ? this.kill_time : content.getKillTime();
            for (int i = 0; i < _temp.size(); i++) {
                if(!ids.contains(_temp.get(i))) ids.add(_temp.get(i));
            }
            this.logger.log("Root received " + ids.size() 
                            + " ids at " + System.currentTimeMillis());
            content.clearBytes();
        }
        else {
            // since killed node might still receive broadcast msg from other VMs 
            // so killed nodes do nothing when receiving it.
            if (getScribeImpl().getParent(topic) == null) {
                logger.log("Node "+this.getScribeImpl().getId().toStringFull()+" has null parent");
            }
            // long killtimecheck = 2000000000000L;
            // if (content.getKillTime() >= killtimecheck) {
            //     try {
            //         content = updateKillTime(topic, content);
            //     } catch (FileNotFoundException e) {
            //         // TODO Auto-generated catch block
            //         e.printStackTrace();
            //     }
            // }
            else {
                // try {
                //     Random rand = new Random();
                //     this.getScribeImpl().getEnvironment().getTimeSource().sleep(rand.nextInt(1000, 3000));
                // } catch (InterruptedException e) {
                //     // TODO Auto-generated catch block
                //     e.printStackTrace();
                // }
                content.addId(getScribeImpl().getId().toStringFull());
                content.setSource(getScribeImpl().getEndpoint().getLocalNodeHandle());
                // this.ids.clear();
                sendUpdate(topic, content);
            }
            
            // ids = content.getIdVector();
        }
    }

    public void updateData(Topic topic, UpdateContent content, boolean setKillTime) {
        if(getScribeImpl().isRoot(topic)) {
            content.addId(getScribeImpl().getId().toStringFull());
            // try {
            //     content = updateKillTime(topic, content);
            // } catch (FileNotFoundException e) {
            //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            // }
            Vector _temp = content.getIdVector();
            this.kill_time = (content.getKillTime() > this.kill_time) ? this.kill_time : content.getKillTime();
            for (int i = 0; i < _temp.size(); i++) {
                if(!ids.contains(_temp.get(i))) ids.add(_temp.get(i));
            }
            // System.out.println("Root received " + ids.size() + " ids");
            this.logger.log("Root received " + ids.size() 
                            + " ids at " + System.currentTimeMillis());
            this.logger.log("The earliest kill time is "+ this.kill_time);
        }
        else {
            // since killed node might still receive broadcast msg from other VMs 
            // so killed nodes do nothing when receiving it.
            if (getScribeImpl().getParent(topic) == null) {
                logger.log("Node "+this.getScribeImpl().getId().toStringFull()+" has null parent");
            }
            // long killtimecheck = 2000000000000L;
            // if (content.getKillTime() >= killtimecheck) {
            //     try {
            //         content = updateKillTime(topic, content);
            //     } catch (FileNotFoundException e) {
            //         // TODO Auto-generated catch block
            //         e.printStackTrace();
            //     }
            // }
            else {
                if (setKillTime) {
                    try {
                        content = updateKillTime(topic, content);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                content.addId(getScribeImpl().getId().toStringFull());
                content.setSource(getScribeImpl().getEndpoint().getLocalNodeHandle());
                // this.ids.clear();
                sendUpdate(topic, content);
                content.clearBytes();
            }
            
            // ids = content.getIdVector();
        }
    }

    public void setKillTime(long new_kill_time) {
        this.kill_time = (this.kill_time >= new_kill_time) ? new_kill_time : this.kill_time;
    }

    public long getKillTIme() {
        return this.kill_time;
    }

    public UpdateContent updateKillTime(Topic topic, UpdateContent content) throws FileNotFoundException {
        String file ="main.log";
        long first_kill_time = 2000000000000L;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            while (true) {
                // System.out.println(reader.readLine());
                String currentLine = reader.readLine();
                if (currentLine == null) break;
                if (currentLine.contains("Kill node")) 
                {
                    String _temp_time = "";
                    for (int i = currentLine.length()-13; i < currentLine.length(); i++) {
                        _temp_time += currentLine.charAt(i);
                    }
                    first_kill_time = Long.parseLong(_temp_time);
                    break;
                }
            }
        // } catch (NumberFormatException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        content.setKillTime(first_kill_time);
        return content;
    }

    public void sendUpdate(Topic topic, ScribeContent content) {
        sendUpdate(topic, content instanceof RawScribeContent ? (RawScribeContent)content : new JavaSerializedScribeContent(content));
    }

    public void sendUpdate(Topic topic, RawScribeContent content) {
        this.logger.log("Node: " + getScribeImpl().getId().toStringFull()
                        + " sends UpdateContent \"" + content 
                        + "\" to parent node: " + getScribeImpl().getParent(topic)
                        + " at " + System.currentTimeMillis());
        getScribeImpl().getEndpoint().route(null, 
                                            new UpdateMessage(getScribeImpl().getEndpoint().getLocalNodeHandle(), topic, content), 
                                            getScribeImpl().getParent(topic));
        // try {
        //     getScribeImpl().getEnvironment().getTimeSource().sleep(100);
        // } catch (InterruptedException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
    }

    public Vector getNoReturnId(Map<String, Integer> map) {
        Vector _temp = new Vector();
        Object[] _temp_string = map.keySet().toArray();
        Object[] _temp_int = map.values().toArray();
        for (int i = 0; i < map.size(); i++) {
            if (!ids.contains(_temp_string[i])) _temp.add(_temp_int[i]);
        }
        return _temp;
    }

    @Override
    public void childAdded(Topic topic, NodeHandle child) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void childRemoved(Topic topic, NodeHandle child) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void subscribeFailed(Topic topic) {
        // TODO Auto-generated method stub
        
    }


    public boolean isRoot() {
        return getScribeImpl().isRoot(this.topic);
    }
      
    public NodeHandle getParent() {
    // NOTE: Was just added to the Scribe interface.  May need to cast myScribe to a
    // ScribeImpl if using 1.4.1_01 or older.
        return getScribeImpl().getParent(this.topic); 
    //return myScribe.getParent(myTopic); 
    }
      
    public NodeHandle[] getChildren() {
        return scribe.getChildren(this.topic); 
    }

    public Topic getTopic() {
        return this.topic;
    }

    public ScribeImpl getScribeImpl() {
        return this.scribe;
    }

}

