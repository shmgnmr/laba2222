package org.example;

import org.apache.commons.math3.stat.StatUtils;

import java.util.HashMap;

public class StandartDeviation {
    private  double[] dataarray;
    private HashMap<String, Double> dictionary;
    public StandartDeviation(HashMap<String, Double> dictionary,double[] dataarray){
        this.dictionary=dictionary;
        this.dataarray=dataarray;
    }
    public HashMap<String, Double> getStandartDeviation(HashMap<String, Double> dictionary,double[] dataarray) {
        double geometricMean = StatUtils.variance(dataarray);
        // Вычисление среднего геометрического
        double variace  = Math.pow(geometricMean, 0.5);
        dictionary.put("Cтандартноe отклонениe", variace);
        return dictionary;
    }
}
