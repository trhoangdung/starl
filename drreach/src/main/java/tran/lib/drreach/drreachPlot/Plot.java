package tran.lib.drreach.drreachPlot;
import tran.lib.drreach.drreachComputation.hyperRectangle;
import org.math.plot.*;
import javax.swing.*;

import javax.swing.JFrame;


public class Plot {

    public void plot_hyperRect_1D(hyperRectangle rect, int x_dim){
        // plot hyperRectangle in 1D
        ;
    }

    public void plot_hyperRect_2D(hyperRectangle rect, int x_dim, int y_dim){
        // plot hyperRectangle in 2D

        int dim = rect.get_dim();
        if ((x_dim > dim) || (y_dim > dim)){
            throw new java.lang.Error("invalid dimension to plot");
        }
        else{

            double[] min_vec = rect.get_min_vec();
            double[] max_vec = rect.get_max_vec();
            double[] x = {min_vec[x_dim], min_vec[x_dim], max_vec[x_dim], max_vec[x_dim], min_vec[x_dim]};
            double[] y = {min_vec[y_dim], max_vec[y_dim], max_vec[y_dim], min_vec[y_dim], min_vec[y_dim]};


            Plot2DPanel plot = new Plot2DPanel();
            plot.addLinePlot("test", x, y);
            JFrame frame = new JFrame("A 2D plot of hyperRectangle");
            frame.setContentPane(plot);
            frame.setSize(600,600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

        }


    }

    public void plot_hyperRect_3D(hyperRectangle rect, int x_dim, int y_dim, int z_dim){
        // plot hyperRectangle in 3D
        ;
    }

}
