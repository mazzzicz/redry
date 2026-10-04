package cz.redry.nightfall.audio;

import cz.redry.nightfall.RedryMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class RedrySounds {
    public static final SoundEvent WHISPER = register("whisper");
    public static final SoundEvent SIGNAL = register("signal");
    public static final SoundEvent HEARTBEAT = register("heartbeat");
    public static final SoundEvent TAPE_PLAY = register("tape_play");
    public static final SoundEvent STATIC = register("static");
    public static final SoundEvent DISTANT_KNOCK = register("distant_knock");
    public static final SoundEvent BREATH = register("breath");

    private RedrySounds() {
    }

    private static SoundEvent register(String path) {
        Identifier id = RedryMod.id(path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {
        // Referencing these fields forces registration before resources are loaded.
    }
}
