package tran.lib.drreach.drreachComputation;

public class LiftingSettings {
    public HyperRectangle initRect;    // initial set
    public double reachTime;   // total reach time
    public double initialStepSize;     // the initial size of the steps to use
    public double maxRectWidthBeforeError; // maximum allowed rectangle size
    public int maxRuntimeMilliseconds; // maximum runtime in milliseconds
    public int dynamics_index; // specify dynamics for the lifting
    public UnsafeSet unsafe_set; // specify unsafe set to check in runtime

    public LiftingSettings(HyperRectangle init_rect, double reach_time, double initial_step_size, double max_rect_width_before_error, int max_runtime_milliseconds, int dynamics_index, UnsafeSet unsafe_set){
        // constructor for LiftingSettings

        this.initRect = init_rect;  // initial set

        if (reach_time > 0.0){
            this.reachTime = reach_time;    // time for computing reachable set
        }
        else{
            throw new java.lang.Error("invalid reach time");
        }

        if (initial_step_size > 0.0){
            this.initialStepSize = initial_step_size;   // initial stepSize for faceLifting
        }
        else{
            throw new java.lang.Error("invalid initial step size");
        }

        if (max_rect_width_before_error > 0.0){
            this.maxRectWidthBeforeError = max_rect_width_before_error;     // maximum allowed rectangle size
        }
        else{
            throw new java.lang.Error("invalid maximum rectangle width before error");
        }

        this.maxRuntimeMilliseconds = max_runtime_milliseconds;

        if ((dynamics_index >= 0) && (dynamics_index <= 1)){
            this.dynamics_index = dynamics_index;   // select dynamics based on index
        }
        else{
            throw new java.lang.Error("Invalid dynamics index");
        }

        this.unsafe_set = unsafe_set;   // specify unsafe region
    }


}
