package edu.illinois.mitra.starl.bluetooth;

import java.util.Arrays;

import edu.illinois.mitra.starl.gvh.GlobalVarHolder;
import edu.illinois.mitra.starl.objects.Common;
import edu.illinois.mitra.starl.objects.ItemPosition;
import edu.illinois.mitra.starl.objects.PositionList;

/**
 * iRobot Create specific. Experimental motion automaton set to replace BluetoothInterface once it's fully tested.
 * @author Adam Zimmerman
 * @version 1.1
 * @see BluetoothRobotMotion
 */
public class MotionAutomaton extends RobotMotion  {
	private static final String TAG = "MotionAutomaton";
	private static final String ERR = "Critical Error";
	
	// MOTION CONTROL CONSTANTS
	public static final int R_arc = 700;
	public static final int R_goal = 100;
	public static final int R_slowfwd = 700;
	public static final int A_smallturn = 3;
	public static final int A_straight = 6;
	public static final int A_arc = 25;
	public static final int A_arcexit = 30;
	
	public static final int A_slowturn = 25;
	
	// DELAY BETWEEN EACH RUN OF THE AUTOMATON
	public static final int DELAY_TIME = 60;
	public static final int SAMPLING_PERIOD = 300;
		
	// COLLISION AVOIDANCE CONSTANTS
	public static final int COLLISION_STRAIGHTTIME = 1250;
	
	private GlobalVarHolder gvh;
	private BluetoothInterface bti;
	//private DeadReckoner reckoner;
	
	// Motion tracking
	private ItemPosition destination;
	private ItemPosition mypos;
	private ItemPosition blocker;
	private enum STAGE {
		INIT, ARCING, STRAIGHT, TURN, SMALLTURN, GOAL
	}
	private STAGE next = null;
	private STAGE stage = STAGE.INIT;
	private STAGE prev = null;
	private boolean running = false;
	private enum OPMODE {
		GO_TO, TURN_TO
	}
	private OPMODE mode = OPMODE.GO_TO;
	
	private MotionParameters param;
	private MotionParameters defaultParam = new MotionParameters();
	
	// Collision avoidance
	private enum COLSTAGE {
		TURN, STRAIGHT
	}
	private COLSTAGE colprev = null; 
	private COLSTAGE colstage = COLSTAGE.TURN;
	private COLSTAGE colnext = null; 
	private int col_straightime = 0;

	public MotionAutomaton(GlobalVarHolder gvh, BluetoothInterface bti) {
		super();
		this.gvh = gvh;
		this.bti = bti;
	}
	
	public void goTo(ItemPosition dest) {
		this.destination = dest;
		this.param = defaultParam;
		this.mode = OPMODE.GO_TO;
		startMotion();
	}
	
	public void goTo(ItemPosition dest, MotionParameters param) {
		this.destination = dest;
		this.param = param;
		this.mode = OPMODE.GO_TO;
		startMotion();
	}
	
	public void turnTo(ItemPosition dest) {
		this.destination = dest;
		this.param = defaultParam;
		this.mode = OPMODE.TURN_TO;
		startMotion();
	}
	
	public void turnTo(ItemPosition dest, MotionParameters param) {
		this.destination = dest;
		this.param = param;
		this.mode = OPMODE.TURN_TO;
		startMotion();
	}
	
	@Override
	public synchronized void start() {
		super.start();
		gvh.log.d(TAG, "STARTED!");
		//reckoner.start();
	}
	
	@Override
	public void run() {
		super.run();
		boolean colliding = false;
		while(true) {
			while(running) {
				mypos = gvh.gps.getMyPosition();//reckoner.getLatestEstimate();
				int distance = mypos.distanceTo(destination);
				int angle = mypos.angleTo(destination);
				int absangle = Math.abs(angle);
				
				colliding = collision();
				
				if(!colliding && stage != null) {
					if(stage != prev) gvh.log.e(TAG, "Stage is: " + stage.toString());
					switch(stage) {
					case INIT:
						if(mode == OPMODE.GO_TO) {
							if(distance <= R_goal) {
								next = STAGE.GOAL;
							} else if(distance <= R_arc && absangle <= param.ARCANGLE_MAX && param.ENABLE_ARCING) {
								next = STAGE.ARCING;
							} else {
								next = STAGE.TURN;
							}
						} else {
							next = STAGE.TURN;
						}
						break;
					case ARCING:
						// If this is the first run of ARCING, begin the arc
						if(stage != prev) {
							int radius = curveRadius();
							curve(param.ARCSPEED_MAX,radius);
						} else {
							// Otherwise, check exit conditions
							if(absangle > A_arcexit) next = STAGE.TURN;					
							if(absangle < A_straight) next = STAGE.STRAIGHT;
							if(distance <= R_goal) next = STAGE.GOAL;
						}
						break;
					case STRAIGHT:
						if(stage != prev) {
							straight(LinSpeed(distance));
						} else {
							if(Common.inRange(distance, R_goal, R_slowfwd)) straight(LinSpeed(distance));
							if(Common.inRange(absangle, A_smallturn, param.ARCANGLE_MAX)) next = STAGE.SMALLTURN;
							if(absangle > param.ARCANGLE_MAX) next = STAGE.TURN;
							if(distance <= R_goal) next = STAGE.GOAL;
						}
						break;
					case TURN:
						if(stage != prev) {
							turn(TurnSpeed(absangle), angle);
						} else {
							if(absangle <= A_smallturn) {
								gvh.log.i(TAG, "Turn stage: within angle bounds!");
								next = (mode == OPMODE.GO_TO) ? STAGE.STRAIGHT : STAGE.GOAL;
							} else if(absangle <= A_slowturn) {
								// Resend a reduced-speed turn command if we're within the slow-turn window
								turn(TurnSpeed(absangle), angle);
							}
						}
						break;
					case SMALLTURN:
						if(stage != prev) {
							int radius = curveRadius()/2;
							curve(LinSpeed(distance), radius);
						} else {
							if(absangle <= A_smallturn) stage = STAGE.STRAIGHT;
							if(distance <= R_goal) stage = STAGE.GOAL;
						}
						break;
					case GOAL:
						gvh.log.i(TAG, "At goal!");
						straight(0);
						running = false;
						inMotion = false;
						break;
					}
					
					prev = stage;
					if(next != null) {
						stage = next;
						gvh.log.i(TAG, "Stage transition to " + stage.toString());
						gvh.trace.traceEvent(TAG, "Stage transition", stage.toString());
					}
					next = null;
					sleep(DELAY_TIME);
				} else if(param.ENABLE_COLAVOID) {					
					// Collision imminent! Stop the robot
					if(stage != null) {
						gvh.log.d(TAG, "Imminent collision detected!");
						stage = null;
						straight(0);
						colnext = null;
						colprev = null;
						colstage = COLSTAGE.TURN;
					}
					
					switch(colstage) {
					case TURN:
						if(colstage != colprev) {
							gvh.log.d(TAG, "Colliding: sending turn command");
							turn(param.TURNSPEED_MAX, -1*mypos.angleTo(blocker));
						}
						
						if(!collision()) {
							colnext = COLSTAGE.STRAIGHT;
							gvh.log.i(TAG, "FREE OF BLOCKER!");
						}
						break;
					case STRAIGHT:
						if(colstage != colprev) {
							gvh.log.d(TAG, "Colliding: sending straight command");
							straight(param.LINSPEED_MAX);
							col_straightime = 0;
						} else {
							col_straightime += DELAY_TIME;
							// If a collision is imminent (again), return to the turn stage
							if(collision()) {
								gvh.log.d(TAG, "Collision imminent! Cancelling straight stage");
								straight(0);
								colnext = COLSTAGE.TURN;
							}
							// If we're collision free and have been for enough time, restart normal motion
							if(!collision() && col_straightime >= COLLISION_STRAIGHTTIME) {
								gvh.log.d(TAG, "Free! Returning to normal execution");
								colprev = null;
								colnext = null;
								colstage = null;
								stage = STAGE.INIT;
							}
						}
						break;
					}
					
					sleep(DELAY_TIME);
					colprev = colstage;
					if(colnext != null) {
						colstage = colnext;
						gvh.log.i(TAG, "Advancing stage to " + colnext);
					}
					colnext = null;

				} else if(colliding && !param.ENABLE_COLAVOID) {
					// Stop the robot if collision avoidance is disabled and a collision is immminent
					gvh.log.d(TAG, "No collision avoidance! Halting.");
					straight(0);
					stage = STAGE.INIT;
				}
			}
			sleep(DELAY_TIME);
		}
	}
	
	public void cancel() {
		running = false;
		bti.disconnect();
	}
	
	@Override
	public void motion_stop() {
		bti.send(BluetoothCommands.straight(0));
		running = false;
	}
	

	@Override
	public void motion_resume() {
		running = true;
	}
	
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Calculates the radius of curvature to meet a target
	private int curveRadius() {
		int x0 = mypos.x;
		int y0 = mypos.y;
		int x1 = destination.x;
		int y1 = destination.y;
		int theta = mypos.angle;
		double alpha = -180+Math.toDegrees(Math.atan2((y1-y0),(x1-x0)));
		double rad = -(Math.sqrt(Math.pow(x1-x0,2) + Math.pow(y1-y0,2))/(2*Math.sin(Math.toRadians(alpha-theta))));
		return (int) rad;
	}
	
	private void startMotion() {
		running = true;
		stage = STAGE.INIT;
		inMotion = true;
	}
	
	private void sendMotionEvent(int motiontype, int ... argument) {
		gvh.trace.traceEvent(TAG, "Motion", Arrays.asList(argument).toString());
		gvh.sendRobotEvent(Common.EVENT_MOTION, motiontype);
	}
	
	private void curve(int velocity, int radius) {
		if(running) {
			sendMotionEvent(Common.MOT_ARCING, velocity, radius);
			bti.send(BluetoothCommands.curve(velocity, radius));
		}
	}
	
	private void straight(int velocity) {
		if(running) {
			if(velocity != 0) {
				sendMotionEvent(Common.MOT_STRAIGHT, velocity);
			} else {
				sendMotionEvent(Common.MOT_STOPPED, 0);
			}
			bti.send(BluetoothCommands.straight(velocity));
		}
	}
	
	private void turn(int velocity, int angle) {
		if(running) {
			sendMotionEvent(Common.MOT_TURNING, velocity, angle);
			bti.send(BluetoothCommands.turn(velocity, angle));
		}
	}
	
	// Ramp linearly from min at a_smallturn to max at a_slowturn 
	public int TurnSpeed(int angle) {
		if(angle > A_slowturn) {
			return param.TURNSPEED_MAX;
		} else if(angle > A_smallturn && angle <= A_slowturn) {
			double m = (param.TURNSPEED_MAX - param.TURNSPEED_MIN)/(A_slowturn-A_smallturn);
			return param.TURNSPEED_MIN+ (int)((angle-A_smallturn)*m);
		} else {
			return param.TURNSPEED_MIN;
		}
	}
	private int LinSpeed(int distance) {
		if(distance > R_slowfwd) return param.LINSPEED_MAX;
		if(distance > R_goal && distance <= R_slowfwd) {
			double m = (param.LINSPEED_MAX - param.LINSPEED_MIN)/(1.0*(R_slowfwd - R_goal));
			return param.LINSPEED_MIN + (int)((distance-R_goal)*m);
		}
		return param.LINSPEED_MIN;
	}	
	
	
	// Detects an imminent collision with another robot
	private boolean collision() {
		ItemPosition me = mypos;
		PositionList others = gvh.gps.getPositions();
		for(ItemPosition current : others.getList()) {
			if(!current.name.equals(me.name)) {
				if(me.isFacing(current, 200) && me.distanceTo(current) < 475) {
					blocker = current;
					return true;
				}
			}
		}
		blocker = null;
		return false;
	}
}