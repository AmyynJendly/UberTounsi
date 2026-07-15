package com.covoitdark.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * CO2 savings, money savings, and trees equivalent calculator
 * based on Tunisian city distances.
 */
public class CO2Calculator {

    /** Average car CO2 emissions in grams per km */
    private static final double CO2_PER_KM_G = 120.0;

    /** Fuel cost estimate per km in TND */
    private static final double COST_PER_KM_TND = 0.18;

    /** City-pair distance map (km). Key: "CITY1|CITY2" (alphabetical order) */
    private static final Map<String, Integer> DISTANCES = new HashMap<>();

    static {
        addDistance("Tunis", "Sfax", 270);
        addDistance("Tunis", "Sousse", 140);
        addDistance("Tunis", "Bizerte", 65);
        addDistance("Tunis", "Kairouan", 160);
        addDistance("Tunis", "Gabès", 400);
        addDistance("Tunis", "Gafsa", 350);
        addDistance("Tunis", "Nabeul", 75);
        addDistance("Tunis", "Monastir", 160);
        addDistance("Tunis", "Mahdia", 200);
        addDistance("Tunis", "Kasserine", 280);
        addDistance("Tunis", "Sidi Bouzid", 260);
        addDistance("Tunis", "Jendouba", 155);
        addDistance("Tunis", "Kef", 170);
        addDistance("Tunis", "Siliana", 135);
        addDistance("Tunis", "Zaghouan", 55);
        addDistance("Tunis", "Ben Arous", 10);
        addDistance("Tunis", "Ariana", 8);
        addDistance("Tunis", "Manouba", 15);
        addDistance("Tunis", "Médenine", 500);
        addDistance("Tunis", "Tataouine", 570);
        addDistance("Tunis", "Tozeur", 450);
        addDistance("Tunis", "Kébili", 480);
        addDistance("Sfax", "Sousse", 130);
        addDistance("Sfax", "Gabès", 130);
        addDistance("Sfax", "Mahdia", 75);
        addDistance("Sfax", "Kairouan", 120);
        addDistance("Sfax", "Médenine", 230);
        addDistance("Sfax", "Gafsa", 200);
        addDistance("Sousse", "Monastir", 20);
        addDistance("Sousse", "Kairouan", 60);
        addDistance("Sousse", "Mahdia", 60);
        addDistance("Gabès", "Médenine", 120);
        addDistance("Gabès", "Gafsa", 170);
        addDistance("Médenine", "Tataouine", 80);
        addDistance("Kasserine", "Gafsa", 90);
        addDistance("Kasserine", "Sidi Bouzid", 80);
        addDistance("Kef", "Jendouba", 60);
        addDistance("Tozeur", "Kébili", 90);
        addDistance("Tozeur", "Gafsa", 100);
    }

    private static void addDistance(String city1, String city2, int km) {
        String key = city1.compareTo(city2) < 0 ? city1 + "|" + city2 : city2 + "|" + city1;
        DISTANCES.put(key, km);
    }

    /**
     * Get distance between two cities in km. Returns 100 if unknown.
     */
    public static int getDistance(String city1, String city2) {
        if (city1 == null || city2 == null) return 100;
        if (city1.equalsIgnoreCase(city2)) return 0;
        String key = city1.compareTo(city2) < 0 ? city1 + "|" + city2 : city2 + "|" + city1;
        return DISTANCES.getOrDefault(key, 100);
    }

    /**
     * CO2 saved in kg when 'passengers' share a car over 'distance' km.
     * Each passenger avoids driving their own car.
     */
    public static double calculateCO2Saved(String departure, String arrival, int passengers) {
        int distance = getDistance(departure, arrival);
        return (CO2_PER_KM_G * distance * passengers) / 1000.0;
    }

    /**
     * Money saved in TND by sharing the trip cost.
     * If each passenger would have driven alone, they save fuel cost.
     */
    public static double calculateMoneySaved(String departure, String arrival, int passengers) {
        int distance = getDistance(departure, arrival);
        return COST_PER_KM_TND * distance * passengers;
    }

    /**
     * Number of trees needed to absorb that CO2 in one year.
     * 1 tree absorbs ~22 kg CO2/year.
     */
    public static double treesEquivalent(double co2Kg) {
        return co2Kg / 22.0;
    }

    /** All Tunisian cities available in the system */
    public static String[] getTunisianCities() {
        return new String[]{
            "Tunis", "Sfax", "Sousse", "Bizerte", "Kairouan", "Gabès", "Gafsa",
            "Nabeul", "Monastir", "Mahdia", "Kasserine", "Sidi Bouzid", "Jendouba",
            "Kef", "Siliana", "Zaghouan", "Ben Arous", "Ariana", "Manouba",
            "Médenine", "Tataouine", "Tozeur", "Kébili"
        };
    }
}
