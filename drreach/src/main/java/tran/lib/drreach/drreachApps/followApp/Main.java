package tran.lib.drreach.drreachApps.followApp;

// Follow App for real-time reachability analysis
// Dung Tran: 5/22/2018

import edu.illinois.mitra.starlSim.main.SimSettings;
import edu.illinois.mitra.starlSim.main.Simulation;

public class Main {

	public static void main(String[] args) {
		SimSettings.Builder settings = new SimSettings.Builder();
        settings.N_IROBOTS(0);
		settings.N_GHOSTS(0);
		settings.N_IROBOTS(0);
		settings.N_MAVICS(1); // only work for one drone at one time // TODO: can we have two drones perform different missions?
		//TODO: for examples, one drone follows circle waypoints, while the other one follows square waypoints.
		// May be this depends on how starL currently simulate the system

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
