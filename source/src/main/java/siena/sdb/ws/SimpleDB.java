/*
 * Copyright 2009 Alberto Gimeno <gimenete at gmail.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package siena.sdb.ws;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SimpleDB {

	private static final String VERSION = "2009-04-15";
	private static final String SIGNATURE_METHOD = "HmacSHA256";
	private static final String SIGNATURE_VERSION = "2";
	private static final String ENCODING = "UTF-8";

	private static final String HOST = "sdb.amazonaws.com";
	private static final String PROTOCOL = "http";
	private static final String PATH = "/";
	private static final String METHOD = "POST";

	private String awsAccessKeyId = null;
	private String awsSecretAccessKey = null;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") {
		private static final long serialVersionUID = 1L;
		{
			setTimeZone(TimeZone.getTimeZone("UTC"));
		}
	};

	public SimpleDB(String awsAccessKeyId, String awsSecretAccessKey) {
		this.awsAccessKeyId = awsAccessKeyId;
		this.awsSecretAccessKey = awsSecretAccessKey;
	}

	private String sign(String data, String key) {
		try {
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(ENCODING), SIGNATURE_METHOD);
			Mac mac = Mac.getInstance(SIGNATURE_METHOD);
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(data.getBytes(ENCODING));
			return Base64.encodeBytes(rawHmac);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void signParams(String httpverb, String host, String uri, String secretAccessKey, TreeMap<String, String> parameters) {
		parameters.put("AWSAccessKeyId", awsAccessKeyId);
		parameters.put("SignatureVersion", SIGNATURE_VERSION);
		parameters.put("SignatureMethod", SIGNATURE_METHOD);
		parameters.put("Timestamp", timestamp());
		parameters.put("Version", VERSION);
		String signature = signature(httpverb, host, uri, parameters);
		parameters.put("Signature", sign(signature, secretAccessKey));
	}

	private String query(TreeMap<String, String> parameters) {
		if(parameters.isEmpty()) return "";
		StringBuilder params = new StringBuilder();
		for (String key : parameters.keySet()) {
			params.append("&");
			params.append(urlEncode(key));
			params.append("=");
			params.append(urlEncode(parameters.get(key)));
		}

		return params.toString().substring(1);
	}

	private String signature(String httpverb, String host, String uri, TreeMap<String, String> parameters) {
		return httpverb+"\n"+host+"\n"+uri+"\n"+query(parameters);
	}

	private String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, ENCODING).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public Response createDomain(String domain) {
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		parameters.put("Action", "CreateDomain");
		parameters.put("DomainName", domain);

		return request(parameters, new PlainHandler());
	}

	public Response putAttributes(String domain, Item item) {
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		parameters.put("Action", "PutAttributes");
		parameters.put("DomainName", domain);
		parameters.put("ItemName", item.name);
		
		int i = 0;
		for (Map.Entry<String, List<String>> entry : item.attributes.entrySet()) {
			List<String> values = entry.getValue();
			for (String value : values) {
				parameters.put("Attribute."+i+".Name", entry.getKey());
				parameters.put("Attribute."+i+".Value", value);
				parameters.put("Attribute."+i+".Replace", "true"); // FIXME always replaces
				i++;
			}
		}

		return request(parameters, new PlainHandler());
	}
	
	public GetAttributesResponse getAttributes(String domain, String itemName) {
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		parameters.put("Action", "GetAttributes");
		parameters.put("DomainName", domain);
		parameters.put("ItemName", itemName);
		
		return request(parameters, new GetAttributesHandler(itemName));
	}
	
	public ListDomainsResponse listDomains(String maxNumberOfDomains, String nextToken) {
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		parameters.put("Action", "ListDomains");
		if(maxNumberOfDomains != null)
			parameters.put("MaxNumberOfDomains", maxNumberOfDomains);
		if(nextToken != null)
			parameters.put("NextToken", nextToken);
		
		return request(parameters, new ListDomainsHandler());
	}
	
	public Response deleteDomain(String domain) {
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		parameters.put("Action", "DeleteDomain");
		parameters.put("DomainName", domain);

		return request(parameters, new PlainHandler());
	}
	
	public DomainMetadataResponse domainMetadata(String domain) {
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		parameters.put("Action", "DomainMetadata");
		parameters.put("DomainName", domain);

		return request(parameters, new DomainMetadataHandler());
	}
	
	public Response deleteAttributes(String domain, Item item) {
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		parameters.put("Action", "DeleteAttributes");
		parameters.put("DomainName", domain);
		parameters.put("ItemName", item.name);

		int i = 0;
		for (Map.Entry<String, List<String>> entry : item.attributes.entrySet()) {
			List<String> values = entry.getValue();
			for (String value : values) {
				parameters.put("Attribute."+i+".Name", entry.getKey());
				parameters.put("Attribute."+i+".Value", value);
				i++;
			}
		}

		return request(parameters, new PlainHandler());
	}
	
	public SelectResponse select(String selectExpression, String nextToken) {
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		parameters.put("Action", "Select");
		if(selectExpression != null)
			parameters.put("SelectExpression", selectExpression);
		if(nextToken != null)
			parameters.put("NextToken", nextToken);

		return request(parameters, new SelectHandler());
	}

	private static String timestamp() {
		return DATE_FORMAT.format(new Date());
	}

	private <T extends Response> T request(TreeMap<String, String> parameters, BasicHandler<T> handler) {
		signParams(METHOD, HOST, PATH, awsSecretAccessKey, parameters);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);
			SAXParser parser = factory.newSAXParser();

			URL url = new URL(PROTOCOL+"://"+HOST+PATH);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset="+ENCODING);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			OutputStream os = connection.getOutputStream();
			// System.out.println(PROTOCOL+"://"+HOST+PATH+"?"+query(parameters));
			os.write(query(parameters).getBytes(ENCODING));
			os.close();
			parser.parse(connection.getInputStream(), handler);
			return handler.response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String quote(String s) {
		return "\""+s.replace("'", "''")+"\"";
	}

} abstract class BaseHandler extends DefaultHandler {

	protected StringBuilder buffer = new StringBuilder();

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		buffer.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		read(name, buffer.toString());
		buffer = new StringBuilder();
	}

	public abstract void read(String name, String value);

} class BasicHandler<T extends Response> extends BaseHandler {

	protected T response = null;
	
	public BasicHandler(T response) {
		this.response = response;
	}

	@Override
	public void read(String name, String value) {
		if(name.equals("RequestId"))
			response.metadata.requestId = value;
		else if(name.equals("BoxUsage"))
			response.metadata.boxUsage = value;
	}
	
	public T getResponse() {
		return response;
	}

} class PlainHandler extends BasicHandler<Response> {
	
	public PlainHandler() {
		super(new Response());
	}

} class GetAttributesHandler extends BasicHandler<GetAttributesResponse> {

	private String attrname;
	private String attrvalue;
	
	public GetAttributesHandler(String itemName) {
		super(new GetAttributesResponse(itemName));
	}
	
	@Override
	public void read(String name, String value) {
		super.read(name, value);
		if(name.equals("Attribute")) {
			response.item.add(attrname, attrvalue);
		} else if(name.equals("Name")) {
			attrname = value;
		} else if(name.equals("Value")) {
			attrvalue = value;
		}
	}
	
} class ListDomainsHandler extends BasicHandler<ListDomainsResponse> {
	
	public ListDomainsHandler() {
		super(new ListDomainsResponse());
	}
	
	@Override
	public void read(String name, String value) {
		super.read(name, value);
		if(name.equals("DomainName"))
			response.domains.add(value);
	}
	
} class DomainMetadataHandler extends BasicHandler<DomainMetadataResponse> {
	
	public DomainMetadataHandler() {
		super(new DomainMetadataResponse());
	}
	
	@Override
	public void read(String name, String value) {
		super.read(name, value);
		if(name.equals("Timestamp"))
			response.domainMetadata.timestamp = Long.parseLong(value);
		else if(name.equals("ItemCount"))
			response.domainMetadata.itemCount = Long.parseLong(value);
		else if(name.equals("AttributeValueCount"))
			response.domainMetadata.attributeValueCount = Long.parseLong(value);
		else if(name.equals("AttributeNameCount"))
			response.domainMetadata.attributeNameCount = Long.parseLong(value);
		else if(name.equals("ItemNamesSizeBytes"))
			response.domainMetadata.itemNamesSizeBytes = Long.parseLong(value);
		else if(name.equals("AttributeValuesSizeBytes"))
			response.domainMetadata.attributeValuesSizeBytes = Long.parseLong(value);
		else if(name.equals("AttributeNamesSizeBytes"))
			response.domainMetadata.attributeNamesSizeBytes = Long.parseLong(value);
	}

} class SelectHandler extends BasicHandler<SelectResponse> {
	
	private Item item;
	private boolean attribute;

	private String attrname;
	private String attrvalue;
	
	public SelectHandler() {
		super(new SelectResponse());
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		if(name.equals("Item")) {
			item = new Item();
			response.items.add(item);
		} else if(name.equals("Attribute")) {
			attribute = true;
		}
	}
	
	@Override
	public void read(String name, String value) {
		super.read(name, value);
		if(name.equals("Name")) {
			if(attribute)
				attrname = value;
			else
				item.name = value;
		} else if(name.equals("Value")) {
			attrvalue = value;
		} else if(name.equals("NextToken")) {
			response.nextToken = value;
		} else if(name.equals("Attribute")) {
			attribute = false;
			item.add(attrname, attrvalue);
		}
	}
	
}