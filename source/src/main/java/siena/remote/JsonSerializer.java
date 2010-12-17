package siena.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import siena.Json;

public class JsonSerializer implements Serializer {

	public Document deserialize(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		Json json = Json.load(reader);
		return fromJson(json);
	}

	public void serialize(Document document, OutputStream out)
			throws IOException {
		Json json = toJson(document);
		json.write(new OutputStreamWriter(out));
	}
	
	private static Json toJson(Document doc) {
		Json json = Json.map();
		Element root = doc.getRootElement();
		Json map = Json.map();
		json.put(root.getName(), map);
		toJson(root, map);
		return json;
	}
	
	@SuppressWarnings("unchecked")
	private static void toJson(Element element, Json e) {
		List<Element> elements = element.elements();
		if(elements.isEmpty()) {
			if(element.hasContent()) {
				e.put("@", element.getText());
//			} else {
//				e.put("@", null);
			}
		} else {
			Json map = Json.map();
			e.put("@", map);
			for (Element elem : elements) {
				Json j = Json.map();
				map.put(elem.getName(), j);
				toJson(elem, j);
			}
		}
		
		List<Attribute> attributes = element.attributes();
		for (Attribute attr : attributes) {
			e.put(attr.getName(), attr.getValue());
		}
	}
	
	private static Document fromJson(Json json) {
		String root = json.keys().iterator().next();
		
		Document doc = DocumentHelper.createDocument();
		fromJson(json.get(root), doc.addElement(root));
		return doc;
	}
	
	private static void fromJson(Json json, Element element) {
		Set<String> keys = json.keys();
		for (String key : keys) {
			if("@".equals(key)) {
				Json value = json.get(key);
				if(value.isString()) {
					element.setText(value.str());
				} else {
					Set<String> ks = value.keys();
					for (String k : ks) {
						Element child = element.addElement(k);
						fromJson(value.get(k), child);
					}
				}
			} else {
				element.addAttribute(key, json.get(key).str());
			}
		}
	}
	
	public static void main(String[] args) {
		Document doc = DocumentHelper.createDocument();
		doc.addElement("root")
			.addAttribute("foo", "bar")
			.addAttribute("foobar", "baz")
			.addElement("child")
			.addAttribute("x", "y")
			.addElement("grandchild")
			.addAttribute("a", "b")
			.setText("foo bar");
		
		System.out.println("Original document");
		System.out.println(doc.asXML());
		System.out.println();
		
		Json json = toJson(doc);
		System.out.println("As Json");
		System.out.println(json);
		System.out.println();
		
		Document result = fromJson(json);
		System.out.println("Back to document");
		System.out.println(result.asXML());
		System.out.println();
	}

}
