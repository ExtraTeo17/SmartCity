package LightStrategies;

import Agents.LightManager;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import java.util.List;

public abstract class LightStrategy {
    public abstract void ApplyStrategy(LightManager agent);

    public abstract void drawCrossroad(List<Painter<JXMapViewer>> waypointPainter);
}
