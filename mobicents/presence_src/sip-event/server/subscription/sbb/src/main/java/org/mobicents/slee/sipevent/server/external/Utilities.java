package org.mobicents.slee.sipevent.server.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.slee.facilities.Tracer;

public class Utilities {
	
	public static void flushToDatabase(String uri, String notifyContent, Tracer tracer) {
		try {
			Connection connection = DBProvider.getInstance().getConnection();
			PreparedStatement verifyStatement = connection.prepareStatement(
					"SELECT * FROM "+DBProvider.NOTIFICATIONS+" WHERE sip_uri=?");
			verifyStatement.setString(1, uri);
			ResultSet rs = verifyStatement.executeQuery();
			if (rs.isBeforeFirst()) {
				String sql = "UPDATE "+DBProvider.NOTIFICATIONS+
						" SET document=? WHERE sip_uri=?";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setString(2, uri);
				statement.setString(1, notifyContent);
				statement.executeUpdate();
			}
			else {
				String sql = "INSERT INTO "+DBProvider.NOTIFICATIONS+
						"(sip_uri, document) VALUES (?,?)";
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.setString(1, uri);
				statement.setString(2, notifyContent);
				statement.executeUpdate();
			}
		}
		catch (SQLException e) {
			tracer.info(e.toString());
			e.printStackTrace();
		}
	}
	
	public static void removeFromDatabase(String notifier, Tracer tracer) {
		try {
			Connection connection = DBProvider.getInstance().getConnection();
			PreparedStatement verifyStatement = connection.prepareStatement(
					"DELETE FROM "+DBProvider.NOTIFICATIONS+" WHERE sip_uri=?");
			verifyStatement.setString(1, notifier);
			verifyStatement.executeUpdate();
		}
		catch (SQLException e) {
			tracer.info(e.toString());
			e.printStackTrace();
		}
	}
}
