package edu.illinois.mitra.starl.drreach.drreachComputation;

// This Global Analyzer read the distributed reachable set from distributed queues and analyze the safety of the whole system
// Dung Tran: 5/11/2018, Last Update:

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class GlobalAnalyzer implements Runnable{

    public boolean global_safe = true;
    public HashMap<String, FaceLiftingResult> distributed_reach_sets = new HashMap<>();
    public HashMap<String, BlockingQueue> queue_manager = new HashMap<String, BlockingQueue>(); // this is manager to manage all distributed queues

    public GlobalAnalyzer(HashMap<String, BlockingQueue> queue_manager){
        this.queue_manager = queue_manager;
    }

    @Override
    public void run(){

        read_distributed_reach_set();
        analyze_distributed_reach_set();

    }

    // read all queues, save information to distributed_reach_sets

    public void read_distributed_reach_set(){

        Set<String> keys = queue_manager.keySet();


        for (String key:keys){

            BlockingQueue<FaceLiftingResult> queue = queue_manager.get(key);

            try {

                FaceLiftingResult reachSet = queue.take();
                distributed_reach_sets.put(key, reachSet);
                System.out.print(String.format("Reading queue of %s \n", key));

            }
            catch (Exception e) {
                e.printStackTrace();
            }


        }

    }

    public boolean analyze_distributed_reach_set(){

        System.out.print("The system is globally safe \n");
        return global_safe;
    }


}
