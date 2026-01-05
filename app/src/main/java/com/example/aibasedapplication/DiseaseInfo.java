package com.example.aibasedapplication;

import java.util.HashMap;

public class DiseaseInfo {

    private static HashMap<String, String[]> diseaseMap;

    static {
        diseaseMap = new HashMap<>();

        // ✅ Pepper Bell
        diseaseMap.put("Pepper__bell___Bacterial_spot", new String[]{
                "Remove infected leaves, apply copper-based fungicide",
                "Rotate crops, avoid overhead watering"
        });
        diseaseMap.put("Pepper__bell___healthy", new String[]{
                "No treatment needed",
                "Maintain proper watering and care"
        });

        // ✅ Potato
        diseaseMap.put("Potato___Early_blight", new String[]{
                "Apply copper fungicide, remove infected leaves",
                "Use resistant varieties, rotate crops annually"
        });
        diseaseMap.put("Potato___Late_blight", new String[]{
                "Spray fungicide containing Mancozeb",
                "Avoid water on leaves, rotate crops"
        });
        diseaseMap.put("Potato___healthy", new String[]{
                "No treatment needed",
                "Maintain good soil and watering practices"
        });

        // ✅ Tomato
        diseaseMap.put("Tomato_Bacterial_spot", new String[]{
                "Remove infected leaves, apply copper fungicide",
                "Avoid overhead watering, rotate crops"
        });
        diseaseMap.put("Tomato_Early_blight", new String[]{
                "Use fungicide spray, remove infected leaves",
                "Plant resistant varieties, rotate crops"
        });
        diseaseMap.put("Tomato_Late_blight", new String[]{
                "Spray fungicide with Mancozeb",
                "Avoid wet leaves, crop rotation"
        });
        diseaseMap.put("Tomato_Leaf_Mold", new String[]{
                "Remove affected leaves, use fungicide",
                "Ensure proper spacing for airflow"
        });
        diseaseMap.put("Tomato_Septoria_leaf_spot", new String[]{
                "Remove infected leaves, apply fungicide",
                "Avoid wetting leaves, rotate crops"
        });
        diseaseMap.put("Tomato_Spider_mites_Two_spotted_spider_mite", new String[]{
                "Use miticide or insecticidal soap",
                "Keep humidity up, remove weeds"
        });
        diseaseMap.put("Tomato__Target_Spot", new String[]{
                "Remove infected parts, apply fungicide",
                "Avoid overhead watering"
        });
        diseaseMap.put("Tomato__Tomato_YellowLeaf__Curl_Virus", new String[]{
                "No chemical treatment, remove infected plants",
                "Control whiteflies, use virus-free seeds"
        });
        diseaseMap.put("Tomato__Tomato_mosaic_virus", new String[]{
                "Remove infected plants, disinfect tools",
                "Use resistant varieties, avoid tobacco exposure"
        });
        diseaseMap.put("Tomato_healthy", new String[]{
                "No treatment needed",
                "Maintain good care and watering"
        });

        // Unknown fallback
        diseaseMap.put("Unknown", new String[]{
                "No information available",
                "No information available"
        });
    }

    public static String[] getInfo(String className){
        return diseaseMap.getOrDefault(className, diseaseMap.get("Unknown"));
    }
}
