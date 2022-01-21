package ru.barabo.selector.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.annotation.QuerySelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.selector.entity.ClientWithAccount

object ClientWithAccountService : StoreFilterService<ClientWithAccount>(AfinaOrm, ClientWithAccount::class.java),
    ParamsSelect, QuerySelect {

    lateinit var filter: SqlFilterEntity<ClientWithAccount>

    init {
        checkInitFilter().initStoreChecker(this)
    }

    private fun checkInitFilter(): SqlFilterEntity<ClientWithAccount> {
        if(!(::filter.isInitialized)) {

            filter = SqlFilterEntity( ClientWithAccount() )
        }

        return filter
    }

    override fun selectParams(): Array<Any?> = checkInitFilter().getSqlParams()

    override fun selectQuery(): String = "{ ? = call OD.PTKB_LOAN_METHOD_JUR.getClientJurWithAccountOpen( ? ) }"
}