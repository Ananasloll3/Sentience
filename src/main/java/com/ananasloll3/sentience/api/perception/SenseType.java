package com.ananasloll3.sentience.api.perception;

import com.ananasloll3.sentience.Sentience;
import net.minecraft.util.Identifier;

/**
 * Identifiant d'un sens. Les sens intégrés sont ci-dessous ; un mod peut créer les siens
 * (odorat, écholocation…) avec {@code new SenseType(Identifier.of("monmod", "odorat"))}.
 */
public record SenseType(Identifier id) {

    public static final SenseType HEARING = builtin("hearing");
    public static final SenseType SIGHT = builtin("sight");
    public static final SenseType DAMAGE = builtin("damage");
    public static final SenseType TOUCH = builtin("touch");
    /** Information transmise par un allié ou injectée par du code. */
    public static final SenseType REPORT = builtin("report");

    private static SenseType builtin(String path) {
        return new SenseType(Sentience.id(path));
    }

    @Override
    public String toString() {
        return id.getPath();
    }
}
