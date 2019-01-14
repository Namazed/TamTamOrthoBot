package com.namazed.amspacebackend.client.model

import com.google.gson.annotations.SerializedName

class Dictionary(
    @SerializedName("def") val def: List<Def>
)

class Def(
    @SerializedName("pos") val pos: String,
    @SerializedName("text") val text: String,
    @SerializedName("tr") val tr: List<Tr>
)

class Main(
    @SerializedName("ex") val ex: List<Examples>,
    @SerializedName("mean") val mean: List<Mean>,
    @SerializedName("pos") val pos: String,
    @SerializedName("syn") val synonyms: List<Synonym>,
    @SerializedName("text") val text: String
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