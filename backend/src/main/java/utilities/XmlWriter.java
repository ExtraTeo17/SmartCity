package utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;

public class XmlWriter {
    public static final String DEFAULT_OUTPUT_PATH = "output.xml";

    private static final Logger logger = LoggerFactory.getLogger(XmlWriter.class);
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public static void write(Document xmlDoc, String path) {
        try {
            DOMSource source = new DOMSource(xmlDoc);
            File file = new File(path);
            if (file.createNewFile()) {
                FileWriter writer = new FileWriter(file);
                StreamResult result = new StreamResult(writer);

                var transformer = transformerFactory.newTransformer();
                transformer.transform(source, result);
            }
        } catch (Exception e) {
            logger.warn("Could not write to file", e);
        }
    }

    public static void write(Document xmlDoc) {
        write(xmlDoc, DEFAULT_OUTPUT_PATH);
    }
}
