package com.nopo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.suggestion.Suggestion;
import com.nopo.ChatEmojiMod;
import com.nopo.Emoji;
import com.nopo.mixin.accessor.AccessorCommandSuggestions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.regex.Pattern;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class MixinSuggestionsList {

    @Unique
    private static final Pattern EMOJI_PATTERN = Pattern.compile("^:.*:$");

    @Shadow
    @Final
    private Rect2i rect;

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/components/CommandSuggestions;suggestionLineLimit:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int captureThis(
            CommandSuggestions instance,
            Operation<Integer> original,
            @Share("upper")LocalRef<CommandSuggestions> upper
    ) {
        upper.set(instance);
        return original.call(instance);
    }

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V",
                    ordinal = 4
            )
    )
    private void renderEmoji(
            GuiGraphics instance,
            int x0,
            int y0,
            int x1,
            int y1,
            int col,
            Operation<Void> original,
            @Local(name = "suggestion") Suggestion suggestion,
            @Local(name = "n") int n,
            @Share("offset") LocalIntRef offset,
            @Share("upper")LocalRef<CommandSuggestions> upper
    ) {
        offset.set(0);
        Component emojiComponent = null;
        String text = suggestion.getText().trim();
        if (EMOJI_PATTERN.matcher(text).matches()) {
            for (Emoji emoji : ChatEmojiMod.INSTANCE.getEmojis()) {
                if (emoji.isEmoji(text.replaceAll("(:)", ""))) {
                    emojiComponent = ChatEmojiMod.INSTANCE.buildEmojiComponent(emoji.getName());
                    offset.set(((AccessorCommandSuggestions)upper.get()).chatemojimod$font().width(emojiComponent) + 1);
                    break;
                }
            }
        }
        original.call(instance, x0, y0, x1 + offset.get(), y1, col);
        if (emojiComponent != null) {
            instance.drawString(((AccessorCommandSuggestions)upper.get()).chatemojimod$font(),
                    emojiComponent,
                    this.rect.getX() + 1,
                    this.rect.getY() + 2 + 12 * n,
                    -1
            );
        }
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            ),
            index = 2
    )
    private int moveText(int original, @Share("offset") LocalIntRef offset) {
        return original + offset.get();
    }
}
