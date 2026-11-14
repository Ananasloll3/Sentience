package com.ananasloll3.sentience.impl.debug;

import com.ananasloll3.sentience.api.perception.hearing.HearingSystem;
import com.ananasloll3.sentience.api.perception.hearing.NoiseType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * /sentience debug        → bascule l'affichage debug
 * /sentience bruit &lt;dB&gt;   → émet un bruit test à ta position
 */
public final class SentienceCommand {

    private SentienceCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sentience")
                .requires(CommandManager.requirePermissionLevel(2))
                .then(CommandManager.literal("debug").executes(ctx -> {
                    SentienceDebug.setEnabled(!SentienceDebug.isEnabled());
                    ctx.getSource().sendFeedback(() -> Text.literal("Debug Sentience : "
                            + (SentienceDebug.isEnabled() ? "§aactivé" : "§cdésactivé")), true);
                    return 1;
                }))
                .then(CommandManager.literal("bruit")
                        .then(CommandManager.argument("db", FloatArgumentType.floatArg(0f, 150f)).executes(ctx -> {
                            float db = FloatArgumentType.getFloat(ctx, "db");
                            ServerCommandSource src = ctx.getSource();
                            HearingSystem.report(src.getWorld(), src.getPosition(), db, NoiseType.GENERIC, src.getEntity());
                            src.sendFeedback(() -> Text.literal("Bruit de " + db + " dB émis."), false);
                            return 1;
                        }))));
    }
}
