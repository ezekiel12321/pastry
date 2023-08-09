package rice.BanditRouting;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import rice.environment.Environment;
import rice.p2p.commonapi.Node;
// import rice.p2p.commonapi.testing.CommonAPITest;
import rice.p2p.commonapi.NodeHandle;
// import rice.environment.logging.Logger;

public class BanditSimulator extends CommonAPITest {

    public static String INSTANCE = "SimAggTest";
    // public Logger logger;

    public BanditSimulator(Environment env) throws IOException{
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
        
    }

    

    @Override
    protected void runTest() {

        for (int j = 0; j < 500; j++) {
            for(int i = 0; i < apps.size(); i++) {
                Client _temp = (Client)apps.get(i);
                if(((Client)apps.get(i)).isRoot(_temp.topic)) {
                    this.logger.log("Root is "+_temp.getId()+" and number "+i);
                    _temp.startPublishTask_neighboring_set();
                    break;
                }
            }
            try {
                this.environment.getTimeSource().sleep(10000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        
    }

    public static void main(String args[]) throws IOException {
		//args==NULL, take the args from default freepastry in "\dedrs\bin"
		Environment env = parseArgs(args);

		BanditSimulator simAggSimulator = new BanditSimulator(env);
		/*
	    env.getSelectorManager().getTimer().schedule(new TimerTask() {
	    	public void run() {
	    		simulator.stop();
	    	}
	    }, 50000);*/
		// start is the function defined in commonapi class and it calls runtest
		simAggSimulator.start();
		env.destroy();
	}
}
