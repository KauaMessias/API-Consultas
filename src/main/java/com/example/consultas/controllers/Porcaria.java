package com.example.consultas.controllers;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Porcaria {


    public static void main(String[] args) {


        int[] nums = {-1, 2, -3, 3};

        double[] nums2 = {1.3, 45.31, 54.69, 943.53};

        HashMap<String, Double> maps = new HashMap<>();
        maps.put("Ana", 43.5);
        maps.put("Roberto", 53.5);
        maps.put("Luis", 91.2);
        maps.put("Claudio", 12.9);
        maps.put("Ricardo", 69.7);
        System.out.print(IntStream.of(nums).max().orElse(-1));

        System.out.println(DoubleStream.of(nums2).max().orElse(-1));

        System.out.println(maps.values().stream().mapToDouble(Double::doubleValue).average().getAsDouble());

    }
}
