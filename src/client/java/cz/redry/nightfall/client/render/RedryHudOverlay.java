package cz.redry.nightfall.client.render;

import cz.redry.nightfall.RedryMod;
import cz.redry.nightfall.entity.RedryAnomalyEntity;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

/** A subtle, client-only CRT/signal wash that strengthens near an anomaly or during darkness. */
public final class RedryHudOverlay {
    private static float dread;
    private static long lastFrameMillis;

    private RedryHudOverlay() {
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                RedryMod.id("signal_interference"),
                RedryHudOverlay::render
        );
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.screen != null) {
            dread = Math.max(0.0F, dread - 0.04F);
            return;
        }

        long now = System.currentTimeMillis();
        long elapsed = lastFrameMillis == 0L ? 16L : Math.max(0L, Math.min(100L, now - lastFrameMillis));
        lastFrameMillis = now;

        long dayTime = Math.floorMod(client.level.getOverworldClockTime(), 24_000L);
        boolean night = client.level.dimension() == Level.OVERWORLD && dayTime >= 13_000L && dayTime <= 23_000L;
        float target = night ? 0.11F : 0.0F;
        if (client.player.hasEffect(MobEffects.DARKNESS)) {
            target = Math.max(target, 0.88F);
        }
        if (client.player.hasEffect(MobEffects.BLINDNESS)) {
            target = Math.max(target, 0.96F);
        }
        if (night && client.player.getHealth() < client.player.getMaxHealth() * 0.35F) {
            target = Math.max(target, 0.30F);
        }

        for (RedryAnomalyEntity anomaly : client.level.getEntitiesOfClass(
                RedryAnomalyEntity.class,
                client.player.getBoundingBox().inflate(48.0D)
        )) {
            double distance = Math.sqrt(client.player.distanceToSqr(anomaly));
            float pressure = (float) Math.max(0.12D, 0.84D - Math.max(0.0D, distance - 4.0D) / 58.0D);
            target = Math.max(target, pressure);
        }

        float blend = (float) (1.0D - Math.exp(-elapsed / 310.0D));
        dread += (target - dread) * blend;
        if (dread < 0.025F) {
            return;
        }

        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        int hazeAlpha = Math.min(72, (int) (dread * 66.0F));
        int edgeAlpha = Math.min(150, (int) (dread * 146.0F));
        graphics.fill(0, 0, width, height, argb(hazeAlpha, 5, 2, 8));

        int edgeHeight = Math.max(8, height / 8);
        graphics.fillGradient(0, 0, width, edgeHeight, argb(edgeAlpha, 21, 2, 12), 0x00000000);
        graphics.fillGradient(0, height - edgeHeight, width, height, 0x00000000, argb(edgeAlpha, 18, 1, 9));
        graphics.fill(0, 0, Math.max(2, width / 115), height, argb(edgeAlpha / 3, 72, 3, 20));
        graphics.fill(width - Math.max(2, width / 115), 0, width, height, argb(edgeAlpha / 3, 72, 3, 20));

        // Thin displaced scanlines make the world feel like damaged archived footage.
        int scanlines = Math.min(14, 2 + (int) (dread * 13.0F));
        long seed = client.level.getGameTime() * 31L + now / 137L;
        for (int index = 0; index < scanlines; index++) {
            int y = (int) Math.floorMod(seed + index * 79L, Math.max(1, height));
            int x = (int) Math.floorMod(seed / 3L + index * 53L, Math.max(1, width));
            int lineWidth = Math.max(6, (int) (width * (0.05F + 0.13F * Math.abs((float) Math.sin(seed + index)))));
            int endX = Math.min(width, x + lineWidth);
            graphics.fill(x, y, endX, Math.min(height, y + 1), argb(Math.min(145, edgeAlpha), 156, 18, 43));
        }

        // An eleven-step archive meter appears only when the signal has begun to affect the player.
        if (dread > 0.22F) {
            int meterWidth = Math.min(132, width / 5);
            int activeBars = Math.min(11, Math.max(1, (int) Math.ceil(dread * 11.0F)));
            graphics.fill(12, 12, 12 + meterWidth, 22, 0x9A07090D);
            for (int index = 0; index < 11; index++) {
                int left = 16 + index * 10;
                int color = index < activeBars ? argb(Math.min(240, 90 + edgeAlpha), 156, 24 + index * 5, 45)
                        : 0x604A4245;
                graphics.fill(left, 15, left + 7, 19, color);
            }
            graphics.fill(16, 21, 16 + Math.min(11 * 10, (int) (dread * 110.0F)), 22,
                    argb(Math.min(180, edgeAlpha), 212, 33, 58));
        }
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return (Math.clamp(alpha, 0, 255) << 24)
                | (Math.clamp(red, 0, 255) << 16)
                | (Math.clamp(green, 0, 255) << 8)
                | Math.clamp(blue, 0, 255);
    }
}
