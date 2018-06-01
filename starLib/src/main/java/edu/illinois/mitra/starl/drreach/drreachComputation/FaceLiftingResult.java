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

    public List<String> reachSets = new ArrayList<>();

    public HyperRectangle unsafe_rect; // unsafe rectangle
    public double unsafe_time;
    public Timestamp unsafe_time_exact;

    public void set_unsafe_rect(HyperRectangle unsafe_rect) {
        this.unsafe_rect = unsafe_rect;
    }

    public void set_unsafe_time(double unsafe_time) {
        this.unsafe_time = unsafe_time;
    }

    public void set_unsafe_time_exact(Timestamp unsafe_time_exact) {
        this.unsafe_time_exact = unsafe_time_exact;
    }

    public void set_start_time(Timestamp start_time) {
        this.startTime = start_time;
    }

    public void set_end_time(Timestamp end_time) {
        this.endTime = end_time;
    }

    public void update_hull(HyperRectangle new_hull) {
        this.hull = new_hull;
    }

    public void update_safety(boolean new_safe) {
        this.safe = new_safe;
    }

    public void update_iteration_number(int new_iter_num) {
        this.iter_num = new_iter_num;
    }


    public void reset_reach_set() {

        if (reachSets != null && !reachSets.isEmpty()) {
            this.reachSets.clear();
        }
    }

    public void update_intermediate_reach_set(Double reachTimeAdvance, HyperRectangle trackedRect){

        String str = "";
        for (int d = 0; d < trackedRect.dim; d++) {
            str += Double.toString(trackedRect.intervals[d].min) + "  ";
            if (d < trackedRect.dim - 1) {
                str += Double.toString(trackedRect.intervals[d].max) + "  ";
            } else {
                str += Double.toString(trackedRect.intervals[d].max);
            }
        }
        //System.out.println("write to file \n");
        //System.out.println(reachTimeAdvance + "  " + str + "\n");
        str = Double.toString(reachTimeAdvance) + "  " + str;
        reachSets.add(str);

    }

    public void update_stepSize(double current_stepSize) {
        this.stepSize_used = current_stepSize;
    }

    public List<String> messageEncoder(long send_at_time) {

        // Encode the face-lifting result as a message (A list of string) to send over network
        // Message Structure: DIM, hull.dim,
        //                    INTERVAlS, interval[0].min, interval[0].max, interval[1].min, interval[1].max, ...
        //                    START_TIME, start_time
        //                    END_TIME, end_time

        List<String> contents = new ArrayList<>();

        if (hull != null) {
            String send_time = "SENT_AT_TIME," + Long.toString(send_at_time);
            String dim = "DIM," + Integer.toString(hull.dim);
            String intervals = "INTERVALS,";
            for (int i = 0; i < hull.dim; i++) {
                intervals += Double.toString(hull.intervals[i].min) + ",";
                if (i < hull.dim - 1) {
                    intervals += Double.toString(hull.intervals[i].max) + ",";
                } else {
                    intervals += Double.toString(hull.intervals[i].max);
                }
            }
            String start_time = "START_TIME," + Long.toString(startTime.getTime());
            String end_time = "END_TIME," + Long.toString(endTime.getTime());

            contents.add(send_time);
            contents.add(dim);
            contents.add(intervals);
            contents.add(start_time);
            contents.add(end_time);
        }

        return contents;
    }

    public void print_intermediate_reach_set(){
        System.out.println(reachSets);
    }

    public void write_intermediate_reach_set_to_a_file(PrintWriter writer){

        for (String str: reachSets){
            writer.write(str + "\n");
        }
        writer.close();
    }

}
