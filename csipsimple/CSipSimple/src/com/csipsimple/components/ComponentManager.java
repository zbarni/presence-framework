package com.csipsimple.components;

import java.io.StringWriter;
//import org.apache.commons.lang.StringEscapeUtils;

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
import org.w3c.dom.Text;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import com.csipsimple.api.ISipService;
import com.csipsimple.service.SipService;
import com.csipsimple.utils.Log;

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

	private static String presenceXml = "@zajzi init";
	private static Document presenceDoc;
	// Service connection
    private ISipService service;
	private static Context mContext;
    
	// Sensors
	public static Proximity proximity;
	public static LocationTracker location;
	public static ActuatorComponent vibrator;
	public static Headset headset;
	
	// Actuators	

	public ComponentManager(Context context) {
		mContext = context;
	}
	
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
	
	public void initComponents() {
		proximity = new Proximity(this.mContext, "proximity","Proximity", Component.SENSOR);
		headset = new Headset(this.mContext, "wired.headset", "Wired headset", Component.SENSOR);

//		location = new LocationTracker(context, "gps.location","GPS receiver");
//		vibrator = new ActuatorComponent(this.mContext, "vibrator","Vibrator");
		
//		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//		v.vibrate(300);
	}
	
	public void publish() {
		createPresenceDoc();
		safelyConnectTheService();
		Log.d(THIS_FILE,"XML finished connecting.. waiting for async call, probably");
	}
	
	@SuppressLint("NewApi")
	private void createPresenceDoc() {
		Log.d(THIS_FILE,"Building XML");
		String version = "";
		try {
		    version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
		    Log.e("tag", e.getMessage());
		}
		
		try {
			presenceDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = presenceDoc.createElement(XML_UA);
			Element device = presenceDoc.createElement(XML_DEVICE);
			Element type = presenceDoc.createElement(XML_TYPE);
			Element model = presenceDoc.createElement(XML_MODEL);
			Element os = presenceDoc.createElement(XML_OS);
			Element os_name = presenceDoc.createElement(XML_OS_NAME);
			Element os_version = presenceDoc.createElement(XML_OS_VERSION);
			Element uri = presenceDoc.createElement(XML_URI);
			Element sensors = presenceDoc.createElement(XML_SENSORS);
			Element actuators = presenceDoc.createElement(XML_ACTUATORS);
			
			presenceDoc.appendChild(root);
			root.setAttribute("id", "randomuri");
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
		    root.appendChild(sensors);
//		    Element s_proximity = presenceDoc.createElement(XML_SENSOR);
//		    Element s_name = presenceDoc.createElement(XML_SENSOR);
//		    s_proximity.setAttribute("id", proximity.getmId());
//		    
//		    sensors.appendChild(newChild)
		    String string_sensors;
		    string_sensors = proximity.getComponentXml();
		    string_sensors += headset.getComponentXml();
		    Text sensors_text = presenceDoc.createTextNode(string_sensors);
		    sensors.setTextContent(string_sensors);
		    
//		    type.setTextContent("");
//		    device.appendChild();
//		    type.setTextContent("");
//		    device.appendChild();
//		    type.setTextContent("");
//		    device.appendChild();
//		    type.setTextContent("");
		    
		    
//		    tagStudy.setAttribute(Study.ID, String.valueOf(study.mId));
		    try {
		    	Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    	transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    	StringWriter writer = new StringWriter();
		    	StreamResult result = new StreamResult(writer);
		    	transformer.transform(new DOMSource(presenceDoc), result);
		    	
		    	presenceXml = writer.toString();
		    	// remove XML header because it goes into the body
		    	presenceXml = StringEscapeUtils.unescapeXml(presenceXml);
		    	Log.d(THIS_FILE,"XML " + presenceXml);
		    }
		    catch (TransformerException e) {
		    	Log.e(THIS_FILE,"Transformer exception.");
		    }
		}
		catch (ParserConfigurationException e) {
			Log.e(THIS_FILE,"Parseconfiguration exception.");
		}
	}
	
//	@Override
//	public void onServiceConnected(ComponentName name, IBinder _service) {
//		Log.d(THIS_FILE, "The service is now connected!");
//		service = ISipService.Stub.asInterface(_service);
//		Log.d(THIS_FILE, "Querying the message...");
//	}
//	
//	@Override
//	public void onServiceDisconnected(ComponentName name) {
//		Log.d(THIS_FILE, "The connection to the service got disconnected unexpectedly!");
//		service = null;
//	}
	
	/**
	 * Method to connect the Service.
	 */
	public void safelyConnectTheService() {
		if(service == null) {
			Log.d(THIS_FILE,"XML binding to service");
			mContext.bindService(new Intent(mContext, SipService.class), connection, Context.BIND_AUTO_CREATE);
//			Intent bindIntent = new Intent();
//			bindIntent.setClassName(AIDL_MESSAGE_SERVICE_PACKAGE, AIDL_MESSAGE_SERVICE_PACKAGE + AIDL_MESSAGE_SERVICE_CLASS);
//			bindService(bindIntent, this, Context.BIND_AUTO_CREATE);
//			Log.d(THIS_FILE, "The Service will be connected soon (asynchronus call)!");
		}
	}	
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = ISipService.Stub.asInterface(arg1);
            try {
            	service.setComponentPresence(2, presenceXml, 1);
			} catch (RemoteException e) {
				// TODO: handle exception
				Log.d(THIS_FILE,"@zajzi presence: Could not set component presence");
			}
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            service = null;
        }
    };

}
