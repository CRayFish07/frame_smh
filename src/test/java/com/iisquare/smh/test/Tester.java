package com.iisquare.smh.test;

import com.iisquare.smh.frame.util.FileUtil;

public class Tester {
	
	public static void print(String[] strs) {
		for (String str : strs) {
			System.out.println(str);
		}
	}
	
	public static void main(String[] args) {
		String content = FileUtil.getContent("C:\\Users\\Ouyang\\Desktop\\tt.txt");
		System.out.println(content);
	}
}
