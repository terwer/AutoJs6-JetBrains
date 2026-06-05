package org.autojs.autojs6.jetbrains.run

import org.autojs.autojs6.jetbrains.script.AutoJs6ScriptCommand
import org.jdom.Element

object AutoJs6ScriptConfigurationSerializer {
    fun readScriptPath(element: Element): String =
        element.getAttributeValue(AutoJs6ScriptCommand.SCRIPT_PATH_ATTRIBUTE) ?: ""

    fun writeScriptPath(element: Element, scriptPath: String) {
        val value = scriptPath.trim()
        if (value.isEmpty()) {
            element.removeAttribute(AutoJs6ScriptCommand.SCRIPT_PATH_ATTRIBUTE)
        } else {
            element.setAttribute(AutoJs6ScriptCommand.SCRIPT_PATH_ATTRIBUTE, value)
        }
    }
}
