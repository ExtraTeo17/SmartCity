package utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileWrapper {
    public static final String DEFAULT_OUTPUT_PATH_XML = "target/output.xml";
    public static final String DEFAULT_OUTPUT_PATH_JSON = "target/output.json";
    public static final String DEFAULT_OUTPUT_PATH_CACHE = ".cache";

    private static final Logger logger = LoggerFactory.getLogger(FileWrapper.class);
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

    public static void write(String jsonObj) {
        write(jsonObj, DEFAULT_OUTPUT_PATH_JSON);
    }

    public static void write(String jsonString, String path) {
        try {
            File file = new File(path);
            if (file.exists() || file.createNewFile()) {
                var writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                mapper.writerWithDefaultPrettyPrinter().writeValue(writer, jsonString);
            }
        } catch (Exception e) {
            logger.warn("Could not write to file", e);
        }
    }

    public static <T extends Serializable> void cacheToFile(T obj, String fileName) {
        String path = DEFAULT_OUTPUT_PATH_CACHE + "/" + fileName + ".ser";
        try {
            File file = new File(path);
            if (file.exists() || file.createNewFile()) {
                var fileStream = new FileOutputStream(file);
                var objectStream = new ObjectOutputStream(fileStream);
                objectStream.writeObject(obj);
            }
        } catch (Exception e) {
            logger.warn("Could not serialize to file:  " + path, e);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public static <T extends Serializable> T getFromCache(String fileName) {
        String path = DEFAULT_OUTPUT_PATH_CACHE + "/" + fileName + ".ser";
        T data = null;
        File file = null;
        try {
            file = new File(path);
            if (file.exists()) {
                var fileStream = new FileInputStream(file);
                var objectStream = new ObjectInputStream(fileStream);
                data = (T) objectStream.readObject();
            }
        } catch (Exception e) {
            logger.warn("Could not get file from cache: " + path, e);
            if (file != null && file.exists()) {
                if (file.delete()) {
                    logger.info("Deleted: " + file.getPath());
                }
            }
        }

        return data;
    }
}
