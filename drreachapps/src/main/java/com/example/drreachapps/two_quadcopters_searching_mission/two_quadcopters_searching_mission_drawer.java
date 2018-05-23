package com.example.drreachapps.two_quadcopters_searching_mission;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import edu.illinois.mitra.starl.interfaces.LogicThread;
import edu.illinois.mitra.starl.objects.ItemPosition;
import edu.illinois.mitra.starlSim.draw.Drawer;

public class two_quadcopters_searching_mission_drawer extends Drawer {

    private Stroke stroke = new BasicStroke(8);
    private Color selectColor = new Color(0, 0, 255, 100);

    @Override
    public void draw(LogicThread lt, Graphics2D g) {
        two_quadcopters_searching_mission_app app = (two_quadcopters_searching_mission_app) lt;

        g.setColor(Color.RED);
        for (ItemPosition dest : app.destinations.values()) {
            g.fillRect(dest.getX() - 13, dest.getY() - 13, 26, 26);
        }

        g.setColor(selectColor);
        g.setStroke(stroke);
        if (app.currentDestination != null)
            g.drawOval(app.currentDestination.getX() - 20, app.currentDestination.getY() - 20, 40, 40);
    }
}