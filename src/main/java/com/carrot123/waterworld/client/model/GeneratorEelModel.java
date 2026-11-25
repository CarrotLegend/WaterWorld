package com.carrot123.waterworld.client.model;

import com.carrot123.waterworld.entity.GeneratorEelEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class GeneratorEelModel extends GeoModel<GeneratorEelEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("waterworld", "geo/generator_eel.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("waterworld", "textures/entity/generator_eel.png");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("waterworld", "animations/generator_eel.animation.json");

    @Override
    public ResourceLocation getModelResource(GeneratorEelEntity eel) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(GeneratorEelEntity eel) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(GeneratorEelEntity eel) {
        return ANIMATIONS;
    }
}
