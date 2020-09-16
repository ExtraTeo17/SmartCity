package testutils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XmlParser {
    private static final String resourcePath = "src/test/resources/";
    private static final DocumentBuilder builder;

    static {
        var factory = DocumentBuilderFactory.newDefaultInstance();
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document getDocument(String filename) {
        File file = new File(resourcePath + filename);
        if (!file.exists()) {
            throw new RuntimeException("No such file: " + file.getPath());
        }

        Document document;
        try {
            document = builder.parse(file);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return document;
    }
}
