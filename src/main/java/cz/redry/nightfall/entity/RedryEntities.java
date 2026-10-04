package cz.redry.nightfall.entity;

import cz.redry.nightfall.RedryMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class RedryEntities {
    public static final EntityType<HerobrineEntity> HEROBRINE = register(
            "herobrine", HerobrineEntity::new, 0.72F, 2.05F);
    public static final EntityType<StillOneEntity> STILL_ONE = register(
            "still_one", StillOneEntity::new, 0.78F, 2.1F);
    public static final EntityType<SignalEchoEntity> CHAT_ECHO = register(
            "chat_echo", SignalEchoEntity::new, 0.62F, 1.95F);

    private RedryEntities() {
    }

    private static <T extends Entity> EntityType<T> register(
            String path, EntityType.EntityFactory<T> factory, float width, float height) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, RedryMod.id(path));
        EntityType<T> type = EntityType.Builder.of(factory, MobCategory.MONSTER)
                .sized(width, height)
                .clientTrackingRange(12)
                .updateInterval(2)
                .build(key);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(HEROBRINE, HerobrineEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(STILL_ONE, StillOneEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CHAT_ECHO, SignalEchoEntity.createAttributes());
    }
}
