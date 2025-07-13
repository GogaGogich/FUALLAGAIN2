package com.example.laboratory.utils;

import org.bukkit.Location;

public class LocationUtils {
    
    public static String locationToString(Location location) {
        if (location == null) return "Unknown";
        
        return String.format("%s: %d, %d, %d", 
            location.getWorld().getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        );
    }
    
    public static String formatCoordinates(Location location) {
        if (location == null) return "Unknown";
        
        return String.format("§7%d, %d, %d", 
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        );
    }
    
    public static double getDistance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return Double.MAX_VALUE;
        if (!loc1.getWorld().equals(loc2.getWorld())) return Double.MAX_VALUE;
        
        return loc1.distance(loc2);
    }
    
    public static boolean isInRange(Location center, Location target, double range) {
        return getDistance(center, target) <= range;
    }
}