package tran.lib.drreach.drreachPlot.Tests;

import java.util.Arrays;

import tran.lib.drreach.drreachComputation.hyperRectangle;
import tran.lib.drreach.drreachPlot.Plot;

public class test_plot_2d {

    public static void main(String[] args){
        // test hyperRectangle class

        double[] min_vec1 = {0.1, 0.2, 0.3};
        double[] max_vec1 = {0.3, 0.4, 1.0};

        double[] min_vec2 = {0.4, 0.4, 0.5};
        double[] max_vec2 = {0.6, 0.6, 0.7};


        hyperRectangle hyperRect1 = new hyperRectangle(min_vec1, max_vec1);
        hyperRectangle hyperRect2 = new hyperRectangle(min_vec2, max_vec2);

        hyperRectangle[] hyperRect_list = {hyperRect1, hyperRect2};

        Plot plot = new Plot();
        plot.plot_hyperRects_2D(hyperRect_list, 0, 0);

    }
}


