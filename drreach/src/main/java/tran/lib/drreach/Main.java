package tran.lib.drreach;

import edu.illinois.mitra.starlSim.main.SimSettings;
import edu.illinois.mitra.starlSim.main.Simulation;

public class Main {

	public static void main(String[] args) {
		SimSettings.Builder settings = new SimSettings.Builder();
		settings.OBSPOINT_FILE("Obstacles.wpt");
		settings.N_o3DR(0);
		settings.OBSPOINT_FILE("");
		settings.N_IROBOTS(0);
		settings.N_QUADCOPTERS(1);
		settings.N_GHOSTS(0);
		settings.N_MAVICS(0);
		settings.GPS_POSITION_NOISE(4);
		settings.TIC_TIME_RATE(1);
        //settings.WAYPOINT_FILE("four.wpt");
        //settings.INITIAL_POSITIONS_FILE("start.wpt");
        settings.DRAW_TRACE_LENGTH(-1);
		settings.DRAW_WAYPOINTS(false);
		settings.DRAW_WAYPOINT_NAMES(false);
		settings.DRAWER(new drreachDrawer());
		settings.DRAW_TRACE(true);
		//settings.TRACE_OUT_DIR("/home/trhoangdung/tools/starl");
		settings.DRAW__ROBOT_TYPE(true);
		//
		Simulation sim = new Simulation(drreachApp.class, settings.build());
		sim.start();
	}

}
