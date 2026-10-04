package cz.redry.nightfall.item;

import cz.redry.nightfall.audio.RedrySounds;
import cz.redry.nightfall.entity.RedryAnomalyEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.Comparator;

/** A hand-held receiver that gives a rough bearing, never exact coordinates. */
public final class SignalReceiverItem extends Item {
    private static final String[] BEARINGS = {
            "jih", "jihovýchod", "východ", "severovýchod",
            "sever", "severozápad", "západ", "jihozápad"
    };

    public SignalReceiverItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel
                && player instanceof ServerPlayer serverPlayer) {
            RedryAnomalyEntity anomaly = serverLevel.getEntitiesOfClass(
                            RedryAnomalyEntity.class,
                            player.getBoundingBox().inflate(112.0D),
                            entity -> entity.isAlive()
                    ).stream()
                    .min(Comparator.comparingDouble(player::distanceToSqr))
                    .orElse(null);

            if (anomaly == null) {
                serverPlayer.sendSystemMessage(Component.literal("§8[RX-04] §7Žádný čitelný signál. Zkus znovu po setmění."), true);
            } else {
                double dx = anomaly.getX() - player.getX();
                double dz = anomaly.getZ() - player.getZ();
                double angle = Math.atan2(dx, dz);
                int sector = Math.floorMod((int) Math.round(angle / (Math.PI / 4.0D)), 8);
                int approximateRange = (int) Math.round(Math.sqrt(player.distanceToSqr(anomaly)) / 8.0D) * 8;
                serverPlayer.sendSystemMessage(Component.literal(
                        "§4[RX-04] §cPULZ §8// §7" + BEARINGS[sector]
                                + " · přibližně " + approximateRange + " bloků · přesné souřadnice odmítnuty"
                ), true);
            }

            serverLevel.playSound(null, player.blockPosition(), RedrySounds.SIGNAL,
                    SoundSource.PLAYERS, 0.72F, 0.64F + serverLevel.getRandom().nextFloat() * 0.16F);
            player.getCooldowns().addCooldown(this, 35);
        }
        return InteractionResult.SUCCESS;
    }
}
