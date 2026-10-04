package com.example.firespell;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireSpellEntity extends LargeFireball {

    public FireSpellEntity(EntityType<? extends LargeFireball> type, Level level) {
        super(type, level);
        this.explosionPower = 2; // bigger number = bigger boom (TNT is 4)
    }

    public FireSpellEntity(Level level, LivingEntity owner, Vec3 direction) {
        this(FireSpellMod.FIRE_SPELL.get(), level);
        Vec3 d = direction.normalize();
        this.setOwner(owner);
        this.setPos(owner.getX() + d.x * 1.5,
                owner.getEyeY() - 0.2 + d.y * 1.5,
                owner.getZ() + d.z * 1.5);
        this.setDeltaMovement(d.scale(0.8));
        this.xPower = d.x * 0.05;
        this.yPower = d.y * 0.05;
        this.zPower = d.z * 0.05;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            spawnTrail();
        }
    }

    /** Two flame spirals twisting around the fireball + a hot core. */
    private void spawnTrail() {
        Vec3 v = this.getDeltaMovement();
        if (v.lengthSqr() < 1.0E-4) return;
        Vec3 f = v.normalize();
        Vec3 ref = Math.abs(f.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = f.cross(ref).normalize();
        Vec3 up = right.cross(f).normalize();

        double t = this.tickCount * 0.7;
        double radius = 0.7;
        for (int i = 0; i < 2; i++) {
            double a = t + i * Math.PI;
            Vec3 off = right.scale(Math.cos(a) * radius).add(up.scale(Math.sin(a) * radius));
            this.level().addParticle(ParticleTypes.FLAME,
                    getX() + off.x, getY() + 0.5 + off.y, getZ() + off.z, 0, 0.01, 0);
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    getX() - off.x * 0.5, getY() + 0.5 - off.y * 0.5, getZ() - off.z * 0.5, 0, 0, 0);
        }
        this.level().addParticle(ParticleTypes.LAVA, getX(), getY() + 0.5, getZ(), 0, 0, 0);
        this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                getX() - f.x, getY() + 0.5 - f.y, getZ() - f.z, 0, 0, 0);
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 80, 0.6, 0.6, 0.6, 0.2);
            sl.sendParticles(ParticleTypes.LAVA, getX(), getY(), getZ(), 25, 0.5, 0.5, 0.5, 0.0);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        }
        super.onHit(result);
    }
}
