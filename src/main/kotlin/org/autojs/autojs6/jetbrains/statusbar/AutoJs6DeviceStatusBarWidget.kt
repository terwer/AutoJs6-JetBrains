package org.autojs.autojs6.jetbrains.statusbar

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import org.autojs.autojs6.jetbrains.device.AutoJs6ConnectionListener
import org.autojs.autojs6.jetbrains.device.AutoJs6ConnectionService
import org.autojs.autojs6.jetbrains.device.AutoJs6Device
import org.autojs.autojs6.jetbrains.device.AutoJs6DeviceSnapshot
import javax.swing.Icon

class AutoJs6DeviceStatusBarWidget : StatusBarWidget, AutoJs6ConnectionListener {
    private val service = service<AutoJs6ConnectionService>()
    private var statusBar: StatusBar? = null
    private val presentation = AutoJs6DevicePresentation()

    override fun ID(): String = WIDGET_ID

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
        service.addListener(this)
        updateWidget()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getPresentation(): StatusBarWidget.WidgetPresentation = presentation

    override fun dispose() {
        service.removeListener(this)
        statusBar = null
    }

    override fun deviceAttached(device: AutoJs6Device) = updateWidget()

    override fun deviceDetached(device: AutoJs6Device, reason: String) = updateWidget()

    override fun selectedDeviceChanged(device: AutoJs6Device?) = updateWidget()

    private fun updateWidget() {
        ApplicationManager.getApplication().invokeLater {
            statusBar?.updateWidget(WIDGET_ID)
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    private inner class AutoJs6DevicePresentation : StatusBarWidget.MultipleTextValuesPresentation {
        override fun getSelectedValue(): String =
            AutoJs6DeviceStatusText.selectedValue(service.deviceSnapshots(), service.selectedDeviceKey())

        override fun getMaxValue(): String =
            AutoJs6DeviceStatusText.maxValue(service.deviceSnapshots())

        override fun getTooltipText(): String =
            AutoJs6DeviceStatusText.tooltip(service.deviceSnapshots(), service.selectedDeviceKey())

        override fun getIcon(): Icon? = null

        override fun getPopup(): JBPopup? {
            val snapshots = service.deviceSnapshots()
            val step = if (snapshots.isEmpty()) {
                object : BaseListPopupStep<String>("AutoJs6 Devices", listOf("未连接设备")) {
                    override fun isSelectable(value: String): Boolean = false
                }
            } else {
                object : BaseListPopupStep<AutoJs6DeviceSnapshot>("AutoJs6 Devices", snapshots) {
                    override fun getTextFor(value: AutoJs6DeviceSnapshot): String =
                        AutoJs6DeviceStatusText.popupLabel(value)

                    override fun onChosen(selectedValue: AutoJs6DeviceSnapshot, finalChoice: Boolean): PopupStep<*> {
                        service.selectDevice(selectedValue.key)
                        return PopupStep.FINAL_CHOICE
                    }
                }
            }
            return JBPopupFactory.getInstance().createListPopup(step)
        }
    }

    companion object {
        const val WIDGET_ID = "AutoJs6DeviceStatus"
    }
}
