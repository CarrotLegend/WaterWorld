package com.carrot123.waterworld.client.renderer;

import com.carrot123.waterworld.client.model.GeneratorEelModel;
import com.carrot123.waterworld.entity.GeneratorEelEntity;
import com.carrot123.waterworld.registry.ModEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GeneratorEelRenderer extends GeoEntityRenderer<GeneratorEelEntity> {
    public GeneratorEelRenderer(EntityRendererProvider.Context context) {
        super(context, new GeneratorEelModel());
        withScale(GeneratorEelEntity.MODEL_SCALE);
        shadowRadius = 0.0F;
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GENERATOR_EEL.get(), GeneratorEelRenderer::new);
    }

    @Override
    protected void applyRotations(GeneratorEelEntity eel, PoseStack pose, float age, float yaw, float partialTick) {
        pose.mulPose(Axis.YP.rotationDegrees(-eel.getYRot()));
    }
}
