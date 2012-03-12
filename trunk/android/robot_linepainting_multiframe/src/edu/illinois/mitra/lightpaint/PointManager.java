package edu.illinois.mitra.lightpaint;

import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.illinois.mitra.lightpaint.main.LogicThread;
import edu.illinois.mitra.starl.comms.RobotMessage;
import edu.illinois.mitra.starl.objects.globalVarHolder;
import edu.illinois.mitra.starl.objects.itemPosition;

public class PointManager {
	private static final String TAG = "DivideLines";
	private static final String ERR = "Critical Error";
	
	private globalVarHolder gvh;
	
	private HashSet<ImagePoint> startingPoints;
	private SortedSet<ImagePoint>[] points;	
	private int numMutex = -1;
	
	public PointManager(globalVarHolder gvh) {
		this.gvh = gvh;
		startingPoints = new HashSet<ImagePoint>();
		points = new TreeSet[gvh.getParticipants().size()];
		for(int i = 0; i<gvh.getParticipants().size(); i++) {
			points[i] = new TreeSet<ImagePoint>();
		}
	}
	
	public void parseWaypoints() {
		for(itemPosition i : gvh.getWaypointPositions().getList()) {
			try {
				ImagePoint next = new ImagePoint(i);
				points[next.robot].add(next);
				if(next.start) startingPoints.add(next);
				numMutex = Math.max(numMutex, next.mutex+1);
			} catch (ImproperWaypointException e) {
				// The waypoint isn't for an image, skip it
			}
		}
	}
		
	public void Assign() {
		if(startingPoints.size() != gvh.getParticipants().size()) {
			throw new RuntimeException("Different numbers of participants and starting points!");
		}
		
		// Find the closest robot to each starting position
		String[] robots = gvh.getParticipants().toArray(new String[0]);
		for(ImagePoint i : startingPoints) {
			int closestIdx = closestRobot(robots, i.pos);
			String current = robots[closestIdx];
			RobotMessage sendAssignment = new RobotMessage(current, gvh.getName(), LogicThread.MSG_INFORMLINE, Integer.toString(i.robot));
			gvh.addOutgoingMessage(sendAssignment);
			robots[closestIdx] = "";
		}
	}
	
	public SortedSet<ImagePoint> getPoints(int robot) {
		return points[robot];
	}
	
	public int getNumMutex() {
		return numMutex;
	}
	
	// Given an array of robot names and a location, returns the index of the closest robot to that location
	private int closestRobot(String[] robots, itemPosition pos) {
		int minDist = 9999999;
		int minidx = -1;
		int dist = 0;
		itemPosition start = null;
		
		for(int i = 0; i < robots.length; i ++) {
			if(!robots[i].equals("")) {
				dist = gvh.getPosition(robots[i]).distanceTo(start);
				if(dist < minDist) {
					minDist = dist;
					minidx = i;
				}
			}
		}
		return minidx;
	}
}