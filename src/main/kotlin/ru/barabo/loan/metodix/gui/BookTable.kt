package ru.barabo.loan.metodix.gui

import ru.barabo.gui.swing.cross.CrossColumn
import ru.barabo.gui.swing.cross.CrossColumns
import ru.barabo.gui.swing.cross.CrossTable
import ru.barabo.loan.metodix.entity.BookForm
import ru.barabo.loan.metodix.service.BookForm1Service
import ru.barabo.loan.metodix.service.BookForm2Service
import ru.barabo.loan.metodix.service.yearDate
import java.sql.Timestamp
import java.time.format.DateTimeFormatter

private val yearFormatter = DateTimeFormatter.ofPattern("на MM.yyyy")

private val columnsBookForm = arrayOf(
    CrossColumn({ "Наименование" }, BookForm::name, 50),

    CrossColumn({ "Код" }, BookForm::code),

   // CrossColumn({ formatDateAdd(yearDate, -9) } , BookForm::valueMonthMinus9 ),

   // CrossColumn({ formatDateAdd(yearDate, -6) } , BookForm::valueMonthMinus6 ),

    CrossColumn({ formatDateAdd(yearDate, -3) } , BookForm::valueMonthMinus3 ),

    CrossColumn({ formatDateAdd(yearDate) } , BookForm::valueMonth1 ),

    CrossColumn({ formatDateAdd(yearDate, 3) }, BookForm::valueMonth4 ),

    CrossColumn({ formatDateAdd(yearDate, 6) }, BookForm::valueMonth7 ),

    CrossColumn({ formatDateAdd(yearDate, 9) }, BookForm::valueMonth10 ),

    CrossColumn({ formatDateAdd(yearDate, 12) }, BookForm::valueMonthPlus12 )
)

private val crossBookFormColumns = CrossColumns(2, true, columnsBookForm)

object TableBookForm1 : CrossTable<BookForm>( crossBookFormColumns, BookForm1Service) {
    const val NAME_FORM = "Баланс: Форма - 1"
}

object TableBookForm2 : CrossTable<BookForm>( crossBookFormColumns, BookForm2Service) {
    const val NAME_FORM = "Отчет о прибылях и убытках: Форма - 2"
}

fun formatDateAdd(yearDate: Timestamp, addMonth: Long = 0L): String =
    yearFormatter.format(yearDate.toLocalDateTime().plusMonths(addMonth) )

