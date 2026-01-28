package net.jeremy.gardenkingmod.item;

import java.util.List;
import java.util.OptionalInt;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;

public record ToolBuffDefinition(Identifier itemId, OptionalInt durabilityOverride,
                List<EnchantmentEntry> enchantments, List<AttributeEntry> attributeModifiers) {

        public record EnchantmentEntry(Identifier id, int level) {
        }

        public record AttributeEntry(Identifier attributeId, double amount,
                        EntityAttributeModifier.Operation operation, EquipmentSlot slot, String name) {
        }
}
