package edu.illinois.mitra.starl.gvh;

import edu.illinois.mitra.starl.interfaces.TrackedRobot;
import edu.illinois.mitra.starl.motion.ReachAvoid;
import edu.illinois.mitra.starl.motion.RobotMotion;
import tran.lib.drreach.drreachComputation.FaceLiftingResult;

/**
 * Stub class implementing platform specific methods.
 * 
 * @author Adam Zimmerman
 * @version 1.0
 *
 */
public class AndroidPlatform {
	
	public ReachAvoid reachAvoid;
	
	public RobotMotion moat;
	
	public TrackedRobot model;

	public FaceLiftingResult reachset; // Dung Tran: 5/26/2018
		
    public void setDebugInfo(String debugInfo) {
	}
	
	public void sendMainToast(String debugInfo) {
	}
	
	public void sendMainMsg(int type, Object data) {
	}
	
	public void sendMainMsg(int type, int arg1, int arg2) {		
	}

	public TrackedRobot getModel() {
		return model;
	}

	public FaceLiftingResult getReachset(){return reachset;} // Dung Tran: 5/26/2018

	public void setReachset(FaceLiftingResult rs){reachset = rs;}
}
