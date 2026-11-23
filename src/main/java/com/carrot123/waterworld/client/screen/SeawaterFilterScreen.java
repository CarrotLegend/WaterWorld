package com.carrot123.waterworld.client.screen;

import com.carrot123.waterworld.blockentity.SeawaterFilterBlockEntity;
import com.carrot123.waterworld.menu.SeawaterFilterMenu;
import com.carrot123.waterworld.registry.ModMenus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SeawaterFilterScreen extends AbstractContainerScreen<SeawaterFilterMenu> {
    public SeawaterFilterScreen(SeawaterFilterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 191;
        inventoryLabelY = 97;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @SubscribeEvent
    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenus.SEAWATER_FILTER.get(), SeawaterFilterScreen::new));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF202020);
        graphics.renderOutline(leftPos + 1, topPos + 1, imageWidth - 2, imageHeight - 2, 0xFFF4F4F4);
        menu.slots.forEach(slot -> drawSlot(graphics, leftPos + slot.x, topPos + slot.y));

        int x = leftPos + 84;
        int y = topPos + 43;
        int height = 28;
        graphics.renderOutline(x - 1, y - 1, 10, height + 2, 0xFF373737);
        graphics.fill(x, y, x + 8, y + height, 0xFF555555);
        float progress = menu.progress();
        if (menu.isRunning() && progress > 0 && progress < SeawaterFilterBlockEntity.PROCESS_TICKS) {
            progress = progress - 1 + Mth.clamp(partialTick, 0.0F, 1.0F);
        }
        int filled = Mth.floor(height * Mth.clamp(progress / SeawaterFilterBlockEntity.PROCESS_TICKS, 0.0F, 1.0F));
        if (filled > 0) {
            graphics.fill(x, y, x + 8, y + filled, 0xFF247DA8);
            graphics.fill(x, y, x + 2, y + filled, 0xFF58BCD8);
        }
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
        graphics.fill(x, y, x + 17, y + 17, 0xFFFFFFFF);
        graphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
    }
}
