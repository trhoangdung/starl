package tran.lib.drreach.drreachTest;

import android.renderscript.ScriptIntrinsicYuvToRGB;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import tran.lib.drreach.drreachComputation.FaceLiftingResult;
import tran.lib.drreach.drreachComputation.HyperRectangle;
import tran.lib.drreach.drreachComputation.Interval;

public class Test_MessageDecoder {
    public static void main(String[] args){
        // test hyperRectangle class

        List<String> reachSetMsg = new ArrayList<>();
        String dim = "DIM,2";
        String intervals = "INTERVALS,0.01,0.02,0.1,0.2";
        String start_time = "STARL_TIME,1234";
        String end_time = "END_TIME,1325";

        reachSetMsg.add(dim);
        reachSetMsg.add(intervals);
        reachSetMsg.add(start_time);
        reachSetMsg.add(end_time);

        FaceLiftingResult rs = reachMsgDecoder(reachSetMsg);

    }

    private static FaceLiftingResult reachMsgDecoder(List<String> msg){
        FaceLiftingResult rs = new FaceLiftingResult();

        String dim = msg.get(0);
        String intervals = msg.get(1);
        String start_time = msg.get(2);
        String end_time = msg.get(3);

        System.out.print(dim + "\n");
        dim = dim.replace("DIM,","");
        System.out.print(dim + "\n");
        int d = Integer.parseInt(dim);
        System.out.print(intervals + "\n");
        intervals = intervals.replace("INTERVALS,","");
        System.out.print(intervals + "\n");
        double[] min_vec = new double[d];
        double[] max_vec = new double[d];
        String min_char;
        String max_char;
        int index;

        for (int i=0; i<d; i++){

            index = intervals.indexOf(",");
            min_char = intervals.substring(0, index);
            min_vec[i] = Double.parseDouble(min_char);
            intervals = intervals.substring(index + 1).trim();
            if(i < d - 1){
                index = intervals.indexOf(",");
                max_char = intervals.substring(0,index);
                intervals = intervals.substring(index + 1).trim();
            }
            else{
                max_char = intervals;
            }
            max_vec[i] = Double.parseDouble(max_char);
            System.out.printf("min_vec[%d] = %f \n", i, min_vec[i]);
            System.out.printf("max_vec[%d] = %f \n", i, max_vec[i]);
        }

        HyperRectangle hull = new HyperRectangle(Interval.vector2intervals(min_vec, max_vec));
        start_time = start_time.replace("STARL_TIME,", "");
        end_time = end_time.replace("END_TIME,","");
        long start_time_long = Long.parseLong(start_time);
        long end_time_long = Long.parseLong(end_time);

        rs.set_start_time(new Timestamp(start_time_long));
        rs.set_end_time(new Timestamp(end_time_long));
        rs.update_hull(hull);

        return rs;

    }
}
