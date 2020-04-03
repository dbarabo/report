package ru.barabo.loan.metodix.gui

import ru.barabo.gui.swing.cross.CrossColumn
import ru.barabo.gui.swing.cross.CrossColumns
import ru.barabo.gui.swing.cross.CrossTable
import ru.barabo.loan.metodix.entity.BookForm
import ru.barabo.loan.metodix.service.BookForm1Service
import java.sql.Timestamp
import java.time.format.DateTimeFormatter

private val yearFormatter = DateTimeFormatter.ofPattern("на MM.yyyy")

object TableBookForm1 : CrossTable<BookForm>( CrossColumns(2, columnsBookForm1), BookForm1Service)

private val columnsBookForm1 = arrayOf(
    CrossColumn("Наименование", BookForm::name, 50),

    CrossColumn("Код", BookForm::code),

    CrossColumn(formatDateAdd(BookForm1Service.yearDate), BookForm::valueMonth1 ),

    CrossColumn(formatDateAdd(BookForm1Service.yearDate, 3), BookForm::valueMonth4 ),

    CrossColumn(formatDateAdd(BookForm1Service.yearDate, 6), BookForm::valueMonth7 ),

    CrossColumn(formatDateAdd(BookForm1Service.yearDate, 9), BookForm::valueMonth10 )
)

fun formatDateAdd(yearDate: Timestamp, addMonth: Long = 0L): String =
    yearFormatter.format(yearDate.toLocalDateTime().plusMonths(addMonth) )

