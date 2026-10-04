package cz.redry.nightfall.entity;

import cz.redry.nightfall.audio.RedrySounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** RED-404: a walking chat message. It never attacks; it disappears when its lie is noticed. */
public final class SignalEchoEntity extends RedryAnomalyEntity {
    private static final String[] GLITCH_LINES = {
            "§5[CHAT] §dredry se připojil k světu",
            "§5[CHAT] §dneotáčej se, už to nestihneš",
            "§5[CHAT] §d§k██████████ §r§5[CHAT] §dvidím tvůj monitor",
            "§5[CHAT] §dposlední divák odešel před třemi nocemi"
    };

    public SignalEchoEntity(EntityType<? extends SignalEchoEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnomalyAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.19D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 42.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        // No combat goals: RED-404 exists to confuse and unsettle, not to farm kills.
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() || !this.isAlive() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        List<ServerPlayer> witnesses = serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                this.getBoundingBox().inflate(42.0D),
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator()
        );
        ServerPlayer player = witnesses.stream()
                .min((first, second) -> Double.compare(this.distanceToSqr(first), this.distanceToSqr(second)))
                .orElse(null);
        if (player == null) {
            if (this.tickCount > 120) {
                this.discard();
            }
            return;
        }

        double distanceSquared = this.distanceToSqr(player);
        boolean noticed = distanceSquared < 38.0D * 38.0D
                && player.hasLineOfSight(this)
                && isLookingAt(player);
        if (distanceSquared < 7.0D * 7.0D || (noticed && distanceSquared < 34.0D * 34.0D)) {
            player.sendSystemMessage(Component.literal(
                    GLITCH_LINES[serverLevel.getRandom().nextInt(GLITCH_LINES.length)]
            ), true);
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 28, 0, true, false, true));
            serverLevel.playSound(null, this.blockPosition(), RedrySounds.STATIC,
                    SoundSource.AMBIENT, 0.92F, 0.58F + this.random.nextFloat() * 0.18F);
            this.discard();
            return;
        }

        if (this.tickCount % 12 == 0) {
            this.getNavigation().moveTo(player, distanceSquared < 18.0D * 18.0D ? 0.92D : 0.62D);
        }

        if (this.tickCount % 48 == 0) {
            serverLevel.playSound(null, this.blockPosition(), RedrySounds.BREATH,
                    SoundSource.AMBIENT, 0.24F, 0.48F);
        }
    }

    private boolean isLookingAt(Player player) {
        Vec3 towardEntity = this.position().subtract(player.getEyePosition()).normalize();
        return player.getViewVector(1.0F).dot(towardEntity) > 0.91D;
    }
}
