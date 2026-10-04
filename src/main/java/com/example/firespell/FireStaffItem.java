package com.example.firespell;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FireStaffItem extends Item {
    public FireStaffItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            FireSpellEntity fireball = new FireSpellEntity(level, player, player.getLookAngle());
            level.addFreshEntity(fireball);
            level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                    SoundSource.PLAYERS, 1.0F, 0.8F);
        }
        player.getCooldowns().addCooldown(this, 30); // 1.5 second cooldown
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
