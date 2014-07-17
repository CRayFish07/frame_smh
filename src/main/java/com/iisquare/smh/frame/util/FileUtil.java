package com.iisquare.smh.frame.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 文件处理操作类
 */
public class FileUtil {
	
	public static final String encoding = "UTF-8";
	
	public static boolean isExists(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}
	
	public static boolean delete(String filePath) {
		File file = new File(filePath);
		return file.delete();
	}
	
	public static String getContent(String fileName) {
		return getContent(fileName, false);
	}
	
	/**
	 * 获取文件内容
	 * @param fileName 文件路径
	 * @param bDislodgeLine 是否去除换行
	 * @return 文件不存在或读取异常时返回null
	 */
	public static String getContent(String fileName, boolean bDislodgeLine) {
		String output = "";
		File file = new File(fileName);
		if (!file.exists()) return null;
		if (!file.isFile()) return null;
		InputStream inputStream = null;
        InputStreamReader inputReader = null;
        BufferedReader bufferReader = null;
		try {
			inputStream = new FileInputStream(file);
            inputReader = new InputStreamReader(inputStream, encoding);
            bufferReader = new BufferedReader(inputReader);
			StringBuilder sb = new StringBuilder();
			String text;
			while ((text = bufferReader.readLine()) != null) {
				sb.append(text);
				if(!bDislodgeLine) sb.append("\n");
			}
			int length = sb.length();
			output = length > 0 ? sb.substring(0, length - 1) : sb.toString();
		} catch (IOException ioException) {
			return null;
		} finally {
			close(bufferReader, inputReader, inputStream);
		}
		return output; 
	}
	
	public static void close(Closeable...args) {
		try {
			for (Closeable arg : args) {
				if(null != arg) arg.close();
			}
		} catch (Exception e) {}
	}
}
