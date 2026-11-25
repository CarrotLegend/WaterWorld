package com.carrot123.waterworld.entity;

import com.carrot123.waterworld.blockentity.ElectricEelGeneratorBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class GeneratorEelEntity extends Entity implements GeoEntity {
    public static final float MODEL_SCALE = 0.8F;
    private static final double NORTH_ORIGIN_X = 0.504D;
    private static final double ORIGIN_Y = 0.155D;
    private static final double NORTH_ORIGIN_Z = 0.534D;
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private BlockPos generatorPos;

    public GeneratorEelEntity(EntityType<? extends GeneratorEelEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public void attachTo(BlockPos primaryPos) {
        generatorPos = primaryPos.immutable();
    }

    public boolean belongsTo(BlockPos primaryPos) {
        return primaryPos.equals(generatorPos);
    }

    public void anchorAt(BlockPos secondaryPos, Direction facing) {
        double x = NORTH_ORIGIN_X - 0.5D;
        double z = NORTH_ORIGIN_Z - 0.5D;
        Direction right = facing.getClockWise();
        double worldX = secondaryPos.getX() + 0.5D + x * right.getStepX() - z * facing.getStepX();
        double worldY = secondaryPos.getY() + ORIGIN_Y;
        double worldZ = secondaryPos.getZ() + 0.5D + x * right.getStepZ() - z * facing.getStepZ();
        float yaw = (facing.get2DDataValue() + 2) % 4 * 90.0F;
        if (getX() != worldX || getY() != worldY || getZ() != worldZ || getYRot() != yaw) {
            moveTo(worldX, worldY, worldZ, yaw, 0.0F);
        }
    }

    @Override
    public void tick() {
        if (level() instanceof ServerLevel server
                && (generatorPos == null || ElectricEelGeneratorBlockEntity.isEelOrphaned(server, generatorPos, this))) {
            discard();
            return;
        }
        super.tick();
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(0.5D);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        generatorPos = tag.contains("GeneratorPos", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("GeneratorPos")) : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (generatorPos != null) {
            tag.putLong("GeneratorPos", generatorPos.asLong());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
