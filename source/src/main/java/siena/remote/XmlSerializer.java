package siena.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class XmlSerializer implements Serializer {

	public Document deserialize(InputStream in) throws IOException {
		try {
			return new SAXReader().read(in);
		} catch (DocumentException e) {
			throw new RuntimeException("Error while parsing stream", e);
		}
	}

	public void serialize(Document document, OutputStream out) throws IOException {
		new XMLWriter(out, OutputFormat.createCompactFormat()).write(document);
	}

}
