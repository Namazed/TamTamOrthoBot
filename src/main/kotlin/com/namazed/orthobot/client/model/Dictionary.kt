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

fun createDictionaryText(dictionary: Dictionary): String {
    val stringBuilder = StringBuilder()
    if (dictionary.def.isEmpty()) {
        return "Извините я не смог найти данное слово в Яндекс словаре"
    }
    dictionary.def[0].main.asIterable().mapIndexed { index, mainInfo: Main ->
        if (index == 0) {
            stringBuilder.append("Значение: [ ${mainInfo.text} ]")
        } else {
            stringBuilder.append("\nЗначение: [ ${mainInfo.text} ]")
        }
        if (mainInfo.synonyms.isNotEmpty()) {
            stringBuilder.append("\nСинонимы:\n")
            stringBuilder.append("[ ")
        }
        mainInfo.synonyms.asIterable().mapIndexed { synIndex, synonym ->
            stringBuilder.append(synonym.text)
            if (synIndex < mainInfo.synonyms.size - 1) {
                stringBuilder.append(", ")
            }
            synonym
        }
        if (mainInfo.synonyms.isNotEmpty()) {
            stringBuilder.append(" ]\n")
        }
        mainInfo
    }

    return stringBuilder.toString()
}