package dev.delewer.letstroll.menu;

public interface Screen {

    String id();

    Menu build(ScreenContext context);
}
