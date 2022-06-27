package com.letgo

import com.letgo.core.app.LetGoApplication
import com.letgo.core.internal.IConfigModule

class App:LetGoApplication() {
    override fun registerModules(): List<IConfigModule> {
        return listOf(AppConfigModule())
    }
}