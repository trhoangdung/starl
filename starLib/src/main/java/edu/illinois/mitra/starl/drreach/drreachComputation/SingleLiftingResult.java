package edu.illinois.mitra.starl.drreach.drreachComputation;

// A return object of single lift operation
// contain : 1) timeElapsed
//           2) trackedRect

// Dung Tran: 5/9/2018, update:

public class SingleLiftingResult {
    public double timeElapsed;
    public HyperRectangle trackedRect;

    public SingleLiftingResult(double timeElapsed, HyperRectangle trackedRect){
        this.timeElapsed = timeElapsed;
        this.trackedRect = trackedRect;
    }
}
