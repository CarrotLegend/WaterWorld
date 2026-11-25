package com.carrot123.waterworld.client.renderer;

import com.carrot123.waterworld.block.RecyclingStationBlock;
import com.carrot123.waterworld.blockentity.RecyclingStationBlockEntity;
import com.carrot123.waterworld.registry.ModBlockEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RecyclingStationRenderer implements BlockEntityRenderer<RecyclingStationBlockEntity> {
    private final ItemRenderer items;

    public RecyclingStationRenderer(BlockEntityRendererProvider.Context context) {
        items = context.getItemRenderer();
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.RECYCLING_STATION.get(), RecyclingStationRenderer::new);
    }

    @Override
    public void render(RecyclingStationBlockEntity station, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int light, int overlay) {
        ItemStack stack = station.getStoredItem();
        if (stack.isEmpty()) {
            return;
        }
        pose.pushPose();
        var state = station.getBlockState();
        if (state.getValue(RecyclingStationBlock.PART) == RecyclingStationBlock.Part.MAIN) {
            Direction facing = state.getValue(RecyclingStationBlock.FACING);
            Direction across = facing.getClockWise();
            pose.translate(0.5D + 0.5D * across.getStepX(), 1.05D, 0.5D + 0.5D * across.getStepZ());
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        } else {
            pose.translate(0.5D, RecyclingStationBlock.LEGACY_SURFACE_Y + 0.04D, 0.5D);
        }
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.scale(0.65F, 0.65F, 0.65F);
        items.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffer,
                station.getLevel(), (int) station.getBlockPos().asLong());
        pose.popPose();
    }
}
