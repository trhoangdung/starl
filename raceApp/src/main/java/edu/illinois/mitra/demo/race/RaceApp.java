package edu.illinois.mitra.demo.race;

import com.sun.org.apache.xpath.internal.operations.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.illinois.mitra.starl.comms.RobotMessage;
import edu.illinois.mitra.starl.gvh.GlobalVarHolder;
import edu.illinois.mitra.starl.interfaces.LogicThread;
import edu.illinois.mitra.starl.models.Model_GhostAerial;
import edu.illinois.mitra.starl.models.Model_quadcopter;
import edu.illinois.mitra.starl.motion.MotionParameters;
import edu.illinois.mitra.starl.motion.RRTNode;
import edu.illinois.mitra.starl.motion.MotionParameters.COLAVOID_MODE_TYPE;
import edu.illinois.mitra.starl.objects.ItemPosition;
import edu.illinois.mitra.starl.objects.ObstacleList;
import edu.illinois.mitra.starl.objects.PositionList;

public class RaceApp extends LogicThread {
	private static final boolean RANDOM_DESTINATION = false;
	public static final int ARRIVED_MSG = 22;

	final Map<String, ItemPosition> destinations = new HashMap<String, ItemPosition>();
	PositionList<ItemPosition> destinationsHistory = new PositionList<ItemPosition>();
	ItemPosition currentDestination;
	PositionList<ItemPosition> doReachavoidCalls = new PositionList<ItemPosition>();
	ObstacleList obs;
	public RRTNode kdTree;

	private enum Stage {
		PICK, GO, DONE, FAIL
	};

	private Stage stage = Stage.PICK;

	public RaceApp(GlobalVarHolder gvh) {
		super(gvh);
		MotionParameters.Builder settings = new MotionParameters.Builder();
//		settings.ROBOT_RADIUS(400);
		settings.COLAVOID_MODE(COLAVOID_MODE_TYPE.USE_COLAVOID);
		settings.GOAL_RADIUS(150);
		MotionParameters param = settings.build();

		gvh.plat.moat.setParameters(param);


		gvh.comms.addMsgListener(this, ARRIVED_MSG);
		obs = gvh.gps.getObspointPositions();
	}

	@Override
	public List<Object> callStarL() {

		for(ItemPosition i : gvh.gps.getWaypointPositions()){
			if(gvh.id.getName().equals("quadcopter0") && (i.getName().equals("A") || i.getName().equals("B"))){
				destinations.put(i.getName(), i);
				destinationsHistory.update(i);
			}
			else if(gvh.id.getName().equals("quadcopter1") && (i.getName().equals("C") || i.getName().equals("D"))){
				destinations.put(i.getName(), i);
				destinationsHistory.update(i);
			}

		}
		while(true) {


			if(gvh.plat.model instanceof Model_quadcopter){
				gvh.log.i("WIND", ((Model_quadcopter)gvh.plat.model).windxNoise + " " +  ((Model_quadcopter)gvh.plat.model).windyNoise);
			} else if(gvh.plat.model instanceof Model_GhostAerial){
				gvh.log.i("WIND", ((Model_GhostAerial)gvh.plat.model).windxNoise + " " +  ((Model_GhostAerial)gvh.plat.model).windyNoise);
			}
			switch(stage) {
				case PICK:
					//	System.out.println("picking");
					if(destinations.isEmpty()) {
						stage = Stage.DONE;
					} else {
						currentDestination = getRandomElement(destinations);
						gvh.plat.reachAvoid.doReachAvoid(gvh.gps.getMyPosition(), currentDestination, obs);
						kdTree = gvh.plat.reachAvoid.kdTree;
						gvh.log.i("DoReachAvoid", currentDestination.x + " " +currentDestination.y);
						doReachavoidCalls.update(new ItemPosition(name + "'s " + "doReachAvoid Call to destination: " + currentDestination.name, gvh.gps.getMyPosition().x,gvh.gps.getMyPosition().y));
						System.out.println(name + " going to " + currentDestination.getName());
						stage = Stage.GO;
					}
					break;
				case GO:
					if(gvh.plat.reachAvoid.doneFlag) {
						//System.out.println("doneFlag????");
						gvh.log.i("DoneFlag", "read");
						if(currentDestination != null)
							destinations.remove(currentDestination.getName());
						RobotMessage inform = new RobotMessage("ALL", name, ARRIVED_MSG, currentDestination.getName());
						gvh.comms.addOutgoingMessage(inform);
						stage = Stage.PICK;
					}
					else if(gvh.plat.reachAvoid.failFlag){
						//ra.doReachAvoid(gvh.gps.getMyPosition(), currentDestination, obs);
						gvh.log.i("FailFlag", "read");
						stage = Stage.FAIL;
					}
					break;
				case FAIL:
					System.out.println(gvh.log.getLog());
					break;
				case DONE:
					System.out.println(gvh.log.getLog());
					return null;
			}
			sleep(100);
		}
	}

	@Override
	protected void receive(RobotMessage m) {
		String posName = m.getContents(0);
		if(destinations.containsKey(posName))
			destinations.remove(posName);

		if(currentDestination.getName().equals(posName)) {
			//	gvh.plat.moat.cancel();
			gvh.plat.reachAvoid.cancel();
			stage = Stage.PICK;
		}
	}

	private static final Random rand = new Random();

	@SuppressWarnings("unchecked")
	private <X, T> T getRandomElement(Map<X, T> map) {
		if(RANDOM_DESTINATION)
			return (T) map.values().toArray()[rand.nextInt(map.size())];
		else
			return (T) map.values().toArray()[0];
	}
}