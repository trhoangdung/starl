package edu.illinois.mitra.demo.follow;

import edu.illinois.mitra.starlSim.main.SimSettings;
import edu.illinois.mitra.starlSim.main.Simulation;

public class Main {

	public static void main(String[] args) {
		SimSettings.Builder settings = new SimSettings.Builder();
        settings.N_IROBOTS(0);
		settings.N_GHOSTS(0);
		settings.N_IROBOTS(0);
		settings.N_MAVICS(0);
		settings.N_QUADCOPTERS(1);
		settings.TIC_TIME_RATE(5);
		settings.WAYPOINT_FILE("square.wpt");
        //settings.WAYPOINT_FILE("sphere.wpt");
		settings.DRAW_WAYPOINTS(false);
		settings.DRAW_WAYPOINT_NAMES(false);
		settings.DRAWER(new FollowDrawer());
		
		Simulation sim = new Simulation(FollowApp.class, settings.build());
		sim.start();
	}

}
