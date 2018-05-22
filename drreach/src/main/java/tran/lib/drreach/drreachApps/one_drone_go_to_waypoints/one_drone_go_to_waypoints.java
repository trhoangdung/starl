package tran.lib.drreach.drreachApps.one_drone_go_to_waypoints;

// This app is used to test how to read velocity and angles of a drone when it moving
// Dung Tran: 5/21/2018

import edu.illinois.mitra.starl.gvh.GlobalVarHolder;
import edu.illinois.mitra.starl.interfaces.LogicThread;
import edu.illinois.mitra.starl.models.Model_quadcopter;
import edu.illinois.mitra.starl.motion.MotionParameters;
import edu.illinois.mitra.starl.objects.ItemPosition;
import edu.illinois.mitra.starl.objects.ObstacleList;
import edu.illinois.mitra.starl.objects.PositionList;
import tran.lib.drreach.drreachComputation.GlobalAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class one_drone_go_to_waypoints extends LogicThread {

    final List<ItemPosition> destinations = new ArrayList<ItemPosition>();

    PositionList<ItemPosition> destinationsHistory = new PositionList<ItemPosition>();
    ObstacleList obs;

    private Model_quadcopter quadcopter;

    ItemPosition currentPosition = new ItemPosition("curdes", 1000, 800);
    ItemPosition currentDestination;
    private int des_ind = 0;

    private enum Stage{
        PICK, GO, DONE
    };

    private Stage stage = Stage.PICK;


    public one_drone_go_to_waypoints(GlobalVarHolder gvh){
        super(gvh);

        destinations.add(new ItemPosition("des1", 400, 500));
        destinations.add(new ItemPosition("des2", 400, 800));
        destinations.add(new ItemPosition("des3", 600, 1000));

        MotionParameters.Builder settings = new MotionParameters.Builder(); // setting for motions
        MotionParameters param = settings.build();
        gvh.plat.moat.setParameters(param); // set parameters for robot motions
        obs = gvh.gps.getObspointPositions();

    }

    @Override
    public List<Object> callStarL(){

        while(true){

            quadcopter = (Model_quadcopter) gvh.plat.getModel();
            System.out.print("current pitch = " +quadcopter.pitch + ", current height  = " +quadcopter.height + "\n");

            switch (stage){

                case PICK:

                    // pick a destination
                    if((des_ind >= 0) && (des_ind < destinations.size())){
                        currentDestination = destinations.get(des_ind);
                        des_ind += 1;
                        System.out.print(String.format("Current Destination (x = %d, y = %d) \n", currentDestination.x, currentDestination.y));
                        System.out.print("destination index = "+des_ind + "\n");
                        gvh.plat.moat.goTo(currentDestination); // goto current destination
                        stage = Stage.GO;
                    }else{
                        des_ind = 0;
                        stage = Stage.DONE;
                    }

                    break;

                case GO:

                    // wait for finish the current task
                    if(gvh.plat.moat.done){

                        stage = Stage.PICK;
                        destinationsHistory.update(currentDestination);
                        break;

                    }

                case DONE:
                    System.out.print("Task Done!\n");
                    return null;

            }

            sleep(100);

        }

    }
}
