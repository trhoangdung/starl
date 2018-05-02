package tran.lib.drreach.drreachComputation;

// FaceLifting Class implements Face-Lifting method
// Dung Tran: 5/2/2018
// References: 1) Reachability Analysis via Face-Lifting
//             2) Real-time Reachability Anlysis for Verified Simplex Design


public class FaceLifting {

    public static HyperRectangle make_neighborhood_rect(HyperRectangle originalRect, int at_dim, boolean isMin, double nebWidth){
        // Make a neighborhood rectangle based on provided width

        HyperRectangle bloatedRect = new HyperRectangle(originalRect.dim);

        // At each dimension, there are two faces corresponding to that dimension, minimum_face and maximum_face
        // For example, Rect: 0 <= x <= 2: the minimum_face is at x = 0 (a point in this case), the maximum_face is at x = 2
        // For two dimensional Rectangle:     0 <= x1 <= 2; 1 <= x2 <= 3: at the dimension 1 (i.e., x1 axis) the minimum face
        // is a line x1 = 0, 1 <= x2 <= 3 and the maximum face is a line x1 = 2, 1 <= x2 <= 3


        // flatten, i.e. a rectangle with width = 0, this is used to initialized a neighborhood rectangle
        if (isMin){
            // this neighborhood is made at the minimum face
            bloatedRect.intervals[at_dim].min = originalRect.intervals[at_dim].min;
            bloatedRect.intervals[at_dim].max = originalRect.intervals[at_dim].min;
        }
        else{
            // this neighborhood is made at the maximum face
            bloatedRect.intervals[at_dim].min = originalRect.intervals[at_dim].max;
            bloatedRect.intervals[at_dim].max = originalRect.intervals[at_dim].max;
        }

        if (nebWidth < 0){
            bloatedRect.intervals[at_dim].min += nebWidth;
        }
        else{
            bloatedRect.intervals[at_dim].max += nebWidth;
        }

        return bloatedRect;
    }

    public double lift_single_rect(HyperRectangle rect, double stepsize, double timeRemaining){
        // Do a single face lifting operation.
        // !!! Note that this is done for all faces of the hyperRectangle
        // Return time elapsed

        double timeElapsed = 0;

        int numFaces = 2 * rect.dim; // number of faces need to be lifted

        HyperRectangle bloatedRect = rect; // initial rectangle

        double[] nebWidth = new double[numFaces]; // an array of nebWidth that used to lift faces (i.e., bloat the rect in all dimensions)

        ;


        return timeElapsed;

    }
}
