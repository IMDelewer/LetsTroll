package dev.delewer.letstroll.menu;

import java.util.ArrayList;
import java.util.List;

public final class MenuLayout {

    public static final int ROW_WIDTH = 9;
    public static final int PADDING = 1;

    private MenuLayout() {
    }

    public static List<Integer> content(int rows) {
        List<Integer> slots = new ArrayList<>();
        for (int row = PADDING; row <= lastContentRow(rows); row++) {
            for (int column = PADDING; column < ROW_WIDTH - PADDING; column++) {
                slots.add(row * ROW_WIDTH + column);
            }
        }
        return slots;
    }

    public static List<Integer> spread(int count, int row) {
        List<Integer> slots = new ArrayList<>();
        if (count <= 0) {
            return slots;
        }
        int usable = ROW_WIDTH - PADDING * 2;
        int step = count * 2 - 1 <= usable ? 2 : 1;
        int width = Math.min(usable, (count - 1) * step + 1);
        int start = row * ROW_WIDTH + PADDING + Math.max(0, (usable - width) / 2);
        for (int index = 0; index < count; index++) {
            int slot = start + index * step;
            if (slot % ROW_WIDTH >= ROW_WIDTH - PADDING) {
                break;
            }
            slots.add(slot);
        }
        return slots;
    }

    public static int center(int row) {
        return row * ROW_WIDTH + ROW_WIDTH / 2;
    }

    public static int lastContentRow(int rows) {
        return rows - 2;
    }

    public static int contentCorner(int rows) {
        return lastContentRow(rows) * ROW_WIDTH + ROW_WIDTH - PADDING - 1;
    }

    public static int serviceSlot(int rows, int column) {
        int clamped = Math.max(0, Math.min(ROW_WIDTH - 1, column));
        return (rows - 1) * ROW_WIDTH + clamped;
    }

    public static int serviceStart(int rows) {
        return (rows - 1) * ROW_WIDTH;
    }
}
