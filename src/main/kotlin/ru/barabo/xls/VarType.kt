package ru.barabo.xls

enum class VarType(val sqlType: Int, val isEqualVal: (it1: Any, it2: Any)-> Boolean) {
    UNDEFINED(-1, {_, _ -> false }),
    INT(java.sql.Types.BIGINT, {it1, it2 ->  (it1 as Number).toLong() == (it2 as Number).toLong() } ),
    NUMBER(java.sql.Types.DOUBLE, {it1, it2 ->  (it1 as Number).toDouble() == (it2 as Number).toDouble() } ),
    VARCHAR(java.sql.Types.VARCHAR, {it1, it2 ->  it1.toString() == it2.toString() }),
    DATE(java.sql.Types.TIMESTAMP, {it1, it2 -> (it1 as java.util.Date).time == (it2 as java.util.Date).time }),
    RECORD(-1, {_, _ -> false }),
    CURSOR(-1, {_, _ -> false }),
    SQL_PROC(-1, {_, _ -> false });

    fun toSqlValueNull(): Any {
        return when(this) {
            INT -> Long::class.javaObjectType

            NUMBER -> Double::class.javaObjectType

            VARCHAR -> ""

            DATE -> java.time.LocalDateTime::class.javaObjectType

            else -> throw Exception("undefined type for null value")
        }
    }

    companion object {
        fun varTypeBySqlType(sqlType: Int): VarType {
           return values().firstOrNull { it.sqlType == sqlType } ?: varTypeBySqlTypeMore(sqlType)
        }

        private fun varTypeBySqlTypeMore(sqlType: Int): VarType {
            return when(sqlType) {
            java.sql.Types.BIT,
            java.sql.Types.TINYINT,
            java.sql.Types.SMALLINT,
            java.sql.Types.INTEGER -> INT

            java.sql.Types.NUMERIC,
            java.sql.Types.DECIMAL,
            java.sql.Types.FLOAT,
            java.sql.Types.REAL -> NUMBER

            java.sql.Types.CHAR -> VARCHAR

            else -> throw java.lang.Exception("not supported sqlType=$sqlType")
            }
        }
    }
}

fun Int.toSqlValueNull(): Any {
    return when(this) {
        java.sql.Types.INTEGER,
        java.sql.Types.SMALLINT,
        java.sql.Types.BIT,
        java.sql.Types.BIGINT -> Long::class.javaObjectType

        java.sql.Types.FLOAT,
        java.sql.Types.REAL,
        java.sql.Types.DOUBLE,
        java.sql.Types.NUMERIC,
        java.sql.Types.DECIMAL -> Double::class.javaObjectType

        java.sql.Types.CHAR,
        java.sql.Types.VARCHAR -> ""

        java.sql.Types.DATE,
        java.sql.Types.TIME,
        java.sql.Types.TIMESTAMP ->   java.time.LocalDateTime::class.javaObjectType

        else -> throw Exception("undefined type for null value")
    }
}

data class Var(var name: String, var result: VarResult) {

    private fun toSqlValue(columnName: String? = null): Any =
            result.value?.let { toSqlValueIt(columnName, it) } ?: result.type.toSqlValueNull()

    @Suppress("UNCHECKED_CAST")
    private fun toSqlValueIt(columnName: String?, itValue: Any): Any {
        return when(result.type) {
            VarType.INT -> (itValue as Number).toLong()
            VarType.NUMBER -> itValue
            VarType.VARCHAR -> itValue
            VarType.DATE -> itValue
            VarType.RECORD -> getRecordValue(columnName, itValue as Record)
            else -> throw Exception("undefined value for $name.$columnName")
        }
    }

    private fun getRecordValue(columnName: String?, value: Record): Any {

        val index = if(columnName == null) 0 else columnName.trim().toIntOrNull()

        return index?.let { value.columns[it].toSqlValue() } ?:
        value.columns.firstOrNull { it.name.equals(columnName, true) } ?:
        throw Exception("not found record $name.$columnName")
    }
}

enum class Oper {
    APPLY,
    SQL_EXEC,
    FUN,
    VAR
}

interface ReturnResult {
    fun getVar(): VarResult

    fun setVar(newVar: VarResult)

    fun getSqlValue(): Any
}

data class VarResult(var type: VarType = VarType.UNDEFINED, var value: Any? = null) : ReturnResult {

    companion object {
        val UNDEFINED = VarResult()
    }

    override fun getVar(): VarResult = this

    override fun setVar(newVar: VarResult) {
        this.type = newVar.type
        this.value = newVar.value
    }

    override fun getSqlValue(): Any {
        return value ?: type.toSqlValueNull()
    }
}

fun VarResult?.toBoolean(): Boolean {
    if(this?.value == null) return false

    return when(this.type) {
        VarType.UNDEFINED -> false
        VarType.INT -> (this.value as Number).toLong() != 0L
        VarType.NUMBER -> (this.value as Number).toDouble() != 0.0
        VarType.VARCHAR -> this.value.toString().isBlank()
        VarType.DATE -> true
        else -> false
    }
}

data class OperVar(val oper: Oper,
                   val info: String = "",
                   val vars: List<ReturnResult> = emptyList()  ) : ReturnResult {

    override fun getVar(): VarResult {
        val params = vars.map { it.getVar() }

        return oper(params)
    }

    override fun setVar(newVar: VarResult) {}

    override fun getSqlValue(): Any = getVar().getSqlValue()

    private fun oper(params: List<VarResult>): VarResult {

        return operations[oper]?.invoke(params, info) ?: throw Exception("operations for $oper not found")
    }
}

private val operations = mapOf<Oper, (List<VarResult>, String)->VarResult >(
    Oper.APPLY to ::apply,
    Oper.SQL_EXEC to ::sqlProcExec,
    Oper.FUN to ::funOper,
    Oper.VAR to ::varOper
)

private fun varOper(params: List<VarResult>, info: String): VarResult {
    return params[0]
}

private fun apply(params: List<VarResult>, info: String): VarResult {

    val result = params[1].getVar()

    if(result.type != VarType.RECORD) {
        params[0].setVar(result)
        return params[0]
    }

    (params[0].value as Record).setApply(result.value as Record)

    return params[0]
}

private fun sqlProcExec(params: List<VarResult>, info: String): VarResult {

    val querySession: QuerySession = params[0].value as? QuerySession ?: throw Exception("first param for sqlProcExec must be QuerySession")

    val outTypes =   params.filter { it.value == VarType.UNDEFINED}.map { it.type.sqlType }.toIntArray()

    val param: Array<Any?> = params.filter { it.value != VarType.UNDEFINED && it.type != VarType.SQL_PROC}.map {it.getVar().getSqlValue()}.toTypedArray()

    val outParams = querySession.query.execute(query = info, params = param,
          outParamTypes = outTypes, sessionSetting = querySession.sessionSetting)

    outParams?.withIndex()?.forEach {
        params[it.index + 1].setVar(VarResult(params[it.index + 1].type, it.value))
    }

  return VarResult.UNDEFINED
}

private fun funOper(params: List<VarResult>, info: String): VarResult =
        funMap[info.toUpperCase()]?.invoke(params) ?: throw Exception("fun for $info not found")

private val funMap = mapOf<String, (List<VarResult>)->VarResult> (
        "OUT" to ::outFun,
        "EQUAL" to ::equalFun,
        "NOTEQUAL" to ::notEqualFun,
        "NOT" to ::notFun,
        "AND" to ::andFun,
        "OR" to ::orFun
)

private fun outFun(params: List<VarResult>): VarResult = params[0].apply { this.value = VarType.UNDEFINED }

private fun notEqualFun(params: List<VarResult>): VarResult = VarResult( VarType.INT, if(params[0].value != params[1].value)1 else 0)

private fun equalFun(params: List<VarResult>): VarResult = VarResult( VarType.INT, if(params[0].value == params[1].value)1 else 0)

private fun notFun(params: List<VarResult>): VarResult = VarResult( VarType.INT, if(toSign(params[0]) == 0) 1 else 0)

private fun andFun(params: List<VarResult>): VarResult = VarResult( VarType.INT, toSign(params[0]) and toSign(params[1]))

private fun orFun(params: List<VarResult>): VarResult = VarResult( VarType.INT, toSign(params[0]) or toSign(params[1]))

private fun toSign(vars: VarResult): Int {

    if(vars.value == null) return 0

    return when(vars.type) {
    VarType.INT,
    VarType.NUMBER -> if((vars.value as? Number)?.toInt()?:0 == 0) 0 else 1

    VarType.VARCHAR -> if("" == vars.value) 0 else 1

    VarType.DATE -> 1
    else -> throw Exception("do not to Int type ${vars.type}")
    }
}