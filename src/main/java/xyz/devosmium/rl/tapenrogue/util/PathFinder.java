// Copyright (C) 2019 Owen Salter <owen@devosmium.xyz>
// 
// This file is part of tapenrogue.
// 
// tapenrogue is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// tapenrogue is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with tapenrogue.  If not, see <http://www.gnu.org/licenses/>.

package xyz.devosmium.rl.tapenrogue.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import xyz.devosmium.rl.tapenrogue.creatures.Creature;

public class PathFinder {
    private ArrayList<Point> open;
    private ArrayList<Point> closed;
    private HashMap<Point, Point> parents;
    private HashMap<Point, Integer> totalCost;

    /**
     * Creates a new PathFinder with everything needed for it
     */
    public PathFinder() {
        this.open = new ArrayList<Point>();
        this.closed = new ArrayList<Point>();
        this.parents = new HashMap<Point, Point>();
        this.totalCost = new HashMap<Point, Integer>();
    }

    /**
     * Computes the cost of moving from a point to a point
     * @param from The origin point
     * @param to The destination point
     * @see xyz.devosmium.rl.util.Point
     */
    private int heuristicCost(Point from, Point to) {
        return Math.max(Math.abs(from.x - to.x), Math.abs(from.y - to.y));
    }

    private int costToGetTo(Point from) {
        return parents.get(from) == null ? 0 : (1 + costToGetTo(parents.get(from)));
    }

    /**
     * Gets the total cost to get to a point from a point
     */
    private int totalCost(Point from, Point to) {
        if (totalCost.containsKey(from))
            return totalCost.get(from);

        int cost = costToGetTo(from) + heuristicCost(from, to);
        totalCost.put(from, cost);
        return cost;
    }

    private void reParent(Point child, Point parent) {
        parents.put(child, parent);
        totalCost.remove(child);
    }

    /***
     * Finds a path between an origin point and a destination point
     * @return An ArrayList of Points to follow to get to the destination
     * @param creature The creature to find a path for
     * @param start The origin point
     * @param end The destination
     * @param maxTries The maximum number of times to attempt to find a path
     * @author Owen Salter
     * @since 0.7.4
     * @see Creature
     * @see Point
     */
    public ArrayList<Point> findPath(Creature creature, Point start, Point end, int maxTries) {
        open.clear();
        closed.clear();
        parents.clear();
        totalCost.clear();

        open.add(start);

        for (int tries = 0; tries < maxTries && open.size() > 0; tries++) {
            Point closest = getClosestPoint(end);

            open.remove(closest);
            closed.add(closest);

            if (closest.equals(end))
                return createPath(start, closest);
            else
                checkNeighbors(creature, end, closest);
        }
        return null;
    }

    private Point getClosestPoint(Point end) {
        Point closest = open.get(0);
        for (Point other : open) {
            if (totalCost(other, end) < totalCost(closest, end))
                closest = other;
        }
        return closest;
    }

    private void checkNeighbors(Creature creature, Point end, Point closest) {
        for (Point neighbor : closest.neighbors8()) {
            if (closed.contains(neighbor)
                    || !creature.canEnter(neighbor.x, neighbor.y, creature.z) && !neighbor.equals(end))
                continue;

            if (open.contains(neighbor))
                reParentNeighborIfNecessary(closest, neighbor);
            else
                reParentNeighbor(closest, neighbor);
        }
    }

    private void reParentNeighbor(Point closest, Point neighbor) {
        reParent(neighbor, closest);
        open.add(neighbor);
    }

    private void reParentNeighborIfNecessary(Point closest, Point neighbor) {
        Point originalParent = parents.get(neighbor);
        double currentCost = costToGetTo(neighbor);
        reParent(neighbor, closest);
        double reparentCost = costToGetTo(neighbor);

        if (reparentCost < currentCost)
            open.remove(neighbor);
        else
            reParent(neighbor, originalParent);
    }

    private ArrayList<Point> createPath(Point start, Point end) {
        ArrayList<Point> path = new ArrayList<Point>();

        while (!end.equals(start)) {
            path.add(end);
            end = parents.get(end);
        }

        Collections.reverse(path);
        return path;
    }
}