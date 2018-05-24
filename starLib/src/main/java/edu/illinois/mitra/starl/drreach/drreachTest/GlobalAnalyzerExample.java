package edu.illinois.mitra.starl.drreach.drreachTest;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GlobalAnalyzerExample {
    /**
     * This global analyzer collects data from distributed queues to analyze the data
     */

    public static void main(String[] args){

        BlockingQueue<Integer> q1 = new ArrayBlockingQueue<Integer>(1);    // queue for agent 1
        BlockingQueue<Integer> q2 = new ArrayBlockingQueue<Integer>(1);    // queue for agent 2

        HashMap<String, BlockingQueue> queue_manager = new HashMap<>(); // create a manager to manage all distributed queues
        queue_manager.put("Agent1", q1);
        queue_manager.put("Agent2", q2);

        GlobalAnalyzer analyzer = new GlobalAnalyzer(queue_manager);

        Task agent1 = new Task("demo task 1", q1);
        Task agent2 = new Task("demo task 2", q2);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

        System.out.println("The time is : " + new Date());

        executor.scheduleAtFixedRate(agent1, 1, 5, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(agent2, 1, 5, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(analyzer, 1, 5, TimeUnit.SECONDS);

        try {
            TimeUnit.SECONDS.sleep(20);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();

    }
}
