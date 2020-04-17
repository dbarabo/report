package ru.barabo.loan.metodix.gui

import org.jdesktop.swingx.JXHyperlink
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
import ru.barabo.gui.swing.*
import ru.barabo.gui.swing.cross.CrossColumns
import ru.barabo.loan.metodix.entity.ClientBook
import ru.barabo.loan.metodix.service.ClientBookService
import ru.barabo.loan.metodix.service.year
import ru.barabo.selector.entity.ClientWithAccount
import ru.barabo.selector.gui.TabClientWithAccount
import java.awt.GridBagLayout
import java.util.*
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JTable
import javax.swing.JToolBar

class ToolBarBook(private val crossColumnsList: List<CrossColumns<*>>, private val tables: List<JTable>) : JToolBar() {
    private val comboClient: JComboBox<ClientBook>

    private val itemsClient: Vector<ClientBook>

    private lateinit var saveButton: JButton

    init {
        layout = GridBagLayout()

        onOffButton("Только просмотр", TableBookForm1.crossColumns.isReadOnly) {

            var isEdit = false

            crossColumnsList.forEach {
                it.isReadOnly = !it.isReadOnly

                isEdit = isEdit || (!it.isReadOnly)
            }

            saveButton.isEnabled = isEdit
        }

        add(JButton("Сохранить", ResourcesManager.getIcon("save24")).apply {
            addActionListener {

                for(table in tables) {
                    try {
                        table.cellEditor.stopCellEditing()
                    } catch (e : Exception){}
                }
            }

            saveButton = this
        })

        saveButton.isEnabled = false

        comboBoxWithItems("Клиент", 0, ClientBookService.elemRoot(), 1).apply {

            AutoCompleteDecorator.decorate(first)

            first.selectedItem = ClientBookService.selectedEntity()

            first.addActionListener {
                ClientBookService.selectedRowIndex = first.selectedIndex
            }
        }.apply {
            comboClient = first
            itemsClient = second!!
        }

        comboBox("Год", 0, ClientBookService.yearBooks, 3).apply {

            selectedItem = year

            addActionListener {
                year = selectedItem as String
            }

            this.maximumRowCount = this.itemCount
        }

        add(JXHyperlink().apply {
            text = "Добавить нового клиента..."

            addActionListener {
                TabClientWithAccount.selectTab(this, ::processSelectNewClient)
            }
        })

        add(JButton("➜в Excel", ResourcesManager.getIcon("exportXLS24")).apply {
            addActionListener {

                processShowError {
                    ClientBookService.runReportBookForm(TableBookForm1.firstBook()!!.selectedIndex)
                }
            }
        })

        maxSpaceXConstraint(7)
    }

    private fun processSelectNewClient(newClient: ClientWithAccount?) {

        newClient?.let {
            val client = ClientBookService.addNewClient(it)

            itemsClient.removeAllElements()
            itemsClient.addAll(ClientBookService.elemRoot())

            comboClient.selectedItem = client
        }
    }
}

