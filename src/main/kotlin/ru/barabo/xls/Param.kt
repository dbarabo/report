package ru.barabo.xls

import org.jdesktop.swingx.JXDatePicker
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
import ru.barabo.db.toSqlDate
import ru.barabo.gui.swing.maxSpaceYConstraint
import ru.barabo.gui.swing.processShowError
import java.awt.Container
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.sql.Timestamp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.time.ZoneId
import java.util.*
import javax.swing.*
import javax.swing.text.NumberFormatter

data class Param(val componentType: ComponentType,
                 val varParam: Var,
                 val cursor: CursorData? = null)

enum class ComponentType(val countParam: Int) {
    TEXTFIELD(1),
    TEXTFIELDINT(1),
    TEXTFIELDAMOUNT(1),
    DATEPICKER(1),
    DATETIMEPICKER(1),
    CHECKBOX(1),
    COMBOBOX(2),
    COMBOSEARCH(2),
    TABLEBOX(2)
}

fun paramFunByName(funName: String): ComponentType? = ComponentType.values().firstOrNull { it.name == funName }

// private val logger = LoggerFactory.getLogger(Param::class.java)

fun buildParams(paramContainer: ParamContainer, params: List<Param>, processOk:()->Unit) {

    val container = paramContainer.container

    container.removeAll()
    container.revalidate()
    container.parent.revalidate()
    container.repaint()

    container.layout = GridBagLayout()

    for( (index, param) in params.withIndex()) {
        when(param.componentType) {
            ComponentType.TEXTFIELD -> container.textField(param.varParam, index)
            ComponentType.TEXTFIELDINT -> container.textFieldInt(param.varParam, index)
            ComponentType.TEXTFIELDAMOUNT -> container.textFieldAmount(param.varParam, index)
            ComponentType.DATEPICKER -> container.datePicker(param.varParam, index)
            ComponentType.DATETIMEPICKER -> container.dateTimePicker(param.varParam, index)
            ComponentType.CHECKBOX -> container.checkBox(param.varParam, index)
            ComponentType.COMBOBOX -> container.comboBox(param.varParam, param.cursor!!, index)
            ComponentType.COMBOSEARCH -> container.comboSearch(param.varParam, param.cursor!!, index)
            // ComponentType.TABLEBOX ->
            else -> throw Exception("component not found for componentType=${param.componentType}")
        }
    }

    container.add(JButton("Ok").apply {
        addActionListener {
            processShowError {
                paramContainer.checkRunReport()

                processOk()
            }
        }
    }, labelConstraint(params.size) )

    container.maxSpaceYConstraint(params.size + 1)

    container.revalidate()
    container.parent.revalidate()
    container.repaint()

    paramContainer.afterParamCreate()
}

private fun Container.datePicker(varParam: Var, gridY: Int): JXDatePicker {

    val label = varParam.name.replace('_', ' ').toLowerCase()

    add( JLabel(label), labelConstraint(gridY) )

    val datePicker = JXDatePicker().apply { this.date = varParam.result.value as? Date}

    datePicker.addActionListener { varResultDateListener(varParam.result, datePicker) }

    datePicker.editor.addFocusListener(object : FocusListener {
        override fun focusLost(e: FocusEvent?) {
            varResultDateListener(varParam.result, datePicker)
        }

        override fun focusGained(e: FocusEvent?) {}
    })

    this.add(datePicker, textConstraint(gridY = gridY, gridX = 1) )

    return datePicker
}

private fun Container.dateTimePicker(varParam: Var, gridY: Int): JXDatePicker {

    val label = varParam.name.replace('_', ' ').toLowerCase()

    add( JLabel(label), labelConstraint(gridY) )

    val dateTimePicker = JXDatePicker().apply { this.date = varParam.result.value as? Date}

    dateTimePicker.setFormats("dd.MM.yyyy HH:mm:ss")

    dateTimePicker.addActionListener { varResultDateTimeListener(varParam.result, dateTimePicker) }

    dateTimePicker.editor.addFocusListener(object : FocusListener {
        override fun focusLost(e: FocusEvent?) {
            varResultDateTimeListener(varParam.result, dateTimePicker)
        }

        override fun focusGained(e: FocusEvent?) {}
    })

    this.add(dateTimePicker, textConstraint(gridY = gridY, gridX = 1) )

    return dateTimePicker
}

private fun Container.comboBox(varParam: Var, cursor: CursorData, gridY: Int): JComboBox<ComboArray> {

    val comboData = Vector(cursor.data.map { ComboArray(it) }.toMutableList())

    val combo = JComboBox(comboData)

    val label = varParam.name.replace('_', ' ').toLowerCase()

    add( JLabel(label), labelConstraint(gridY) )

    this.add(combo, textConstraint(gridY = gridY, gridX = 1) )

    cursor.findRowByRecord(varParam.result.value as Record)?.let {
        combo.selectedIndex = it

        cursor.setRecordByRow(varParam.result.value as Record, combo.selectedIndex)
    }

    combo.addActionListener {
        cursor.setRecordByRow(varParam.result.value as Record, combo.selectedIndex)
    }

    if(comboData.size > 8) {
        combo.maximumRowCount = if(comboData.size > 12) 12 else comboData.size
    }

    return combo
}

private fun Container.comboSearch(varParam: Var, cursor: CursorData, gridY: Int): JComboBox<ComboArray> {
    val comboData = Vector(cursor.data.map { ComboArray(it) }.toMutableList())

    val combo = JComboBox(comboData)

    AutoCompleteDecorator.decorate(combo)

    val label = varParam.name.replace('_', ' ').toLowerCase()

    add( JLabel(label), labelConstraint(gridY) )

    this.add(combo, textConstraint(gridY = gridY, gridX = 1) )

    cursor.findRowByRecord(varParam.result.value as Record)?.let {
        combo.selectedIndex = it

        cursor.setRecordByRow(varParam.result.value as Record, combo.selectedIndex)
    }

    combo.addActionListener {
        cursor.setRecordByRow(varParam.result.value as Record, combo.selectedIndex)
    }

    if(comboData.size > 8) {
        combo.maximumRowCount = if(comboData.size > 12) 12 else comboData.size
    }

    return combo
}

private class ComboArray(val item: Array<Any?>) {
    override fun toString(): String = if(item.isEmpty() || item[0] == null)"" else item[0].toString()
}

private fun Container.checkBox(varParam: Var, gridY: Int): JCheckBox {
    val label = varParam.name.replace('_', ' ').toLowerCase()

    add( JLabel(label), labelConstraint(gridY) )

    val checkBox = JCheckBox("", varParam.result.toBoolean() )

    checkBox.addActionListener { varResultCheckOnOff(varParam.result) }

    this.add(checkBox, textConstraint(gridY = gridY, gridX = 1) )

    return checkBox
}

private fun Container.textFieldAmount(varParam: Var, gridY: Int): JTextField {
    val label = varParam.name.replace('_', ' ').toLowerCase()

    add( JLabel(label), labelConstraint(gridY) )

    val decimalFormat = DecimalFormat("#########.0#").apply {
        this.isGroupingUsed = false
        this.decimalFormatSymbols = DecimalFormatSymbols().apply {
            this.decimalSeparator = '.'
        }
    }

    val textField = JFormattedTextField( decimalFormat )

    textField.text = (varParam.result.value as? Number)?.toDouble()?.toString() ?: "0"

    this.add(textField, textConstraint(gridY = gridY, gridX = 1) )

    textField.addKeyListener(VarKeyLister(varParam.result) )

    textField.addFocusListener(object : FocusListener {
        override fun focusLost(e: FocusEvent?) {
            varResultTextFieldListener(varParam.result, textField)
        }

        override fun focusGained(e: FocusEvent?) {}
    })

    return textField
}

private fun Container.textFieldInt(varParam: Var, gridY: Int): JTextField {
    val label = varParam.name.replace('_', ' ').toLowerCase()

    add( JLabel(label), labelConstraint(gridY) )

    val textField = JFormattedTextField( integerFormat() )

    textField.text = (varParam.result.value as? Number)?.toInt()?.toString() ?: "0"

    this.add(textField, textConstraint(gridY = gridY, gridX = 1) )

    textField.addKeyListener(VarKeyLister(varParam.result) )

    textField.addFocusListener(object : FocusListener {
        override fun focusLost(e: FocusEvent?) {
            varResultTextFieldListener(varParam.result, textField)
        }

        override fun focusGained(e: FocusEvent?) {}
    })

    return textField
}

private fun integerFormat(): NumberFormatter {
    val format = NumberFormat.getInstance()
    format.isGroupingUsed = false //Remove comma from number greater than 4 digit

    return NumberFormatter(format).apply {
        // valueClass = Int::class.java
        minimum = Int.MIN_VALUE
        maximum = Int.MAX_VALUE
        allowsInvalid = false
        commitsOnValidEdit = true
    }
}

private fun Container.textField(varParam: Var, gridY: Int): JTextField {
    val label = varParam.name.replace('_', ' ').toLowerCase()

    add( JLabel(label), labelConstraint(gridY) )

    val textField = JTextField(varParam.result.value?.toString() ?: "")

    this.add(textField, textConstraint(gridY = gridY, gridX = 1) )

    textField.addKeyListener(VarKeyLister(varParam.result) )

    textField.addFocusListener(object : FocusListener {
        override fun focusLost(e: FocusEvent?) {
            varResultTextFieldListener(varParam.result, textField)
        }

        override fun focusGained(e: FocusEvent?) {}
    })

    return textField
}

class VarKeyLister(private val varResult: VarResult? = null, private val setter: (String?)->Unit = {}) : KeyListener {
    override fun keyTyped(e: KeyEvent?) {}

    override fun keyPressed(e: KeyEvent?) {}

    override fun keyReleased(e: KeyEvent?) {

        val textField = (e?.source as? JTextField) ?: return

        setter(textField.text)

        varResultTextFieldListener(varResult, textField)
    }
}

private fun varResultTextFieldListener(varResult: VarResult?, textField: JTextField) {
    val type = varResult?.type ?: return

    if(textField.text?.isEmpty() != false) {
        varResult.value = null
        return
    }

    when (type) {
        VarType.INT -> varResult.value = textField.text.trim().toIntOrNull()
        VarType.NUMBER -> varResult.value = textField.text.trim().toDoubleOrNull()
        VarType.VARCHAR -> varResult.value = textField.text
        else -> {}
    }
}

private fun varResultDateTimeListener(varResult: VarResult, datePicker: JXDatePicker) {

    datePicker.editor.commitEdit()

    varResult.value = if(datePicker.editor.value is Date)Timestamp((datePicker.editor.value as Date).time)
                        else Timestamp(datePicker.date.time)
}

private fun varResultDateListener(varResult: VarResult, datePicker: JXDatePicker) {

    datePicker.editor.commitEdit()

    val date = datePicker.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    varResult.value = Timestamp(date.toSqlDate().time)
}

private fun varResultCheckOnOff(varResult: VarResult) {
    varResult.value = if(varResult.toBoolean() ) 0 else 1
}

internal fun textConstraint(gridY: Int, height: Int = 1, gridX: Int = 0, width: Int = 1) =
        GridBagConstraints(gridX, gridY, width, height, 1.0, 0.6,
                GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL,
                Insets(5, 2, 5, 2), 0, 0)

internal fun labelConstraint(gridY: Int, gridX: Int = 0) =
        GridBagConstraints(gridX, gridY, 1, 1, 0.0, 0.0,
                GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL,
                Insets(5, 2, 5, 2), 0, 0)



