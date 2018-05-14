package tran.lib.drreach.drreachComputation;

// Setting for Face-lifting method
// Dung Tran: 5/3/2018 LastUpdate: 5/14/2018

public class ComputationSetting {
    // Some setting for reachable set computation using face lifting method

    public final double MAX_DER = 99999; // necessary to guarantee loop termination
    public final double MIN_DER = -99999;
    public final double DBL_MAX = Double.MAX_VALUE; // used to intialize the minimum Neighborhood cross time.
    public final double DBL_MIN = Double.MIN_VALUE; // used as -Infinity for safety checking
}
