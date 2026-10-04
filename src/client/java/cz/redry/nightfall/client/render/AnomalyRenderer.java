package cz.redry.nightfall.client.render;

import cz.redry.nightfall.entity.SignalEchoEntity;
import cz.redry.nightfall.entity.StillOneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

public final class AnomalyRenderer<T extends Monster> extends MobRenderer<T, AnomalyRenderState, AnomalyModel> {
    private final Identifier texture;
    private final boolean stillOne;
    private final boolean chatEcho;

    public AnomalyRenderer(EntityRendererProvider.Context context, Identifier texture,
                           boolean stillOne, boolean chatEcho, float shadowRadius) {
        super(context, new AnomalyModel(context.bakeLayer(AnomalyModel.LAYER)), shadowRadius);
        this.texture = texture;
        this.stillOne = stillOne;
        this.chatEcho = chatEcho;
    }

    @Override
    public AnomalyRenderState createRenderState() {
        return new AnomalyRenderState();
    }

    @Override
    public void extractRenderState(T entity, AnomalyRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.animationAge = entity.tickCount + partialTick;
        state.walkCycle = entity.tickCount * 0.66F + partialTick;
        Vec3 velocity = entity.getDeltaMovement();
        state.walkAmount = (float) Math.min(1.0D, velocity.horizontalDistance() * 4.0D);
        state.headYaw = (entity.getYHeadRot() - entity.getYRot()) * ((float) Math.PI / 180.0F);
        state.headPitch = entity.getXRot() * ((float) Math.PI / 180.0F);
        state.stillOne = this.stillOne || entity instanceof StillOneEntity;
        state.chatEcho = this.chatEcho || entity instanceof SignalEchoEntity;
        state.watched = entity.isNoAi();
    }

    @Override
    public Identifier getTextureLocation(AnomalyRenderState state) {
        return this.texture;
    }
}
