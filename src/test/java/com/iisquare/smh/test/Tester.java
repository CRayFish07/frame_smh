package com.iisquare.smh.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Tester {
	
	public static void print(String[] strs) {
		for (String str : strs) {
			System.out.println(str);
		}
	}
	
	public static void main(String[] args) {
		Map<String, Integer> map1 = new HashMap<String, Integer>(128, 0.79f);
		Map<String, Integer> map2 = new HashMap<String, Integer>(128, 0.8f);
		Map<String, Integer> map3 = new HashMap<String, Integer>(128, 0.78125f);
		Map<String, Integer> map4 = new HashMap<String, Integer>(128, 1f);
		Long start = System.nanoTime();
		for (int i = 0; i < 100; i++) {
			map1.put(String.valueOf(i), i);
		}
		System.out.print("map1:");
		System.out.println(System.nanoTime() - start);
		start = System.nanoTime();
		for (int i = 0; i < 100; i++) {
			map2.put(String.valueOf(i), i);
		}
		System.out.print("map2:");
		System.out.println(System.nanoTime() - start);
		start = System.nanoTime();
		for (int i = 0; i < 100; i++) {
			map3.put(String.valueOf(i), i);
		}
		System.out.print("map3:");
		System.out.println(System.nanoTime() - start);
		start = System.nanoTime();
		for (int i = 0; i < 100; i++) {
			map4.put(String.valueOf(i), i);
		}
		System.out.print("map4:");
		System.out.println(System.nanoTime() - start);
		start = System.nanoTime();
		for (Entry<String, Integer> entry : map1.entrySet()) {
			entry.getKey();
			entry.getValue();
		}
		System.out.print("map1:");
		System.out.println(System.nanoTime() - start);
		start = System.nanoTime();
		for (Entry<String, Integer> entry : map2.entrySet()) {
			entry.getKey();
			entry.getValue();
		}
		System.out.print("map2:");
		System.out.println(System.nanoTime() - start);
		start = System.nanoTime();
		for (Entry<String, Integer> entry : map3.entrySet()) {
			entry.getKey();
			entry.getValue();
		}
		System.out.print("map3:");
		System.out.println(System.nanoTime() - start);
		start = System.nanoTime();
		for (Entry<String, Integer> entry : map4.entrySet()) {
			entry.getKey();
			entry.getValue();
		}
		System.out.print("map4:");
		System.out.println(System.nanoTime() - start);
	}
}
