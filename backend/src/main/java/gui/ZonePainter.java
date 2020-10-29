package gui;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import routing.core.IGeoPosition;

import java.awt.*;

public class ZonePainter implements Painter<JXMapViewer> {
    private Color color = Color.YELLOW;
    private final boolean antiAlias = true;
    private int radius = 500;
    private final IGeoPosition center;

    ZonePainter(IGeoPosition center, int radius, Color color) {
        this.color = color;
        this.radius = radius;
        this.center = center;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();

        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        g.setColor(color);
        g.setStroke(new BasicStroke(2));

        g.dispose();
    }
}
