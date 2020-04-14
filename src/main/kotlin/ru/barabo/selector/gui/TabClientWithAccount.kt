package ru.barabo.selector.gui

import ru.barabo.gui.swing.ResourcesManager
import ru.barabo.gui.swing.maxSpaceXConstraint
import ru.barabo.gui.swing.onlyButton
import ru.barabo.gui.swing.table.ColumnTableModel
import ru.barabo.gui.swing.table.EntityTable
import ru.barabo.gui.swing.table.doubleClickEvent
import ru.barabo.selector.entity.ClientWithAccount
import ru.barabo.selector.service.ClientWithAccountService
import ru.barabo.selector.service.SqlFilterEntity
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.JToolBar
import javax.swing.SwingConstants.LEFT
import kotlin.reflect.KProperty0

object TabClientWithAccount : SelectorTab<ClientWithAccount>("Клиенты с ИНН и счетами") {
    init {
        layout = BorderLayout()

        TableClientWithAccount.doubleClickEvent {
            tabsSaver.select(ClientWithAccountService.selectedEntity())
        }

        add(JScrollPane(TableClientWithAccount), BorderLayout.CENTER)

        val toolBarClientWithAccount = ToolBarClientWithAccount(::tabsSaver)

        add(toolBarClientWithAccount, BorderLayout.NORTH)
    }
}

object TableClientWithAccount : EntityTable<ClientWithAccount>(clientWithAccountColumns, ClientWithAccountService)

private val clientWithAccountColumns = listOf(
    ColumnTableModel("Наименование клиента", 50, ClientWithAccount::label, false),
    ColumnTableModel("ИНН", 20, ClientWithAccount::tax, false),
    ColumnTableModel("№ счета", 20, ClientWithAccount::accountCode, false),
    ColumnTableModel("Дата открытия", 40, ClientWithAccount::openFormat, false),
    ColumnTableModel("id", 50, ClientWithAccount::id, false)
)

class ToolBarClientWithAccount(process: KProperty0<TabsBookProcessOk<ClientWithAccount>>) : JToolBar() {

    init {

        findAnyText(ClientWithAccountService)

        onlyButton("Выбрать", 0, 2, "outClient24"){

            process.get().select(ClientWithAccountService.selectedEntity())
        }.apply {
            maximumSize = Dimension(100, 24)
            preferredSize = Dimension(100, 24)
        }

        onlyButton("Отменить", 0, 3, "deleteDB24"){
            process.get().cancel()
        }.apply {
            maximumSize = Dimension(100, 24)
            preferredSize = Dimension(100, 24)
        }

        maxSpaceXConstraint(4)
    }
}

fun Container.findAnyText(store: ClientWithAccountService): JTextField {
    this@findAnyText.add(JLabel("Поиск по клиенту, ИНН или №счета", ResourcesManager.getIcon("find"), LEFT) )
    return JTextField().apply {
        addKeyListener( FilterKeyLister(store.filter) { store.filter.filterEntity.label = it} )

        this@findAnyText.add(this)

        toolTipText = "Поиск по Клиенту, ИНН или № счета, примеры: Беркут* 254*001 40702810*"
        minimumSize = Dimension(150, 32)
        preferredSize = Dimension(150, 32)
        maximumSize = Dimension(150, 32)
    }
}

class FilterKeyLister(private val filter: SqlFilterEntity<*>? = null, private val setter: (String?)->Unit) : KeyListener {
    override fun keyTyped(e: KeyEvent?) {}

    override fun keyPressed(e: KeyEvent?) {}

    override fun keyReleased(e: KeyEvent?) {

        val textField = (e?.source as? JTextField) ?: return

        setter(textField.text)

        filter?.applyFilter()
    }
}

