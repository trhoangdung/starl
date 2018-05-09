package tran.lib.drreach.drreachComputation;

// Object to contain FaceLifting Result
// Dung Tran: 5/9/2018 Update:

public class FaceLiftingResult {
    public HyperRectangle hull; // convex_hull of all tracked rectangles in face lifting process
    public boolean safe;
    public int iter_num; // iteration number

    public FaceLiftingResult(HyperRectangle hull, boolean safe, int iter_num){
        this.hull = hull;
        this.safe = safe;
        this.iter_num = iter_num;
    }
}
