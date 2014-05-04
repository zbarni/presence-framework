package org.mobicents.slee.sipevent.server.external;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBProvider {
	public static final String DB_NAME = "components";
	public static final String MODELS = "master_documents";
	public static final String NOTIFICATIONS = "notifications";
	public static final String DB_USERNAME = "presence";
	public static final String DB_PASSWORD = "columbianyc";
	public static final String DB_HOST = "localhost";
	public static final String DB_URL = "jdbc:postgresql://localhost/components";
	public static final String DB_DRIVER = "org.postgresql.Driver";

	private Connection dbConnection;
	private static DBProvider instance;

	public static DBProvider getInstance() {
		if (instance == null) {
			instance = new DBProvider();
		} 
		return instance;
	}

	public Connection getConnection() {
		return this.dbConnection;
	}
	
	private DBProvider() {
		try {
			Class.forName(DBProvider.DB_DRIVER).newInstance();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.dbConnection = null;
		try {
			this.dbConnection = DriverManager.getConnection(
					DBProvider.DB_URL,
					DBProvider.DB_USERNAME,
					DBProvider.DB_PASSWORD);
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
}
