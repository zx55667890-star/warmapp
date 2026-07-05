package com.example.myapplication.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    class Dynamic(val value: String) : UiText()
    class Resource(@StringRes val resId: Int, vararg val args: Any) : UiText() {
        val resolvedArgs: Array<out Any> = args
    }

    @Composable
    fun asString(): String = when (this) {
        is Dynamic -> value
        is Resource -> stringResource(resId, *resolvedArgs)
    }
}
