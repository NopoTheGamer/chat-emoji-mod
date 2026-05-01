package com.nopo

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import java.util.Optional

fun componentBuilder(init: MutableComponent.() -> Unit): Component {
    return Component.empty().also(init)
}

val ALWAYS get(): (Style?) -> Boolean = { true }

fun MutableComponent.append(string: String = "", init: MutableComponent.() -> Unit): MutableComponent {
    return this.append(Component.literal(string).also(init))
}

fun Component.replace(
    oldValue: String,
    newValue: Component,
    onlyReplaceFirst: Boolean = false,
    predicate: (Style?) -> Boolean = ALWAYS
): MutableComponent? {
    val newComp = Component.empty()
    var hasEdited = false

    this.visit({ currentStyle: Style, string: String ->
        if (string?.contains(oldValue) == true && (!onlyReplaceFirst || !hasEdited) && predicate(style)) {
            val split = string.split(oldValue)
            newComp.append(
                componentBuilder {
                    for ((index, str) in split.withIndex()) {
                        append(Component.literal(str).withStyle(currentStyle))
                        if (index < split.size - 1) {
                            if (!onlyReplaceFirst || !hasEdited) {
                                append(newValue)
                                hasEdited = true
                            } else {
                                append(oldValue) {
                                    style = currentStyle
                                }
                            }
                        }
                    }
                }
            )
        } else {
            newComp.append(Component.literal(string).withStyle(currentStyle))
        }
        Optional.empty<Component>()
    }, Style.EMPTY)

    if (!hasEdited) return null
    return newComp
}