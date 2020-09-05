package utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;

public class FileWriterWrapper {
    public static final String DEFAULT_OUTPUT_PATH_XML = "target/output.xml";
    public static final String DEFAULT_OUTPUT_PATH_JSON = "target/output.json";

    private static final Logger logger = LoggerFactory.getLogger(FileWriterWrapper.class);
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void write(Document xmlDoc) {
        write(xmlDoc, DEFAULT_OUTPUT_PATH_XML);
    }

    public static void write(Document xmlDoc, String path) {
        try {
            DOMSource source = new DOMSource(xmlDoc);
            File file = new File(path);
            if (file.exists() || file.createNewFile()) {
                java.io.FileWriter writer = new java.io.FileWriter(file);
                StreamResult result = new StreamResult(writer);

                var transformer = transformerFactory.newTransformer();
                transformer.transform(source, result);
            }
        } catch (Exception e) {
            logger.warn("Could not write to file", e);
        }
    }

    public static void write(JSONObject jsonObj) {
        write(jsonObj, DEFAULT_OUTPUT_PATH_JSON);
    }

    public static void write(JSONObject jsonObj, String path) {
        try {
            File file = new File(path);
            if (file.exists() || file.createNewFile()) {
                FileWriter writer = new FileWriter(file);
                mapper.writerWithDefaultPrettyPrinter().writeValue(writer, jsonObj);
            }
        } catch (Exception e) {
            logger.warn("Could not write to file", e);
        }
    }
}
