package org.autojs.autojs6.jetbrains.run

import org.jdom.Element

object AutoJs6ProjectConfigurationSerializer {
    const val PROJECT_ROOT_ATTRIBUTE = "autojs6-project-root"

    fun readProjectRoot(element: Element): String =
        element.getAttributeValue(PROJECT_ROOT_ATTRIBUTE) ?: ""

    fun writeProjectRoot(element: Element, value: String) {
        if (value.isBlank()) {
            element.removeAttribute(PROJECT_ROOT_ATTRIBUTE)
        } else {
            element.setAttribute(PROJECT_ROOT_ATTRIBUTE, value)
        }
    }
}
