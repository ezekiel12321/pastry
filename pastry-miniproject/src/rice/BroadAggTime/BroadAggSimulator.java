package rice.BroadAggTime;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;


import rice.environment.Environment;
import rice.p2p.commonapi.Node;
import rice.pastry.PastryNode;

public class BroadAggSimulator extends CommonAPITest {

    public static String INSTANCE = "SimAggTest";
    // public Logger logger;

    public BroadAggSimulator(Environment env) throws IOException{
        super(env);

    }

    public void setupParams(Environment env) {
        super.setupParams(env);
        // we want to see if messages are dropped because not ready
    //    if (!env.getParameters().contains("rice.p2p.scribe.ScribeImpl@ScribeRegrTest_loglevel"))
    //      env.getParameters().setInt("rice.p2p.scribe.ScribeImpl@ScribeRegrTest_loglevel",Logger.INFO);
        
        // want to retry fast because of problems with isReady()
        env.getParameters().setInt("p2p_scribe_message_timeout",3000); 
      }

    @Override
    protected void processNode(int num, Node node) {
        // TODO Auto-generated method stub
        
    }


    

    // ================= DO NOT DELETE =================
    @Override
    protected void runTest() {

        int global_round = 1;
        boolean hasRoot = false;

        // if VM has root, the root in the VM should check the msgs from other nodes and VMs.
        if (getRoot(false) != null) {
            hasRoot = true;
            logger.log("hasRoot is " + true);
            // MiniProjectClient _temp = (MiniProjectClient) apps.get(root_idx);
            while (global_round > 0) {
                MiniProjectClient root_client = getRoot(true);
                root_client.startPublishTask();
                // simulate(8); // 0.25s * 8 = 2s
                global_round--;
            }
        }
        // If VM does not has root, it should keep its nodes running until the root makes sure failure recovery is done.
        // else {
        //     // long first_kill_time = (Long) obj[0];
        //     logger.log("hasRoot is " + false);
        //     simulate(5);
        //     while (global_round > 0) {
        //         int rand = environment.getRandomSource().nextInt(apps.size());
        //         if (apps.get(rand) != null) {
        //             MiniProjectClient _tem_client = ((MiniProjectClient)apps.get(rand));
        //             UpdateContent upcontent = new UpdateContent(_tem_client.getScribeImpl().getEndpoint().getLocalNodeHandle(), 
        //                                                         _tem_client.getTopic());
        //             // upcontent.setKillTime(first_kill_time);
        //             logger.log("Node "+_tem_client.getScribeImpl().getId()+" sent kill time msg to the root");
        //             _tem_client.updateData(_tem_client.getTopic(), upcontent);
        //         }
        //         simulate(5);
        //         global_round--;
        //     }
        //     // long first_kill_time = (Long) obj[0];
        //     // logger.log("The ")
        // }
        if (hasRoot) simulate(500);
        else simulate(480);
        
    }
    // ================= DO NOT DELETE =================

    // protected void runTest() {
    //     long first_kill_time = killNode_Dis(getRoot(false) == null ? true : false);
    //     int global_round = 50;
    //     simulate(5);
    //     while (global_round > 0) {
    //         int rand = environment.getRandomSource().nextInt(apps.size());
    //         if (nodes[rand] != null && first_kill_time < 2000000000000L) {
    //             MiniProjectClient _tem_client = ((MiniProjectClient)apps.get(rand));
    //             UpdateContent upcontent = new UpdateContent(_tem_client.getScribeImpl().getEndpoint().getLocalNodeHandle(), 
    //                                                         _tem_client.getTopic());
    //             upcontent.setKillTime(first_kill_time);
    //             logger.log("Node "+_tem_client.getScribeImpl().getId().toStringFull()+" sent kill time msg to the root");
    //             _tem_client.updateData(_tem_client.getTopic(), upcontent);
    //         }
    //         for (int i = 0; i < apps.size(); i++) {
    //             if (nodes[i] != null) {
    //                 MiniProjectClient _tem_client = ((MiniProjectClient)apps.get(i));
    //                 UpdateContent upcontent = new UpdateContent(_tem_client.getScribeImpl().getEndpoint().getLocalNodeHandle(), 
    //                                                         _tem_client.getTopic());
    //                 _tem_client.updateData(_tem_client.getTopic(), upcontent, false);
    //             }
    //         }
    //         simulate(2);
    //         global_round--;
    //     }

    // }

    /**
     * this method is for the distributed environment.
     * @return True if the VM has root, false otherwise.
     */
    // protected boolean checkIfVMhasRoot() {
    //     Iterator _ite = apps.iterator();
    //     while (_ite.hasNext()) {
    //         if (((MiniProjectClient)_ite.next()).isRoot()) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    protected MiniProjectClient getRoot(boolean write_log) {
        // Iterator _ite = apps.iterator();
        // int idx = 0;
        // while (_ite.hasNext()) {
        //     MiniProjectClient _temp = (MiniProjectClient) _ite.next();
        //     if (_temp.isRoot()) {
        //         if (write_log) logger.log("Root is "+_temp.getScribeImpl().getId()+" and port is "+(8000+idx));
        //         return _temp;
        //     }
        //     idx++;
        // }
        // return null;
        for (int i = 0; i < apps.size(); i++) {
            MiniProjectClient _temp = (MiniProjectClient) apps.get(i);
            if (_temp.isRoot()){
                if (write_log) logger.log("Root is "+_temp.getScribeImpl().getId().toStringFull()+" and port is "+(i));
                return _temp;
            }
        }
        return null;
    }

    /**
     * this method is for killing node in the distributed environment.
     * @param hasRoot if this VM has root
     * @return if hasRoot is true, return 1. Integer root_index, 2. Long first_kill_time
     *         if hasRoot is false, return first_kill_time
     */
    protected Long killNode_Dis(boolean hasRoot) {
        if (hasRoot) return killNode();
        else {
            int killed_num_nodes = 0;
            Vector killed_node_seq = new Vector();
            long first_kill_time = 2000000000000L;
            for (;killed_num_nodes < NUM_KILL_NODES;) {
                int rand = environment.getRandomSource().nextInt(NUM_NODES);
                if (apps.get(rand) != null) {
                    MiniProjectClient _temp = (MiniProjectClient) apps.get(rand);
                    if (killed_node_seq.contains(rand) || _temp.isRoot()) continue;
                    if (killed_num_nodes == 0) {
                        first_kill_time = System.currentTimeMillis();
                    }
                    logger.log("Kill node: " + _temp.getScribeImpl().getId().toStringFull()+" with port "+(8000+rand)+" at "+System.currentTimeMillis());
                    ((MiniProjectClient)apps.get(rand)).getScribeImpl().destroy();
                    nodes[rand].getEnvironment().getLogManager().getLogger(PastryNode.class, null).log("Killed at "+System.currentTimeMillis());
                    // id_seq.remove(nodes[rand].getId().toStringFull());
                    ((PastryNode)nodes[rand]).destroy();
                    nodes[rand] = null;
                    // apps.remove(_temp);6
                    killed_num_nodes++;
                    killed_node_seq.add(rand);
                }
            }
            return first_kill_time;
        }

    }
    
    protected Long killNode() {
        // note that when running on the distributed environment with one single tree, the root is only in one VM.
        int killed_num_nodes = 0;
        long first_kill_time = 2000000000000L;
        Vector killed_node_seq = new Vector();
        for (;killed_num_nodes < NUM_KILL_NODES;){
            int rand = environment.getRandomSource().nextInt(NUM_NODES);
            if (apps.get(rand) != null) {
                MiniProjectClient _temp = (MiniProjectClient) apps.get(rand);
                if (_temp.isRoot()) continue;
                else {
                    if (killed_node_seq.contains(rand)) continue;
                    if (killed_num_nodes == 0) {
                        first_kill_time = System.currentTimeMillis();
                    }
                    logger.log("Kill node: "+_temp.getScribeImpl().getId().toStringFull()+" with port "+(8000+rand)+" at "+System.currentTimeMillis());
                    nodes[rand].getEnvironment().getLogManager().getLogger(PastryNode.class, null).log("Killed at "+System.currentTimeMillis());
                    // id_seq.remove(nodes[rand].getId().toStringFull());
                    ((MiniProjectClient)apps.get(rand)).getScribeImpl().destroy();
                    ((PastryNode)nodes[rand]).destroy();
                    // apps.remove(_temp);
                    killed_num_nodes++;
                    killed_node_seq.add(rand);
                }
            }
        }
        // if (!root_find) {
        //     for (int i = 0; i < apps.size(); i ++) {
        //         MiniProjectClient _temp = (MiniProjectClient) apps.get(i);
        //         if(_temp.isRoot()) {
        //             root_Index = i;
        //             logger.log("Root is "+_temp.getScribeImpl().getId()+" and port is "+(8000+i));
        //             break;
        //         }
        //         // clients[i].endpoint.scheduleMessage(new MaintenanceMessage(), 3000, 3000);
        //         // endpoint.scheduleMessage(new MaintenanceMessage(), environment.getRandomSource().nextInt(60000), 5000);
        //     }
        // }
        return first_kill_time;
    }

    public static void main(String args[]) throws IOException {
        //args==NULL, take the args from default freepastry in "\dedrs\bin"
		System.out.println("WEEHOE");
        
        Environment env = parseArgs(args);

		BroadAggSimulator FailSimulator = new BroadAggSimulator(env);
		/*
	    env.getSelectorManager().getTimer().schedule(new TimerTask() {
	    	public void run() {
	    		simulator.stop();
	    	}
	    }, 50000);*/
        // start is the function defined in commonapi class and it calls runtest
        
		FailSimulator.start();
		env.destroy();

        // String [] cmd = {"python", "calculate_msg_hop.py", Integer.toString(scribeTest.NUM_NODES), Integer.toString(scribeTest.NUM_KILL_NODES)};
        // ProcessBuilder pb = new ProcessBuilder().command(cmd);
        // try {
        // Process p = pb.start();
        // BufferedReader in = new BufferedReader(
        //         new InputStreamReader(p.getInputStream()));
        // StringBuilder buffer = new StringBuilder();
        // String line = null;
        // while ((line = in.readLine()) != null){           
        // buffer.append(line);
        // }
        // int exitcode = p.waitFor();
        // System.out.println(buffer.toString());                
        // } catch (IOException e) {
        // System.out.println(e);
        // e.printStackTrace();
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
	}
}
