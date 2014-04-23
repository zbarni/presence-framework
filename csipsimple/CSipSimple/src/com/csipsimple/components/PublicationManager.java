package com.csipsimple.components;

import java.io.StringWriter;

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

import com.csipsimple.api.SipProfileState;
import com.csipsimple.components.ComponentProfile.Components;
import com.csipsimple.service.ComponentService;
import com.csipsimple.ui.Installation;
import com.csipsimple.utils.Log;

public class PublicationManager {
	public static final String THIS_FILE = "PUB MGR";
	private ComponentService mComponentService;
	private Context mContext;

	public PublicationManager(ComponentService cService) {
		this.mComponentService = cService;
		this.mContext = (Context) cService.getSipService();
	}

	/**
	 * 
	 * @param c Component class of sensor or actuator 
	 */
	public void updateComponentModel(AbstractComponent c) {
		Node comp;
		Node idNode;
		String idVal;

		for (AccountWrapper acc : mComponentService.getAccounts().values()) {		
			Document doc = acc.getDocument();

			NodeList compList = doc.getElementsByTagName(c.getType());
			Log.d(THIS_FILE,"@XML updating CM for id : " + c.getId() 
					+ " size of complist : " + compList.getLength() +
					" type of comp : " + c.getType());

			// remove component with id from DOM
			for (int i=0; i < compList.getLength(); ++i) {
				comp = compList.item(i);
				idVal = null;
				idNode = comp.getAttributes().getNamedItem("id");

				// <ua> tag has id attribute, correct
				if (idNode != null) {
					idVal = idNode.getNodeValue();
				}
				// id we were searching for
				if (idVal == c.getId()) {
					// we have to remove it
					Log.d(THIS_FILE,"@XML node with id was found");
					try {
						Node parentNode = comp.getParentNode();
						parentNode.removeChild(comp);
						Log.d(THIS_FILE,"@XML removed child");
						Element tmp;
						if ((tmp = c.getComponentElement(doc)) != null) {
							try {
								parentNode.appendChild(tmp);
								Log.d(THIS_FILE,"@XML appneding child to parent");
							}
							catch (Exception e) {
								Log.e(THIS_FILE,"@XML problem appending child to parent",e);
							}
						}

						updatePublication(acc);
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
					if (c.getType() == ComponentProfile.SENSOR) {
						NodeList nl = doc.getElementsByTagName(ComponentProfile.XML_SENSORS);
						if (nl.getLength() > 0) {
							Log.d(THIS_FILE,"@XML nl length + " +nl.getLength());
							Node el = nl.item(0);
							el.appendChild(elem);
						}
					}
					else {
						NodeList nl = doc.getElementsByTagName(ComponentProfile.XML_ACTUATORS);
						if (nl.getLength() > 0) {
							nl.item(0).appendChild(elem);
						}
					}
					updatePublication(acc);
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

	/**
	 * For each active account sends a PUBLISH message containing the 
	 * component model. 
	 */
	public void publish() {
		// looping over registered URIs
		//		ArrayList<SipProfile> profiles = SipProfile.getAllProfiles(mContext, true);
		//
		//		for (SipProfile p : profiles) {
		//			Cursor c = mContext.getContentResolver().query(ContentUris
		//					.withAppendedId(SipProfile.ACCOUNT_STATUS_ID_URI_BASE, p.id), 
		//					null, null, null, null);
		//			if (c != null) {
		//				try {
		//					if(c.getCount() > 0) {
		//						c.moveToFirst();
		//						SipProfileState account = new SipProfileState(c);
		//						Log.d(THIS_FILE,"@XML Iterating over accounts, gruu is " + account.getGruu());
		//						if (ComponentManager.getInstance().isAccountRegistered(account.getAccountId())) {
		//							Log.d(THIS_FILE,"@XML Account is registered, will update gruu and publish");
		//							ComponentManager.getInstance().updateGRUU(account.getAccountId(),account.getGruu());
		//							publish(account.getAccountId(),ComponentManager.getInstance().getPublicationDocument(account.getAccountId()));
		//						}
		//						else {
		//							Log.d(THIS_FILE,"@XML New account, no gruu update");
		//							ComponentManager.getInstance().addAccount(account.getAccountId(),account.getGruu());
		//							publish(account.getAccountId(),ComponentManager.getInstance().getPublicationDocument(account.getAccountId()));
		//						}
		//					}
		//				} catch (Exception e) {
		//					Log.e(THIS_FILE, "Error on looping over sip profiles", e);
		//				} finally {
		//					c.close();
		//				}
		//			}
		//		}
	}

	/**
	 * 
	 * @param xmlDoc
	 */
	public void updatePublication(AccountWrapper acc) {
		Log.d(THIS_FILE,"@XML Publish function called in component service [xml string] :");
		updateGRUU(acc);
		if (mComponentService.getSipService() != null) {
			mComponentService.getSipService().setComponentPresence(1,
					domToXmlString(acc.getDocument()),
					acc.getProfile().id);
		}
		else {
			Log.d(THIS_FILE,"@XML SipService in CompServ is null");
		}
	}

	/**
	 * Transforms the Document component model into a String, and escapes
	 * the XML header.
	 * @return true on success, false otherwise0
	 */
	private static String domToXmlString(Document doc) {
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

	public boolean updateGRUU(AccountWrapper acc) {
		SipProfileState sp = new SipProfileState(acc.getProfile());
		Log.d(THIS_FILE,"@XML Setting new GRUU : " + sp.getGruu());
		NodeList nodeList = acc.getDocument().
				getElementsByTagName(ComponentProfile.XML_UA);

		// there should be exactly one <ua> element
		assert(nodeList.getLength() == 1);

		Node ua = nodeList.item(0);
		Node idNode = ua.getAttributes().getNamedItem("id");

		// <ua> tag has id attribute, correct
		if (idNode != null) {
			idNode.setNodeValue(sp.getGruu() + ";gr=urn:uuid:" + Installation.id(mContext));
			return true;
		}
		else {
			Log.e(THIS_FILE,"@XML <ua> element does not have ID attribute");
			return false;
		}
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
	public Document createPresenceDoc(String uri) {
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
			Element root = doc.createElement(ComponentProfile.XML_UA);
			Element device = doc.createElement(ComponentProfile.XML_DEVICE);
			Element type = doc.createElement(ComponentProfile.XML_TYPE);
			Element category = doc.createElement(ComponentProfile.XML_CATEGORY);
			Element model = doc.createElement(ComponentProfile.XML_MODEL);
			Element os = doc.createElement(ComponentProfile.XML_OS);
			Element os_name = doc.createElement(ComponentProfile.XML_OS_NAME);
			Element os_version = doc.createElement(ComponentProfile.XML_OS_VERSION);
			Element sensorsNode = doc.createElement(ComponentProfile.XML_SENSORS);
			Element actuatorsNode = doc.createElement(ComponentProfile.XML_ACTUATORS);

			doc.appendChild(root);
			root.setAttribute("id", uri + ";gr=urn:uuid:" + Installation.id(mContext));
			root.appendChild(device);
			device.appendChild(type);
			type.setTextContent("Smartphone");
			device.appendChild(category);
			category.setTextContent("mobile");

			device.appendChild(model);
			model.setTextContent(ComponentManager.getDeviceName());

			device.appendChild(os);
			os.appendChild(os_name);
			os_name.setTextContent("Android");
			os.appendChild(os_version);
			os_version.setTextContent(version);

			//sensors
			root.appendChild(sensorsNode);
			try {
				Element elem;
				for (Components c : Components.values()) {
					AbstractComponent ac = ComponentManager.getInstance().getAbstractComponent(c);
					if (ac != null && ac.getType().equals(ComponentProfile.SENSOR)) {
						elem = ac.getComponentElement(doc);
						if (elem != null) {
							sensorsNode.appendChild(elem);
						}
					}
				}
			}
			catch (Exception e) {
				Log.e(THIS_FILE,"@XML problem adding sensor child",e);
			}

			//actuators
			root.appendChild(actuatorsNode);
			try {
				Element elem;
				for (Components c : Components.values()) {
					AbstractComponent ac = ComponentManager.getInstance().getAbstractComponent(c);
					if (ac != null && ac.getType().equals(ComponentProfile.ACTUATOR)) {
						elem = ac.getComponentElement(doc);
						if (elem != null) {
							actuatorsNode.appendChild(elem);
						}
					}
				}
			}
			catch (Exception e) {
				Log.e(THIS_FILE,"@XML problem adding actuator child",e);
			}
		}
		catch (ParserConfigurationException e) {
			Log.e(THIS_FILE,"Parseconfiguration exception.");
		}
		Log.d(THIS_FILE,"Leaving Building Component Model for account" + uri);
		return doc;
	}
}
