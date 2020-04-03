package ru.barabo.loan.metodix.gui

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
import ru.barabo.gui.swing.comboBox
import ru.barabo.gui.swing.maxSpaceXConstraint
import ru.barabo.loan.metodix.service.BookForm1Service
import ru.barabo.loan.metodix.service.ClientBookService
import java.awt.GridBagLayout
import javax.swing.JToolBar

class ToolBarBook : JToolBar() {
    init {
        layout = GridBagLayout()

        comboBox("Клиент", 0, ClientBookService.elemRoot(), 0).apply {

            AutoCompleteDecorator.decorate(this)

            selectedItem = ClientBookService.selectedEntity()

            addActionListener {
                ClientBookService.selectedRowIndex = selectedIndex
            }
        }

        comboBox("Год", 0, ClientBookService.yearBooks, 2).apply {

            selectedItem = BookForm1Service.year

            addActionListener {
                BookForm1Service.year = selectedItem as String
            }
        }

        maxSpaceXConstraint(4)
    }
}