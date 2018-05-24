package edu.illinois.mitra.starl.drreach.drreachTest;

import net.sourceforge.interval.ia_math.RealInterval;

import java.util.concurrent.BlockingQueue;

import edu.illinois.mitra.starl.drreach.drreachComputation.ComputationSetting;
import edu.illinois.mitra.starl.drreach.drreachComputation.FaceLifting;
import edu.illinois.mitra.starl.drreach.drreachComputation.FaceLiftingResult;
import edu.illinois.mitra.starl.drreach.drreachComputation.HyperRectangle;
import edu.illinois.mitra.starl.drreach.drreachComputation.Interval;
import edu.illinois.mitra.starl.drreach.drreachComputation.LiftingSettings;
import edu.illinois.mitra.starl.drreach.drreachComputation.SingleLiftingResult;
import edu.illinois.mitra.starl.drreach.drreachComputation.UnsafeSet;

// Test single face lifting method
// Dung Tran: 5/14/2018

public class Test_Single_Lifting {

    public static void main(String[] arg) {
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
        BlockingQueue<FaceLiftingResult> queue = null;

        // lifting setting
        double initialStepSize = 0.001;
        double reachTime = 1.0;
        long max_runtime_milliseconds = 10;
        int dynamics_index = 0; // linear pendulum dynamics
        double max_rect_width_before_error = 100;

        LiftingSettings setting = new LiftingSettings(init_rect, reachTime, initialStepSize, max_rect_width_before_error, max_runtime_milliseconds, dynamics_index, unsafe_set);


        FaceLifting Agent1 = new FaceLifting(setting, queue);


        double timeRemaining = reachTime;
        SingleLiftingResult rs = Agent1.lift_single_rect(dynamics_index, init_rect, initialStepSize, timeRemaining);


    }
}
