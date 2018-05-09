package tran.lib.drreach.drreachComputation;

import net.sourceforge.interval.ia_math.RealInterval;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;

public class HyperRectangle {
    public Interval[] intervals;
    public int dim;

    // constructor
    public HyperRectangle(Interval[] input_intervals){
        this.intervals = input_intervals;
        this.dim = intervals.length;
    }
    // constructor
    public HyperRectangle(int dimensions){
        if (dimensions > 0){
            this.dim = dimensions;
            this.intervals = new Interval[dim];
        }
        else{
            throw new java.lang.Error("invalid dimension");
        }
    }

    public int get_dim(){
        return dim;
    }

    public Interval get_interval(int at_dim){

        if (at_dim <= dim){
            for (int i = 0; i < dim; i++){
                Interval interval = intervals[i];
            }
            return intervals[at_dim];
        }
        else{
            throw new java.lang.Error("empty HyperRectangle");
        }

    }

    public boolean contains(HyperRectangle inside, boolean printErrors){
        // check if this rectangle contains another rectangle

        boolean rv = true;

        if (dim != inside.dim){
            throw new java.lang.Error("two rectangles do not have the same dimensions");
        }
        else{

            int NUM_DIMS = dim;
            for (int d = 0; d < NUM_DIMS; ++d){
                if ((inside.intervals[d].min < intervals[d].min) || (inside.intervals[d].max > intervals[d].max)){

                    if (printErrors && (inside.intervals[d].min < intervals[d].min)){
                        System.out.print(String.format("inside.intervals[%d].min = %f < outside.intervals[%d].min = %f \n", d, inside.intervals[d].min, d, intervals[d].min));
                    }
                    else if (printErrors && (inside.intervals[d].max > intervals[d].max)){
                        System.out.print(String.format("inside.intervals[%d].max = %f > outside.intervals[%d].max = %f \n", d, inside.intervals[d].max, d, intervals[d].max));
                    }

                    rv = false;
                    break;
                }
            }
        }


        return rv;
    }

    public HyperRectangle convex_hull(HyperRectangle contained){
        // do convex-hull of two rectangles

        if (this.dim != contained.dim){
            throw new java.lang.Error("Error: Dimension inconsistency to make a convex hull");
        }
        else{
            for (int d = 0; d < this.dim; ++d){
                if (contained.intervals[d].min < this.intervals[d].min){
                    this.intervals[d].min = contained.intervals[d].min;
                }
                if (contained.intervals[d].max > this.intervals[d].max){
                    this.intervals[d].max = contained.intervals[d].max;
                }
            }
        }

        return this;
    }

    public boolean check_intersect(UnsafeSet unsafe_set){
        // check if this rectangle intersect with unsafe region

        boolean rv = false;

        HashMap<Integer, RealInterval> us = unsafe_set.unsafe_set;

        Set<Integer> dims = us.keySet();

        for (Integer d: dims){

            if ((d > this.dim) || (d < 0)){
                throw new java.lang.Error("Invalid dimension for checking safety");
            }
            else{
                RealInterval interval = us.get(d);

                if ((this.intervals[d].max >= interval.lo()) && (this.intervals[d].max <= interval.hi())){

                    rv = true;
                    break;

                }

                if ((this.intervals[d].min >= interval.lo()) && (this.intervals[d].min <= interval.hi())){

                    rv = true;
                    break;

                }

            }
        }

        return rv;
    }

}
