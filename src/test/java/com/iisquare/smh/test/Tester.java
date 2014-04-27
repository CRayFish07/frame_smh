package com.iisquare.smh.test;

public class Tester {
	
	public static void print(String[] strs) {
		for (String str : strs) {
			System.out.println(str);
		}
	}
	
	public static void main(String[] args) {
		String str = "parent.id";
		System.out.println(str.replaceAll("\\.", "_"));
	}
}
