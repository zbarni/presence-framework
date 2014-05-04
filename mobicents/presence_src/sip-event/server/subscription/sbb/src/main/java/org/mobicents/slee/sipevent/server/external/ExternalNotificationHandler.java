package org.mobicents.slee.sipevent.server.external;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import javax.sip.address.Address;
import javax.sip.message.Request;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NotificationHandler {

	private ExternalSubscriptionHandler externalSubscriptionHandler;
	
	public NotificationHandler(ExternalSubscriptionHandler h) {
		externalSubscriptionHandler = h;
	}
	
	public void processNotify(Request request) {
//		try {
//			PreparedStatement verifyStatement = DBProvider.getInstance().getConnection().prepareStatement("SELECT * FROM master_documents WHERE sip_uri=?");
//			verifyStatement.setString(1, uri.toString());
//			ResultSet rs = verifyStatement.executeQuery();
//			// previous doc exists
//			while (rs.next()) {
//				String masterDoc = rs.getString(2);
//				
//				Document document = getDocumentFromString(masterDoc); 
//				NodeList uaList = document.getElementsByTagName("ua");
//				for (int i=0; i < uaList.getLength(); ++i) {
//					Node ua = uaList.item(i);
//					Node idNode = ua.getAttributes().getNamedItem("id");
//					// <ua> tag has id attribute, correct
//					if (idNode != null) {
//						Address gruuAddress = sbb.getAddressFactory().createAddress(idNode.getNodeValue());
//						list.add(gruuAddress);
//					}
//				}
//			}
//		}
//		catch (SQLException e) {
//			tracer.info(e.toString());
//		} catch (DOMException e) {
//			e.printStackTrace();
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
	}
}
