package tran.lib.drreach.drreachComputation;

// Unsafe set is represent as a collection of Real Intervals at specific dimension
// for example, x1 >= 8 is unsafe region, then we have dim = 1 and Interval = [8, +Infinity)
// x1 >= 8 && x2 <= 2 is unsafe region, then we have : dim = 1 -> Interval = [8, +Infinity) && dim = 2 -> Interval = (-Infinity, 2]

// Dung Tran: 5/9/2018, Update:


import net.sourceforge.interval.ia_math.RealInterval;

import java.util.HashMap;

public class UnsafeSet {

    public HashMap <Integer, RealInterval> unsafe_set = new HashMap<Integer, RealInterval>();

    public UnsafeSet(int dim, RealInterval interval){
        this.unsafe_set.put(dim, interval);
    }

    public UnsafeSet add_constrain(int dim, RealInterval interval){
        this.unsafe_set.put(dim, interval);
        return this;
    }

}
