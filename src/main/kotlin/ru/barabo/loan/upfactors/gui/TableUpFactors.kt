package ru.barabo.loan.upfactors.gui

import org.slf4j.LoggerFactory
import ru.barabo.gui.swing.cross.CrossColumn
import ru.barabo.gui.swing.cross.CrossColumns
import ru.barabo.gui.swing.cross.CrossTable
import ru.barabo.gui.swing.processShowError
import ru.barabo.loan.quality.gui.RemarkEditor
import ru.barabo.loan.quality.gui.remarkMonth
import ru.barabo.loan.quality.gui.valueMonth
import ru.barabo.loan.ratingactivity.gui.CheckBoxEditor
import ru.barabo.loan.ratingactivity.gui.CrossRendererAutoHeight
import ru.barabo.loan.upfactors.entity.UpFactors
import ru.barabo.loan.upfactors.service.UpFactorsService
import javax.swing.table.TableCellEditor
import kotlin.reflect.KMutableProperty1

private val logger = LoggerFactory.getLogger(TableUpFactors::class.java)

class TableUpFactors : CrossTable<UpFactors>( crossUpFactorsColumns, UpFactorsService,
    CrossRendererAutoHeight(crossUpFactorsColumns, UpFactorsService)
) {

    private val remarkEditor = RemarkEditor()

    private val checkBoxEditor = CheckBoxEditor()

    override fun getCellEditor(row: Int, column: Int): TableCellEditor {

        return if(column in listOf(1, 3, 5, 7)) checkBoxEditor
        else remarkEditor
    }

    fun pasteFromTemplate() {
        try {
            cellEditor.stopCellEditing()
        } catch (e: Exception){
            logger.error("pasteFromTemplate", e)
        }

        processShowError {

            if(selectedColumn !in listOf(3, 5, 7, 9)) throw Exception("Вставка из шаблона возможно только если текущий столбец Ремарка")

            val propColumn = crossColumns.columns[selectedColumn].prop as KMutableProperty1<UpFactors, Any?>

            UpFactorsService.pasteFromTemplate(propColumn)
        }
    }

    companion object {
        const val NAME = "Повышающие факторы"
    }
}

private val crossUpFactors = arrayOf(
    CrossColumn({ "Повышающий фактор" }, UpFactors::name, 40),

    //CrossColumn({ "Шаблон" }, UpFactors::template, 10),

    CrossColumn({ valueMonth() } , UpFactors::valueMonth1, 5 ),

    CrossColumn({ remarkMonth() } , UpFactors::remarkMonth1),

    CrossColumn({ valueMonth(3) }, UpFactors::valueMonth4, 5 ),

    CrossColumn({ remarkMonth( 3) }, UpFactors::remarkMonth4 ),

    CrossColumn({ valueMonth( 6) }, UpFactors::valueMonth7, 5 ),

    CrossColumn({ remarkMonth( 6) }, UpFactors::remarkMonth7 ),

    CrossColumn({ valueMonth(9) }, UpFactors::valueMonth10, 5 ),

    CrossColumn({ remarkMonth(9) }, UpFactors::remarkMonth10 )
)

val crossUpFactorsColumns = CrossColumns(1, true, crossUpFactors)