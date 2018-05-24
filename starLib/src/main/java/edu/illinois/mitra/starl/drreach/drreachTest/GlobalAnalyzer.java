package edu.illinois.mitra.starl.drreach.drreachTest;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.BlockingQueue;


public class GlobalAnalyzer implements Runnable {

    public boolean global_safe = true;
    public HashMap<String, Integer> distributed_reach_sets = new HashMap<>();
    public HashMap<String, BlockingQueue> queue_manager = new HashMap<String, BlockingQueue>();

    public GlobalAnalyzer(HashMap<String, BlockingQueue> queue_manager){
        this.queue_manager = queue_manager;
    }

    @Override
    public void run(){

        read_distributed_reach_set();

    }

    // read all queues, save information to distributed_reach_sets

    public HashMap<String, Integer> getDistributed_reach_sets() {
        return distributed_reach_sets;
    }

    public HashMap<String, Integer> read_distributed_reach_set(){

        Set<String> keys = queue_manager.keySet();


        for (String key:keys){

            BlockingQueue<Integer> queue = queue_manager.get(key);

            try {

                Integer task_value = queue.take();
                distributed_reach_sets.put(key, task_value);
                System.out.print(String.format("Reading queue %s \n", key));
                System.out.print(String.format("%s value is %d \n", key, task_value));

            }
            catch (Exception e) {
                e.printStackTrace();
            }


        }

        return distributed_reach_sets;

    }

}
