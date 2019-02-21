package com.example.drreachapps.multiple_quadcopters_formation_fly_mission;

import edu.illinois.mitra.starlSim.main.SimSettings;
import edu.illinois.mitra.starlSim.main.Simulation;

public class Main {
    public static void main(String[] args) {
        SimSettings.Builder settings = new SimSettings.Builder();
        settings.N_IROBOTS(0);
        settings.N_GHOSTS(0);
        settings.N_IROBOTS(0);
        settings.N_MAVICS(0);
        settings.N_QUADCOPTERS(8); // set from 1 to 8
        settings.TIC_TIME_RATE(5);
        settings.WAYPOINT_FILE("multiple_quadcopters_searching_mission.wpt");
        settings.INITIAL_POSITIONS_FILE("multiple_quadcopters_searching_mission_start.wpt");
        settings.DRAW_WAYPOINTS(false);
        settings.DRAW_WAYPOINT_NAMES(false);
        settings.DRAWER(new multiple_quadcopters_searching_mission_drawer());

        Simulation sim = new Simulation(multiple_quadcopters_searching_mission_app.class, settings.build());
        sim.start();
    }
}
