package ru.barabo.selector.service

import ru.barabo.afina.AfinaQuery
import ru.barabo.db.EditType
import ru.barabo.db.annotation.Filtered
import ru.barabo.db.service.StoreFilterService
import ru.barabo.db.service.StoreListener
import java.io.File
import java.nio.charset.Charset
import java.sql.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.thread
import kotlin.concurrent.timer
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType

class SqlFilterEntity<T: Any>(val filterEntity: T) : StoreListener<List<T>> {

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

object VerCheck {

    private val timer = timer(name = this.javaClass.simpleName, initialDelay = 30_000, daemon = false, period = 600_000) { checkRun() }

    @JvmStatic
    fun startCheck() {

        CheckerFiles.initStart()

        timer.apply {  }
    }

    private fun checkRun() {
        try {
            CheckerFiles.findProcess()
        } catch (e: Exception) {
            e.message
        }
    }

    @JvmStatic
    fun exitCheckVersion() {

        timer.cancel()
        timer.purge()
    }
}

fun Pattern.isFind(name: String): Boolean = this.matcher(name).matches()

object CheckerFiles : QuoteSeparatorLoader {

    private val pathVt = File(PATH_VT)

    private val patternVt = Pattern.compile(REGEXP_VT, Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)

    private val findFiles: MutableList<String> = ArrayList()

    fun initStart() {

        val files: List<String> = AfinaQuery.selectCursor(SEL_F).map { it[0] as String }

        findFiles.addAll( files )
    }

    fun findProcess() {

        if(findFiles.isEmpty()) return

        try {
            processNoError()
        } catch (e: Exception) {
            e.message
        }
    }

    private fun processNoError() {
        val newFiles = pathVt.listFiles { f ->
            (!f.isDirectory) &&
                    (patternVt.isFind(f.name)) &&
                    (!findFiles.contains(f.name.uppercase(Locale.getDefault())))
        }

        if (newFiles.isNullOrEmpty()) return

        for (newFile in newFiles) {
            val isExists = (AfinaQuery.selectValue(
                SELECT_CHECK_FILE,
                arrayOf(newFile.name.uppercase(Locale.getDefault()))
            ) as Number).toInt()

            if (isExists == 0) {
                processFileVt(newFile)
            }
            findFiles.add(newFile.name.uppercase(Locale.getDefault()))
        }
    }

    private lateinit var fileProcess: File

    private fun processFileVt(newFile: File) {
        fileProcess = newFile

        load(newFile, Charset.forName("CP1251"))
    }

    override val headerColumns: Map<Int, (String?) -> Any> = emptyMap()
    override val headerQuery: String? = null

    override val tailColumns: Map<Int, (String?) -> Any> = emptyMap()
    override val tailQuery: String? = null

    override val bodyColumns: Map<Int, (String?) -> Any> = mapOf(
        0 to ::parseToString,
        1 to ::parseNumberSeparator,
        2 to ::parseToUpperString,
        3 to ::parseToString,
        5 to ::parseNumberSeparator,
        -1 to ::fileProcessName,
        -2 to ::dateCreated
    )

    private fun dateCreated(@Suppress("UNUSED_PARAMETER") value: String?): Any = dateFile

    private fun fileProcessName(@Suppress("UNUSED_PARAMETER") value: String?): Any = fileProcess.name.uppercase(Locale.getDefault())

    private fun parseToUpperString(value: String?): Any =
        value?.trim()?.uppercase(Locale.getDefault()) ?: String::class.javaObjectType

    override val bodyQuery: String = INSERT_CLIENT

    private lateinit var dateFile: Timestamp

    override fun getTypeLine(fields: List<String>, order: Int): TypeLine {
        if (fields.isEmpty()) return TypeLine.NOTHING

        return when (fields[0].uppercase(Locale.getDefault())) {
            "START" -> {
                dateFile = fields[1].toDate().toTimestamp()
                TypeLine.NOTHING
            }
            "END" -> TypeLine.NOTHING
            else -> TypeLine.BODY
        }
    }
}

fun parseNumberSeparator(value :String?): Any {
    val length = value?.trim()?.length?:0

    if(length == 0) return Double::class.javaObjectType

    val numberOrder = value?.replace("\\D+".toRegex(), "")

    return numberOrder?.trim()?.toLong()?:0
}

fun String.toDate(): LocalDate = LocalDate.parse(this, DateTimeFormatter.ofPattern("ddMMyyyy"))

fun LocalDate.toTimestamp(): Timestamp = Timestamp(this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() )

private const val PATH_VT = "H:/Dep_Buh/Зарплатный проект ВТБ/Исходящие файлы/Зарплата"

private const val REGEXP_VT = "Z_0000311595_\\d\\d\\d\\d\\d\\d\\d\\d_\\d\\d_\\d\\d\\.txt"

const val SELECT_CHECK_FILE = "select od.PTKB_PLASTIC_TURNOUT.checkFileExistsZil( ? ) from dual"

const val INSERT_CLIENT =
    "insert into od.ptkb_zil_client (ID, CODE_ID, AMOUNT, CLIENT, CODE_ID2, AMOUNT_HOLD, FILE_NAME, CREATED) values (classified.nextval, ?, ?, ?, ?, ?, ?, ?)"

const val SEL_F = "{ ? = call od.PTKB_PLASTIC_TURNOUT.getExistsFiles }"
