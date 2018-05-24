package edu.illinois.mitra.starl.drreach.drreachTest;

// Test face lifting method
// Dung Tran: 5/14/2018

import net.sourceforge.interval.ia_math.RealInterval;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.illinois.mitra.starl.drreach.drreachComputation.ComputationSetting;
import edu.illinois.mitra.starl.drreach.drreachComputation.FaceLifting;
import edu.illinois.mitra.starl.drreach.drreachComputation.FaceLiftingResult;
import edu.illinois.mitra.starl.drreach.drreachComputation.GlobalAnalyzer;
import edu.illinois.mitra.starl.drreach.drreachComputation.HyperRectangle;
import edu.illinois.mitra.starl.drreach.drreachComputation.Interval;
import edu.illinois.mitra.starl.drreach.drreachComputation.LiftingSettings;
import edu.illinois.mitra.starl.drreach.drreachComputation.UnsafeSet;

public class Test_FaceLifting {

    /**
     * @input : initial set, unsafe set
     * @output: face lifting result object
     */

    public static void main(String[] arg){

        // initial set for linear pendulum benchmark
        double[] min_vec = {-0.1, 0.85, 0, 0};
        double[] max_vec = {-0.09, 0.86, 0, 0};
        HyperRectangle init_rect = new HyperRectangle(Interval.vector2intervals(min_vec, max_vec));

        // unsafe set: velocity <= 0.4, x = [pos_err velocity angle angular_vel]^T

        ComputationSetting cs = new ComputationSetting();
        Double NegInfinity = cs.DBL_MIN;
        Double PosInfinity = cs.DBL_MAX;
        RealInterval velocity_const = new RealInterval(NegInfinity, 0.4);
        UnsafeSet unsafe_set = new UnsafeSet(1, velocity_const);

        // queue to contain face-lifting result
        BlockingQueue<FaceLiftingResult>  queue = null;

        // lifting setting
        double initialStepSize = 0.01;
        double reachTime = 1.0;
        long max_runtime_milliseconds = 50;
        int dynamics_index = 0; // linear pendulum dynamics
        double max_rect_width_before_error = 100;

        LiftingSettings setting = new LiftingSettings(init_rect, reachTime, initialStepSize, max_rect_width_before_error, max_runtime_milliseconds, dynamics_index, unsafe_set);

        // create queue and manage queue for distributed reach set

        BlockingQueue<FaceLiftingResult> q1 = new ArrayBlockingQueue<FaceLiftingResult>(5);    // queue for agent 1
        BlockingQueue<FaceLiftingResult> q2 = new ArrayBlockingQueue<FaceLiftingResult>(5);    // queue for agent 2

        HashMap<String, BlockingQueue> queue_manager = new HashMap<>(); // create a manager to manage all distributed queues
        queue_manager.put("Agent1", q1);
        queue_manager.put("Agent2", q2);

        GlobalAnalyzer analyzer = new GlobalAnalyzer(queue_manager);

        FaceLifting Agent1 = new FaceLifting(setting, q1);
        FaceLifting Agent2 = new FaceLifting(setting, q2);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        System.out.println("Computing reach set in real-time at : " + new Date());

        // execute face-lifting on each agent
        executor.scheduleAtFixedRate(Agent1, 0, 100, TimeUnit.MILLISECONDS);
        //executor.scheduleAtFixedRate(Agent2, 0, 100, TimeUnit.MILLISECONDS);

        try {
            TimeUnit.SECONDS.sleep(1);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();

    }


}
