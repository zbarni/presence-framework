package com.csipsimple.components;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.csipsimple.service.ComponentService;
import com.csipsimple.ui.Installation;
import com.csipsimple.utils.Log;
//import org.apache.commons.lang.StringEscapeUtils;

@SuppressLint("NewApi")
public class ComponentManager {
	public static final String THIS_FILE = "COMP MANAGER";

	public static final String XML_UA = "ua";
	public static final String XML_DEVICE = "device";
	public static final String XML_TYPE = "type";
	public static final String XML_MODEL = "model";
	public static final String XML_OS = "os";
	public static final String XML_URI = "uri";
	public static final String XML_OS_NAME = "name";
	public static final String XML_OS_VERSION = "version";
	public static final String XML_SENSORS = "sensors";
	public static final String XML_SENSOR = "sensor";
	public static final String XML_ACTUATORS = "actuator";
	public static final String XML_ACTUATOR = "actuator";
	public static final String XML_COMP_NAME = "name";
	public static final String XML_PROFILE = "profile";

//	private static String presenceXml = "@zajzi init";
//	private static Document presenceDoc;
//	private static Node sensorsNode = null;
//	private static Node actuatorsNode = null;
	// Service connection
	private static Context mContext;
	private ComponentService mComponentService;
	private static Map<Long, Document> accounts = new HashMap<Long, Document>();

	// Sensors
	public static Proximity proximity;
	public static LocationTracker location;
	public static Headset headset;
	public static Accelerometer accelerometer;

	// Actuators	
	public static Vibrator vibrator;
	public static Loudspeaker loudspeaker; 
	
	public Context getContext() {
		return mContext;
	}

	public ComponentManager(Context context, ComponentService service) {
		mContext = context;
		this.mComponentService = service;
	}

	public void addAccount(long accId,String uri) {
		if (!accounts.containsKey(accId)) {
			Log.d(THIS_FILE,"@XML adding account to compmanager : " + uri);
			accounts.put(accId, createPresenceDoc(uri.replace("<", "").replace(">", "")));
		}
	}
	
	/**
	 * TODO implement
	 */
	public void cleanUp() {
		Log.d(THIS_FILE,"Cleaning up component service...");
	}

	/**
	 * 
	 */
	public void initComponents() {
		proximity = new Proximity("proximity","Proximity", Component.SENSOR, this);
		headset = new Headset("wired.headset", "Wired headset", Component.SENSOR, this);
		location = new LocationTracker("gps.location","GPS receiver",Component.SENSOR, this);
		accelerometer = new Accelerometer("accelerometer","Accelerometer",Component.SENSOR, this);

		vibrator = new Vibrator("vibrator","Vibrator", Component.ACTUATOR, this);
		loudspeaker = new Loudspeaker("built.in.loudspeaker","Built in loudspeaker", Component.ACTUATOR, this);
	}

	public String getPublicationDocument(long accId) {
		return domToXmlString(accounts.get(accId));
	}

	/**
	 * Transforms the Document component model into a String, and escapes
	 * the XML header.
	 * @return true on success, false otherwise0
	 */
	private String domToXmlString(Document doc) {
		String presenceXml;
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			transformer.transform(new DOMSource(doc), result);

			presenceXml = writer.toString();
			// remove XML header because it goes into the body
			presenceXml = StringEscapeUtils.unescapeXml(presenceXml);
			return presenceXml;
		}
		catch (TransformerException e) {
			Log.e(THIS_FILE,"Transformer exception.");
			return null;
		}
	}
	
	public boolean isAccountRegistered(String uri) {
		return accounts.containsKey(uri);
	}

	/**
	 * 
	 * @param c Component class of sensor or actuator 
	 */
	public void updateComponentModel(Component c) {
		for (HashMap.Entry<Long, Document> entry : accounts.entrySet()) {
			Document doc = entry.getValue();
			long accId = entry.getKey();
			// remove component with id from DOM
			NodeList compList = doc.getElementsByTagName(c.getType());
			Log.d(THIS_FILE,"@XML updating CM for id : " + c.getId());
			for (int i=0; i < compList.getLength(); ++i) {
				Node comp = compList.item(i);
				String idVal = null;
				Node idNode = comp.getAttributes().getNamedItem("id");
				// <ua> tag has id attribute, correct
				if (idNode != null) {
					idVal = idNode.getNodeValue();
				}
				if (idVal == c.getId()) {
					// we have to remove it
					Log.d(THIS_FILE,"@XML node with id was found");
					try {
						Node parentNode = comp.getParentNode();
						parentNode.removeChild(comp);
						Element tmp;
						if ((tmp = c.getComponentElement(doc)) != null) {
//							Log.d(THIS_FILE,"@XML heaset was added");
							try {
								parentNode.appendChild(tmp);
							}
							catch (Exception e) {
								Log.e(THIS_FILE,"@XML problem adding headset",e);
							}
						} else {
							Log.d(THIS_FILE,"@XML headset was only removed");
						}
						updatePublication(accId, doc);
					}
					catch (Exception e) {
						Log.e(THIS_FILE,"Could not remove node from parent.",e);
						return;
					}
				}
			}
			// we have to add it, probably just got connected
			Element elem;
			if ((elem = c.getComponentElement(doc)) != null) {
				Log.d(THIS_FILE,"@XML adding component to XML @foo");
				try {
					if (c.getType() == Component.SENSOR) {
						NodeList nl = doc.getElementsByTagName(XML_SENSORS);
						if (nl.getLength() > 0) {
							nl.item(0).appendChild(elem);
						}
					}
					else {
						NodeList nl = doc.getElementsByTagName(XML_ACTUATORS);
						if (nl.getLength() > 0) {
							nl.item(0).appendChild(elem);
						}
					}
					updatePublication(accId,doc);
				}
				catch (Exception e) {
					Log.e(THIS_FILE,"@XML problem adding new component",e);
				}
			}
			else {
				Log.d(THIS_FILE,"@XML component was not added/modified, doc not modified @foo");
			}
		}
	}

	private void updatePublication(long accId, Document doc) {
		Log.d(THIS_FILE,"@XML updating CM for accId : " + accId);
		this.mComponentService.publish(accId,domToXmlString(doc));
	}

	/**
	 * Creates the skeleton of the component model sent in PUBLISH
	 * <user>
	 * 	<aor></aor>
	 * 	<version></version>
	 *  <ua>
	 *  </ua>
	 * </user>
	 */

	@SuppressLint("NewApi")
	private Document createPresenceDoc(String uri) {
		Log.d(THIS_FILE,"Building Component Model for account" + uri);
		String version = "";
		try {
			version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e("tag", e.getMessage());
		}
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement(XML_UA);
			Element device = doc.createElement(XML_DEVICE);
			Element type = doc.createElement(XML_TYPE);
			Element model = doc.createElement(XML_MODEL);
			Element os = doc.createElement(XML_OS);
			Element os_name = doc.createElement(XML_OS_NAME);
			Element os_version = doc.createElement(XML_OS_VERSION);
			Element sensorsNode = doc.createElement(XML_SENSORS);
			Element actuatorsNode = doc.createElement(XML_ACTUATORS);

			doc.appendChild(root);
			root.setAttribute("id", uri + ";gr=urn:uuid:" + Installation.id(mContext));
			root.appendChild(device);
			device.appendChild(type);
			type.setTextContent("Smartphone");

			device.appendChild(model);
			model.setTextContent(getDeviceName());

			device.appendChild(os);
			os.appendChild(os_name);
			os_name.setTextContent("Android");
			os.appendChild(os_version);
			os_version.setTextContent(version);

			//sensors
			root.appendChild(sensorsNode);
			try {
				Element elem = proximity.getComponentElement(doc);
				if (elem != null) sensorsNode.appendChild(elem);
				
				elem = headset.getComponentElement(doc);
				if (elem != null) sensorsNode.appendChild(elem);
				
				elem = vibrator.getComponentElement(doc);
				if (elem != null) sensorsNode.appendChild(elem);
				
				elem = location.getComponentElement(doc);
				if (elem != null) sensorsNode.appendChild(elem);
				
				elem = accelerometer.getComponentElement(doc);
				if (elem != null) sensorsNode.appendChild(elem);
			}
			catch (Exception e) {
				Log.e(THIS_FILE,"@XML problem adding child",e);
			}
			
			//actuators
			root.appendChild(actuatorsNode);
			try {
				Element elem;
				elem = vibrator.getComponentElement(doc);
				if (elem != null) actuatorsNode.appendChild(elem);
				
				elem = loudspeaker.getComponentElement(doc);
				if (elem != null) actuatorsNode.appendChild(elem);
			}
			catch (Exception e) {
				Log.e(THIS_FILE,"@XML problem adding child",e);
			}
		}
		catch (ParserConfigurationException e) {
			Log.e(THIS_FILE,"Parseconfiguration exception.");
		}
		return doc;
	}
	
	/**
	 * Gets device manufacturer and model number.
	 * @return string
	 */
	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	} 
}
