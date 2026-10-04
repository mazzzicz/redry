package cz.redry.nightfall;

import cz.redry.nightfall.audio.RedrySounds;
import cz.redry.nightfall.command.RedryCommands;
import cz.redry.nightfall.entity.RedryEntities;
import cz.redry.nightfall.item.RedryItems;
import cz.redry.nightfall.world.NightDirector;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RedryMod implements ModInitializer {
    public static final String MOD_ID = "redry";
    public static final Logger LOGGER = LoggerFactory.getLogger("REDRY: Noční archiv");

    private RedryMod() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        RedrySounds.register();
        RedryEntities.register();
        RedryItems.register();
        RedryCommands.register();

        boolean packRegistered = ResourceLoader.registerBuiltinPack(
                id("nightfall"),
                FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(),
                Component.literal("REDRY / NIGHTFALL — Noční archiv 26.3"),
                PackActivationType.ALWAYS_ENABLED
        );
        if (!packRegistered) {
            LOGGER.error("Nepodařilo se aktivovat povinný resource pack Nightfall.");
        }

        ServerTickEvents.END_LEVEL_TICK.register(NightDirector::tick);
        LOGGER.info("REDRY: Noční archiv 26.3 se probudil. Signál si pamatuje každé otočení kamery.");
    }
}
