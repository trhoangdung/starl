package tran.lib.drreach.drreachPlot.Tests;

import java.util.Arrays;

import tran.lib.drreach.drreachComputation.hyperRectangle;
import tran.lib.drreach.drreachPlot.Plot;

public class test_plot_2d {

    public static void main(String[] args){
        // test hyperRectangle class

        double[] minimum_vec = new double[3];
        minimum_vec[0]  = 0.1;
        minimum_vec[1] = 0.2;
        minimum_vec[2] = 0.5;
        double[] maximum_vec = new double[3];
        maximum_vec[0] = 0.3;
        maximum_vec[1] = 0.4;
        maximum_vec[2] = 1.0;

        hyperRectangle hyperRec = new hyperRectangle(minimum_vec, maximum_vec);

        Plot plt = new Plot();

        plt.plot_hyperRect_2D(hyperRec, 0, 1);

    }
}


