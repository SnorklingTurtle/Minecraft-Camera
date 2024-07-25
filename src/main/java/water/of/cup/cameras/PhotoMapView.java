package main.java.water.of.cup.cameras;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.ArrayList;
import java.util.List;

public class PhotoMapView implements MapView {

    private int mapID;
    private boolean isLocked = false;
    private World world;
    List<MapRenderer> renderers = new ArrayList<>();

    public PhotoMapView(int id, World world) {
        this.mapID = id;
        this.world = world;
    }

    @Override
    public int getId() {
        return mapID;
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    public Scale getScale() {
        return Scale.NORMAL;
    }

    @Override
    public void setScale(Scale scale) {

    }

    @Override
    public int getCenterX() {
        return 0;
    }

    @Override
    public int getCenterZ() {
        return 0;
    }

    @Override
    public void setCenterX(int x) {

    }

    @Override
    public void setCenterZ(int z) {

    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public List<MapRenderer> getRenderers() {
        return renderers;
    }

    @Override
    public void addRenderer(MapRenderer renderer) {
        renderers.add(renderer);
    }

    @Override
    public boolean removeRenderer(MapRenderer renderer) {
        return renderers.remove(renderer);
    }

    @Override
    public boolean isTrackingPosition() {
        return false;
    }

    @Override
    public void setTrackingPosition(boolean trackingPosition) {

    }

    @Override
    public boolean isUnlimitedTracking() {
        return false;
    }

    @Override
    public void setUnlimitedTracking(boolean unlimited) {

    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}
