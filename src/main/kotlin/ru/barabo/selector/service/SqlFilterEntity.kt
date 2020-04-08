package ru.barabo.selector.service

import org.apache.log4j.Logger
import ru.barabo.db.EditType
import ru.barabo.db.annotation.Filtered
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import kotlin.concurrent.thread
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType

class SqlFilterEntity<T: Any>(val filterEntity: T) : StoreListener<List<T>> {

    private val logger = Logger.getLogger(SqlFilterEntity::class.java.name)

    private val filteredPairs = processAnnotation()

    @Volatile private var isRuningFilter: Boolean = false

    private var priorParams:  Array<Any?> = emptyArray()

    private lateinit var store: StoreFilterService<T>

    private var isCheckedFilter: (T)->Boolean = {true}

    fun initStoreChecker(store: StoreFilterService<T>, isCheckedFilter: (T)->Boolean = {true}) {
        this.store = store

        this.isCheckedFilter = isCheckedFilter

        store.addListener(this)
    }

    fun getSqlParams(): Array<Any?> = filteredPairs.map { it.valueToSql(filterEntity) }.toTypedArray()

    fun applyFilter(): Boolean {
        if(!::store.isInitialized) return false

        if(isRuningFilter) return false

        val newParams = getSqlParams()

        logger.error("SqlFilterEntity.getSqlParams=$newParams")
        logger.error("SqlFilterEntity.getSqlParams=$priorParams")

        if(newParams.contentEquals(priorParams) ) return false

        if(!isCheckedFilter(filterEntity)) return false

        return if(!isRuningFilter) {
            isRuningFilter = true

            priorParams = newParams

            thread {
                run {
                    store.initData()
                }
            }

            true
        } else false
    }

    override fun refreshAll(elemRoot: List<T>, refreshType: EditType) {
        if(refreshType != EditType.INIT) return

        isRuningFilter = false

        applyFilter()
    }

    private fun processAnnotation(): List<KMutableProperty<*>> {
        val list = filterEntity::class.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
            .filter { it.findAnnotation<Filtered>() != null }
            .map { Triple(it, it.findAnnotation<Filtered>()!!.posFrom, it.findAnnotation<Filtered>()!!.posTo) }
            .sortedWith( compareBy { it.second } )


        val result = ArrayList<KMutableProperty<*>>()

        for(item in list) {
            for(index in item.second..item.third ) {
                result += item.first
            }
        }

        return result
    }

    private fun KMutableProperty<*>.valueToSql(entity: T): Any {

        val javaType :Class<*> = returnType.javaType as Class<*>

        return getter.call(entity) ?: javaType
    }
}