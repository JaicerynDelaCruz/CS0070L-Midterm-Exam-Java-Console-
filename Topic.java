/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jaiceryn F Dela Cruz
 */
/**
 * A programming/web development learning category (C++, JavaScript, Python, HTML, CSS).
 */
public class Topic {
    private final String name;
    private final String description;
    private final String commonUses;
    private final String recommendedLevel;

    public Topic(String name, String description, String commonUses, String recommendedLevel) {
        this.name = name;
        this.description = description;
        this.commonUses = commonUses;
        this.recommendedLevel = recommendedLevel;
    }

    public String getName() {
        return name;
    }

    public void display() {
        System.out.println("\n--- " + name + " ---");
        System.out.println("Description      : " + description);
        System.out.println("Common uses      : " + commonUses);
        System.out.println("Recommended level: " + recommendedLevel);
    }
}