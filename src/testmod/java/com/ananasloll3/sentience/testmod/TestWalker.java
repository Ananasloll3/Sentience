package com.ananasloll3.sentience.testmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.ananasloll3.sentience.api.perception.scent.ScentTrails;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.MovementType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Outil de test : un faux joueur qui fait des allers-retours pour tester les IA seul.
 * /sentiencetest marcheur &lt;position&gt; &lt;secondes&gt; [accroupi]
 */
public final class TestWalker {

    @Nullable
    private static Walk current;

    private record Walk(FakePlayer player, Vec3d origin, int totalTicks, boolean sneaking, int[] elapsed) {
    }

    private TestWalker() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sentiencetest")
                .requires(CommandManager.requirePermissionLevel(2))
                .then(CommandManager.literal("piste")
                        .then(CommandManager.argument("de", Vec3ArgumentType.vec3())
                                .then(CommandManager.argument("a", Vec3ArgumentType.vec3())
                                        .executes(ctx -> layTrail(ctx.getSource(),
                                                Vec3ArgumentType.getVec3(ctx, "de"), Vec3ArgumentType.getVec3(ctx, "a"))))))
                .then(CommandManager.literal("marcheur")
                        .then(CommandManager.argument("position", Vec3ArgumentType.vec3())
                                .then(CommandManager.argument("secondes", IntegerArgumentType.integer(1, 600))
                                        .executes(ctx -> start(ctx.getSource(), Vec3ArgumentType.getVec3(ctx, "position"),
                                                IntegerArgumentType.getInteger(ctx, "secondes"), false))
                                        .then(CommandManager.argument("accroupi", BoolArgumentType.bool())
                                                .executes(ctx -> start(ctx.getSource(), Vec3ArgumentType.getVec3(ctx, "position"),
                                                        IntegerArgumentType.getInteger(ctx, "secondes"),
                                                        BoolArgumentType.getBool(ctx, "accroupi"))))))));
    }

    private static int start(ServerCommandSource source, Vec3d pos, int seconds, boolean sneaking) {
        ServerWorld world = source.getWorld();
        FakePlayer player = FakePlayer.get(world);
        player.changeGameMode(GameMode.SURVIVAL);
        player.setSneaking(sneaking);
        player.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
        current = new Walk(player, pos, seconds * 20, sneaking, new int[]{0});
        source.sendFeedback(() -> Text.literal("Marcheur lancé pour " + seconds + " s."), false);
        return 1;
    }

    /**
     * Fabrique une piste comme si un joueur venait de marcher de « de » à « a » (4 blocs/s),
     * la trace la plus fraîche étant à l'arrivée. Pratique pour tester un flaireur.
     */
    private static int layTrail(ServerCommandSource source, Vec3d from, Vec3d to) {
        ServerWorld world = source.getWorld();
        UUID ghost = UUID.nameUUIDFromBytes("sentience:trail".getBytes());
        double length = from.distanceTo(to);
        int points = Math.max(2, (int) Math.ceil(length / 2.0)); // un point tous les ~2 blocs (marche)
        long now = world.getTime();
        for (int i = 0; i <= points; i++) {
            Vec3d pos = from.lerp(to, (double) i / points);
            long tick = now - (long) ((points - i) * 2.0 / 4.0 * 20); // 4 blocs/s
            ScentTrails.deposit(world, new ScentTrails.ScentPoint(pos, tick, 1f, ghost,
                    net.minecraft.entity.EntityType.PLAYER, ScentTrails.TRAIL, false, false));
        }
        int count = points + 1;
        source.sendFeedback(() -> Text.literal("Piste de " + count + " traces posée (" + Math.round(length) + " blocs)."), false);
        return 1;
    }

    /** Allers-retours de 4 blocs sur l'axe Z, à vitesse de marche. */
    public static void tick(MinecraftServer server) {
        Walk walk = current;
        if (walk == null) {
            return;
        }
        int t = walk.elapsed()[0]++;
        if (t >= walk.totalTicks()) {
            current = null;
            return;
        }
        FakePlayer player = walk.player();
        double speed = walk.sneaking() ? 0.065 : 0.215;
        double direction = (t / (int) (4.0 / speed)) % 2 == 0 ? 1 : -1;
        player.setOnGround(true);
        player.setSneaking(walk.sneaking());
        player.move(MovementType.PLAYER, new Vec3d(0, -0.01, speed * direction));
        player.setOnGround(true);
        // Un faux joueur n'est pas dans la liste des joueurs : on dépose son odeur à la main
        if (t % ScentTrails.DEPOSIT_INTERVAL == 0 && player.getEntityWorld() instanceof ServerWorld world) {
            ScentTrails.deposit(world, player, 1f, ScentTrails.TRAIL);
        }
    }
}
