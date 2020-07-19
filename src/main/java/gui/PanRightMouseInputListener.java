//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package gui;

import org.jxmapviewer.JXMapViewer;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D.Double;

public class PanRightMouseInputListener extends MouseInputAdapter {
    private Point prev;
    private JXMapViewer viewer;
    private Cursor priorCursor;

    public PanRightMouseInputListener(JXMapViewer viewer) {
        this.viewer = viewer;
    }

    public void mousePressed(MouseEvent evt) {
        if (SwingUtilities.isRightMouseButton(evt)) {
            if (this.viewer.isPanningEnabled()) {
                this.prev = evt.getPoint();
                this.priorCursor = this.viewer.getCursor();
                this.viewer.setCursor(Cursor.getPredefinedCursor(13));
            }
        }
    }

    public void mouseDragged(MouseEvent evt) {
        if (SwingUtilities.isRightMouseButton(evt)) {
            if (this.viewer.isPanningEnabled()) {
                Point current = evt.getPoint();
                double x = this.viewer.getCenter().getX();
                double y = this.viewer.getCenter().getY();
                if (this.prev != null) {
                    x += (double) (this.prev.x - current.x);
                    y += (double) (this.prev.y - current.y);
                }

                int maxHeight = (int) (this.viewer.getTileFactory().getMapSize(this.viewer.getZoom()).getHeight() * (double) this.viewer.getTileFactory().getTileSize(this.viewer.getZoom()));
                if (y > (double) maxHeight) {
                    y = (double) maxHeight;
                }

                this.prev = current;
                this.viewer.setCenter(new Double(x, y));
                this.viewer.repaint();
            }
        }
    }

    public void mouseReleased(MouseEvent evt) {
        if (SwingUtilities.isRightMouseButton(evt)) {
            this.prev = null;
            this.viewer.setCursor(this.priorCursor);
        }
    }
}
