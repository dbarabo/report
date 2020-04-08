package ru.barabo.selector.gui

import ru.barabo.db.service.StoreFilterService
import ru.barabo.gui.swing.ResourcesManager
import ru.barabo.gui.swing.liteGroup
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
import javax.swing.*
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

        selectCancelButton(process, ClientWithAccountService)
    }
}

fun <T: Any> Container.selectCancelButton(process: KProperty0<TabsBookProcessOk<T>>, store: StoreFilterService<T>): JPanel =
    liteGroup("", 0, 0).apply {

        onlyButton("Выбрать", 0, 0, "outClient24"){

            process.get().select(store.selectedEntity())
        }

        onlyButton("Отменить", 1, 0, "deleteDB24"){
            process.get().cancel()
        }
    }


fun Container.findAnyText(store: ClientWithAccountService): JTextField {
    this@findAnyText.add(JLabel("Поиск по клиенту, ИНН или №счета", ResourcesManager.getIcon("find"), LEFT) )
    return JTextField().apply {
        addKeyListener( FilterKeyLister(store.filter) { store.filter.filterEntity.label = it} )

        this@findAnyText.add(this)

        toolTipText = "Поиск по Клиенту, ИНН или № счета, примеры: Беркут* 254*001 40702810*"
        minimumSize = Dimension(140, 32)
        preferredSize = Dimension(140, 32)
        maximumSize = Dimension(140, 32)
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

