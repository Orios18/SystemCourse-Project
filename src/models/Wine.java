package models;

public class Wine {
    private String quality;
    private String color;

    public Wine(String quality, String color) {
        this.quality = quality;
        this.color = color;
    }

    // Getters and Setters
    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
