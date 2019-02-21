package edu.illinois.mitra.starl.motion;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import edu.illinois.mitra.starl.gvh.GlobalVarHolder;
import edu.illinois.mitra.starl.interfaces.RobotEventListener.Event;
import edu.illinois.mitra.starl.models.Model_quadcopter;
import edu.illinois.mitra.starl.objects.*;

/**
 * This motion controller is for quadcopter models only
 *
 * Motion controller which extends the RobotMotion abstract class. Capable of
 * going to destination or passing through a destination without stopping.
 * Includes optional collision avoidance which is controlled
 * by the motion parameters setting.
 *
 * @author Yixiao Lin
 * @version 1.0
 */

import net.sourceforge.interval.ia_math.RealInterval;

import java.util.concurrent.BlockingQueue;

import edu.illinois.mitra.starl.drreach.drreachComputation.ComputationSetting;
import edu.illinois.mitra.starl.drreach.drreachComputation.FaceLifting;
import edu.illinois.mitra.starl.drreach.drreachComputation.FaceLiftingResult;
import edu.illinois.mitra.starl.drreach.drreachComputation.FaceLifting_for_Quadcopter;
import edu.illinois.mitra.starl.drreach.drreachComputation.HyperRectangle;
import edu.illinois.mitra.starl.drreach.drreachComputation.Interval;
import edu.illinois.mitra.starl.drreach.drreachComputation.LiftingSettings;
import edu.illinois.mitra.starl.drreach.drreachComputation.SingleLiftingResult;
import edu.illinois.mitra.starl.drreach.drreachComputation.UnsafeSet;

/**
 * This is revised by Dung Tran to implement real-time reachability analysis using face-lifting method
 * Dung Tran: 5/24/2018
 */


public class MotionAutomaton_quadcopter extends RobotMotion {
	protected static final String TAG = "MotionAutomaton";
	protected static final String ERR = "Critical Error";
	final int safeHeight = 500;

	protected GlobalVarHolder gvh;
	protected BluetoothInterface bti;

	// Motion tracking
	protected ItemPosition destination;
	private Model_quadcopter mypos;

	protected enum STAGE {
		INIT, MOVE, HOVER, TAKEOFF, LAND, GOAL, STOP
	}

	private STAGE next = null;
	protected STAGE stage = STAGE.INIT;
	private STAGE prev = null;
	protected boolean running = false;
	boolean colliding = false;

	private enum OPMODE {
		GO_TO
	}

	private OPMODE mode = OPMODE.GO_TO;

	private static final MotionParameters DEFAULT_PARAMETERS = MotionParameters.defaultParameters();
	private volatile MotionParameters param = DEFAULT_PARAMETERS;
	//need to pass some more parameteres into this param
	//	MotionParameters.Builder settings = new MotionParameters.Builder();


	//	private volatile MotionParameters param = settings.build();

	public MotionAutomaton_quadcopter(GlobalVarHolder gvh, BluetoothInterface bti) {
		super(gvh.id.getName());
		this.gvh = gvh;
		this.bti = bti;

	}

	public void goTo(ItemPosition dest, ObstacleList obsList) {
		goTo(dest);
	}

	public void goTo(ItemPosition dest) {
		if((inMotion && !this.destination.equals(dest)) || !inMotion) {
			done = false;
			this.destination = new ItemPosition(dest.name,dest.x,dest.y,dest.z);
			//this.destination = dest;
			this.mode = OPMODE.GO_TO;
			startMotion();
		}
	}

	@Override
	public synchronized void start() {
		super.start();
		gvh.log.d(TAG, "STARTED!");
	}

	@Override
	public void run() {
		super.run();
		gvh.threadCreated(this);
		// some control parameters
		double kpx,kpy,kpz, kdx,kdy,kdz;
		kpx = kpy = kpz = 0.00033;
		kdx = kdy = kdz = 0.0006;

		while(true) {
			//			gvh.gps.getObspointPositions().updateObs();
			if(running) {
				mypos = (Model_quadcopter)gvh.plat.getModel();
//				System.out.println(mypos.toString());
				//System.out.printf("mypos (%d, %d) \n", mypos.x, mypos.y);
				//System.out.printf("pitch = (%f) \n", mypos.pitch);
				//System.out.printf("roll = (%f) \n", mypos.roll);
				//System.out.printf("yaw = (%f) \n", mypos.yaw);
				//System.out.printf("v_x = %f \n", mypos.v_x);
				//System.out.printf("v_y = %f \n", mypos.v_y);

				//System.out.printf("destination (%d, %d) \n", destination.x, destination.y);
				int distance = (int) Math.sqrt(Math.pow((mypos.x - destination.x),2) + Math.pow((mypos.y - destination.y), 2));
				//System.out.println("distance:" + distance);
				//int distance = mypos.distanceTo(destination);
				if(mypos.gaz < -50){
					//		System.out.println("going down");
				}
				colliding = (stage != STAGE.LAND && mypos.gaz < -50);

				if(!colliding && stage != null) {
					switch(stage) {
						case INIT:
							if(mode == OPMODE.GO_TO) {
								if(mypos.z < safeHeight){
									// just a safe distance from ground
									takeOff();
									next = STAGE.TAKEOFF;
								}
								else{
									if(distance <= param.GOAL_RADIUS) {
										//System.out.println(">>>Distance: " + distance + " - GOAL_RADIUS " + param.GOAL_RADIUS);
										next = STAGE.GOAL;
									}
									else{
										next = STAGE.MOVE;
									}
								}
							}
							break;
						case MOVE:
							if(mypos.z < safeHeight){
								// just a safe distance from ground
								takeOff();
								next = STAGE.TAKEOFF;
								break;
							}
							if(distance <= param.GOAL_RADIUS) {
								//System.out.println(">>>Distance: " + distance + " - GOAL_RADIUS " + param.GOAL_RADIUS);
								next = STAGE.GOAL;
							}
							else{
								double Ax_d, Ay_d = 0.0;
								double Ryaw, Rroll, Rpitch, Rvs, Ryawsp = 0.0;
								//		System.out.println(destination.x - mypos.x + " , " + mypos.v_x);
								Ax_d = (kpx * (destination.x - mypos.x) - kdx * mypos.v_x) ;
								Ay_d = (kpy * (destination.y - mypos.y) - kdy * mypos.v_y) ;
								Ryaw = Math.atan2(destination.y - mypos.y, destination.x - mypos.x);
								//Ryaw = Math.atan2((destination.y - mypos.x), (destination.x - mypos.y));
								Ryawsp = kpz * ((Ryaw - Math.toRadians(mypos.yaw)));
								Rroll = Math.asin((Ay_d * Math.cos(Math.toRadians(mypos.yaw)) - Ax_d * Math.sin(Math.toRadians(mypos.yaw))) %1);
								Rpitch = Math.asin( (-Ay_d * Math.sin(Math.toRadians(mypos.yaw)) - Ax_d * Math.cos(Math.toRadians(mypos.yaw))) / (Math.cos(Rroll)) %1);
								Rvs = (kpz * (destination.z - mypos.z) - kdz * mypos.v_z);

								//System.out.println(Ryaw + " , " + Ryawsp + " , " +  Rroll  + " , " +  Rpitch + " , " + Rvs);
								//System.out.print("Rpitch = "+Rpitch + "\n");
								//System.out.print("Rroll = "+Rroll + "\n");

								setControlInputRescale(Math.toDegrees(Ryawsp),Math.toDegrees(Rpitch)%360,Math.toDegrees(Rroll)%360,Rvs);

							}
							break;
						case HOVER:
							setControlInput(0,0,0, 0);
							// do nothing
							break;
						case TAKEOFF:
							switch(mypos.z/(safeHeight/2)){
								case 0:// 0 - 1/2 safeHeight
									setControlInput(0,0,0,1);
									break;
								case 1: // 1/2- 1 safeHeight
									setControlInput(0,0,0, 0.5);
									break;
								default: // above safeHeight:
									hover();
									if(prev != null){
										next = prev;
									}
									else{
										next = STAGE.HOVER;
									}
									break;
							}
							break;
						case LAND:
							switch(mypos.z/(safeHeight/2)){
								case 0:// 0 - 1/2 safeHeight
									setControlInput(0,0,0,0);
									next = STAGE.STOP;
									break;
								case 1: // 1/2- 1 safeHeight
									setControlInput(0,0,0, -0.05);
									break;
								default:   // above safeHeight
									setControlInput(0,0,0,-0.5);
									break;
							}
							break;
						case GOAL:
							//System.out.println("Done flag");
							done = true;
							gvh.log.i(TAG, "At goal!");
							gvh.log.i("DoneFlag", "write");
							if(param.STOP_AT_DESTINATION){
								hover();
								next = STAGE.HOVER;
							}
							running = false;
							inMotion = false;
							break;
						case STOP:
							gvh.log.i("FailFlag", "write");
							System.out.println("STOP");
							motion_stop();
							//do nothing
					}
					if(next != null) {
						prev = stage;
						stage = next;
//						System.out.println("Stage transition to " + stage.toString() + ", the previous stage is "+ prev);

						gvh.log.i(TAG, "Stage transition to " + stage.toString());
						gvh.trace.traceEvent(TAG, "Stage transition", stage.toString(), gvh.time());
					}
					next = null;
				}

				if((colliding || stage == null) ) {
					gvh.log.i("FailFlag", "write");
					done = false;
					motion_stop();
					//	land();
					//	stage = STAGE.LAND;
				}
			}

			if (stage == STAGE.MOVE){
				// step 1 : get initial set
				// step 2 : lifting setting
				// step 3 : call face lifting
				// step 4 : display result
				double noise_percent = 0.02; // accuracy sensor

				HyperRectangle init_rect = get_init_set(mypos.x, mypos.v_x, mypos.y, mypos.v_y, noise_percent);
				LiftingSettings lift_setting = get_lifting_setting(init_rect);
				FaceLiftingResult rs = call_face_lifting(lift_setting, mypos.pitch, mypos.roll);
				System.out.print(gvh.id.getName() + " finishes computing reach set and stores the reach set \n");
				gvh.plat.setReachset(rs);

			}

			//gvh.sleep(param.AUTOMATON_PERIOD);
			gvh.sleep(200);
		}
	}

	public void cancel() {
		running = false;
		bti.disconnect();
	}

	@Override
	public void motion_stop() {
		//land();
		//stage = STAGE.LAND;
		this.destination = null;
		running = false;
		inMotion = false;
	}

	public void takePicture(){}

	@Override
	public void motion_resume() {
		running = true;
	}

	private void startMotion() {
		running = true;
		stage = STAGE.INIT;
		inMotion = true;
	}

	protected void sendMotionEvent(int motiontype, int... argument) {
		// TODO: This might not be necessary
		gvh.trace.traceEvent(TAG, "Motion", Arrays.toString(argument), gvh.time());
		gvh.sendRobotEvent(Event.MOTION, motiontype);
	}

	private void setControlInputRescale(double yaw_v, double pitch, double roll, double gaz){
		setControlInput(rescale(yaw_v, mypos.max_yaw_speed), rescale(pitch, mypos.max_pitch_roll), rescale(roll, mypos.max_pitch_roll), rescale(gaz, mypos.max_gaz));
	}

	private double rescale(double value, double max_value){
		if(Math.abs(value) > max_value){
			return (Math.signum(value));
		}
		else{
			return value/max_value;
		}
	}

	protected void setControlInput(double yaw_v, double pitch, double roll, double gaz){
		if(yaw_v > 1 || yaw_v < -1){
			throw new IllegalArgumentException("yaw speed must be between -1 to 1");
		}
		if(pitch > 1 || pitch < -1){
			throw new IllegalArgumentException("pitch must be between -1 to 1");
		}
		if(roll > 1 || roll < -1){
			throw new IllegalArgumentException("roll speed must be between -1 to 1");
		}
		if(gaz > 1 || gaz < -1){
			throw new IllegalArgumentException("gaz, vertical speed must be between -1 to 1");
		}
		//Bluetooth command to control the drone
		//	gvh.log.i(TAG, "control input as, yaw, pitch, roll, thrust " + yaw_v + ", " + pitch + ", " +roll + ", " +gaz);
		/*
		if(running) {
			if(velocity != 0) {
				sendMotionEvent(Common.MOT_STRAIGHT, velocity);
			} else {
				sendMotionEvent(Common.MOT_STOPPED, 0);
			}
			bti.send(BluetoothCommands.straight(velocity));
		}
		 */
	}

	/**
	 *  	take off from ground
	 */
	protected void takeOff(){
		//Bluetooth command to control the drone
		gvh.log.i(TAG, "Drone taking off");
	}

	/**
	 * land on the ground
	 */
	protected void land(){
		//Bluetooth command to control the drone
		gvh.log.i(TAG, "Drone landing");
	}

	/**
	 * hover at current position
	 */
	protected void hover(){
		//Bluetooth command to control the drone
		gvh.log.i(TAG, "Drone hovering");
	}

	@Override
	public void turnTo(ItemPosition dest) {
		throw new IllegalArgumentException("quadcopter does not have a corresponding turn to");
	}

	@Override
	public void setParameters(MotionParameters param) {
		// TODO Auto-generated method stub
	}


	/**
	 * Slow down linearly upon coming within R_slowfwd of the goal
	 *
	 * @param distance
	 * @return
	 */
	/*
	private int LinSpeed(int distance) {
		if(distance > param.SLOWFWD_RADIUS)
			return param.LINSPEED_MAX;
		if(distance > param.GOAL_RADIUS && distance <= param.SLOWFWD_RADIUS) {
			return param.LINSPEED_MIN + (int) ((distance - param.GOAL_RADIUS) * linspeed);
		}
		return param.LINSPEED_MIN;
	}

	// Detects an imminent collision with another robot or with any obstacles

	@Override
	public void setParameters(MotionParameters param) {
		this.param = param;/		this.linspeed = (double) (param.LINSPEED_MAX - param.LINSPEED_MIN) / Math.abs((param.SLOWFWD_RADIUS - param.GOAL_RADIUS));
		this.turnspeed = (param.TURNSPEED_MAX - param.TURNSPEED_MIN) / (param.SLOWTURN_ANGLE - param.SMALLTURN_ANGLE);
	}
	 */

	// ---------------------------------------------------------------------------------------//
	// --------------------------------------------------------------------------------------//
	// ------------- THIS FOR REACHABLE SET COMPUTATION USING FACE-LIFTING METHOD ----------//
	// DUNG TRAN: 5/22/2018

	private HyperRectangle get_init_set(double x, double v_x, double y, double v_y, double noise_percent){
		// create initial set for reachable set computation using face-lifting method
		// Dung Tran: 5/22/2018
		// @input: current position x, y and current velocities v_x, v_y
		// @input: noise assumed in the sensor inputs due to noise or delays
		// For example, the real position x is in this range: [x - noise_percent * abs(x), x + noise_percent * abs(x)]

		double[] min_vec = {x - noise_percent * Math.abs(x), v_x - noise_percent * Math.abs(v_x), y - noise_percent * Math.abs(y), v_y - noise_percent * Math.abs(v_y)};
		double[] max_vec = {x + noise_percent * Math.abs(x), v_x + noise_percent * Math.abs(v_x), y + noise_percent * Math.abs(y), v_y + noise_percent * Math.abs(v_y)};

		HyperRectangle init_set = new HyperRectangle(Interval.vector2intervals(min_vec, max_vec));

		return init_set;
	}

	private RealInterval get_current_pitch_set(double pitch){
		// get current pitch interval for computing reachable set using face-lifting method
		// Dung Tran: 5/22/2018

		RealInterval pitch_set = new RealInterval(pitch);

		return pitch_set;
	}

	private RealInterval get_current_roll_set(double roll){
		// get current roll interval for computing reachable set using face-lifting method
		// Dung Tran: 5/22/2018

		RealInterval roll_set = new RealInterval(roll);

		return roll_set;

	}
	private LiftingSettings get_lifting_setting(HyperRectangle init_rect){
		// setting for face-lifting method
		// Dung Tran: 5/22/2018
		double initialStepSize = 0.01;
		double reachTime = 2.0;
		long max_runtime_milliseconds = 10;
		int dynamics_index = 0; // linear pendulum dynamics
		double max_rect_width_before_error = 100;

		// unsafe set: v_x <= 500, [x, v_x, y, v_y]^T

		ComputationSetting cs = new ComputationSetting();
		Double NegInfinity = cs.DBL_MIN;
		Double PosInfinity = cs.DBL_MAX;
		RealInterval velocity_const = new RealInterval(NegInfinity, 500.0);
		UnsafeSet unsafe_set = new UnsafeSet(1, velocity_const);

		LiftingSettings setting = new LiftingSettings(init_rect, reachTime, initialStepSize, max_rect_width_before_error, max_runtime_milliseconds, dynamics_index, unsafe_set);

		return setting;
	}

	private FaceLiftingResult call_face_lifting(LiftingSettings setting, double current_pitch, double current_roll){
		// call face-lifting method
		// Dung Tran: 5/22/2018

		FaceLifting_for_Quadcopter FL = new FaceLifting_for_Quadcopter();
		long time_offset_between_gvh_and_system_time = gvh.time() - System.currentTimeMillis(); // This is due to simulation time is different from system current time
		FaceLiftingResult rs = FL.face_lifting_iterative_improvement(System.currentTimeMillis(), setting, current_pitch, current_roll);

		// may effect to control performance, do not use in general

		return rs;

	}

	private void display_reach_set(FaceLiftingResult rs){

		System.out.print("Reachable set results\n");
	}

}
