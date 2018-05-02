package tran.lib.drreach.drreachPlot.Tests;

import java.util.Arrays;

import tran.lib.drreach.drreachComputation.HyperRectangle;
import tran.lib.drreach.drreachComputation.Interval;
import tran.lib.drreach.drreachPlot.Plot;

public class test_plot_2d {

    public static void main(String[] args){
        // test hyperRectangle class

        double[] min_vec1 = {0.1, 0.2, 0.3};
        double[] max_vec1 = {0.3, 0.4, 1.0};

        double[] min_vec2 = {0.4, 0.4, 0.5};
        double[] max_vec2 = {0.6, 0.6, 0.7};



        HyperRectangle Rect1 = new HyperRectangle(Interval.vector2intervals(min_vec1, max_vec1));
        HyperRectangle Rect2 = new HyperRectangle(Interval.vector2intervals(min_vec2,max_vec2));

        HyperRectangle[] Rects = {Rect1, Rect2};

        Plot plot = new Plot();
        plot.plot_HyperRects_2D(Rects, 0, 0);

    }
}


