package tran.lib.drreach.drreachPlot;
import tran.lib.drreach.drreachComputation.HyperRectangle;
import tran.lib.drreach.drreachComputation.Interval;
import org.math.plot.*;

import java.awt.Color;

import javax.swing.*;

import javax.swing.JFrame;
import javax.swing.event.*;


public class Plot {

    public void plot_hyperRect_1D(HyperRectangle rect, int x_dim){
        // plot hyperRectangle in 1D
        ;
    }

    private Plot2DPanel plot_HyperRect_2D(Plot2DPanel plot, HyperRectangle rect, int x_dim, int y_dim){

        // plot one hyperRectangle in 2D

        int dim = rect.get_dim();
        if ((x_dim > dim) || (y_dim > dim)){
            throw new java.lang.Error("invalid dimension to plot");
        }
        else{

            Interval x_interval = rect.get_interval(x_dim);
            Interval y_interval = rect.get_interval(y_dim);
            double[] x = {x_interval.min, x_interval.min, x_interval.max, x_interval.max, x_interval.min};
            double[] y = {y_interval.min, y_interval.max, y_interval.max, y_interval.min, y_interval.min};

            plot.addLinePlot("", Color.BLUE, x, y);
        }

        return plot;
    }

    public void plot_HyperRects_2D(HyperRectangle[] rect_list, int x_dim, int y_dim){

        // plot list of hyperRectangle in 2D

        int n = rect_list.length; // number of hyperRectangles

        Plot2DPanel plot = new Plot2DPanel();

        for (int i=0; i < n; i++){
            HyperRectangle rect = rect_list[i];
            plot = plot_HyperRect_2D(plot, rect, x_dim, y_dim);
        }

        JFrame frame = new JFrame("Plot of list of hyperRectangles");
        frame.setSize(600,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(plot);
        frame.setVisible(true);

    }

    public void plot_hyperRect_3D(HyperRectangle rect, int x_dim, int y_dim, int z_dim){
        // plot hyperRectangle in 3D
        ;
    }

}
