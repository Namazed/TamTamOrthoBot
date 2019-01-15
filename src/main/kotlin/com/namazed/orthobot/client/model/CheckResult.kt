package com.namazed.orthobot.client.model

import com.google.gson.annotations.SerializedName


class CheckResult(
    @SerializedName("code")
    val code: Int,
    @SerializedName("col")
    val col: Int,
    @SerializedName("len")
    val initialWordLength: Int,
    @SerializedName("pos")
    val pos: Int,
    @SerializedName("row")
    val row: Int,
    @SerializedName("s")
    val rightWords: List<String>,
    @SerializedName("word")
    val initialWord: String
)