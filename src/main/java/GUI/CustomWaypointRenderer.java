package GUI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.DefaultWaypointRenderer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public class CustomWaypointRenderer implements WaypointRenderer<Waypoint> {
    private static final Log log = LogFactory.getLog(DefaultWaypointRenderer.class);
    private BufferedImage img = null;

    public CustomWaypointRenderer(String imageName) {
        try {
            this.img = ImageIO.read(getClass().getResource("../" + imageName));
        } catch (Exception var2) {
            log.warn("couldn't read " + imageName, var2);
        }
    }

    public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint w) {
        if (this.img != null) {
            Point2D point = map.getTileFactory().geoToPixel(w.getPosition(), map.getZoom());
            int x = (int) point.getX() - this.img.getWidth() / 2;
            int y = (int) point.getY() - this.img.getHeight();
            g.drawImage(this.img, x, y, (ImageObserver) null);
        }
    }
}
