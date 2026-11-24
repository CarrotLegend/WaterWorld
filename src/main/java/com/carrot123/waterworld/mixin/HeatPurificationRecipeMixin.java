package com.carrot123.waterworld.mixin;

import com.google.gson.JsonElement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RecipeManager.class)
public class HeatPurificationRecipeMixin {
    @Unique
    private static final Set<String> waterworld$BLOCKED_HEAT_RECIPES = Set.of(
            "water_bottle_from_campfire_cooking_acceptable",
            "water_bottle_from_campfire_cooking_purified",
            "water_bottle_from_campfire_cooking_slightly_dirty",
            "water_bottle_from_smelting_acceptable",
            "water_bottle_from_smelting_purified",
            "water_bucket_from_campfire_cooking_acceptable",
            "water_bucket_from_campfire_cooking_slight_dirty",
            "water_bucket_from_campfire_purified",
            "water_bucket_from_smelting_acceptable",
            "water_bucket_from_smelting_purfied",
            "terracotta_water_bowl_from_campfire_cooking_acceptable",
            "terracotta_water_bowl_from_campfire_cooking_purified",
            "terracotta_water_bowl_from_campfire_cooking_slightly_dirty",
            "terracotta_water_bowl_from_smelting_acceptable",
            "terracotta_water_bowl_from_smelting_purified");

    @ModifyVariable(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 1, allow = 1)
    private Map<ResourceLocation, JsonElement> waterworld$withoutHeatPurification(Map<ResourceLocation, JsonElement> recipes) {
        Map<ResourceLocation, JsonElement> filtered = recipes;
        for (String path : waterworld$BLOCKED_HEAT_RECIPES) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("thirst", path);
            if (recipes.containsKey(id)) {
                if (filtered == recipes) {
                    filtered = new LinkedHashMap<>(recipes);
                }
                filtered.remove(id);
            }
        }
        return filtered;
    }
}
