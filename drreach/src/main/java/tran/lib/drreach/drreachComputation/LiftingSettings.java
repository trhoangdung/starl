package tran.lib.drreach.drreachComputation;

public class LiftingSettings {
    public HyperRectangle initRect;    // initial set
    public double reachTime;   // total reach time
    public double initialStepSize;     // the initial size of the steps to use
    public double maxRectWidthBeforeError; // maximum allowed rectangle size
    public int maxRuntimeMilliseconds; // maximum runtime in milliseconds

    public LiftingSettings(HyperRectangle init_rect, double reach_time, double initial_step_size, double max_rect_width_before_error, int max_runtime_milliseconds){
        // constructor for LiftingSettings

        initRect = init_rect;

        if (reach_time > 0.0){
            reachTime = reach_time;
        }
        else{
            throw new java.lang.Error("invalid reach time");
        }

        if (initial_step_size > 0.0){
            initialStepSize = initial_step_size;
        }
        else{
            throw new java.lang.Error("invalid initial step size");
        }

        if (max_rect_width_before_error > 0.0){
            maxRectWidthBeforeError = max_rect_width_before_error;
        }
        else{
            throw new java.lang.Error("invalid maximum rectangle width before error");
        }

        maxRuntimeMilliseconds = max_runtime_milliseconds;
    }


}
