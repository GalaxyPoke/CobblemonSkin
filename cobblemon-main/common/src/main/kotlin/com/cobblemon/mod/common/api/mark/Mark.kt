/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.mark

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.util.*
import com.google.gson.annotations.SerializedName
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.StringRepresentable

class Mark(
    identifier: ResourceLocation,
    val name: String,
    val description: String,
    val title: String?,
    @SerializedName(value = "titleColor", alternate = ["titleColour"])
    val titleColour: String?,
    val texture: ResourceLocation,
    val replace: List<ResourceLocation>?,
    val group: String?,
    val chance: Float = 0F,
    val indexNumber: Int?
): StringRepresentable {

    companion object {
        internal fun decode(buffer: RegistryFriendlyByteBuf): Mark {
            return Mark(
                buffer.readIdentifier(),
                buffer.readString(),
                buffer.readString(),
                buffer.readNullable { buffer.readString() },
                buffer.readNullable { buffer.readString() },
                buffer.readResourceLocation(),
                buffer.readNullable { buffer.readList { buffer.readResourceLocation() } },
                buffer.readNullable { buffer.readString() },
                buffer.readFloat(),
                buffer.readNullable { buffer.readInt() }
            )
        }
    }

    @Transient
    var identifier: ResourceLocation = identifier
        internal set

    override fun getSerializedName(): String = identifier.toString()

    fun getName(): MutableComponent = name.asTranslated()

    fun getDescription(): MutableComponent = description.asTranslated()

    fun getTitle(name: MutableComponent): MutableComponent {
        title?.let {
            try {
                return if (titleColour != null) it.asTranslated(name.withColor(-1)).withColor(titleColour.toInt(16))
                else it.asTranslated(name)
            } catch (e: Exception) {
                Cobblemon.LOGGER.error("Error parsing titleColor hexadecimal color code for {}", identifier, e)
                return it.asTranslated(name)
            }
        }
        return name
    }

    fun getChanceGroup(): Pair<String, Float> = Pair(group ?: chance.toString(), chance)

    internal fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(identifier)
        buffer.writeString(name)
        buffer.writeString(description)
        buffer.writeNullable(title) { _, v -> buffer.writeString(v) }
        buffer.writeNullable(titleColour) { _, v -> buffer.writeString(v) }
        buffer.writeResourceLocation(texture)
        buffer.writeNullable(replace) { _, v -> buffer.writeCollection(v) { _, resource -> buffer.writeResourceLocation(resource) } }
        buffer.writeNullable(group) { _, v -> buffer.writeString(v) }
        buffer.writeFloat(chance)
        buffer.writeNullable(indexNumber) { _, v -> buffer.writeInt(v) }
    }
}
