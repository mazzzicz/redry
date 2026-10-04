package cz.redry.nightfall.entity;

import cz.redry.nightfall.audio.RedrySounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** The Watcher freezes under a direct gaze and steals distance whenever the player blinks. */
public final class HerobrineEntity extends RedryAnomalyEntity {
    private int arrivalPause;
    private int lastCloseScareTick = -1_000;
    private int lastKnockTick = -1_000;

    public HerobrineEntity(EntityType<? extends HerobrineEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnomalyAttributes()
                .add(Attributes.MAX_HEALTH, 54.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.285D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    public void beginDistantAppearance(int pauseTicks) {
        this.arrivalPause = pauseTicks;
        this.setNoAi(true);
    }

    public float horrorAnimationPhase(float partialTick) {
        return this.tickCount + partialTick;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !this.isAlive()) {
            return;
        }

        if (this.arrivalPause > 0) {
            this.setNoAi(true);
            this.arrivalPause--;
            if (this.arrivalPause == 0) {
                this.setNoAi(false);
            }
            return;
        }

        Player player = this.level().getNearestPlayer(this, 42.0D);
        if (player == null || !player.isAlive() || player.isCreative() || player.isSpectator()) {
            this.setNoAi(false);
            return;
        }

        double distanceSquared = this.distanceToSqr(player);
        boolean watched = distanceSquared < 34.0D * 34.0D && isLookingDirectlyAt(player);
        this.setNoAi(watched);

        if (watched) {
            this.getNavigation().stop();
            this.getLookControl().setLookAt(player, 12.0F, 12.0F);
            if (distanceSquared < 18.0D * 18.0D && this.tickCount - this.lastKnockTick > 100) {
                this.lastKnockTick = this.tickCount;
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, player.blockPosition().relative(player.getDirection().getOpposite(), 4),
                            RedrySounds.DISTANT_KNOCK, SoundSource.AMBIENT, 0.44F, 0.68F);
                }
            }
            return;
        }

        if (distanceSquared < 7.0D * 7.0D && this.tickCount - this.lastCloseScareTick > 100) {
            this.lastCloseScareTick = this.tickCount;
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 48, 0, true, false, true));
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, player.blockPosition(), RedrySounds.HEARTBEAT,
                        SoundSource.AMBIENT, 0.96F, 0.66F + this.random.nextFloat() * 0.12F);
                serverLevel.playSound(null, player.blockPosition().relative(player.getDirection().getOpposite(), 2),
                        RedrySounds.BREATH, SoundSource.AMBIENT, 0.68F, 0.74F);
            }
        }
    }

    private boolean isLookingDirectlyAt(Player player) {
        if (!player.hasLineOfSight(this)) {
            return false;
        }
        Vec3 towardEntity = this.position().subtract(player.getEyePosition()).normalize();
        return player.getViewVector(1.0F).dot(towardEntity) > 0.968D;
    }
}
