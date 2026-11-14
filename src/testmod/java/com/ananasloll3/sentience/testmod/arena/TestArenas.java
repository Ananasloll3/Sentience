package com.ananasloll3.sentience.testmod.arena;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Arènes de test, une par mob de démo.
 * /sentiencetest arene &lt;rodeur|sentinelle|flaireur|abomination|toutes&gt;
 * L'arène est construite devant toi (ou relancée si tu la reconstruis au même endroit).
 */
public final class TestArenas {

    private static final List<String> NAMES = List.of("rodeur", "sentinelle", "flaireur", "abomination", "toutes");

    private TestArenas() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("sentiencetest")
                .requires(CommandManager.requirePermissionLevel(2))
                .then(CommandManager.literal("arene")
                        .then(CommandManager.argument("nom", StringArgumentType.word())
                                .suggests((ctx, b) -> CommandSource.suggestMatching(NAMES, b))
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "nom");
                                    ServerCommandSource src = ctx.getSource();
                                    BlockPos pos = BlockPos.ofFloored(src.getPosition());
                                    if (!build(src.getWorld(), name, pos.add(3, 0, 3))) {
                                        src.sendError(Text.literal("Arène inconnue : " + name));
                                        return 0;
                                    }
                                    src.sendFeedback(() -> Text.literal("Arène « " + name + " » construite."), false);
                                    return 1;
                                }))));
    }

    /** @param origin coin de l'arène, au niveau du sol (le sol est posé à origin.y - 1) */
    public static boolean build(ServerWorld world, String name, BlockPos origin) {
        ArenaBuilder b = new ArenaBuilder(world, origin);
        switch (name) {
            case "rodeur" -> stalker(b);
            case "sentinelle" -> sentinel(b);
            case "flaireur" -> hound(b);
            case "abomination" -> apex(b);
            case "toutes" -> all(b);
            default -> {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ toutes + hub

    private static void all(ArenaBuilder b) {
        // Hub d'accueil
        b.clearEntities(-15, -1, -8, -1, 6, 6);
        b.fill(-15, -1, -8, -1, 5, 6, "minecraft:air");
        b.fill(-15, -1, -8, -1, -1, 6, "minecraft:polished_blackstone_bricks");
        for (int x = -14; x <= -2; x += 6) {
            for (int z = -7; z <= 5; z += 6) {
                b.set(x, -1, z, "minecraft:sea_lantern");
            }
        }
        b.label(-8, 3.6, -1, "gold",
                "SENTIENCE — ARÈNES DE TEST",
                "Tape /sentience debug pour voir ce que pensent les mobs",
                "(état, sens, score, incertitude, jauge de vue)",
                "Passe en survie pour être attaqué : /gamemode survival",
                "Rejoue une arène : /sentiencetest arene <nom>",
                "Les arènes sont le long du chemin, vers l'est →");

        // Chemin éclairé vers les arènes
        b.fill(-1, -1, 4, 172, -1, 6, "minecraft:dirt_path");
        for (int x = 0; x <= 170; x += 10) {
            b.set(x, 0, 3, "minecraft:spruce_fence");
            b.set(x, 1, 3, "minecraft:lantern");
        }

        stalker(b.at(6, 0, 10));
        sentinel(b.at(40, 0, 10));
        hound(b.at(90, 0, 10));
        apex(b.at(140, 0, 10));
    }

    // ------------------------------------------------------------------ Rôdeur : ouïe

    private static void stalker(ArenaBuilder b) {
        b.clearEntities(-1, -1, -3, 25, 6, 25);
        b.fill(-1, -1, -1, 25, 6, 25, "minecraft:air");
        b.fill(0, -1, 0, 24, 5, 24, "minecraft:deepslate_bricks hollow");
        b.fill(1, -1, 1, 23, -1, 23, "minecraft:deepslate_tiles");
        b.fill(6, 0, 0, 6, 1, 0, "minecraft:air");

        // Quatre salles
        b.fill(12, 0, 1, 12, 4, 23, "minecraft:deepslate_bricks");
        b.fill(1, 0, 12, 23, 4, 12, "minecraft:deepslate_bricks");
        b.fill(12, 0, 6, 12, 1, 6, "minecraft:air");
        b.fill(6, 0, 12, 6, 1, 12, "minecraft:air");
        b.fill(18, 0, 12, 18, 1, 12, "minecraft:air");
        b.fill(12, 0, 18, 12, 1, 18, "minecraft:air");

        // 1. Gravier : pas normaux
        b.fill(1, -1, 1, 11, -1, 11, "minecraft:gravel");
        // 2. Laine : pas étouffés
        b.fill(13, -1, 1, 23, -1, 11, "minecraft:black_wool");
        // 3. Cascade : l'eau qui coule masque les bruits
        b.set(1, 4, 21, "minecraft:water");
        b.set(1, 4, 16, "minecraft:water");
        // 4. Mécanismes + cachette en laine
        b.set(15, 0, 14, "minecraft:note_block");
        b.set(15, 1, 14, "minecraft:stone_button[face=floor]");
        b.set(20, 0, 14, "minecraft:bell[attachment=floor,facing=north]");
        b.set(14, 0, 22, "minecraft:barrel[facing=up]");
        b.fill(19, -1, 19, 23, 3, 23, "minecraft:white_wool hollow");
        b.door(19, 0, 21, "oak", "west");

        b.set(6, 4, 6, "minecraft:soul_lantern[hanging=true]");
        b.set(18, 4, 6, "minecraft:soul_lantern[hanging=true]");
        b.set(6, 4, 18, "minecraft:soul_lantern[hanging=true]");
        b.set(15, 4, 17, "minecraft:soul_lantern[hanging=true]");

        b.label(6, 4.2, -2, "dark_aqua",
                "RÔDEUR — L'OUÏE",
                "Aveugle : il ne te repère qu'au bruit (et au toucher)",
                "Parle au micro, cours, casse des blocs : il vient",
                "Accroupi = aucun bruit de pas");
        b.label(6, 3.4, 6, "yellow", "Gravier", "pas normaux (50 dB)");
        b.label(18, 3.4, 6, "yellow", "Laine", "pas étouffés (42 dB)");
        b.label(6, 3.4, 18, "yellow", "Cascade", "l'eau qui coule couvre tes bruits");
        b.label(16, 3.4, 18, "yellow", "Mécanismes & cachette", "note, cloche, tonneau", "la laine étouffe ce qui est derrière");

        b.summon("sentience_testmod:stalker", 15.5, 0, 20.5);
    }

    // ------------------------------------------------------------------ Sentinelle : vue

    private static void sentinel(ArenaBuilder b) {
        b.clearEntities(-1, -1, -3, 41, 8, 33);
        b.fill(-1, -1, -1, 41, 8, 33, "minecraft:air");
        b.fill(0, -1, 0, 32, -1, 32, "minecraft:stone_bricks");
        b.fill(0, 0, 0, 32, 2, 0, "minecraft:stone_bricks");
        b.fill(0, 0, 32, 32, 2, 32, "minecraft:stone_bricks");
        b.fill(0, 0, 0, 0, 2, 32, "minecraft:stone_bricks");
        b.fill(32, 0, 0, 32, 2, 32, "minecraft:stone_bricks");
        b.fill(15, 0, 0, 17, 1, 0, "minecraft:air");

        // Moitié gauche éclairée, moitié droite dans le noir
        for (int x = 3; x <= 13; x += 5) {
            for (int z = 4; z <= 28; z += 6) {
                b.set(x, -1, z, "minecraft:sea_lantern");
            }
        }
        b.fill(16, -1, 1, 16, -1, 31, "minecraft:polished_andesite");

        // Couvertures, vitre, barreaux
        b.fill(6, 0, 9, 9, 1, 9, "minecraft:mossy_stone_bricks");
        b.fill(3, 0, 19, 3, 1, 22, "minecraft:mossy_stone_bricks");
        b.fill(10, 0, 26, 12, 1, 26, "minecraft:mossy_stone_bricks");
        b.fill(21, 0, 9, 24, 1, 9, "minecraft:mossy_stone_bricks");
        b.fill(27, 0, 15, 27, 1, 18, "minecraft:mossy_stone_bricks");
        b.fill(20, 0, 27, 23, 1, 27, "minecraft:mossy_stone_bricks");
        b.fill(21, 0, 22, 26, 2, 22, "minecraft:glass");
        b.fill(5, 0, 14, 9, 2, 14, "minecraft:iron_bars");

        // Deux sentinelles qui se préviennent par radio
        b.fill(15, 0, 15, 17, 2, 17, "minecraft:chiseled_stone_bricks");
        b.set(26, 0, 4, "minecraft:chiseled_stone_bricks");
        b.set(26, 1, 4, "minecraft:chiseled_stone_bricks");
        b.summon("sentience_testmod:sentinel", 16.5, 3, 16.5);
        b.summon("sentience_testmod:sentinel", 26.5, 2, 4.5);

        // Chenil : un Rôdeur qui entend l'alarme
        b.fill(33, -1, 12, 40, 3, 20, "minecraft:deepslate_bricks hollow");
        b.fill(32, 0, 15, 33, 1, 16, "minecraft:air");
        b.summon("sentience_testmod:stalker", 37.5, 0, 16.5);

        // Équipement de test
        b.set(13, 0, -2, "minecraft:chest[facing=north]{Items:["
                + "{Slot:0b,id:\"minecraft:potion\",count:1,components:{\"minecraft:potion_contents\":{potion:\"minecraft:long_invisibility\"}}},"
                + "{Slot:1b,id:\"minecraft:potion\",count:1,components:{\"minecraft:potion_contents\":{potion:\"minecraft:long_invisibility\"}}},"
                + "{Slot:2b,id:\"minecraft:torch\",count:16},"
                + "{Slot:3b,id:\"minecraft:shield\",count:1},"
                + "{Slot:4b,id:\"minecraft:snowball\",count:16},"
                + "{Slot:5b,id:\"minecraft:carved_pumpkin\",count:1}]}");

        b.label(16, 4.6, -2, "red",
                "SENTINELLE — LA VUE",
                "Sourde et immobile : elle ne fait que regarder",
                "Gauche éclairée · droite dans le noir",
                "Accroupi, invisibilité (coffre), torche en main",
                "Citrouille sur la tête : camouflage (règles du mod de démo)",
                "Vitre et barreaux : elle voit à travers",
                "Repéré : flèches + alarme (le Rôdeur du chenil l'entend)");
        b.label(23.5, 3.2, 21.5, "aqua", "Vitre");
        b.label(7, 3.2, 13.5, "aqua", "Barreaux");
        b.label(36.5, 3.6, 16.5, "dark_aqua", "Chenil", "il accourt à l'alarme");
    }

    // ------------------------------------------------------------------ Flaireur : odorat

    private static void hound(ArenaBuilder b) {
        String hedge = "minecraft:oak_leaves[persistent=true]";
        b.clearEntities(-1, -1, -3, 41, 5, 25);
        b.fill(-1, -1, -1, 41, 5, 25, "minecraft:air");
        b.fill(0, -1, 0, 40, -1, 24, "minecraft:grass_block");
        b.fill(0, 0, 0, 40, 1, 0, hedge);
        b.fill(0, 0, 24, 40, 1, 24, hedge);
        b.fill(0, 0, 0, 0, 1, 24, hedge);
        b.fill(40, 0, 0, 40, 1, 24, hedge);
        b.fill(6, 0, 0, 7, 1, 0, "minecraft:air");

        // Chemin en serpentin
        b.fill(1, 0, 6, 34, 1, 6, hedge);
        b.fill(6, 0, 12, 39, 1, 12, hedge);
        b.fill(1, 0, 18, 34, 1, 18, hedge);

        // Rivière : l'eau ne garde pas l'odeur
        b.fill(16, -1, 1, 24, -1, 23, "minecraft:water");

        // Enclos du Flaireur
        b.fill(1, 0, 1, 4, 0, 1, "minecraft:oak_fence");
        b.fill(1, 0, 5, 4, 0, 5, "minecraft:oak_fence");
        b.fill(4, 0, 1, 4, 0, 5, "minecraft:oak_fence");
        b.set(4, 0, 3, "minecraft:oak_fence_gate[facing=east,open=false]");
        b.summon("sentience_testmod:hound", 2.5, 0, 3.5);

        // Cabane d'arrivée
        b.fill(1, -1, 19, 5, 3, 23, "minecraft:spruce_planks hollow");
        b.door(5, 0, 21, "spruce", "east");

        b.label(7, 3.8, -2, "green",
                "FLAIREUR — L'ODORAT",
                "Ouvre l'enclos, puis suis le chemin",
                "Ses règles : la pluie n'efface pas ta piste pour lui",
                "Il remonte ta piste à l'odeur, pas à pas",
                "La rivière coupe la piste, la pluie l'efface",
                "Cache-toi dans la cabane au bout du chemin");
        b.label(20, 3, 3, "aqua", "Rivière", "ta piste s'arrête ici");
        b.label(3, 4.2, 21, "aqua", "Cabane");
    }

    // ------------------------------------------------------------------ Abomination : tout

    private static void apex(ArenaBuilder b) {
        b.clearEntities(-1, -1, -3, 27, 7, 21);
        b.fill(-1, -1, -1, 27, 7, 21, "minecraft:air");
        b.fill(0, -1, 0, 26, 5, 20, "minecraft:stripped_dark_oak_log hollow");
        b.fill(1, -1, 1, 25, -1, 19, "minecraft:dark_oak_planks");

        // Six pièces
        b.fill(9, 0, 1, 9, 4, 19, "minecraft:dark_oak_planks");
        b.fill(17, 0, 1, 17, 4, 19, "minecraft:dark_oak_planks");
        b.fill(1, 0, 10, 25, 4, 10, "minecraft:dark_oak_planks");
        int[][] openings = {{9, 5}, {9, 15}, {17, 5}, {17, 15}};
        for (int[] o : openings) {
            b.fill(o[0], 0, o[1], o[0], 1, o[1], "minecraft:air");
        }
        b.fill(4, 0, 10, 4, 1, 10, "minecraft:air");
        b.fill(13, 0, 10, 13, 1, 10, "minecraft:air");
        b.fill(21, 0, 10, 21, 1, 10, "minecraft:air");
        b.door(4, 0, 0, "dark_oak", "north");

        // Salon (tapis), cuisine (pierre, tonneaux), chambre (placard), bibliothèque, antre
        b.fill(10, 0, 1, 16, 0, 9, "minecraft:red_carpet");
        b.fill(18, -1, 1, 25, -1, 9, "minecraft:polished_andesite");
        b.fill(25, 0, 2, 25, 0, 8, "minecraft:barrel[facing=up]");
        b.fill(1, -1, 17, 3, 2, 19, "minecraft:spruce_planks hollow");
        b.door(2, 0, 17, "spruce", "south");
        b.fill(10, 0, 19, 16, 2, 19, "minecraft:bookshelf");
        b.fill(18, -1, 11, 25, -1, 19, "minecraft:soul_soil");

        // Fenêtres
        b.fill(12, 1, 0, 14, 2, 0, "minecraft:glass_pane");
        b.fill(26, 1, 3, 26, 2, 6, "minecraft:glass_pane");
        b.fill(0, 1, 13, 0, 2, 15, "minecraft:glass_pane");

        // Une lampe + un levier par pièce
        int[][] lamps = {{4, 5}, {13, 5}, {21, 5}, {5, 14}, {13, 14}, {21, 14}};
        for (int[] l : lamps) {
            b.set(l[0], 4, l[1], "minecraft:redstone_lamp");
            b.set(l[0], 3, l[1], "minecraft:lever[face=ceiling,facing=north]");
        }

        b.label(13, 4.6, -2, "dark_red",
                "ABOMINATION — TOUT SENTIENCE",
                "Ouïe fine + micro, vue nocturne, odorat, toucher",
                "Craint la lumière : actionne les leviers des lampes",
                "Placard dans la chambre, tapis du salon = pas étouffés",
                "Quand elle chasse, elle prévient les autres créatures");
        b.summon("sentience_testmod:apex", 22.5, 0, 15.5);
    }
}
