package com.example.drreachapps.two_quadcopters_searching_mission;

/**
 * Created by Dung Tran on 5/22/2018.
 * Two quadcopters do their searching missions defined by set of waypoints
 */


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.illinois.mitra.starl.comms.MessageContents;
import edu.illinois.mitra.starl.comms.RobotMessage;
import edu.illinois.mitra.starl.gvh.GlobalVarHolder;
import edu.illinois.mitra.starl.interfaces.LogicThread;
import edu.illinois.mitra.starl.models.Model_quadcopter;
import edu.illinois.mitra.starl.motion.MotionParameters;
import edu.illinois.mitra.starl.motion.MotionParameters.COLAVOID_MODE_TYPE;
import edu.illinois.mitra.starl.objects.ItemPosition;
import edu.illinois.mitra.starl.drreach.drreachComputation.FaceLiftingResult;
import edu.illinois.mitra.starl.drreach.drreachComputation.HyperRectangle;
import edu.illinois.mitra.starl.drreach.drreachComputation.Interval;

public class two_quadcopters_searching_mission_app extends LogicThread {
    private static final String TAG = "Follow App";
    public static final int ARRIVED_MSG = 22;
    public static final int REACH_MSG = 20;
    private int destIndex;
    private int messageCount = 0;
    private int numBots;
    private int numWaypoints;
    private boolean arrived = false;
    private boolean goForever = true;
    private int msgNum = 0;
    private HashSet<RobotMessage> receivedMsgs = new HashSet<RobotMessage>();
    private int reachSetMsgNum = 0;
    private int reachSetMsgCount = 0;

    FaceLiftingResult current_reach_set;
    private PrintWriter encoding_time_writer;
    private int encoding_counts = 0;
    private PrintWriter decoding_time_writer;
    private int decoding_counts = 0;
    private PrintWriter transferring_time_writer;
    private int transferring_counts = 0;
    private PrintWriter reach_set_writer; // to write reach set to a file for ploting
    private int reach_set_counts = 0;
    private PrintWriter unsafe_set_writer;
    private boolean reach_set_writer_flag = true;
    private boolean unsafe_set_writer_flag = true;
    private boolean collision_flag = false; // true if there may be a collision in future
    private boolean global_safety_flag = false; // true if the global property is violated
    private boolean useful_message_flag = false;

    final Map<String, ItemPosition> destinations = new HashMap<String, ItemPosition>();
    ItemPosition currentDestination;

    private enum Stage {
        INIT, PICK, GO, DONE, WAIT
    };

    private Stage stage = Stage.INIT;

    public two_quadcopters_searching_mission_app(GlobalVarHolder gvh) {
        super(gvh);
        MotionParameters.Builder settings = new MotionParameters.Builder();
//		settings.ROBOT_RADIUS(400);
        settings.COLAVOID_MODE(COLAVOID_MODE_TYPE.USE_COLAVOID);
        MotionParameters param = settings.build();
        gvh.plat.moat.setParameters(param);
        for(ItemPosition i : gvh.gps.getWaypointPositions())
            if(gvh.id.getName().equals("quadcopter0")){
                if(i.getName().charAt(0) == 'A'){
                    destinations.put(i.getName(), i);
                }
            }
            else if(gvh.id.getName().equals("quadcopter1")){
                if(i.getName().charAt(0) == 'B'){
                    destinations.put(i.getName(), i);
                }
            }


        gvh.comms.addMsgListener(this, ARRIVED_MSG);
        gvh.comms.addMsgListener(this, REACH_MSG); // listen to reach-set message from others
        // bot names must be bot0, bot1, ... botn for this to work
        String intValue = name.replaceAll("[^0-9]", "");
        destIndex = Integer.parseInt(intValue);
        numBots = gvh.id.getParticipants().size();

        try{
            encoding_time_writer = new PrintWriter(gvh.id.getName() +"_encoding_time.dat", "UTF-8");
            decoding_time_writer = new PrintWriter(gvh.id.getName() + "_decoding_time.dat", "UTF-8");
            transferring_time_writer = new PrintWriter(gvh.id.getName() + "_transferring_time.dat", "UTF-8");
            reach_set_writer = new PrintWriter(gvh.id.getName() + "_reach_set.txt", "UTF-8");
            unsafe_set_writer = new PrintWriter(gvh.id.getName() + "_local_unsafe_set.txt", "UTF-8");
        }catch(IOException e){
            e.printStackTrace();
        }


    }

    @Override
    public List<Object> callStarL() {
        while(true) {
            switch(stage) {
                case INIT:

                    numWaypoints = destinations.size();
                    stage = Stage.PICK;
                case PICK:
                    arrived = false;
                    if(destinations.isEmpty()) {
                        stage = Stage.DONE;
                    } else {

                        if(destIndex >= numWaypoints) {
                            destIndex = 0;
                        }
                        currentDestination = getDestination(destinations, destIndex);
                        //Log.d(TAG, currentDestination.toString());
                        destIndex++;
                        gvh.plat.moat.goTo(currentDestination);
                        stage = Stage.GO;
                    }
                    break;
                case GO:
                    if(!gvh.plat.moat.inMotion) {
                        if(!goForever) {
                            if (currentDestination != null)
                                destinations.remove(currentDestination.getName());
                        }
                        RobotMessage inform = new RobotMessage("ALL", name, ARRIVED_MSG, Integer.toString(msgNum));
                        msgNum++;
                        gvh.log.d(TAG, "At Goal, sent message");
                        gvh.comms.addOutgoingMessage(inform);

                        arrived = true;
                        stage = Stage.WAIT;
                    }

                    // broadcast reach set to other robots
                    if(gvh.plat.reachset != null){
                        current_reach_set = gvh.plat.reachset;
                        System.out.print(gvh.id.getName() + " computes it reach set from "+current_reach_set.startTime + " to "+current_reach_set.endTime + "\n");
                        long now = System.nanoTime();
                        MessageContents reach_set_msg_content = new MessageContents(current_reach_set.messageEncoder(System.currentTimeMillis()));
                        RobotMessage reachset_msg = new RobotMessage("ALL", name,REACH_MSG, reach_set_msg_content);
                        long encoding_time = System.nanoTime() - now;
                        System.out.print(gvh.id.getName() + " encodes its reach set to send out in " +((double)encoding_time)/1000000 + " milliseconds\n");
                        System.out.print(gvh.id.getName() + " broadcasts its reach set to others\n");
                        gvh.comms.addOutgoingMessage(reachset_msg);

                        if(!gvh.plat.reachset.safe){

                            // write unsafe set to a file, just for plotting figure, this affect control performance of the system, just use one time to get reach set
                            //if(encoding_counts >= 10 && unsafe_set_writer_flag){
                            //    gvh.plat.reachset.reach_set_writer(unsafe_set_writer);
                            //    unsafe_set_writer_flag = false;
                            //}

                            System.out.print(gvh.id.getName() + " may violates its local safety specification at time " +gvh.plat.reachset.unsafe_time_exact.toString() +"\n");
                        }
                        else{
                            System.out.print(gvh.id.getName() + " does not violate its local safety property " +"\n");
                        }

                        // get 100 samples of encoding time for plotting
                        //if (encoding_counts < 10){
                        //    encoding_counts++;
                        //    encoding_time_writer.printf("" +encoding_counts +"   "+((double)encoding_time)/1000000 +"\n"); // store 10 samples of encoding time
                        //}
                        //else{
                        //    encoding_time_writer.close();
                        //}

                        //reach_set_counts++;

                        // write reach set to a file just for plotting figure, this affects control performance, don't use it in general
                        //if (reach_set_counts >= 100 && reach_set_writer_flag){
                        //    gvh.plat.reachset.write_intermediate_reach_set_to_a_file(reach_set_writer);
                        //    reach_set_writer_flag = false;
                        //    reach_set_counts = 0;
                        //}

                    }


                    break;
                case WAIT:
                    if((messageCount >= numBots - 1) && arrived) {
                        messageCount = 0;
                        stage = Stage.PICK;
                    }
                    break;
                case DONE:
                    return null;
            }
            sleep(250);
        }
    }

    @Override
    protected void receive(RobotMessage m) {
        boolean alreadyReceived = false;
        for(RobotMessage msg : receivedMsgs) {
            if(msg.getFrom().equals(m.getFrom()) && msg.getContents().equals(m.getContents())) {
                alreadyReceived = true;
                break;
            }
        }
        if(m.getMID() == ARRIVED_MSG && !m.getFrom().equals(name) && !alreadyReceived) {
            gvh.log.d(TAG, "Adding to message count from " + m.getFrom());
            receivedMsgs.add(m);
            messageCount++;
        }

        if(m.getMID() == REACH_MSG && !m.getFrom().equals(name)){

            System.out.print(gvh.id.getName() + " receives reach set (hull) from " +m.getFrom() + "\n");
            String send_at_time = m.getContents(0);
            send_at_time = send_at_time.replace("SENT_AT_TIME,", "");
            long send_at_time_long = Long.parseLong(send_at_time);
            System.out.print("Time for transferring this reach set over network is arround (not considering clock mismatch) " +(System.currentTimeMillis() - send_at_time_long) + " milliseconds\n");

            //if(transferring_counts < 10){
            //    transferring_counts++;
            //    transferring_time_writer.printf("" +transferring_counts +"  "+(System.currentTimeMillis() - send_at_time_long) +"\n"); // store 10 samples of transferring time
            //}else{transferring_time_writer.close();}

            long now = System.nanoTime();
            FaceLiftingResult rs = reachMsgDecoder(m);
            long decoding_time = System.nanoTime() - now;
            System.out.print("Decoding message from "+m.getFrom() + " takes " +((double)decoding_time)/1000000  + " milliseconds \n");

            // get 10 samples of decoding time for plotting
            //if(decoding_counts < 10){
            //    decoding_counts++;
            //    decoding_time_writer.printf("" +decoding_counts +"  "+((double)decoding_time)/1000000 +"\n"); // store 100 samples of decoding time
            //}else{decoding_time_writer.close();}

            System.out.print("Reach set (hull) of "+m.getFrom() + " that is valid from " + rs.startTime + " to " + rs.endTime + " of its local time is:\n");
            rs.hull.print();
            now = System.currentTimeMillis();
            System.out.print("Current reach set (hull) of " + gvh.id.getName() + " that is valid from " + current_reach_set.startTime + " to " + current_reach_set.endTime + " of its local time is:\n");
            current_reach_set.hull.print();
            System.out.print("Current local time of " + gvh.id.getName() + " is " +new Timestamp(now) + "\n");

            // check if message is useful

            long available_time_for_checking = rs.endTime.getTime() - 3 - 3 - now;

            if (available_time_for_checking > 0){ // time-synchronization error is 3 milliseconds
                System.out.print("Useful time for checking collision and global safety property is " +available_time_for_checking + " milliseconds\n");
                useful_message_flag = true;
                System.out.print("The received reachable set from " +m.getFrom() + " is useful \n");
            }
            else{
                useful_message_flag = false;
                System.out.print("The received reachable set from " +m.getFrom() + " is not useful, delete it \n");
            }


            if (useful_message_flag){
                // check collision using useful message
                double allowable_distance = 100;
                collision_flag = check_collision(current_reach_set, rs, allowable_distance);
                if (collision_flag) {
                    System.out.print(gvh.id.getName() + " may collide with " +m.getFrom() + " in the next " + ((double)available_time_for_checking)/1000 + " seconds\n");
                }
                else{System.out.print(gvh.id.getName() + " will not collide with " +m.getFrom() + " in the next " + ((double)available_time_for_checking)/1000 + " seconds\n");}

                // check global property

                // global unsafe region:    x0 > 1200 and x1 > 2500 // Todo: define more general global safety sepcification

                double x0_max = current_reach_set.hull.intervals[0].max;
                double x0_min = current_reach_set.hull.intervals[0].min;
                double x1_max = rs.hull.intervals[0].max;
                double x1_min = rs.hull.intervals[0].min;

                if ((900 < x0_min) && (x0_max < 1200) && (x1_min > 900) && (x1_max < 1200)){
                    global_safety_flag = false;
                    System.out.print("The global property may be violated in the next " + ((double)available_time_for_checking)/1000 + " seconds\n");
                }
                else{
                    global_safety_flag = true;
                    System.out.print("The global safety property is guarantee in the next " + ((double)available_time_for_checking)/1000 + " seconds\n");
                }


            }

        }

    }


    private FaceLiftingResult reachMsgDecoder(RobotMessage m){
        FaceLiftingResult rs = new FaceLiftingResult();

        String send_time = m.getContents(0); // send at time, not used here
        String dim = m.getContents(1);
        String intervals = m.getContents(2);
        String start_time = m.getContents(3);
        String end_time = m.getContents(4);

        dim = dim.replace("DIM,","");

        int d = Integer.parseInt(dim);

        intervals = intervals.replace("INTERVALS,","");

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

        }

        HyperRectangle hull = new HyperRectangle(Interval.vector2intervals(min_vec, max_vec));
        start_time = start_time.replace("START_TIME,","");
        end_time = end_time.replace("END_TIME,","");
        long start_time_long = Long.parseLong(start_time);
        long end_time_long = Long.parseLong(end_time);

        rs.set_start_time(new Timestamp(start_time_long));
        rs.set_end_time(new Timestamp(end_time_long));
        rs.update_hull(hull);

        return rs;
    }

    private boolean check_collision(FaceLiftingResult self_current_rs, FaceLiftingResult sender_current_rs, double allowable_distance){

        boolean collision = false;

        double self_x_min = self_current_rs.hull.intervals[0].min;
        double self_x_max = self_current_rs.hull.intervals[0].max;
        double self_y_min = self_current_rs.hull.intervals[2].min;
        double self_y_max = self_current_rs.hull.intervals[2].max;

        double sender_x_min = sender_current_rs.hull.intervals[0].min;
        double sender_x_max = sender_current_rs.hull.intervals[0].max;
        double sender_y_min = sender_current_rs.hull.intervals[2].min;
        double sender_y_max = sender_current_rs.hull.intervals[2].max;

        double x1 = sender_x_min - self_x_min;
        double x2 = sender_x_min - self_x_max;
        double x3 = sender_x_max - self_x_min;
        double x4 = sender_x_max - self_x_max;

        double y1 = sender_y_min - self_y_min;
        double y2 = sender_y_min - self_y_max;
        double y3 = sender_y_max - self_x_min;
        double y4 = sender_y_max - self_x_max;

        double d1 = Math.pow(x1, 2) + Math.pow(y1, 2);
        double d2 = Math.pow(x1, 2) + Math.pow(y2, 2);
        double d3 = Math.pow(x1, 2) + Math.pow(y3, 2);
        double d4 = Math.pow(x1, 2) + Math.pow(y4, 2);

        double d5 = Math.pow(x2, 2) + Math.pow(y1, 2);
        double d6 = Math.pow(x2, 2) + Math.pow(y2, 2);
        double d7 = Math.pow(x2, 2) + Math.pow(y3, 2);
        double d8 = Math.pow(x2, 2) + Math.pow(y4, 2);

        double d9 = Math.pow(x3, 2) + Math.pow(y1, 2);
        double d10 = Math.pow(x3, 2) + Math.pow(y2, 2);
        double d11 = Math.pow(x3, 2) + Math.pow(y3, 2);
        double d12 = Math.pow(x3, 2) + Math.pow(y4, 2);

        double d13 = Math.pow(x4, 2) + Math.pow(y1, 2);
        double d14 = Math.pow(x4, 2) + Math.pow(y2, 2);
        double d15 = Math.pow(x4, 2) + Math.pow(y3, 2);
        double d16 = Math.pow(x4, 2) + Math.pow(y4, 2);

        double d[] = {d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13, d14, d15, d16};

        Arrays.sort(d);

        double dmin = d[0];

        if (dmin < allowable_distance){
            collision = true;
        }

        return collision;
    }

    @SuppressWarnings("unchecked")
    private <X, T> T getDestination(Map<X, T> map, int index) {
        // Keys must be 0-A format for this to work

        String key = "";
        if(gvh.id.getName().equals("quadcopter0")){
            key = key + "A-" + Integer.toString(index);

        }
        else if(gvh.id.getName().equals("quadcopter1")){
            key = key+ "B-" + Integer.toString(index);
        }

        return map.get(key);
    }
}