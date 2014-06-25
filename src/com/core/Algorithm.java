package com.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.dao.Dao;

public class Algorithm {
	Dao dao = new Dao();

	int distance = 0, distance1 = 0, distance2 = 0, distance3 = 0, line1 = 0,
			line2 = 0, line3 = 0;

	List<String> way = new ArrayList<String>();
	List<String> way1 = new ArrayList<String>();
	List<String> way2 = new ArrayList<String>();
	List<String> way3 = new ArrayList<String>();

	String point1 = null;
	String point2 = null;

	Set<Integer> startSet = new HashSet<Integer>();
	Set<Integer> stopSet = new HashSet<Integer>();
	Set<String> startSet1 = new HashSet<String>();
	Set<String> stopSet1 = new HashSet<String>();
	Set<Integer> startSet2 = new HashSet<Integer>();
	Set<String> startSet3 = new HashSet<String>();
	Set<Integer> resultSet = new HashSet<Integer>();
	Set<String> resultSet1 = new HashSet<String>();

	Iterator<Integer> it;
	Iterator<String> it1;

	/**
	* @Title: Tickets
	* @Description:主程序
	* @param start
	* @param stop
	* @return
	*/
	public String Tickets(String start, String stop) {
		long starttime = System.currentTimeMillis(); // 统计运算时间

		distance = 10000;
		distance1 = distance2 = distance3 = 0;
		startSet1.clear();
		stopSet1.clear();
		startSet2.clear();
		startSet3.clear();
		startSet = dao.getLineByPointName(start);	//获取起点所在的所有线路
		stopSet = dao.getLineByPointName(stop);		//获取终点所在的所有线路
		resultSet.clear();
		resultSet.addAll(startSet);
		resultSet.retainAll(stopSet);				//取出两点共同所在的线
		List<Object> retuList = null;
		List<Object> retuList2 = null;
		if (resultSet.size() > 0) { 				//两点有直达线路，不需要换乘
			retuList = this.getLine(start, stop);	//获取两点所在线中两点距离最短的情况
			line1 = (int) retuList.get(0);
			distance1 = (int) retuList.get(1);
			way = dao.getWayByPointNameAndLine(start, stop, line1);	//直接取出线路
		} else {									//两点间没有直达线路，需要换乘
			it = startSet.iterator();				//遍历起点所在的所有线路
			while (it.hasNext()) {
				startSet1.addAll(dao.getPointNameByLine(it.next())); //获取起点线路中所有的点
			}
			it = stopSet.iterator();
			while (it.hasNext()) {
				stopSet1.addAll(dao.getPointNameByLine(it.next())); //获取终点线路中所有的点
			}
			resultSet1.clear();
			resultSet1.addAll(startSet1);
			resultSet1.retainAll(stopSet1);				//取两者交集，若有交集，则换乘一次即可，反之换乘2次（本题最多换乘2次）
			if (resultSet1.size() > 0) { 				// 需要换乘一次
				retuList = this.getPoint(start, stop);	//获取一次换乘情况下 的点信息
				point1 = (String) retuList.get(0);	
				line1 = (int) retuList.get(1);
				line2 = (int) retuList.get(2);
				distance1 = (int) retuList.get(3);
				distance2 = (int) retuList.get(4);
				way1 = dao.getWayByPointNameAndLine(start, point1, line1);//一次换乘获取第1段最短线路后直接查出最短路径
				way2 = dao.getWayByPointNameAndLine(point1, stop, line2);//一次换乘获取第2段最短线路后直接查出最短路径
				way = way1;
				way2.remove(0);//删除重复的 交点
				way.addAll(way2);
			} else {							// 需要换乘两次，文字较多，面谈。
				it1 = startSet1.iterator();
				while (it1.hasNext()) {
					startSet2.addAll(dao.getLineByPointName(it1.next()));
				}
				it = startSet2.iterator();
				while (it.hasNext()) {
					startSet3.addAll(dao.getPointNameByLine(it.next()));
				}
				resultSet1.clear();
				resultSet1.addAll(startSet3);
				resultSet1.retainAll(stopSet1);
				for (String tmp : resultSet1) {
					retuList = this.getPoint(start, tmp);
					retuList2 = this.getLine(tmp, stop);
					String point_tmp = (String) retuList.get(0);
					int line_tmp1 = (int) retuList.get(1);
					int line_tmp2 = (int) retuList.get(2);
					int line_tmp3 = (int) retuList2.get(0);
					int dis_tmp1 = (int) retuList.get(3);
					int dis_tmp2 = (int) retuList.get(4);
					int dis_tmp3 = (int) retuList2.get(1);

					if (distance > dis_tmp1 + dis_tmp2 + dis_tmp3) {
						distance = dis_tmp1 + dis_tmp2 + dis_tmp3;
						line1 = line_tmp1;
						line2 = line_tmp2;
						line3 = line_tmp3;
						point1 = point_tmp;
						point2 = tmp;
						distance1 = dis_tmp1;
						distance2 = dis_tmp2;
						distance3 = dis_tmp3;
					}
					way1 = dao.getWayByPointNameAndLine(start, point1, line1);
					way2 = dao.getWayByPointNameAndLine(point1, point2, line2);
					way3 = dao.getWayByPointNameAndLine(point2, stop, line3);
					way = way1;
					way2.remove(0);
					way.addAll(way2);
					way3.remove(0);
					way.addAll(way3);
				}
			}
		}
		String startandstop = "";
		double price = getPrice(distance1, distance2, distance3);
		int finalprice = (int) price;
		if (price == finalprice) {
			startandstop = start + "," + stop + "=" + finalprice + ":";
		} else {
			startandstop = start + "," + stop + "=" + price + ":";
		}

		String finalway = "";
		for (String ways : way) {
			finalway += ways + ",";
		}
		long duration = System.currentTimeMillis() - starttime;
		System.out.println("执行一次查询，查询耗时：" + duration + " ms");
		return startandstop + finalway.substring(0, finalway.length() - 1);
	}

	/**
	* @Title: getLine
	* @Description:获取同一条线上的两点的最短距离信息
	* @param start：起点
	* @param stop：终点
	* @return	
	* 			line:最短线路ID
				distance：最短距离
	*/
	public List getLine(String start, String stop) {
		Set<Integer> resultSetX = new HashSet<Integer>();
		List returnList = new ArrayList();
		startSet = dao.getLineByPointName(start);
		stopSet = dao.getLineByPointName(stop);
		resultSetX.addAll(startSet);
		resultSetX.retainAll(stopSet);
		int distance = 10000, line = 0;
		it = resultSetX.iterator();
		while (it.hasNext()) {
			int temp = it.next();
			if (distance > dao.getDisByPointNameAndLine(start, stop, temp)) {
				distance = dao.getDisByPointNameAndLine(start, stop, temp);
				line = temp;
			}
		}
		returnList.add(line);
		returnList.add(distance);
		return returnList;
	}

	/**
	* @Title: getPoint
	* @Description:获取一次换乘情况下的节点点信息
	* @param start 起点
	* @param stop 终点
	* @return	point：换乘节点
				line3：起点  ——> 换乘点 最短线路ID  
				line4：换乘点  ——> 终点 最短线路ID  
				distance3：起点  ——> 换乘点 最短距离
				distance4：换乘点  ——> 终点 最短距离  
	*/
	public List getPoint(String start, String stop) {
		Set<String> resultSetX = new HashSet<String>();
		startSet = dao.getLineByPointName(start);
		stopSet = dao.getLineByPointName(stop);
		resultSet.addAll(startSet);
		resultSet.retainAll(stopSet);
		it = startSet.iterator();
		while (it.hasNext()) {
			startSet1.addAll(dao.getPointNameByLine(it.next()));
		}
		it = stopSet.iterator();
		while (it.hasNext()) {
			stopSet1.addAll(dao.getPointNameByLine(it.next()));
		}
		resultSetX.addAll(startSet1);
		resultSetX.retainAll(stopSet1);
		String point = null;
		int distance = 10000;
		it1 = resultSetX.iterator();
		List returnList = null;
		int line3 = 0, line4 = 0, distance3 = 0, distance4 = 0;
		while (it1.hasNext()) {
			String tmp = it1.next();
			returnList = this.getLine(start, tmp);
			List returnList2 = this.getLine(tmp, stop);
			int line1 = (int) returnList.get(0);
			int line2 = (int) returnList2.get(0);
			int distance1 = (int) returnList.get(1);
			int distance2 = (int) returnList2.get(1);
			if (distance > distance1 + distance2) {
				distance = distance1 + distance2;
				point = tmp;
				line3 = line1;
				line4 = line2;
				distance3 = distance1;
				distance4 = distance2;
			}
		}
		returnList.clear();
		returnList.add(point);
		returnList.add(line3);
		returnList.add(line4);
		returnList.add(distance3);
		returnList.add(distance4);
		return returnList;
	}

	/**
	* @Title: getPrice
	* @Description:获取票价
	* @param distance1 第一段路径长度
	* @param distance2 第二段路径长度 （换乘2次 = 0）
	* @param distance3 第三段路径长度 （不换乘    = 0）（换乘1次 = 0）
	* @return	票价
	*/
	public double getPrice(int distance1, int distance2, int distance3) {
		double price = 0;
		if (distance1 > 5) {
			price += 2 + 0.5 * (distance1 - 5);
		} else if (distance1 > 0) {
			price += 2;
		}
		if (distance2 > 5) {
			price += 2 + 0.5 * (distance2 - 5);
		} else if (distance2 > 0) {
			price += 2;
		}
		if (distance3 > 5) {
			price += 2 + 0.5 * (distance3 - 5);
		} else if (distance3 > 0) {
			price += 2;
		}
		return price;
	}
}
