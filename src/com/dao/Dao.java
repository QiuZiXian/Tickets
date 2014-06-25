package com.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.util.DbUtil;

/**
 * @author Alex Maven
 * @date 2014年6月12日
 * @Company:
 */
public class Dao {
	private PreparedStatement pstmt;
	private ResultSet rs;
	private DbUtil dbUtil = DbUtil.getInstance();
	private List<Integer> linelistInt;
	private List<String> linelistStr;

	/**
	 * @Title: getLineByPointName
	 * @Description:通过节点NAME获取所属线路
	 * @param pointname
	 * @return 所属线路
	 */
	public Set<Integer> getLineByPointName(String pointname) {
		linelistInt = new ArrayList<Integer>();
		try {
			pstmt = dbUtil
					.getConnection()
					.prepareStatement(
							"SELECT PL.LINEID FROM POINTS_LINES PL, POINTS P WHERE PL.POINTID = P.ID AND P.NAME = ? GROUP BY PL.LINEID;");
			pstmt.setString(1, pointname);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				linelistInt.add(rs.getInt(1));
			}
			return new HashSet<Integer>(linelistInt);
		} catch (SQLException e) {
			System.out.println("SQL查询出错" + e);
			return null;
		}
	}

	/**
	 * @Title: getPointNameByLine
	 * @Description:通过线路获取节点NAME集合
	 * @param line
	 * @return NAME集合
	 */
	public Set<String> getPointNameByLine(int line) {
		linelistStr = new ArrayList<String>();
		try {
			pstmt = dbUtil
					.getConnection()
					.prepareStatement(
							"SELECT P.NAME FROM POINTS_LINES PL, POINTS P WHERE PL.POINTID = P.ID AND PL.LINEID = ? GROUP BY PL.ID;");
			pstmt.setInt(1, line);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				linelistStr.add(rs.getString(1));
			}
			return new HashSet<String>(linelistStr);
		} catch (SQLException e) {
			System.out.println("SQL查询出错" + e);
			return null;
		}
	}

	/**
	 * @Title: getDisByPointNameAndLine
	 * @Description:获取某路线上指定ID的两点的最短距离
	 * @param startpoint
	 * @param endpoint
	 * @param line
	 * @return 最短距离
	 */
	public int getDisByPointNameAndLine(String startpoint, String endpoint,
			int line) {
		try {
			pstmt = dbUtil
					.getConnection()
					.prepareStatement(
							"SELECT ABS( PL.PO - (SELECT PL.PO FROM POINTS_LINES PL, POINTS P WHERE P.ID = PL.POINTID AND P.NAME = ? AND PL.LINEID = ? )) FROM POINTS_LINES PL, POINTS P WHERE P.ID = PL.POINTID AND P.NAME = ? AND PL.LINEID = ? ;");
			pstmt.setString(1, startpoint);
			pstmt.setInt(2, line);
			pstmt.setString(3, endpoint);
			pstmt.setInt(4, line);
			rs = pstmt.executeQuery();
			rs.next();
			int dis = rs.getInt(1);
			if (line == 1) {
				if (dis > 10) {
					return 21 - dis;
				} else {
					return dis;
				}
			} else {
				return dis;
			}
		} catch (SQLException e) {
			System.out.println("SQL查询出错" + e);
			return -1;
		}
	}

	/**
	 * @Title: getWayByPointNameAndLine
	 * @Description:获取某路线上指定NAME的两点的最短路线
	 * @param startpoint
	 * @param endpoint
	 * @param line
	 * @return 最短路线
	 */
	public List<String> getWayByPointNameAndLine(String startpoint,
			String endpoint, int line) {
		return getWayByPoAndLine(getRelativePoByNameAndLine(startpoint, line),
				getRelativePoByNameAndLine(endpoint, line), line);
	}

	/**
	 * @Title: getWayByPoAndLine
	 * @Description: 根据起点和终点以及线路获取最短路径
	 * @param startposition
	 *            起点
	 * @param stopposition
	 *            终点
	 * @param line
	 *            所属线路
	 * @return
	 */
	private List<String> getWayByPoAndLine(int startposition, int stopposition,
			int line) {
		List<String> waylist = new ArrayList<String>();
		if (line == 1) {
			if (startposition < stopposition) {
				if ((stopposition - startposition) <= 10) {
					try {
						pstmt = dbUtil
								.getConnection()
								.prepareStatement(
										"SELECT P.NAME FROM POINTS_LINES PL,POINTS P WHERE PL.POINTID = P.ID AND PL.PO >= ? AND PL.PO <= ? AND PL.LINEID = ? ORDER BY PL.PO ;");
						pstmt.setInt(1, startposition);
						pstmt.setInt(2, stopposition);
						pstmt.setInt(3, line);
						rs = pstmt.executeQuery();
						while (rs.next()) {
							waylist.add(rs.getString(1));
						}
						return waylist;
					} catch (SQLException e) {
						System.out.println("SQL查询出错" + e);
						return null;
					}
				} else {
					try {
						pstmt = dbUtil
								.getConnection()
								.prepareStatement(
										"SELECT P.NAME FROM POINTS_LINES PL,POINTS P WHERE PL.POINTID = P.ID AND PL.PO >= ? AND PL.PO <= ? AND PL.LINEID = ? ORDER BY PL.PO DESC;");
						pstmt.setInt(1, 1);
						pstmt.setInt(2, startposition);
						pstmt.setInt(3, line);
						rs = pstmt.executeQuery();
						while (rs.next()) {
							waylist.add(rs.getString(1));
						}
						pstmt = dbUtil
								.getConnection()
								.prepareStatement(
										"SELECT P.NAME FROM POINTS_LINES PL,POINTS P WHERE PL.POINTID = P.ID AND PL.PO >= ? AND PL.PO <= ? AND PL.LINEID = ? ORDER BY PL.PO DESC;");
						pstmt.setInt(1, stopposition);
						pstmt.setInt(2, 21);
						pstmt.setInt(3, line);
						rs = pstmt.executeQuery();
						while (rs.next()) {
							waylist.add(rs.getString(1));
						}
						return waylist;
					} catch (SQLException e) {
						System.out.println("SQL查询出错" + e);
						return null;
					}
				}
			} else {
				if ((startposition - stopposition) <= 10) {
					try {
						pstmt = dbUtil
								.getConnection()
								.prepareStatement(
										"SELECT P.NAME FROM POINTS_LINES PL,POINTS P WHERE PL.POINTID = P.ID AND PL.PO >= ? AND PL.PO <= ? AND PL.LINEID = ? ORDER BY PL.PO DESC;");
						pstmt.setInt(1, stopposition);
						pstmt.setInt(2, startposition);
						pstmt.setInt(3, line);
						rs = pstmt.executeQuery();
						while (rs.next()) {
							waylist.add(rs.getString(1));
						}
						return waylist;
					} catch (SQLException e) {
						System.out.println("SQL查询出错" + e);
						return null;
					}
				} else {
					try {
						pstmt = dbUtil
								.getConnection()
								.prepareStatement(
										"SELECT P.NAME FROM POINTS_LINES PL,POINTS P WHERE PL.POINTID = P.ID AND PL.PO >= ? AND PL.PO <= ? AND PL.LINEID = ? ORDER BY PL.PO ;");
						pstmt.setInt(1, startposition);
						pstmt.setInt(2, 21);
						pstmt.setInt(3, line);
						rs = pstmt.executeQuery();
						while (rs.next()) {
							waylist.add(rs.getString(1));
						}
						pstmt = dbUtil
								.getConnection()
								.prepareStatement(
										"SELECT P.NAME FROM POINTS_LINES PL,POINTS P WHERE PL.POINTID = P.ID AND PL.PO >= ? AND PL.PO <= ? AND PL.LINEID = ? ORDER BY PL.PO ;");
						pstmt.setInt(1, 1);
						pstmt.setInt(2, stopposition);
						pstmt.setInt(3, line);
						rs = pstmt.executeQuery();
						while (rs.next()) {
							waylist.add(rs.getString(1));
						}
						return waylist;
					} catch (SQLException e) {
						System.out.println("SQL查询出错" + e);
						return null;
					}
				}
			}
		} else {
			try {
				if (startposition < stopposition) {
					pstmt = dbUtil
							.getConnection()
							.prepareStatement(
									"SELECT P.NAME FROM POINTS_LINES PL,POINTS P WHERE PL.POINTID = P.ID AND PL.PO >= ? AND PL.PO <= ? AND PL.LINEID = ? ORDER BY PL.PO ;");
					pstmt.setInt(1, startposition);
					pstmt.setInt(2, stopposition);
				} else {
					pstmt = dbUtil
							.getConnection()
							.prepareStatement(
									"SELECT P.NAME FROM POINTS_LINES PL,POINTS P WHERE PL.POINTID = P.ID AND PL.PO >= ? AND PL.PO <= ? AND PL.LINEID = ? ORDER BY PL.PO DESC;");
					pstmt.setInt(1, stopposition);
					pstmt.setInt(2, startposition);
				}
				pstmt.setInt(3, line);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					waylist.add(rs.getString(1));
				}
				return waylist;
			} catch (SQLException e) {
				System.out.println("SQL查询出错" + e);
				return null;
			}
		}
	}

	/**
	 * @Title: getRelativePoByNameAndLine
	 * @Description:根据节点NAME和所在线路获取节点的相对位置
	 * @param name
	 *            节点名
	 * @param line
	 *            所在线路
	 * @return
	 */
	public int getRelativePoByNameAndLine(String name, int line) {
		try {
			pstmt = dbUtil
					.getConnection()
					.prepareStatement(
							"SELECT PL.PO FROM POINTS_LINES PL, POINTS P WHERE P.ID = PL.POINTID AND P.NAME = ? AND PL.LINEID = ? ;");
			pstmt.setString(1, name);
			pstmt.setInt(2, line);
			rs = pstmt.executeQuery();
			rs = pstmt.executeQuery();
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			System.out.println("SQL查询出错" + e);
			return -1;
		}
	}
}
