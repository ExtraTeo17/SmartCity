package testutils;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileLoader {
    public static final String resourcePath = "src/test/resources/";
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
        File file = getFile(filename);
        Document document;
        try {
            document = builder.parse(file);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return document;
    }

    private static File getFile(String filename) {
        File file = new File(resourcePath + filename);
        if (!file.exists()) {
            throw new RuntimeException("No such file: " + file.getPath());
        }
        return file;
    }

    public static String getJsonString(String filename) {
        File file = getFile(filename);
        String result;
        try {
            var fileStream = new FileInputStream(file);
            result = IOUtils.toString(fileStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return result;
    }
}
