package edu.illinois.mitra.starl.drreach.drreachTest;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

class Task implements Runnable
{
    private String name;
    private int task_value = 0;

    protected BlockingQueue<Integer> queue = null;

    public Task(String name, BlockingQueue<Integer> queue) {
        this.name = name;
        this.queue = queue;
    }

    @Override
    public void run()
    {
        try {
            System.out.println("Doing a task during : " + name + " - Time - " + new Date());
            task_value += 1;
            queue.put(task_value);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        ;
    }
}