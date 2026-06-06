package org.autojs.autojs6.jetbrains.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class AutoJs6DeviceStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = AutoJs6DeviceStatusBarWidget.WIDGET_ID

    override fun getDisplayName(): String = "AutoJs6 Connected Device"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget = AutoJs6DeviceStatusBarWidget()

    override fun disposeWidget(widget: StatusBarWidget) {
        Disposer.dispose(widget)
    }

    override fun isEnabledByDefault(): Boolean = true
}
