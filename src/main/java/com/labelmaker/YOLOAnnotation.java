package net.runelite.client.plugins.labelmaker;

public class YOLOAnnotation {
    private int classId;
    private double x; // Center x-coordinate (normalized)
    private double y; // Center y-coordinate (normalized)
    private double width;
    private double height;

    // Constructor
    public YOLOAnnotation(int classId, double x, double y, double width, double height, int window_width, int window_height) {
        this.classId = classId;
        this.x = x / window_width;
        this.y = y / window_height;
        this.width = width / window_width;
        this.height = height / window_height;
    }


    @Override
    public String toString() {
        return classId + " " + x + " " + y + " " + width + " " + height;
    }
}
