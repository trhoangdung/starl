package tran.lib.drreach.drreachComputation;

// Object to contain FaceLifting Result
// Dung Tran: 5/9/2018 Update: 5/14/2018

import java.util.HashMap;

public class FaceLiftingResult {
    public HyperRectangle hull; // convex_hull of all tracked rectangles in face lifting process
    public boolean safe;
    public int iter_num; // iteration number
    public double stepSize_used = 0.0; // the step size currently used to get this result
    public double startTime; // store the start time when a face lifting method is called. (in seconds)
    public double validTime; // the time that this face lifting result is valid, validTime = startTime + reachTime (in seconds)

    public HashMap<Double, HyperRectangle> reachSets = new HashMap<Double, HyperRectangle>(); // store reachable set overtime

    public HyperRectangle unsafe_rect; // unsafe rectangle
    public double unsafe_time;

    public void set_unsafe_rect(HyperRectangle unsafe_rect){
        this.unsafe_rect = unsafe_rect;
    }

    public void set_unsafe_time(double unsafe_time){
        this.unsafe_time = unsafe_time;
    }

    public void set_start_time(double start_time){
        this.startTime = start_time;
    }

    public void set_valid_time(double valid_time){
        this.validTime = valid_time;
    }

    public void update_hull(HyperRectangle new_hull){
        this.hull = new_hull;
    }

    public void update_safety(boolean new_safe){
        this.safe = new_safe;
    }

    public void update_iteration_number(int new_iter_num){
        this.iter_num = new_iter_num;
    }

    public void update_reach_set(double reachTimeAdvance, HyperRectangle trackedRect){
        // this is to store reachable set a long time for checking and plotting if necessary

        this.reachSets.put(reachTimeAdvance, trackedRect);

    }

    public void reset_reach_set(){

        if (reachSets != null && !reachSets.isEmpty()){
            this.reachSets.clear();
        }
    }

    public void update_stepSize(double current_stepSize){
        this.stepSize_used = current_stepSize;
    }
}
