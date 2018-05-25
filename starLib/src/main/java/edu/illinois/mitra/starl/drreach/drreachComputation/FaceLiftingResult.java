package edu.illinois.mitra.starl.drreach.drreachComputation;

// Object to contain FaceLifting Result
// Dung Tran: 5/9/2018 Update: 5/14/2018

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FaceLiftingResult {
    public HyperRectangle hull; // convex_hull of all tracked rectangles in face lifting process
    public boolean safe = true; // local safety
    public int iter_num; // iteration number
    public double stepSize_used = 0.0; // the step size currently used to get this result
    public Timestamp startTime; // store the start time when a face lifting method is called. (in seconds)
    public Timestamp endTime; // the time between startTime and endTime is the valid time for the result

    public LinkedHashMap<Double, HyperRectangle> reachSets = new LinkedHashMap<>(); // store reachable set overtime

    public HyperRectangle unsafe_rect; // unsafe rectangle
    public double unsafe_time;
    public Timestamp unsafe_time_exact;

    public void set_unsafe_rect(HyperRectangle unsafe_rect){
        this.unsafe_rect = unsafe_rect;
    }

    public void set_unsafe_time(double unsafe_time){
        this.unsafe_time = unsafe_time;
    }

    public void set_unsafe_time_exact(Timestamp unsafe_time_exact){this.unsafe_time_exact = unsafe_time_exact;}

    public void set_start_time(Timestamp start_time){
        this.startTime = start_time;
    }

    public void set_end_time(Timestamp end_time){
        this.endTime = end_time;
    }

    public void update_hull(HyperRectangle new_hull){
        this.hull = new_hull;
    }

    public void update_safety(boolean new_safe){
        this.safe = new_safe;
    }

    public void update_iteration_number(int new_iter_num){
        this.iter_num = new_iter_num;
    }

    public void update_reach_set(double reachTimeAdvance, HyperRectangle trackedRect){
        // this is to store reachable set a long time for checking and plotting if necessary

        this.reachSets.put(reachTimeAdvance, trackedRect);

    }

    public void reset_reach_set(){

        if (reachSets != null && !reachSets.isEmpty()){
            this.reachSets.clear();
        }
    }

    public void update_stepSize(double current_stepSize){
        this.stepSize_used = current_stepSize;
    }

    public List<String> messageEncoder(){

        // Encode the face-lifting result as a message (A list of string) to send over network
        // Message Structure: DIM, hull.dim,
        //                    INTERVAlS, interval[0].min, interval[0].max, interval[1].min, interval[1].max, ...
        //                    START_TIME, start_time
        //                    END_TIME, end_time

        List<String> contents = new ArrayList<>();

        if (hull != null){
            String dim = "DIM," + Integer.toString(hull.dim);
            String intervals = "INTERVALS,";
            for (int i= 0; i< hull.dim; i++){
                intervals += Double.toString(hull.intervals[i].min) + ",";
                if(i < hull.dim - 1){intervals += Double.toString(hull.intervals[i].max) + ",";}
                else{intervals += Double.toString(hull.intervals[i].max);}
            }
            String start_time = "START_TIME," +Long.toString(startTime.getTime());
            String end_time = "END_TIME," +Long.toString(endTime.getTime());

            contents.add(dim);
            contents.add(intervals);
            contents.add(start_time);
            contents.add(end_time);
        }

        return contents;
    }

    public void reach_set_writer(PrintWriter writer){
        // write reach sets to a file for plotting

        // get set of the entries
        if ((reachSets != null) && (writer != null)){
            Set set = reachSets.entrySet();
            // get an iterator
            Iterator i = set.iterator();
            while(i.hasNext()){
                Map.Entry me  = (Map.Entry) i.next();
                double key_time = Double.parseDouble(me.getKey().toString());
                HyperRectangle trackedRect = reachSets.get(key_time);

                // time, x1-min, x1-max, x2-min, x2-max, ...

                String str = "";
                for (int d=0; d < trackedRect.dim; d++){
                    str += Double.toString(trackedRect.intervals[d].min) + ",";
                    if(d < trackedRect.dim - 1){
                        str += Double.toString(trackedRect.intervals[d].max) + ",";
                    }
                    else{
                        str += Double.toString(trackedRect.intervals[d].max);
                    }
                }

                writer.printf("" + me.getKey() + "," + str + "\n");
            }
            writer.close();
        }

    }
}
