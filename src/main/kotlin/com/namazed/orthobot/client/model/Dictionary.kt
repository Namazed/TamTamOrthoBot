package com.namazed.orthobot.client.model

import com.google.gson.annotations.SerializedName

class Dictionary(
    @SerializedName("def") val def: List<Def>
)

class Def(
    @SerializedName("pos") val pos: String,
    @SerializedName("text") val text: String,
    @SerializedName("tr") val main: List<Main> = emptyList()
)

class Main(
    @SerializedName("ex") val examples: List<Examples> = emptyList(),
    @SerializedName("mean") val means: List<Mean> = emptyList(),
    @SerializedName("pos") val pos: String = "",
    @SerializedName("syn") val synonyms: List<Synonym> = emptyList(),
    @SerializedName("text") val text: String = ""
)

class Examples(
    @SerializedName("text") val text: String,
    @SerializedName("tr") val tr: List<Tr>
)

class Tr(
    @SerializedName("text") val text: String
)

class Mean(
    @SerializedName("text") val text: String
)

class Synonym(
    @SerializedName("text") val text: String
)