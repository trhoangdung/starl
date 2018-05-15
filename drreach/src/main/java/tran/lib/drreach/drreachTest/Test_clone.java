package tran.lib.drreach.drreachTest;

import tran.lib.drreach.drreachComputation.HyperRectangle;
import tran.lib.drreach.drreachComputation.Interval;
import tran.lib.drreach.drreachPlot.Plot;

// See how clone-method work
//https://javarevisited.blogspot.com/2013/09/how-clone-method-works-in-java.html
//Dung Tran: 5/15/2018

//This does not work for our HyperRectangle since it contains mutable object
//https://www.quora.com/What-is-the-difference-between-mutable-and-immutable-objects-in-Java

public class Test_clone {
    public static void main(String[] args){
        // test hyperRectangle class

        double[] min_vec1 = {0.1, 0.2, 0.3};
        double[] max_vec1 = {0.3, 0.4, 1.0};

        HyperRectangle Rect1 = new HyperRectangle(Interval.vector2intervals(min_vec1, max_vec1));

        HyperRectangle Rect2 = Rect1.copy();

        Rect2.intervals[0].min = 0.4;
        System.out.print("Rect2 new \n");
        Rect2.print();
        System.out.print("Rect1 \n");
        Rect1.print();

    }
}
