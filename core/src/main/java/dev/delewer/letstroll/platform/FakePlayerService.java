package dev.delewer.letstroll.platform;

public interface FakePlayerService {

    void spawn(PlayerRef target, FakePlayerSpec spec);

    void despawnAll(PlayerRef target);
}
