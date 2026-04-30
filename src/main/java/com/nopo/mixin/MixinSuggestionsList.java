package com.nopo.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.suggestion.Suggestion;
import com.nopo.ChatEmojiMod;
import com.nopo.Emoji;
import com.nopo.mixin.accessor.AccessorCommandSuggestions;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class MixinSuggestionsList {

    @Unique
    private static final Pattern EMOJI_PATTERN = Pattern.compile("^:.*:$");

    @Shadow
    @Final
    CommandSuggestions this$0;

    @Shadow
    @Final
    private Rect2i rect;

    @Shadow
    private int offset;

    @Inject(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V",
                    ordinal = 4
            )
    )
    private void prepEmoji(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            CallbackInfo ci,
            @Local(name = "suggestion") Suggestion suggestion,
            @Local(name = "i") int i,
            @Share("offset") LocalIntRef offset,
            @Share("emojiComponent")LocalRef<Component> emojiComponent
            ) {
        offset.set(0);
        String text = suggestion.getText().trim();
        if (EMOJI_PATTERN.matcher(text).matches()) {
            for (Emoji emoji : ChatEmojiMod.INSTANCE.getEmojis()) {
                if (emoji.isEmoji(text.replaceAll("(:)", ""))) {
                    emojiComponent.set(ChatEmojiMod.INSTANCE.buildEmojiComponent(emoji.getName()));
                    offset.set(((AccessorCommandSuggestions)this$0).chatemojimod$font().width(emojiComponent.get()) + 1);
                    break;
                }
            }
        }
    }

    @Inject(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V",
                    ordinal = 4,
                    shift = At.Shift.AFTER
            )
    )
    private void renderEmoji(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            CallbackInfo ci,
            @Local(name = "i") int i,
            @Share("emojiComponent")LocalRef<Component> emojiComponent
    ) {
        graphics.text(((AccessorCommandSuggestions)this$0).chatemojimod$font(),
                emojiComponent.get(),
                this.rect.getX() + 1,
                this.rect.getY() + 2 + 12 * i,
                -1
        );
    }

    @ModifyArg(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V",
                    ordinal = 4
            ),
            index = 2
    )
    private int enlargeBackground(int original, @Share("offset") LocalIntRef offset) {
        return original + offset.get();
    }

    @ModifyArg(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            ),
            index = 2
    )
    private int moveText(int original, @Share("offset") LocalIntRef offset) {
        return original + offset.get();
    }
}
