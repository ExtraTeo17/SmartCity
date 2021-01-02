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
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Used for I/O interaction.
 */
public class FileWrapper {
    public static final String DEFAULT_OUTPUT_PATH_XML = "target/output.xml";
    public static final String DEFAULT_OUTPUT_PATH_JSON = "target/output.json";
    public static final String DEFAULT_OUTPUT_PATH_CACHE = ".cache";

    private static final Logger logger = LoggerFactory.getLogger(FileWrapper.class);
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        var dir = new File(DEFAULT_OUTPUT_PATH_CACHE);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
    }

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

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T getFromCache(String fileName) {
        String path = DEFAULT_OUTPUT_PATH_CACHE + "/" + fileName + ".ser";
        Object data = tryReadFile(path);
        if (data == null) {
            return null;
        }

        try {
            return (T) data;
        } catch (Exception e) {
            logger.warn("Could not get file from cache: " + path + ", msg:\n" + e.getMessage());
            tryDeleteFile(path);
        }

        return null;
    }

    private static Object tryReadFile(String path) {
        Object data = null;
        FileInputStream fileStream = null;
        ObjectInputStream objectStream = null;
        boolean error = false;
        try {
            var file = new File(path);
            if (file.exists()) {
                fileStream = new FileInputStream(file);
                objectStream = new ObjectInputStream(fileStream);
                data = objectStream.readObject();
            }
        } catch (Exception e) {
            logger.warn("Could not read file: " + path, e);
            error = true;
        }

        try {
            if (objectStream != null) {
                objectStream.close();
            }
            if (fileStream != null) {
                fileStream.close();
            }
        } catch (Exception e) {
            logger.info("Failed to close file streams");
        }

        if (error) {
            tryDeleteFile(path);
        }

        return data;
    }

    private static void tryDeleteFile(String path) {
        try {
            var pathObj = Path.of(path);
            Files.delete(pathObj);
            logger.info("Deleted: " + path);
        } catch (Exception eDelete) {
            logger.warn("Failed to delete file" + path, eDelete);
        }
    }
}
