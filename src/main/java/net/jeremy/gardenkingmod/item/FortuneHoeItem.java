package net.jeremy.gardenkingmod.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

/**
 * A hoe item that provides a built-in fortune level without requiring an enchantment.
 */
public class FortuneHoeItem extends HoeItem implements FortuneProvidingItem {
        private final int fortuneLevel;

        public FortuneHoeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings,
                        int fortuneLevel) {
                super(toolMaterial, attackDamage, attackSpeed, settings);
                this.fortuneLevel = fortuneLevel;
        }

        @Override
        public int gardenkingmod$getFortuneLevel(ItemStack stack) {
                return fortuneLevel;
        }

        @Override
        public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
                super.appendTooltip(stack, world, tooltip, context);
                tooltip.add(Text.translatable("tooltip.gardenkingmod.built_in_fortune",
                                Text.translatable("enchantment.minecraft.fortune"),
                                Text.translatable("enchantment.level." + fortuneLevel)).formatted(Formatting.GRAY));
        }
}
