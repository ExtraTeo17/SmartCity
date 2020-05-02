package SmartCity;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.painter.Painter;

public class ZonePainter implements Painter<JXMapViewer>
{
    private Color color = Color.YELLOW;
    private boolean antiAlias = true;
    private int radius = 500;

    private GeoPosition center;

    public ZonePainter(GeoPosition center, int radius, Color color)
    {
        this.color = color;
        this.radius = radius;
        this.center = center;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h)
    {
        g = (Graphics2D) g.create();

        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        if (antiAlias)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(color);
        g.setStroke(new BasicStroke(2));

        double newLat = center.getLatitude() + radius * 0.0000089;
        Point2D onCircle = map.getTileFactory().geoToPixel(new GeoPosition(newLat, center.getLongitude()), map.getZoom());
        Point2D pt = map.getTileFactory().geoToPixel(center, map.getZoom());
        int r = (int)(pt.getY() - onCircle.getY());
        g.drawOval((int)pt.getX() - r, (int)pt.getY() - r, 2*r, 2*r);

        g.dispose();
    }
}
