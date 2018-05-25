package com.example.drreachapps.two_quadcopters_searching_mission;

/**
 * Created by Dung Tran on 5/22/2018.
 * Two quadcopters do their searching missions defined by set of waypoints
 */


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
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
    private HashSet<RobotMessage> reachsetMsgs = new HashSet<>();
    private int reachSetMsgNum = 0;
    private int reachSetMsgCount = 0;

    private PrintWriter encoding_time_writer;
    private int encoding_counts = 0;
    private PrintWriter decoding_time_writer;
    private int decoding_counts = 0;
    private PrintWriter reach_set_writer; // to write reach set to a file for ploting
    private PrintWriter unsafe_set_writer;
    private boolean reach_set_writer_flag = true;
    private boolean unsafe_set_writer_flag = true;
    private boolean collision_flag = false; // true if there may be a collision in future

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
            reach_set_writer = new PrintWriter(gvh.id.getName() + "_reach_set.dat", "UTF-8");
            unsafe_set_writer = new PrintWriter(gvh.id.getName() + "_local_unsafe_set.dat", "UTF-8");
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

                        System.out.print(gvh.id.getName() + " computes it reach set from "+gvh.plat.reachset.startTime + " to "+gvh.plat.reachset.endTime + "\n");
                        long now = System.nanoTime();
                        MessageContents reach_set_msg_content = new MessageContents(gvh.plat.reachset.messageEncoder(System.currentTimeMillis()));
                        RobotMessage reachset_msg = new RobotMessage("ALL", name,REACH_MSG, reach_set_msg_content);
                        long encoding_time = System.nanoTime() - now;
                        System.out.print(gvh.id.getName() + " encodes its reach set to send out in " +((double)encoding_time)/1000000 + " milliseconds\n");
                        if (encoding_counts < 100){
                            encoding_time_writer.printf("" +(new Timestamp(System.currentTimeMillis())) +","+((double)encoding_time)/1000000 +"\n"); // store 100 samples of encoding time
                            encoding_counts++;
                        }
                        else{
                            encoding_time_writer.close();
                        }

                        // write reach set to a file just for plotting figure, this affects control performance, don't use it in general
                        //if (encoding_counts >= 100 && reach_set_writer_flag){
                        //    gvh.plat.reachset.reach_set_writer(reach_set_writer);
                        //    reach_set_writer_flag = false;
                        //}


                        System.out.print(gvh.id.getName() + " broadcasts its reach set to others\n");
                        if(!gvh.plat.reachset.safe){

                            // write unsafe set to a file, just for plotting figure, this affect control performance of the system, just use one time to get reach set
                            //if(encoding_counts >= 100 && unsafe_set_writer_flag){
                            //    gvh.plat.reachset.reach_set_writer(unsafe_set_writer);
                            //    unsafe_set_writer_flag = false;
                            //}

                            System.out.print(gvh.id.getName() + " may violates its local safety specification at time " +gvh.plat.reachset.unsafe_time_exact.toString() +"\n");
                        }
                        gvh.comms.addOutgoingMessage(reachset_msg);
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
            sleep(150);
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

            System.out.print(gvh.id.getName() + " receive reach set (hull) from " +m.getFrom() + "\n");
            String send_at_time = m.getContents(0);
            send_at_time = send_at_time.replace("SENT_AT_TIME,", "");
            long send_at_time_long = Long.parseLong(send_at_time);
            System.out.print("Time for transferring this reach set over network is arround (not considering clock mismatch) " +(System.currentTimeMillis() - send_at_time_long) + " milliseconds\n");
            long now = System.nanoTime();
            FaceLiftingResult rs = reachMsgDecoder(m);
            long decoding_time = System.nanoTime() - now;
            System.out.print("Decoding message from "+m.getFrom() + " takes " +((double)decoding_time)/1000000  + " milliseconds \n");
            if(decoding_counts < 100){
                decoding_time_writer.printf("" +(new Timestamp(System.currentTimeMillis())) +","+((double)decoding_time)/1000000 +"\n"); // store 100 samples of decoding time
                decoding_counts++;
            }else{decoding_time_writer.close();}

            System.out.print("Reach set (hull) of "+m.getFrom() + " that is valid from " + rs.startTime + " to " + rs.endTime + " of its local time is:\n");
            rs.hull.print();
            now = System.currentTimeMillis();
            System.out.print("Current reach set (hull) of " + gvh.id.getName() + " that is valid from " + gvh.plat.reachset.startTime + " to " + gvh.plat.reachset.endTime + " of its local time is:\n");
            gvh.plat.reachset.hull.print();
            System.out.print("Current local time of " + gvh.id.getName() + " is " +new Timestamp(now) + "\n");

            collision_flag = check_collision(rs);
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

    private boolean check_collision(FaceLiftingResult other_rs){

        boolean collision = false;



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