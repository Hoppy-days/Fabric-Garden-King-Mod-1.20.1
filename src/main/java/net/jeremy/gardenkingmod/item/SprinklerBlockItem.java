package net.jeremy.gardenkingmod.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.jeremy.gardenkingmod.block.sprinkler.SprinklerBlock;
import net.jeremy.gardenkingmod.block.sprinkler.SprinklerTier;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class SprinklerBlockItem extends BlockItem {
        private final SprinklerTier tier;

        public SprinklerBlockItem(SprinklerBlock block, Settings settings) {
                super(block, settings);
                this.tier = block.getTier();
        }

        @Override
        public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
                super.appendTooltip(stack, world, tooltip, context);
                tooltip.add(Text.translatable("tooltip.gardenkingmod.sprinkler.radius", this.tier.getHorizontalRadius(),
                                this.tier.getVerticalRadius()).formatted(Formatting.GRAY));
        }
}
