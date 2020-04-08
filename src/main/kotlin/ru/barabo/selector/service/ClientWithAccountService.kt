package ru.barabo.selector.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.db.annotation.ParamsSelect
import ru.barabo.db.annotation.QuerySelect
import ru.barabo.db.service.StoreFilterService
import ru.barabo.selector.entity.ClientWithAccount

object ClientWithAccountService : StoreFilterService<ClientWithAccount>(AfinaOrm, ClientWithAccount::class.java),
    ParamsSelect, QuerySelect {

    val filter = SqlFilterEntity( ClientWithAccount() )

    init {
        filter.initStoreChecker(this)
    }

    override fun selectParams(): Array<Any?>? = filter.getSqlParams()

    override fun selectQuery(): String = "{ ? = call OD.PTKB_LOAN_METHOD_JUR.getClientJurWithAccountOpen( ? ) }"


}