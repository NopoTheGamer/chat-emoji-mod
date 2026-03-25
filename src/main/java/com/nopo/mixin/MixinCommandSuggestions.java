package com.nopo.mixin;

import com.nopo.ChatEmojiMod;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(CommandSuggestions.class)
public abstract class MixinCommandSuggestions {

    @Unique
    private static final Pattern COLON_PATTERN = Pattern.compile("(:)");
    @Unique
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    @Shadow
    @Final
    private EditBox input;

    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    public abstract void showSuggestions(boolean bl);

    @Inject(at = @At("TAIL"), method = "updateCommandInfo", cancellable = true)
    private void init(CallbackInfo ci) {
        String text = this.input.getValue();
        StringReader stringReader = new StringReader(text);
        boolean hasSlash = stringReader.canRead() && stringReader.peek() == '/';
        if (hasSlash) {
            stringReader.skip();
        }
        int cursor = this.input.getCursorPosition();
        String textUptoCursor = text.substring(0, cursor);
        int start = Math.max(getLastPattern(textUptoCursor, COLON_PATTERN) - 1, 0);
        int whitespace = getLastPattern(textUptoCursor, WHITESPACE_PATTERN);
        if (start < textUptoCursor.length() && start >= whitespace) {
            if (textUptoCursor.charAt(start) == ':') {
                this.pendingSuggestions = SharedSuggestionProvider.suggest(ChatEmojiMod.INSTANCE.getChatList(), new SuggestionsBuilder(textUptoCursor, start));
                this.pendingSuggestions.thenRun(() -> {
                    if (!this.pendingSuggestions.isDone()) {
                        return;
                    }
                    this.showSuggestions(false);
                });
                ci.cancel();
            }

        }
    }

    @Unique
    private int getLastPattern(String input, Pattern pattern) {
        if (Strings.isNullOrEmpty(input)) {
            return 0;
        }
        int i = 0;
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            i = matcher.end();
        }
        return i;
    }
}