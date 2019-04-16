/*
 * Java compiler version: 5 (49.0)
 */
package db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import org.ini4j.Profile.Section;

import cmmn.Common;

public class MsDBConnectionPoolMgr {
	private static MsDBConnectionPoolMgr instance;
	protected Common comm = Common.getInstance();

	public static synchronized MsDBConnectionPoolMgr getInstance() {
		if (instance == null)
			instance = new MsDBConnectionPoolMgr();
		connectionClients += 1;
		return instance;
	}


	public void setDbInfoMsSql(String type) throws Exception {
		//System.out.println(">>> MSSQL DBConnectionPoolMgr.java setDbInfoMsSql ");

		// 접속한 서버가 운영서버,개발서버,로컬인지 판단하여 properties를 셋팅.
		Section section = (Section) comm.setIniFileLoad();

		className = section.get("RoadDigDBClass");
		url = section.get("RoadDigDBLinkPreStr")+ section.get("RoadDigDBLink");
		userName = section.get("RoadDigDBUserId");
		passWord = section.get("RoadDigDBUserPw");

		//System.out.println(className);

		initConnections = 0;
		try {
			Class.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	public synchronized Connection getConnection() {
		Connection connection = null;
		if (connectionPools.size() > 0) {
			connection = (Connection) connectionPools.firstElement();
			connectionPools.removeElementAt(0);
			try {
				if (!connection.isClosed())
				{
					if (connection != null)
						incConnections += 1;
					return connection;
				}

				connection = getConnection();
			} catch (java.sql.SQLException sqlexception) {
				connection = getConnection();
			}
		} else if ((initConnections == 0) || (incConnections < initConnections)) {
			try {
				connection = java.sql.DriverManager.getConnection(url,
						userName, passWord);
			} catch (java.sql.SQLException sqlexception1) {
				System.out.println("[getConnection()] SQLException : "
						+ sqlexception1.getMessage());
			}
		}
		return connection;
	}

	public synchronized void returnConnection(Connection connection) {
		connectionPools.addElement(connection);
		incConnections -= 1;
		notifyAll();
		if (--connectionClients != 0)
			return;
		for (java.util.Enumeration enumeration = connectionPools.elements(); enumeration
				.hasMoreElements();) {
			Connection connection1 = (Connection) enumeration.nextElement();
			try {
				connection1.close();
			} catch (java.sql.SQLException sqlexception) {
				System.out.println("[release()] Connection Close : "
						+ sqlexception.getMessage());
			}
		}

		connectionPools.removeAllElements();
	}

	public void destroy() {
		for (java.util.Enumeration enumeration = connectionPools.elements(); enumeration
				.hasMoreElements();) {
			Connection connection = (Connection) enumeration.nextElement();
			try {
				connection.close();
			} catch (java.sql.SQLException sqlexception) {
				System.out.println("SQLException Connection Close: "
						+ sqlexception.getMessage());
			}
		}

		connectionPools.removeAllElements();
	}

	private static java.util.Vector connectionPools = new java.util.Vector();
	private static int connectionClients;
	private static int incConnections;
	private static int initConnections;
	private static String className;
	private static String passWord;
	private static String url;
	private static String userName;
}