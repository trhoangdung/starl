package tran.lib.drreach.drreachComputation;

import javax.management.RuntimeErrorException;

// hyperRectangle class
// Dung Tran: 5/1/2018, update:
// Reference: Real-Time Reachability for Verified Simplex Design, RTSS 2014

public class hyperRectangle {
    double[] min_vec;
    double[] max_vec;
    int dim = 0;

    // constructors

    public hyperRectangle(int dim){
        min_vec = new double[dim];
        max_vec = new double[dim];
        dim = dim;
    }

    public hyperRectangle(double[] minimum_vec, double[] maximum_vec){
        if (minimum_vec.length == maximum_vec.length) {
            min_vec = minimum_vec;
            max_vec = maximum_vec;
            dim = minimum_vec.length;
        }
        else {
            throw new java.lang.Error("inconsistency between dimensions of minimum_vec and maximum_vec");
        }

    }

    public double[] get_min_vec(){
        return min_vec;
    }

    public double[] get_max_vec(){
        return max_vec;
    }

    public int get_dim(){
        return dim;
    }

    public hyperRectangle make_neighborhood_rec(int at_dim, boolean isMin, double nebWidth){

        // at_dim define the dimension
        // isMin = True -> min_face else -> max_face

        hyperRectangle bloatedRect = new hyperRectangle(dim);

        if (at_dim > dim){
            throw new java.lang.Error("invalid dimension input");
        }
        else{

            if (isMin) {
                bloatedRect.min_vec[at_dim] = min_vec[at_dim];
                bloatedRect.max_vec[at_dim] = min_vec[at_dim];
            }
            else{
                bloatedRect.min_vec[at_dim] = max_vec[at_dim];
                bloatedRect.max_vec[at_dim] = max_vec[at_dim];
            }

            if (nebWidth < 0){
                bloatedRect.min_vec[at_dim] += nebWidth;
            }
            else{
                bloatedRect.max_vec[at_dim] += nebWidth;
            }

        }

        return bloatedRect;
    }

    public void plot_1D(int x_dim){
        // plot hyperRectangle in 1D
        ;
    }

    public void plot_2D(int x_dim, int y_dim){
        // plot hyperRectangle in 2D
        ;
    }

    public void plot_3D(int x_dim, int y_dim, int z_dim){
        // plot hyperRectangle in 3D
        ;
    }

}


