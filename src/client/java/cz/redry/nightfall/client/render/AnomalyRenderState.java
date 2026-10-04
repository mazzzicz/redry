package cz.redry.nightfall.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public final class AnomalyRenderState extends LivingEntityRenderState {
    public float animationAge;
    public float walkCycle;
    public float walkAmount;
    public float headYaw;
    public float headPitch;
    public boolean stillOne;
    public boolean chatEcho;
    public boolean watched;
}
