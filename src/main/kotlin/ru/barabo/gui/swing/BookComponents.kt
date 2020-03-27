package ru.barabo.gui.swing

import java.awt.Component
import java.awt.Container
import javax.swing.JTabbedPane

fun Container.mainBook(): JTabbedPane? {

    var loopParent: Container? = parent

    var topApplicationBook: JTabbedPane? = null

    while (loopParent != null) {

        if(loopParent is JTabbedPane) {
            topApplicationBook = loopParent
        }
        loopParent = loopParent.parent
    }
    return topApplicationBook
}

fun JTabbedPane.saveTabs(): TabsInBook {

    val panels = ArrayList<Pair<String, Component>>()

    for (index in 0 until tabCount) {

        panels += Pair(getTitleAt(index), getComponentAt(index))
    }

    val selectIndex = if(selectedIndex < 0)0 else selectedIndex

    removeAll()

    return TabsInBook(panels, selectIndex, this)
}

data class TabsInBook(val panels: List<Pair<String, Component>> = emptyList(), val selectedPanel: Int = 0, val book: JTabbedPane = JTabbedPane()) {

    fun restoreTabs() {

        book.removeAll()

        for (pairs in panels) book.addTab(pairs.first, pairs.second)

        book.selectedIndex = selectedPanel
    }
}