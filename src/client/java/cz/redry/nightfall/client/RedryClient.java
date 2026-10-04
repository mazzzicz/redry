package cz.redry.nightfall.client;

import cz.redry.nightfall.RedryMod;
import cz.redry.nightfall.client.render.AnomalyModel;
import cz.redry.nightfall.client.render.AnomalyRenderer;
import cz.redry.nightfall.client.render.RedryHudOverlay;
import cz.redry.nightfall.entity.RedryEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;

public final class RedryClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(AnomalyModel.LAYER, AnomalyModel::createLayer);

        EntityRenderers.register(RedryEntities.HEROBRINE, context -> new AnomalyRenderer<>(
                context, RedryMod.id("textures/entity/herobrine.png"), false, false, 0.34F));
        EntityRenderers.register(RedryEntities.STILL_ONE, context -> new AnomalyRenderer<>(
                context, RedryMod.id("textures/entity/still_one.png"), true, false, 0.28F));
        EntityRenderers.register(RedryEntities.CHAT_ECHO, context -> new AnomalyRenderer<>(
                context, RedryMod.id("textures/entity/chat_echo.png"), false, true, 0.18F));

        RedryHudOverlay.register();
    }
}
