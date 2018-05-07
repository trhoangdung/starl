package tran.lib.drreach.drreachComputation;

public class HyperRectangle {
    public Interval[] intervals;
    public int dim;

    // constructor
    public HyperRectangle(Interval[] input_intervals){
        intervals = input_intervals;
        dim = intervals.length;
    }
    // constructor
    public HyperRectangle(int dimensions){
        if (dimensions > 0){
            dim = dimensions;
            intervals = new Interval[dim];
        }
        else{
            throw new java.lang.Error("invalid dimension");
        }
    }

    public int get_dim(){
        return dim;
    }

    public Interval get_interval(int at_dim){

        if (at_dim <= dim){
            for (int i = 0; i < dim; i++){
                Interval interval = intervals[i];
            }
            return intervals[at_dim];
        }
        else{
            throw new java.lang.Error("empty HyperRectangle");
        }

    }

    public boolean contains(HyperRectangle inside, boolean printErrors){
        // check if this rectangle contains another rectangle

        boolean rv = true;

        if (dim != inside.dim){
            throw new java.lang.Error("two rectangles do not have the same dimensions");
        }
        else{

            int NUM_DIMS = dim;
            for (int d = 0; d < NUM_DIMS; ++d){
                if ((inside.intervals[d].min < intervals[d].min) || (inside.intervals[d].max > intervals[d].max)){

                    if (printErrors && (inside.intervals[d].min < intervals[d].min)){
                        System.out.print(String.format("inside.intervals[%d].min = %f < outside.intervals[%d].min = %f \n", d, inside.intervals[d].min, d, intervals[d].min));
                    }
                    else if (printErrors && (inside.intervals[d].max > intervals[d].max)){
                        System.out.print(String.format("inside.intervals[%d].max = %f > outside.intervals[%d].max = %f \n", d, inside.intervals[d].max, d, intervals[d].max));
                    }

                    rv = false;
                    break;
                }
            }
        }


        return rv;
    }

}
