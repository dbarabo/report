package ru.barabo.selector.gui

import ru.barabo.gui.swing.TabsInBook
import ru.barabo.gui.swing.selectTab
import java.awt.Component
import javax.swing.JPanel

open class SelectorTab<T: Any>(private val title: String) : JPanel() {

    lateinit var tabsSaver: TabsBookProcessOk<T>

    fun selectTab(component: Component, processResultOk: (T?)->Unit) {

        tabsSaver = TabsBookProcessOk(component.selectTab(title, this), processResultOk)
    }
}

data class TabsBookProcessOk<T>(var tabsInBook: TabsInBook, var processOk: (T?)->Unit) {
    fun select(entity: T?) {

        tabsInBook.restoreTabs()

        processOk(entity)
    }

    fun cancel() {
        tabsInBook.restoreTabs()

        processOk(null)
    }
}