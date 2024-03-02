package net.runelite.client.plugins.labelmaker;

public class YOLOAnnotation {
    private int classId;
    private double x; // Center x-coordinate (normalized)
    private double y; // Center y-coordinate (normalized)
    private double width;
    private double height;

    // Constructor
    public YOLOAnnotation(int classId, double x, double y, double width, double height) {
        this.classId = classId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


    @Override
    public String toString() {
        return classId + " " + x + " " + y + " " + width + " " + height;
    }
}
