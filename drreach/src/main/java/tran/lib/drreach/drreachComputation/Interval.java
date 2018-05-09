package tran.lib.drreach.drreachComputation;
// Interval class
// Dung Tran: 5/2/2018

public class Interval {
    public double min;
    public double max;

    public Interval(double input_min, double input_max) {
        this.min = input_min;
        this.max = input_max;
    }

    public double get_width() {
        return max - min;
    }

    public static Interval[] vector2intervals(double[] min_vec, double[] max_vec){
        // Convert vectors to intervals
        // This is convenient to declare a high-dimensional HyperRectangle using min_vec and max_vec

        Interval[] intervals;
        if (min_vec.length != max_vec.length){
            throw new java.lang.Error("inconsistent vectors");
        }
        else{
            int dim = min_vec.length;
                intervals = new Interval[dim];
            for (int i = 0; i < dim; i++){
                Interval interval = new Interval(min_vec[i], max_vec[i]);
                intervals[i] = interval;
            }
        }

        return intervals;

    }
}