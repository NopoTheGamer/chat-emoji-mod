package com.nopo.mixin.accessor;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestions.class)
public interface AccessorCommandSuggestions {
    @Accessor("font")
    Font chatemojimod$font();
}
