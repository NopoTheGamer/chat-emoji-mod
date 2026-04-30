package com.nopo.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.brigadier.suggestion.Suggestion;
import com.nopo.ChatEmojiMod;
import com.nopo.Emoji;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
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
public class SuggestionsListMixin {

    @Unique
    private static final Pattern EMOJI_PATTERN = Pattern.compile("^:.*:$");

    @Shadow
    @Final
    private Rect2i rect;

    @Shadow
    private int offset;

    @Shadow
    private int current;

    @Inject(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            )
    )
    private void renderEmoji(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            CallbackInfo ci,
            @Local(name = "suggestion") Suggestion suggestion,
            @Local(name = "i") int i,
            @Share("renderingEmoji") LocalBooleanRef renderingEmoji
    ) {
        renderingEmoji.set(false);
        String text = suggestion.getText().trim();
        if (EMOJI_PATTERN.matcher(text).matches()) {
            for (Emoji emoji : ChatEmojiMod.INSTANCE.getEmojis()) {
                if (emoji.isEmoji(text.replaceAll("(:)", ""))) {
                    renderingEmoji.set(true);
                    graphics.text(Minecraft.getInstance().font,
                            ChatEmojiMod.INSTANCE.buildEmojiComponent(emoji.getName()),
                            this.rect.getX() + 1,
                            this.rect.getY() + 2 + 12 * i,
                            i + this.offset == this.current ? -256 : -5592406
                    );
                    break;
                }
            }
        }
    }

    @ModifyArg(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
            ),
            index = 2
    )
    private int moveText(int original, @Share("renderingEmoji") LocalBooleanRef renderingEmoji) {
        if (renderingEmoji.get()) {
            return original + 10;
        } else {
            return original;
        }
    }
}
