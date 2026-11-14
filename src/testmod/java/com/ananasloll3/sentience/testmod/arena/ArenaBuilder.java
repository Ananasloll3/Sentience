package com.ananasloll3.sentience.testmod.arena;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Locale;

/** Petit outil de construction : coordonnées relatives à une origine, exécutées en commandes vanilla. */
final class ArenaBuilder {

    private final ServerWorld world;
    private final ServerCommandSource source;
    private final int ox;
    private final int oy;
    private final int oz;

    ArenaBuilder(ServerWorld world, BlockPos origin) {
        this.world = world;
        this.source = world.getServer().getCommandSource().withWorld(world).withSilent();
        this.ox = origin.getX();
        this.oy = origin.getY();
        this.oz = origin.getZ();
    }

    ArenaBuilder at(int dx, int dy, int dz) {
        return new ArenaBuilder(world, new BlockPos(ox + dx, oy + dy, oz + dz));
    }

    void run(String command) {
        world.getServer().getCommandManager().parseAndExecute(source, command);
    }

    void fill(int x1, int y1, int z1, int x2, int y2, int z2, String block) {
        run(String.format("fill %d %d %d %d %d %d %s", ox + x1, oy + y1, oz + z1, ox + x2, oy + y2, oz + z2, block));
    }

    void set(int x, int y, int z, String block) {
        run(String.format("setblock %d %d %d %s", ox + x, oy + y, oz + z, block));
    }

    void door(int x, int y, int z, String wood, String facing) {
        set(x, y, z, "minecraft:" + wood + "_door[facing=" + facing + ",half=lower]");
        set(x, y + 1, z, "minecraft:" + wood + "_door[facing=" + facing + ",half=upper]");
    }

    void summon(String type, double x, double y, double z) {
        run(String.format(Locale.ROOT, "summon %s %.1f %.1f %.1f", type, ox + x, oy + y, oz + z));
    }

    /** Panneau de texte flottant ; chaque ligne est un affichage séparé. */
    void label(double x, double y, double z, String color, String... lines) {
        for (int i = 0; i < lines.length; i++) {
            String text = lines[i].replace("\\", "\\\\").replace("\"", "\\\"");
            String c = i == 0 ? color : "white";
            run(String.format(Locale.ROOT,
                    "summon minecraft:text_display %.2f %.2f %.2f {billboard:\"center\",see_through:0b,"
                            + "text:{text:\"%s\",color:\"%s\",bold:%s},background:1426063360}",
                    ox + x, oy + y - i * 0.3, oz + z, text, c, i == 0 ? "1b" : "0b"));
        }
    }

    /** Supprime tout sauf les joueurs dans la zone (relance propre d'une arène). */
    void clearEntities(int x1, int y1, int z1, int x2, int y2, int z2) {
        run(String.format("kill @e[type=!minecraft:player,x=%d,y=%d,z=%d,dx=%d,dy=%d,dz=%d]",
                ox + x1, oy + y1, oz + z1, x2 - x1, y2 - y1, z2 - z1));
    }
}
