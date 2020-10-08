package gui;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypointRenderer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class CustomWaypointRenderer implements WaypointRenderer<Waypoint> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultWaypointRenderer.class);
    private static final String IMAGES_PATH = "images/";
    private BufferedImage img = null;

    public CustomWaypointRenderer(String imageName) {
        try {
            var path = IMAGES_PATH + imageName;
            var resource = CustomWaypointRenderer.class.getClassLoader().getResource(path);
            if (resource != null) {
                this.img = ImageIO.read(resource);
            }
            else {
                logger.warn("couldn't read " + path);
            }
        } catch (Exception ex) {
            logger.warn("couldn't read " + imageName, ex);
        }
    }

    public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint w) {
        if (this.img != null) {
            Point2D point = map.getTileFactory().geoToPixel(w.getPosition(), map.getZoom());
            int x = (int) point.getX() - this.img.getWidth() / 2;
            int y = (int) point.getY() - this.img.getHeight();
            g.drawImage(this.img, x, y, null);
        }
    }
}
