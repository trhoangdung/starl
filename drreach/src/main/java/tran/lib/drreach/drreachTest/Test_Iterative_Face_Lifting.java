package tran.lib.drreach.drreachTest;

// Test Iteratively improved Face Lifting method
// Dung Tran: 5/14/2018 Last update: 5/15/2018

import net.sourceforge.interval.ia_math.RealInterval;

import java.util.concurrent.BlockingQueue;

import tran.lib.drreach.drreachComputation.ComputationSetting;
import tran.lib.drreach.drreachComputation.FaceLifting;
import tran.lib.drreach.drreachComputation.FaceLiftingResult;
import tran.lib.drreach.drreachComputation.HyperRectangle;
import tran.lib.drreach.drreachComputation.Interval;
import tran.lib.drreach.drreachComputation.LiftingSettings;
import tran.lib.drreach.drreachComputation.SingleLiftingResult;
import tran.lib.drreach.drreachComputation.UnsafeSet;

public class Test_Iterative_Face_Lifting {

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
        double initialStepSize = 0.1;
        double reachTime = 1.0;
        long max_runtime_milliseconds = 100;
        int dynamics_index = 0; // linear pendulum dynamics
        double max_rect_width_before_error = 100;

        LiftingSettings setting = new LiftingSettings(init_rect, reachTime, initialStepSize, max_rect_width_before_error, max_runtime_milliseconds, dynamics_index, unsafe_set);


        FaceLifting Agent1 = new FaceLifting(setting, queue);


        FaceLiftingResult rs = Agent1.face_lifting_iterative_improvement(System.currentTimeMillis(), setting);



    }

}
