package dev.delewer.letstroll.menu;

public enum ClickKind {
    LEFT,
    RIGHT,
    SHIFT_LEFT,
    SHIFT_RIGHT,
    MIDDLE,
    OTHER;

    public boolean isLeft() {
        return this == LEFT || this == SHIFT_LEFT;
    }

    public boolean isRight() {
        return this == RIGHT || this == SHIFT_RIGHT;
    }
}
