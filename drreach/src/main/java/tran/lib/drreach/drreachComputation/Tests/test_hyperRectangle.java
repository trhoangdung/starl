package tran.lib.drreach.drreachComputation.Tests;
import java.util.Arrays;

import tran.lib.drreach.drreachComputation.hyperRectangle;

public class test_hyperRectangle {

    public static void main(String[] args){
        // test hyperRectangle class

        double[] minimum_vec = new double[2];
        minimum_vec[0]  = 0.1;
        minimum_vec[1] = 0.2;
        double[] maximum_vec = new double[2];
        maximum_vec[0] = 0.3;
        maximum_vec[1] = 0.4;

        hyperRectangle hyperRec = new hyperRectangle(minimum_vec, maximum_vec);
        System.out.println("Minimum vector is : " + Arrays.toString(hyperRec.get_min_vec()));
        System.out.println("Maximum vector is : " + Arrays.toString(hyperRec.get_max_vec()));

    }
}
