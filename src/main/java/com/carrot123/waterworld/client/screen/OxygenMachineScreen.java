package com.carrot123.waterworld.client.screen;

import com.carrot123.waterworld.blockentity.OxygenMachineBlockEntity;
import com.carrot123.waterworld.menu.OxygenMachineMenu;
import com.carrot123.waterworld.registry.ModMenus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "waterworld", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class OxygenMachineScreen extends AbstractContainerScreen<OxygenMachineMenu> {
    private static final String TEXT = "gui.waterworld.oxygen_machine.";
    private static final int ENERGY_COLOR = 0xffc94244;
    private static final int WATER_COLOR = 0xff338bd0;
    private static final int BAR_BACKGROUND = 0xff363b43;

    public OxygenMachineScreen(OxygenMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 177;
        inventoryLabelY = 84;
    }

    @SubscribeEvent
    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.ELECTRIC_AIR_COMPRESSOR.get(), OxygenMachineScreen::new);
            MenuScreens.register(ModMenus.ELECTROLYTIC_OXYGEN_GENERATOR.get(), OxygenMachineScreen::new);
        });
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (menu.machineType().usesWater()) {
            barTooltip(graphics, mouseX, mouseY, 22, 22, 10, 42, waterText());
            barTooltip(graphics, mouseX, mouseY, 144, 22, 10, 42, energyText());
        } else {
            barTooltip(graphics, mouseX, mouseY, 31, 59, 114, 8, energyText());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xffc6c6c6);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xff31343a);
        graphics.renderOutline(leftPos + 1, topPos + 1, imageWidth - 2, imageHeight - 2, 0xffeeeeee);
        for (Slot slot : menu.slots) {
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xff40444b);
            graphics.fill(x, y, x + 17, y + 17, 0xffeeeeee);
            graphics.fill(x, y, x + 16, y + 16, 0xff8b8b8b);
        }
        if (menu.machineType().usesWater()) {
            verticalBar(graphics, 22, menu.water(), menu.machineType().waterCapacity(), WATER_COLOR);
            verticalBar(graphics, 144, menu.energy(), menu.machineType().energyCapacity(), ENERGY_COLOR);
        } else {
            int width = menu.energy() * 112 / menu.machineType().energyCapacity();
            graphics.fill(leftPos + 31, topPos + 59, leftPos + 145, topPos + 67, BAR_BACKGROUND);
            graphics.fill(leftPos + 32, topPos + 60, leftPos + 32 + width, topPos + 66, ENERGY_COLOR);
            graphics.drawCenteredString(font, energyText(), leftPos + imageWidth / 2, topPos + 17, 0xff404040);
        }
        String status = switch (menu.workStatus()) {
            case OxygenMachineBlockEntity.WORKING -> menu.machineType().usesWater() ? "generating" : "filling";
            case OxygenMachineBlockEntity.NEEDS_ENERGY -> "needs_energy";
            case OxygenMachineBlockEntity.NEEDS_WATER -> "needs_water";
            default -> "idle";
        };
        Component label = Component.translatable(TEXT + status);
        graphics.drawString(font, label, leftPos + (imageWidth - font.width(label)) / 2, topPos + 72, 0xff303540, false);
    }

    private void verticalBar(GuiGraphics graphics, int offset, int value, int capacity, int color) {
        int x = leftPos + offset;
        int y = topPos + 22;
        int fill = value * 40 / capacity;
        graphics.fill(x, y, x + 10, y + 42, BAR_BACKGROUND);
        graphics.fill(x + 1, y + 41 - fill, x + 9, y + 41, color);
    }

    private Component energyText() {
        return Component.translatable(TEXT + "energy", menu.energy(), menu.machineType().energyCapacity());
    }

    private Component waterText() {
        return Component.translatable(TEXT + "water", menu.water(), menu.machineType().waterCapacity());
    }

    private void barTooltip(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int w, int h, Component text) {
        if (isHovering(x, y, w, h, mouseX, mouseY)) {
            graphics.renderTooltip(font, text, mouseX, mouseY);
        }
    }
}
