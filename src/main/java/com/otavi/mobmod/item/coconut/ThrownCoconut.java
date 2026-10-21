package com.otavi.mobmod.item.coconut;

import com.otavi.mobmod.core.ModEntityTypes;
import com.otavi.mobmod.core.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A thrown coconut. Works exactly like a snowball: knockback but no damage, and it breaks into item particles on impact.
 */
public class ThrownCoconut extends ThrowableItemProjectile {
    /**
     * Same as a snowball, which only hurts blazes.
     */
    public static final float DAMAGE = 0.0F;

    private static final byte BREAK_EVENT = 3;

    public ThrownCoconut(EntityType<? extends ThrownCoconut> type, Level level) {
        super(type, level);
    }

    public ThrownCoconut(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntityTypes.COCONUT, owner, level, stack);
    }

    public ThrownCoconut(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntityTypes.COCONUT, x, y, z, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.COCONUT;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == BREAK_EVENT) {
            var particle = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(this.getItem()));
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(particle, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), DAMAGE);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, BREAK_EVENT);
            this.discard();
        }
    }
}
