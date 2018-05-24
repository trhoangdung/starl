package com.example.drreachapps.two_quadcopters_searching_mission;

/**
 * Created by Dung Tran on 5/22/2018.
 * Two quadcopters do their searching missions defined by set of waypoints
 */


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
import tran.lib.drreach.drreachComputation.FaceLiftingResult;
import tran.lib.drreach.drreachComputation.HyperRectangle;
import tran.lib.drreach.drreachComputation.Interval;


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
                        MessageContents reach_set_msg_content = new MessageContents(gvh.plat.reachset.messageEncoder());
                        RobotMessage reachset_msg = new RobotMessage("ALL", name,REACH_MSG, reach_set_msg_content);
                        System.out.print(gvh.id.getName() + " broadcasts its reach set to others\n");
                        if(!gvh.plat.reachset.safe){
                            System.out.print(gvh.id.getName() + " violates its local safety specification at time " +gvh.plat.reachset.unsafe_time_exact.toString() +"\n");
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

            System.out.print(gvh.id.getName() + " receive reach set from " +m.getFrom() + "\n");
            FaceLiftingResult rs = reachMsgDecoder(m);
            System.out.print("Reach set of "+m.getFrom() +":\n");
            rs.hull.print();
            System.out.print("This reach set is valid from "+rs.startTime.getTime() + " to " +rs.endTime.getTime() +"\n");
        }

    }


    private FaceLiftingResult reachMsgDecoder(RobotMessage m){
        FaceLiftingResult rs = new FaceLiftingResult();

        String dim = m.getContents(0);
        String intervals = m.getContents(1);
        String start_time = m.getContents(2);
        String end_time = m.getContents(3);

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
        start_time = start_time.replace("STARL_TIME,","");
        end_time = end_time.replace("END_TIME,","");
        long start_time_long = Long.parseLong(start_time);
        long end_time_long = Long.parseLong(end_time);

        rs.set_start_time(new Timestamp(start_time_long));
        rs.set_end_time(new Timestamp(end_time_long));
        rs.update_hull(hull);

        return rs;
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