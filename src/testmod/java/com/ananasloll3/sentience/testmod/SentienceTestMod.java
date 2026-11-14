package com.ananasloll3.sentience.testmod;

import com.ananasloll3.sentience.api.agent.AgentTemplates;
import com.ananasloll3.sentience.api.perception.hearing.HearingProfile;
import com.ananasloll3.sentience.api.perception.sight.SightConfig;
import com.ananasloll3.sentience.api.rules.SentienceRules;
import com.ananasloll3.sentience.testmod.arena.TestArenas;
import com.ananasloll3.sentience.testmod.entity.ApexEntity;
import com.ananasloll3.sentience.testmod.entity.HoundEntity;
import com.ananasloll3.sentience.testmod.entity.SentinelEntity;
import com.ananasloll3.sentience.testmod.entity.StalkerEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Mod de démo de Sentience : un mob par fonctionnalité, un qui utilise tout, des zombies vanilla
 * améliorés, et des arènes de test (/sentiencetest arene).
 */
public final class SentienceTestMod implements ModInitializer {

    public static final String MOD_ID = "sentience_testmod";
    private static final List<Item> EGGS = new ArrayList<>();

    /**
     * Règles de perception PROPRES à ce mod : une citrouille sculptée sur la tête sert de camouflage.
     * Seuls les agents qui utilisent ces règles s'y laissent prendre (pas les zombies, ni les mobs
     * d'un autre mod qui utiliserait Sentience).
     */
    public static final SentienceRules RULES = SentienceRules.create(MOD_ID)
            .sight(s -> s.addStealthModifier((observer, target) ->
                    target.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.CARVED_PUMPKIN) ? 0.3f : 1f))
            .build();

    /** Variante pour le Flaireur : la pluie n'efface pas la piste pour lui. */
    public static final SentienceRules HOUND_RULES = RULES.derive(Identifier.of(MOD_ID, "hound"))
            .scent(s -> s.ignoreRain())
            .build();

    /** Ouïe seule (+ voix). */
    public static final EntityType<StalkerEntity> STALKER = register("stalker",
            EntityType.Builder.create(StalkerEntity::new, SpawnGroup.MONSTER).dimensions(0.7f, 2.4f).maxTrackingRange(10));
    /** Vue seule. */
    public static final EntityType<SentinelEntity> SENTINEL = register("sentinel",
            EntityType.Builder.create(SentinelEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.99f).maxTrackingRange(10));
    /** Odorat. */
    public static final EntityType<HoundEntity> HOUND = register("hound",
            EntityType.Builder.create(HoundEntity::new, SpawnGroup.MONSTER).dimensions(0.7f, 0.85f).maxTrackingRange(10));
    /** Tout. */
    public static final EntityType<ApexEntity> APEX = register("apex",
            EntityType.Builder.create(ApexEntity::new, SpawnGroup.MONSTER).dimensions(0.8f, 2.7f).maxTrackingRange(10));

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        attributes(STALKER, StalkerEntity.createAttributes());
        attributes(SENTINEL, SentinelEntity.createAttributes());
        attributes(HOUND, HoundEntity.createAttributes());
        attributes(APEX, ApexEntity.createAttributes());
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> EGGS.forEach(entries::add));

        // Mob vanilla amélioré sans toucher à son code : les zombies entendent et voient (joueurs et villageois).
        // Pas de .rules(...) : ils gardent les règles par défaut, la citrouille ne les trompe pas.
        AgentTemplates.register(EntityType.ZOMBIE, agent -> agent
                .hearing(HearingProfile.DEFAULT)
                .sight(SightConfig.humanoid().withTargets(e -> SightConfig.PLAYERS.test(e) || e instanceof VillagerEntity))
                .damage()
                .defaultGoals(3, 1.0, 1.2));

        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
            TestWalker.register(dispatcher);
            TestArenas.register(dispatcher);
        });
        ServerTickEvents.END_SERVER_TICK.register(TestWalker::tick);
    }

    private static <T extends MobEntity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id(name));
        EntityType<T> type = Registry.register(Registries.ENTITY_TYPE, key, builder.build(key));
        RegistryKey<Item> eggKey = RegistryKey.of(RegistryKeys.ITEM, id(name + "_spawn_egg"));
        EGGS.add(Registry.register(Registries.ITEM, eggKey,
                new SpawnEggItem(new Item.Settings().registryKey(eggKey).spawnEgg(type))));
        return type;
    }

    private static void attributes(EntityType<? extends MobEntity> type, DefaultAttributeContainer.Builder builder) {
        FabricDefaultAttributeRegistry.register(type, builder);
    }
}
