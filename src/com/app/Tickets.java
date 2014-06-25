package com.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.core.Algorithm;
import com.dao.Dao;
import com.util.DbUtil;

/**
 * @author Alex Maven
 * @date 2014年6月12日
 * @Company:
 */
public class Tickets {

	/**
	 * @Title: main
	 * @Description:主函数
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		DbUtil dbUtil = DbUtil.getInstance(); // 初始化H2数据库
		Algorithm algorithm = new Algorithm(); // 实例化算法核心

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream("./in.txt")));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("./out.txt")));

		String finalfile = ""; // 存储最终写入文件的内容
		for (String line = br.readLine(); line != null; line = br.readLine()) { // 按行读取in.txt中的内容
			String startpo = line.substring(0, line.indexOf(",")); // 通过','分割起点和终点
			String stoppo = line
					.substring(line.indexOf(",") + 1, line.length());
			finalfile += algorithm.Tickets(startpo, stoppo) + "\n"; // 构造最终输出的数据
		}
		br.close();
		bw.write(finalfile);
		bw.close();
		
		// 关闭H2数据库
		dbUtil.DB_Stop();
	}
}