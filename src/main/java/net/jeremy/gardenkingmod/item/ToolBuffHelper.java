package net.jeremy.gardenkingmod.item;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import net.jeremy.gardenkingmod.GardenKingMod;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class ToolBuffHelper {
        private static final String BUFF_MARKER = "gardenkingmod:tool_buffs_applied";

        private ToolBuffHelper() {
        }

        public static OptionalInt getDurabilityOverride(Item item) {
                Identifier id = Registries.ITEM.getId(item);
                if (id == null) {
                        return OptionalInt.empty();
                }
                return ToolBuffDefinitions.findByItemId(id)
                                .flatMap(definition -> definition.durabilityOverride().isPresent()
                                                ? Optional.of(definition.durabilityOverride())
                                                : Optional.empty())
                                .orElse(OptionalInt.empty());
        }

        public static void applyBuffsIfNeeded(ItemStack stack) {
                if (stack == null || stack.isEmpty()) {
                        return;
                }

                NbtCompound nbt = stack.getOrCreateNbt();
                if (nbt.getBoolean(BUFF_MARKER)) {
                        return;
                }

                Identifier id = Registries.ITEM.getId(stack.getItem());
                if (id == null) {
                        return;
                }

                Optional<ToolBuffDefinition> definition = ToolBuffDefinitions.findByItemId(id);
                if (definition.isEmpty()) {
                        return;
                }

                applyEnchantments(stack, definition.get());
                applyAttributeModifiers(stack, definition.get(), id);

                nbt.putBoolean(BUFF_MARKER, true);
        }

        private static void applyEnchantments(ItemStack stack, ToolBuffDefinition definition) {
                for (ToolBuffDefinition.EnchantmentEntry entry : definition.enchantments()) {
                        Enchantment enchantment = Registries.ENCHANTMENT.get(entry.id());
                        if (enchantment == null) {
                                GardenKingMod.LOGGER.warn("Skipping missing enchantment {} for {}.", entry.id(),
                                                definition.itemId());
                                continue;
                        }
                        stack.addEnchantment(enchantment, entry.level());
                }
        }

        private static void applyAttributeModifiers(ItemStack stack, ToolBuffDefinition definition, Identifier itemId) {
                for (ToolBuffDefinition.AttributeEntry entry : definition.attributeModifiers()) {
                        EntityAttribute attribute = Registries.ATTRIBUTE.get(entry.attributeId());
                        if (attribute == null) {
                                GardenKingMod.LOGGER.warn("Skipping missing attribute {} for {}.", entry.attributeId(),
                                                itemId);
                                continue;
                        }
                        UUID uuid = createStableUuid(itemId, entry);
                        EntityAttributeModifier modifier = new EntityAttributeModifier(uuid, entry.name(), entry.amount(),
                                        entry.operation());
                        stack.addAttributeModifier(attribute, modifier, entry.slot());
                }
        }

        private static UUID createStableUuid(Identifier itemId, ToolBuffDefinition.AttributeEntry entry) {
                String seed = itemId + ":" + entry.attributeId() + ":" + entry.slot().getName() + ":"
                                + entry.operation().name();
                return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
        }
}
