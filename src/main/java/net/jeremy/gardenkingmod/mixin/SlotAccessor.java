package net.jeremy.gardenkingmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.screen.slot.Slot;

@Mixin(Slot.class)
public interface SlotAccessor {
        @Accessor("x")
        @Mutable
        void setX(int x);

        @Accessor("y")
        @Mutable
        void setY(int y);
}
