package tran.lib.drreach.drreachComputation;

public class HyperRectangle {
    Interval[] intervals;
    int dim;
    public HyperRectangle(Interval[] input_intervals){
        intervals = input_intervals;
        dim = intervals.length;
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
