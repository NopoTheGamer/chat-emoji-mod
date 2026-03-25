package com.nopo

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.objects.AtlasSprite
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import java.nio.file.Files

object ChatEmojiMod : ModInitializer {

	val EMOJI_TYPE: Type? = object : TypeToken<Emojis>() {}.type

	override fun onInitialize() {
		ClientReceiveMessageEvents.MODIFY_GAME.register(::onModify)
		val path = FabricLoader.getInstance().getModContainer("chat-emoji-mod").get()
			.findPath("assets/chat-emoji-mod/emojis.json").get()
		val newInputStream = Files.newInputStream(path).reader()
		val jsonReader = JsonReader(newInputStream)
		val json = Gson().fromJson<Emojis>(jsonReader, EMOJI_TYPE)
		emojis = json.emojis
		for (emoji in emojis) {
			for (part in emoji.getAll()) {
				chatList.add("$part ")
			}
		}
	}

	var emojis = listOf<Emoji>()
	val chatList = mutableListOf<String>()
	val gui: ResourceLocation = ResourceLocation.withDefaultNamespace("gui")


	private fun onModify(message: Component, actionBar: Boolean): Component {
		if (actionBar) return message
		try {
			var component = message.copy()
			val text = message.string
			var hasDone = false
			val split = text.split(":")
			if (split.size < 3) return message

			for (part in split) {
				for (emoji in emojis) {
					if (emoji.isEmoji(part)) {
						component = component.replace(":$part:", buildEmojiComponent(emoji.name)) ?: continue
						hasDone = true
					}
				}
			}


			if (!hasDone) return message
			return component
		} catch (_: Exception) {
			return message
		}
	}

	fun buildEmojiComponent(name: String): Component {
		return Component.`object`(AtlasSprite(gui, ResourceLocation.fromNamespaceAndPath("chat-emoji-mod", name)))
	}
}