package ru.barabo.loan.metodix.gui

import org.jdesktop.swingx.JXHyperlink
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
import ru.barabo.gui.swing.comboBox
import ru.barabo.gui.swing.maxSpaceXConstraint
import ru.barabo.gui.swing.onOffButton
import ru.barabo.loan.metodix.service.ClientBookService
import ru.barabo.loan.metodix.service.year
import java.awt.GridBagLayout
import javax.swing.JToolBar

class ToolBarBook : JToolBar() {
    init {
        layout = GridBagLayout()

        onOffButton("Только просмотр", TableBookForm1.crossColumns.isReadOnly) {
            TableBookForm1.crossColumns.isReadOnly = !TableBookForm1.crossColumns.isReadOnly
        }

        comboBox("Клиент", 0, ClientBookService.elemRoot(), 1).apply {

            AutoCompleteDecorator.decorate(this)

            selectedItem = ClientBookService.selectedEntity()

            addActionListener {
                ClientBookService.selectedRowIndex = selectedIndex
            }
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
        })

        maxSpaceXConstraint(6)
    }
}