package dev.delewer.letstroll.modules.modes;

import java.util.ArrayList;
import java.util.List;

import dev.ua.theroer.magicutils.config.annotations.Comment;
import dev.ua.theroer.magicutils.config.annotations.ConfigValue;
import dev.ua.theroer.magicutils.config.annotations.MaxValue;
import dev.ua.theroer.magicutils.config.annotations.MinValue;

public final class ChainConfig {

    @Comment("Maximum chain length in blocks before players are pulled back")
    @ConfigValue("chain-length")
    @MinValue(5)
    @MaxValue(64)
    private double chainLength = 20.0;

    @Comment("How the chain behaves at full length: RIGID stops you dead, ELASTIC pulls smoothly, RUBBER snaps you back")
    @ConfigValue("link-mode")
    private String linkMode = "ELASTIC";

    @Comment("Carry momentum along the chain, so a running player really drags the other one")
    @ConfigValue("inertia")
    private boolean inertia = true;

    @Comment("How much of the puller's speed is transferred, 0 turns the drag off")
    @ConfigValue("inertia-strength")
    @MinValue(0)
    @MaxValue(2)
    private double inertiaStrength = 0.55;

    @Comment("Draw the chain between the two players")
    @ConfigValue("visual-chain")
    private boolean visualChain = true;

    @Comment("How the chain is drawn: LINKS spawns real chain blocks, PARTICLES uses a particle trail")
    @ConfigValue("chain-style")
    private String chainStyle = "LINKS";

    @Comment("Block used for one chain link, for example minecraft:copper_chain")
    @ConfigValue("link-block")
    private String linkBlock = "minecraft:iron_chain";

    @Comment("How thick one chain link is drawn, in blocks")
    @ConfigValue("link-thickness")
    @MinValue(0.05)
    @MaxValue(1)
    private double linkThickness = 0.32;

    @Comment("How long one chain link is, in blocks, smaller means more links and more entities")
    @ConfigValue("link-length")
    @MinValue(0.25)
    @MaxValue(4)
    private double linkLength = 0.75;

    @Comment("How far the chain sags when the players stand close, in blocks")
    @ConfigValue("sag")
    @MinValue(0)
    @MaxValue(8)
    private double sag = 1.6;

    @Comment("Play a creak when the chain is close to full length")
    @ConfigValue("tension-sound")
    private boolean tensionSound = true;

    @Comment("Saved chain links as pairs of player UUIDs joined with a colon")
    @ConfigValue("links")
    private List<String> links = new ArrayList<>();

    public double chainLength() {
        return chainLength;
    }

    public void setChainLength(double value) {
        this.chainLength = Math.max(5.0, Math.min(64.0, value));
    }

    public LinkMode linkMode() {
        return LinkMode.of(linkMode);
    }

    public void setLinkMode(LinkMode value) {
        this.linkMode = value.name();
    }

    public boolean inertia() {
        return inertia;
    }

    public void setInertia(boolean value) {
        this.inertia = value;
    }

    public double inertiaStrength() {
        return inertiaStrength;
    }

    public boolean visualChain() {
        return visualChain;
    }

    public void setVisualChain(boolean value) {
        this.visualChain = value;
    }

    public boolean useLinks() {
        return !"PARTICLES".equalsIgnoreCase(chainStyle);
    }

    public void setUseLinks(boolean value) {
        this.chainStyle = value ? "LINKS" : "PARTICLES";
    }

    public String linkBlock() {
        return linkBlock == null || linkBlock.isBlank() ? "minecraft:iron_chain" : linkBlock;
    }

    public double linkThickness() {
        return linkThickness;
    }

    public double linkLength() {
        return linkLength;
    }

    public double sag() {
        return sag;
    }

    public boolean tensionSound() {
        return tensionSound;
    }

    public List<String> links() {
        if (links == null) {
            links = new ArrayList<>();
        }
        return links;
    }

    public void setLinks(List<String> value) {
        this.links = value;
    }
}
