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

}
