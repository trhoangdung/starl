package tran.lib.drreach.drreachPlot;
import tran.lib.drreach.drreachComputation.hyperRectangle;
import org.math.plot.*;

import java.awt.Color;

import javax.swing.*;

import javax.swing.JFrame;


public class Plot {

    public void plot_hyperRect_1D(hyperRectangle rect, int x_dim){
        // plot hyperRectangle in 1D
        ;
    }

    private Plot2DPanel plot_hyperRect_2D(Plot2DPanel plot, hyperRectangle rect, int x_dim, int y_dim){

        // plot one hyperRectangle in 2D

        int dim = rect.get_dim();
        if ((x_dim > dim) || (y_dim > dim)){
            throw new java.lang.Error("invalid dimension to plot");
        }
        else{

            double[] min_vec = rect.get_min_vec();
            double[] max_vec = rect.get_max_vec();
            double[] x = {min_vec[x_dim], min_vec[x_dim], max_vec[x_dim], max_vec[x_dim], min_vec[x_dim]};
            double[] y = {min_vec[y_dim], max_vec[y_dim], max_vec[y_dim], min_vec[y_dim], min_vec[y_dim]};

            plot.addLinePlot("", Color.BLUE, x, y);
        }

        return plot;
    }

    public void plot_hyperRects_2D(hyperRectangle[] rect_list, int x_dim, int y_dim){

        // plot list of hyperRectangle in 2D

        int n = rect_list.length; // number of hyperRectangles

        Plot2DPanel plot = new Plot2DPanel();

        for (int i=0; i < n; i++){
            hyperRectangle rect = rect_list[i];
            plot = plot_hyperRect_2D(plot, rect, x_dim, y_dim);
        }

        JFrame frame = new JFrame("Plot of list of hyperRectangles");
        frame.setSize(600,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(plot);
        frame.setVisible(true);

    }

    public void plot_hyperRect_3D(hyperRectangle rect, int x_dim, int y_dim, int z_dim){
        // plot hyperRectangle in 3D
        ;
    }

}
