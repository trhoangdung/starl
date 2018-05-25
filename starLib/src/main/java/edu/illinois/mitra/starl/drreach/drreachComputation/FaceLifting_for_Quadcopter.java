package edu.illinois.mitra.starl.drreach.drreachComputation;

// This implements face-lifting method specifically for quadcopter,
// TODO: some part of this code duplicate FaceLifting class, need to improve
// This is special case where control inputs of quadcopter are time-varying, control inputs is computed from PID controller
// Dung Tran: 5/22/2018


import java.sql.Timestamp;

import edu.illinois.mitra.starl.drreach.drreachDynamics.Simplified_Quadcopter;
import edu.illinois.mitra.starl.gvh.GlobalVarHolder;

public class FaceLifting_for_Quadcopter {

    public static HyperRectangle make_neighborhood_rect(HyperRectangle bloatedRect, HyperRectangle originalRect, int f, double nebWidth){
        // Make a neighborhood rectangle based on provided width

        HyperRectangle out = bloatedRect;

        // At each dimension, there are two faces corresponding to that dimension, minimum_face and maximum_face
        // For example, Rect: 0 <= x <= 2: the minimum_face is at x = 0 (a point in this case), the maximum_face is at x = 2
        // For two dimensional Rectangle:     0 <= x1 <= 2; 1 <= x2 <= 3: at the dimension 1 (i.e., x1 axis) the minimum face
        // is a line x1 = 0, 1 <= x2 <= 3 and the maximum face is a line x1 = 2, 1 <= x2 <= 3

        boolean isMin = (f % 2) == 0;
        int at_dim = f / 2;

        // flatten, i.e. a rectangle with width = 0, this is used to initialized a neighborhood rectangle
        if (isMin){
            // this neighborhood is made at the minimum face
            out.intervals[at_dim].min = originalRect.intervals[at_dim].min;
            out.intervals[at_dim].max = originalRect.intervals[at_dim].min;
        }
        else{
            // this neighborhood is made at the maximum face
            out.intervals[at_dim].min = originalRect.intervals[at_dim].max;
            out.intervals[at_dim].max = originalRect.intervals[at_dim].max;
        }

        if (nebWidth < 0){
            out.intervals[at_dim].min += nebWidth;
        }
        else{
            out.intervals[at_dim].max += nebWidth;
        }

        return out;
    }

    double MAX_DER = new ComputationSetting().MAX_DER;
    double MIN_DER = new ComputationSetting().MIN_DER;
    double DBL_MAX = new ComputationSetting().DBL_MAX;

    public SingleLiftingResult lift_single_rect(HyperRectangle rect, double stepsize, double timeRemaining, double current_pitch, double current_roll){

        // Do a single face lifting operation.
        // !!! Note that this is done for all faces of the hyperRectangle
        // Return time elapsed


        int NUM_FACES = 2 * rect.dim; // number of faces need to be lifted
        int NUM_DIMS = rect.dim; // number of dimensions

        HyperRectangle bloatedRect = rect; // initial rectangle

        double[] nebWidth = new double[NUM_FACES]; // an array of nebWidth that used to lift faces (i.e., bloat the rect in all dimensions)

        //initialize nebWidth
        for (int f = 0; f < NUM_FACES; f ++){
            nebWidth[f] = 0;
        }

        boolean needRecompute = true;
        double minNebCrossTime = DBL_MAX;
        double[] ders = new double[NUM_FACES];

        while (needRecompute){

            needRecompute = false;
            minNebCrossTime = DBL_MAX;

            for (int f = 0; f < NUM_FACES; ++f){

                int dim = f / 2;
                boolean isMin = (f % 2) == 0;

                HyperRectangle faceNebRect;

                // make candidate neighborhood
                faceNebRect = make_neighborhood_rect(bloatedRect, rect, f, nebWidth[f]);

                // test derivative inside neighborhood;
                Simplified_Quadcopter db = new Simplified_Quadcopter();
                double der = db.get_derivative_bounds(faceNebRect, f, current_pitch, current_roll);

                //System.out.print("Derivative at face " +f +" = " +der + "\n");

                if (der > MAX_DER){
                    der = MAX_DER;
                }
                else if (der < MIN_DER){
                    der = MIN_DER;
                }

                double prevNebWidth = nebWidth[f];
                double newNebWidth = der * stepsize;

                boolean grewOutward = (isMin && newNebWidth < 0.0) || (!isMin && newNebWidth > 0.0);
                boolean prevGrewOutward = (isMin && prevNebWidth < 0.0) || (!isMin && prevNebWidth > 0.0);

                // prevent flipping from outward face to inward face
                if (!grewOutward && prevGrewOutward){
                    newNebWidth = 0.0;
                    der = 0.0;
                }

                // if flipping from inward to outward
                if (!prevGrewOutward && grewOutward){
                    needRecompute = true;
                }

                // 2nd condition to recompute, der doubled (which means nebwidth is twice what is was before)
                if (Math.abs(newNebWidth) > 2 * Math.abs(prevNebWidth)){
                    needRecompute = true;
                }

                // adjust bloated rect only if we are requiring a later recomputation

                if (needRecompute){

                    nebWidth[f] = newNebWidth;

                    //System.out.print("adjust bloated rect for later recomputation\n");

                    if (isMin && nebWidth[f] < 0){
                        bloatedRect.intervals[dim].min = rect.intervals[dim].min + nebWidth[f];
                    }
                    else if (!isMin && nebWidth[f] > 0){
                        bloatedRect.intervals[dim].max = rect.intervals[dim].max + nebWidth[f];
                    }

                }
                else{

                    // might be the last iteration, compute min time to cross face
                    // clamp derivation if it changed direction, this means along the face it's inward but in the neighborhood it's outward

                    if (der < 0 && prevNebWidth > 0){
                        der = 0.0;
                    }
                    else if (der > 0 && prevNebWidth < 0){
                        der = 0.0;
                    }

                    if (der != 0.0){
                        double crossTime = prevNebWidth / der;
                        if (crossTime < minNebCrossTime) {
                            minNebCrossTime = crossTime;
                        }
                    }

                    ders[f] = der;

                }


            }


        }

        if (minNebCrossTime * 2 < stepsize){

            System.out.print("MinNebCrossTime = " +minNebCrossTime + "\n");

            throw new Error("minNebCrossTime is less than half of step size");

        }


        // Lift each face by the minimum time //

        double timeToElapse = minNebCrossTime;
        // subtract a tiny amount time due to multiplication / division rounding

        if (timeRemaining  < timeToElapse){
            timeToElapse = timeRemaining;
        }

        // do the lifting

        for (int d = 0; d < NUM_DIMS; ++d){
            rect.intervals[d].min += ders[2*d] * timeToElapse;
            rect.intervals[d].max += ders[2*d + 1] * timeToElapse;
        }

        if (!bloatedRect.contains(rect, true)){
            throw new Error("lifted rect is outside of bloated rect");
        }

        SingleLiftingResult rs = new SingleLiftingResult(timeToElapse, rect);
        return rs;

    }

    public FaceLiftingResult face_lifting_iterative_improvement (long startMs, LiftingSettings setting, double current_pitch, double current_roll){


        int iter = 0; // number of iteration
        double stepSize = setting.initialStepSize;
        int dynamics_index = setting.dynamics_index;
        double runTimeRemaining = setting.maxRuntimeMilliseconds;
        double runTimeAdvance = 0.0;
        boolean safe = true;
        UnsafeSet unsafe_set = setting.unsafe_set;

        FaceLiftingResult rs = new FaceLiftingResult();
        // ** Note that we use System clock to compute the reach set and use it as global time, we are not using gvh.time()
        Timestamp start_time = new Timestamp(startMs);
        rs.set_start_time(start_time);     // set start time
        //System.out.print("Initial Set \n");
        //setting.initRect.print();

        while(runTimeRemaining > 0.0){

            iter++;
            //System.out.print("Iteration = " +iter +"\n");
            //System.out.print("Step size used for this iteration is " +stepSize +"\n");
            rs.update_iteration_number(iter); // update iteration number
            rs.reset_reach_set();   // reset the reachable set to store the new reachable set using new stepSize
            safe = true;

            if (stepSize < 0.0000001){
                break; // break if stepSize is too small
                //throw new java.lang.Error("Step size is too small");
            }

            double reachTimeRemaining = setting.reachTime;
            double reachTimeAdvance = 0.0;


            HyperRectangle trackedRect = setting.initRect.copy();
            HyperRectangle hull = setting.initRect.copy();

            while (reachTimeRemaining > 0.0){ // do face lifting with current stepSize, check safety at runtime

                SingleLiftingResult singleRes = lift_single_rect(trackedRect, stepSize, reachTimeRemaining, current_pitch, current_roll);

                double reachTimeElapsed = singleRes.timeElapsed;
                trackedRect = singleRes.trackedRect;

                hull.convex_hull(trackedRect);      // get convex-hull of reachable sets
                safe = !hull.check_intersect(unsafe_set);    // check safety

                rs.update_stepSize(stepSize); // update step size
                rs.update_safety(safe); // update safety status
                rs.update_hull(hull);   // update hull
                reachTimeAdvance += reachTimeElapsed;
                rs.update_reach_set(reachTimeAdvance, trackedRect); // update reachable set
                long reachTimeAdvanceLong = Double.valueOf(reachTimeAdvance*1000.0).longValue();
                long currentTimeLong = startMs + reachTimeAdvanceLong;

                if (!safe){
                    //System.out.print("System violate its local safety specification at time: "+new Timestamp(currentTimeLong)+"\n");
                    //System.out.print("Unsafe Reach Set\n");
                    //trackedRect.print();
                    rs.set_unsafe_rect(trackedRect);
                    rs.set_unsafe_time(reachTimeAdvance);
                    rs.set_unsafe_time_exact(new Timestamp(currentTimeLong));

                }

                reachTimeRemaining -= reachTimeElapsed;
                //System.out.print("reachTimeAdvance = "+reachTimeAdvance+"\n");
                //System.out.print("remainingReachTime = " +reachTimeRemaining + "\n");
                //System.out.print("Reach set at time: " +new Timestamp(currentTimeLong)+"\n");
                //trackedRect.print();
                //System.out.print("Reach set hull from "+new Timestamp(startMs) +" to "+new Timestamp(currentTimeLong) +" = " +"\n");
                //hull.print();
                rs.set_end_time(new Timestamp(currentTimeLong)); // end time for this faceLifting result

            }

            long current = System.currentTimeMillis();
            long runTimeElapsed = current - startMs;
            //System.out.print("runTimeElaped = "+runTimeElapsed +"\n");
            runTimeRemaining = setting.maxRuntimeMilliseconds - runTimeElapsed;
            //System.out.print("remainingRunTime = "+runTimeRemaining +"\n");

            if (runTimeRemaining > 0.0){ // if we still have time, redoing face lifting with a smaller step to get the as good as possible result.
                stepSize = stepSize / 2.0;
            }

        }

        return rs;
    }

}
