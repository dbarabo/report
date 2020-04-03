package ru.barabo.loan.metodix.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.afina.AfinaQuery
import ru.barabo.db.service.StoreFilterService
import ru.barabo.loan.metodix.entity.ClientBook

object ClientBookService : StoreFilterService<ClientBook>(AfinaOrm, ClientBook::class.java) {

    val yearBooks: List<String>
        get() {
            if(::years.isInitialized) return years

            years = initYears()

            return years
        }

    private lateinit var years: List<String>

    private fun initYears(): List<String> = AfinaQuery.selectCursor(SELECT_YEARS).map { it[0] as String }

    private const val SELECT_YEARS = "{ ? = call od.PTKB_LOAN_METHOD_JUR.getYearsBookForm }"
}