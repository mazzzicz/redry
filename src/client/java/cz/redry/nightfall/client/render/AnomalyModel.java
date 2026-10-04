package cz.redry.nightfall.client.render;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.Identifier;

public final class AnomalyModel extends EntityModel<AnomalyRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("redry", "night_stalker"), "main");

    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart body;
    private final ModelPart mantle;
    private final ModelPart spine;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public AnomalyModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.body = root.getChild("body");
        this.mantle = root.getChild("mantle");
        this.spine = root.getChild("spine");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(46, 32).addBox(-3.0F, -1.0F, -4.15F, 6.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("mantle", CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-5.0F, -0.5F, -2.8F, 10.0F, 3.0F, 5.6F),
                PartPose.offset(0.0F, 0.5F, 0.0F));
        root.addOrReplaceChild("spine", CubeListBuilder.create()
                        .texOffs(56, 48).addBox(-1.0F, 0.0F, 1.7F, 2.0F, 10.0F, 1.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(AnomalyRenderState state) {
        super.setupAnim(state);

        float age = state.animationAge;
        float walk = Math.min(state.walkAmount, 1.0F);
        float stride = state.walkCycle;
        float sway = (float) Math.sin(age * (state.stillOne ? 0.035F : 0.075F));
        float signalJitter = state.chatEcho ? (float) Math.sin(age * 0.31F) : 0.0F;

        this.head.xRot = state.headPitch + (float) Math.sin(age * 0.041F) * (state.chatEcho ? 0.12F : 0.025F);
        this.head.yRot = state.headYaw + (float) Math.sin(age * (state.chatEcho ? 0.073F : 0.019F))
                * (state.chatEcho ? 0.22F : state.stillOne ? 0.08F : 0.035F);
        this.head.zRot = state.chatEcho ? 0.24F + signalJitter * 0.12F
                : state.stillOne ? 0.045F : (float) Math.sin(age * 0.053F) * 0.025F;
        this.jaw.xRot = state.chatEcho ? 0.10F + (float) Math.sin(age * 0.2F) * 0.16F
                : state.stillOne ? -0.02F : (float) Math.sin(age * 0.04F) * 0.012F;

        this.body.xRot = state.chatEcho ? 0.16F + signalJitter * 0.09F : state.stillOne ? -0.055F : sway * 0.018F;
        this.body.yRot = state.chatEcho ? signalJitter * 0.15F : sway * 0.018F;
        this.body.zRot = state.chatEcho ? -0.12F + signalJitter * 0.09F : state.stillOne ? 0.025F : -sway * 0.018F;
        this.mantle.xRot = state.chatEcho ? -0.16F : state.watched ? 0.08F : 0.0F;
        this.mantle.yRot = state.chatEcho ? signalJitter * 0.12F : sway * 0.03F;
        this.spine.xRot = state.chatEcho ? -0.24F + signalJitter * 0.10F : state.stillOne ? 0.10F : 0.0F;

        float armSwing = (float) Math.cos(stride * 0.72F) * walk * (state.stillOne ? 0.52F : 0.86F);
        float idleTension = state.chatEcho ? -0.38F : state.stillOne ? 0.12F : state.watched ? -0.04F : 0.035F;
        this.rightArm.xRot = armSwing - idleTension + signalJitter * 0.15F;
        this.leftArm.xRot = -armSwing - idleTension - signalJitter * 0.18F;
        this.rightArm.zRot = state.chatEcho ? -0.40F + signalJitter * 0.1F : state.stillOne ? -0.20F : 0.035F + sway * 0.035F;
        this.leftArm.zRot = state.chatEcho ? 0.48F - signalJitter * 0.1F : state.stillOne ? 0.20F : -0.035F - sway * 0.035F;

        this.rightLeg.xRot = (float) Math.cos(stride * 0.72F + Math.PI) * walk * (state.chatEcho ? 0.95F : 0.72F);
        this.leftLeg.xRot = (float) Math.cos(stride * 0.72F) * walk * (state.chatEcho ? 0.95F : 0.72F);
        this.rightLeg.zRot = state.stillOne ? -0.015F : state.chatEcho ? 0.05F : 0.0F;
        this.leftLeg.zRot = state.stillOne ? 0.025F : state.chatEcho ? -0.04F : 0.0F;
    }
}
