package smartcity.recreationalplaces;

import org.w3c.dom.Document;
import routing.core.IZone;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ICafesApiManager {
    Optional<Document> getCafesDataXml(IZone zone);
    Set<OSMCafe> parseCafeInfo(Document document);
}
