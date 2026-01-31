package net.jeremy.gardenkingmod.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;

public class BuffedShovelItem extends ShovelItem {
        public BuffedShovelItem(ToolMaterial toolMaterial, float attackDamage, float attackSpeed, Settings settings) {
                super(toolMaterial, attackDamage, attackSpeed, settings);
        }

        @Override
        public void onCraft(ItemStack stack, World world, PlayerEntity player) {
                super.onCraft(stack, world, player);
                ToolBuffHelper.applyBuffsIfNeeded(stack);
        }

        @Override
        public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
                super.inventoryTick(stack, world, entity, slot, selected);
                if (!world.isClient) {
                        ToolBuffHelper.applyBuffsIfNeeded(stack);
                }
        }

        @Override
        public ItemStack getDefaultStack() {
                ItemStack stack = super.getDefaultStack();
                ToolBuffHelper.applyBuffsIfNeeded(stack);
                return stack;
        }

}
