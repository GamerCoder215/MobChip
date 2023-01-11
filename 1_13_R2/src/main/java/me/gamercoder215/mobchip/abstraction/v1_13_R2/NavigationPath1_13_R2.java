package me.gamercoder215.mobchip.abstraction.v1_13_R2;

import me.gamercoder215.mobchip.abstraction.ChipUtil;
import me.gamercoder215.mobchip.ai.navigation.NavigationPath;
import me.gamercoder215.mobchip.util.Position;
import net.minecraft.server.v1_13_R2.PathEntity;
import net.minecraft.server.v1_13_R2.PathPoint;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class NavigationPath1_13_R2 implements NavigationPath {
    private String name;
    private final Mob m;
    private final PathEntity handle;

    public NavigationPath1_13_R2(@NotNull PathEntity nms, @NotNull Mob m) {
        this.m = m;
        this.name = "bukkitpath";
        this.handle = nms;
    }

    private final List<Position> nodes = new ArrayList<>();

    /**
     * Advances this path.
     */
    @Override
    public void advance() {
        try {
            this.handle.a();

            Field points = this.handle.getClass().getDeclaredField("a");
            points.setAccessible(true);
            PathPoint[] pathPoints = (PathPoint[]) points.get(this.handle);

            PathPoint n = pathPoints[handle.e()];
            new EntityController1_13_R2(m).moveTo(n.a, n.b, n.c);
        } catch (Exception e) {
            ChipUtil.printStackTrace(e);
        }
    }

    /**
     * Get this Path's Name.
     *
     * @return this path's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets this Path's Name.
     *
     * @param name this path's new name
     */
    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Whether this NavigationPath is complete.
     *
     * @return true if complete, else false
     */
    @Override
    public boolean isDone() {
        return this.handle.b();
    }

    /**
     * Get the size of this NavigationPath.
     *
     * @return size
     */
    public int size() {
        return nodes.size();
    }

    /**
     * Whether this NavigationPath is empty.
     *
     * @return true if empty, else false
     */
    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    /**
     * Whether this Path contains this Navigation Node.
     *
     * @param o Position
     * @return true if contains, else false
     */
    @Override
    public boolean contains(@Nullable Position o) {
        return nodes.contains(o);
    }

    @Override
    @NotNull
    public Iterator<Position> iterator() {
        return nodes.iterator();
    }

    /**
     * Converts this NavigationPath into an Array of Nodes.
     *
     * @return Array of Position
     */
    @NotNull
    @Override
    public Position[] toArray() {
        return nodes.toArray(new Position[0]);
    }

    /**
     * Returns the index of this Navigation Node.
     *
     * @param o Position to fetch
     * @return Index found
     * @see List#indexOf(Object)
     */
    @Override
    public int indexOf(@Nullable Position o) {
        return nodes.indexOf(o);
    }

    /**
     * Returns the last index of this Navigation Node.
     *
     * @param o Position to fetch
     * @return Index found
     * @see List#lastIndexOf(Object)
     */
    @Override
    public int lastIndexOf(@Nullable Position o) {
        return nodes.lastIndexOf(o);
    }
}
