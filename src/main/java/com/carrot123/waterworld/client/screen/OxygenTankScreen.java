package com.carrot123.waterworld.client.screen;

import com.carrot123.waterworld.item.oxygen.OxygenTankItem;
import com.carrot123.waterworld.menu.OxygenTankMenu;
import com.carrot123.waterworld.registry.ModMenus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class OxygenTankScreen extends AbstractContainerScreen<OxygenTankMenu> {
    public OxygenTankScreen(OxygenTankMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 158;
        inventoryLabelY = 64;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @SubscribeEvent
    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenus.OXYGEN_TANKS.get(), OxygenTankScreen::new));
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
        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
            graphics.fill(x, y, x + 17, y + 17, 0xFFFFFFFF);
            graphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
        }
        drawTankLabel(graphics, 0, "left");
        drawTankLabel(graphics, 1, "right");
    }

    private void drawTankLabel(GuiGraphics graphics, int index, String side) {
        Slot slot = menu.getSlot(index);
        int center = leftPos + slot.x + 8;
        Component label = Component.translatable("container.waterworld.oxygen_tanks." + side);
        graphics.drawString(font, label, center - font.width(label) / 2, topPos + 18, 0x404040, false);
        ItemStack tank = slot.getItem();
        if (!tank.isEmpty()) {
            Component amount = OxygenTankItem.amountText(tank);
            graphics.drawString(font, amount, center - font.width(amount) / 2, topPos + 48, 0x404040, true);
        }
    }
}
