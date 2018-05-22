package tran.lib.drreach.drreachApps.one_drone_go_to_waypoints;

import edu.illinois.mitra.starlSim.main.SimSettings;
import edu.illinois.mitra.starlSim.main.Simulation;
import tran.lib.drreach.drreachApp;
import tran.lib.drreach.drreachDrawer;

public class run_this_app {

	public static void main(String[] args) {
		SimSettings.Builder settings = new SimSettings.Builder();
		settings.OBSPOINT_FILE("Obstacles.wpt");
		settings.N_o3DR(0);
		settings.OBSPOINT_FILE("");
		settings.N_IROBOTS(0);
		settings.N_QUADCOPTERS(0);
		settings.N_GHOSTS(0);
		settings.N_MAVICS(0);
		settings.GPS_POSITION_NOISE(4);
		settings.TIC_TIME_RATE(1);
        settings.DRAW_TRACE_LENGTH(-1);
		settings.DRAW_WAYPOINTS(false);
		settings.DRAW_WAYPOINT_NAMES(false);
		settings.DRAWER(new one_drone_go_to_waypoints_drawer());
		settings.DRAW_TRACE(true);
		settings.DRAW__ROBOT_TYPE(true);

		//
		Simulation sim = new Simulation(one_drone_go_to_waypoints.class, settings.build());
		sim.start();
	}

}
