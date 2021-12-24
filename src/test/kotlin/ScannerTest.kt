import org.apache.poi.ss.formula.ptg.Ref3DPxg
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import java.awt.KeyEventDispatcher
import java.awt.event.KeyEvent
import java.io.FileInputStream


private val logger = LoggerFactory.getLogger("ScannerTest")!!

class ScannerTest {

    //@Test
    fun firstTest0() {
        val ed = "050771.*ED....89.*".indexOf("ED", ignoreCase = true)
        logger.error("050771.*ED....89.*".substring(ed + 6..7 + ed) )
    }

    //@Test
    fun externalLinks() {

        val listWorkBooks = ArrayList<String>()

        val (parent, list, newFile) = workLinks("", "C:/app/План_2021.xlsx")
        listWorkBooks += newFile

        listWorkBooks.addRecurse(parent, list, "ПЛАН")

        logger.error("\n\n\n")

        listWorkBooks.sort()

        listWorkBooks.forEach { logger.error(it) }

        logger.error("\n\n\nBLACK\n\n\n")

        parentBlackList.forEach { logger.error(it) }
    }

    private val checkList = ArrayList<String>()

    private val parentBlackList = ArrayList<String>()

    private fun ArrayList<String>.addRecurse(parentDirectory: String, list: List<String>, parentFiles: String) {

        val check = "$parentDirectory/${list.joinToString()}"

        if(list.isEmpty() || checkList.contains(check)) return

        checkList += check

        for(work in list) {

            val (parent2, list2, newFile) = workLinks(parentDirectory, work)

            /*
            if(absentList.contains(newFile) && (!parentBlackList.contains(parentFiles))) {
                parentBlackList += parentFiles
            }*/

            if(blackParent3/*1*/.contains(newFile) && (!parentBlackList.contains(parentFiles))) {
                parentBlackList += parentFiles
            }

            if(!this.contains(newFile)) {
                this += newFile
            }

            this.addRecurse(parent2, list2, newFile)
        }
    }

    private fun workLinks(parentDirectory: String, link: String): Triple<String, List<String>, String> {

        //logger.error(link)
        val newLink = when {
            link.indexOf("file:///") == 0 -> link.substring(8)

            link.indexOf(':') == 1 -> link

            link.indexOf("/Users") == 0-> {
                //logger.error("!!!!!!!$link")

                return Triple(parentDirectory, emptyList(), link)
            }

            link.indexOf("/Аналитика") == 0 -> "T:$link"

            else -> "$parentDirectory/$link"
        }


        val  newLink2 = newLink.replace("%20", " ")
        //logger.error(newLink2)

        if(newLink2 in absentList ) {
            logger.error(">>>>!!!!!!!$newLink2")

            return Triple(parentDirectory, emptyList(), newLink2)
        }

        val newParentDirectory = newLink2.substringBeforeLast('/')
        //logger.error("parent=$newParentDirectory")

        val fileStream = FileInputStream(newLink2)

        return try {
            fileStream.use { stream ->
                val workbook: XSSFWorkbook = WorkbookFactory.create(stream) as XSSFWorkbook

                workbook.use { w ->

                    Triple(newParentDirectory, w.externalLinksTable.map { it.linkedFileName }, newLink2)
                }
            }
        } catch (e: Exception) {
            logger.error("!!!!!!!!!!!!!!! $newLink2")

            Triple(newParentDirectory, emptyList(), newLink2)
        }
    }

    private val blackParent1 = listOf("T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2019 год/Планирование/От подразделений/ОВК/Финплан ОВК 2019.xlsx",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2018/Финансовый результат на 01.11.2018/Исполнение Финансового плана помесячно 2018г.xlsm",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2018/Финансовый результат на 01.12.2018/Исполнение Финансового плана помесячно 2018г.xlsm",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2019/Финансовый результат на 01.09.2019/Исполнение Финансового плана помесячно 2019г_август.xlsm",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2021 год/Подразделения/КД/План 2021_КД_вариант_2.xlsx",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2021 год/Подразделения/ДОК/План_2021_ДОК_вариант_2.xlsx",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2021 год/Подразделения/ДЭЛБ/План_2021_ДТТС_ДБО.xlsx",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2021 год/Подразделения/Вал.отдел/План_2021_ОВК_вариант_2.xlsx",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2020 год/Подразделения/ДОК/План_ДОК_в.xlsx",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2019/Финансовый результат на 01.01.2020/Исполнение Финансового плана помесячно 2019г_декабрь_СПОД.xlsm",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2021 год/Подразделения/КД/План 2021_КД_вариант_4.xlsx",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2021 год/Подразделения/ДБУиНО/План_2021_ДБУиНО_для варианта 6.xlsxрезультаты/2019/Финансовый результат на 01.09.2019/Рабочие файлы/Факт_январь.xlsx")

    private val blackParent2 = listOf(
    "T:/Аналитика/СМЕТА КАПИТАЛЬНЫХ ЗАТРАТ И РАСХОДОВ/2019 ГОД/СМЕТА затрат на 2019 год.xls",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2019 год/ФИНПЛАН на 2019 год - ФР ДО по балансу.xls",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2021 год/План_2021_2023_вариант_2.xlsx",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2020 год/План_2020_в.xlsx",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2020 год/План_2020_Находка.xlsx",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2020 год/Фин.План_2020.xlsx")

    private val blackParent3 = listOf(
    "T:/Аналитика/СМЕТА КАПИТАЛЬНЫХ ЗАТРАТ И РАСХОДОВ/2020 ГОД/СМЕТА затрат на 2020 год.xls",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2019/Финансовый результат на 01.09.2019/Исполнение Финансового плана помесячно 2019г_август.xlsm",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2019 год/ФИНПЛАН на 2019 год - ФР ДО по балансу.xls",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2020/Финансовый результат на 01.01.2021/Исполнение Финансового плана помесячно 2020_декабрь.xlsm",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2019/Финансовый результат на 01.01.2020/Исполнение Финансового плана помесячно 2019г_декабрь_СПОД.xlsm",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2020 год/План_2020_в.xlsx",
    "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2020/Финансовый результат на 01.01.2021/Исполнение Финансового плана помесячно 2020_декабрь_СПОД.xlsm")

    private val absentList = listOf("T:/Аналитика/СМЕТА КАПИТАЛЬНЫХ ЗАТРАТ И РАСХОДОВ/2020 ГОД/ДБУиО/фин.план 2020 амортизация.xls",
        "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2019/Финансовый результат на 01.09.2019/Рабочие файлы/Факт_январь.xlsx",
        "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2019/Финансовый результат на 01.01.2020/Рабочие файлы/Факт_январь.xlsx",
        "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2019/Финансовый результат на 01.09.2019/Рабочие файлы/Факт_февраль.xlsx",
        "T:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2019/Финансовый результат на 01.01.2020/Рабочие файлы/Факт_февраль.xlsx",
        "C:/Аналитика/СМЕТА КАПИТАЛЬНЫХ ЗАТРАТ И РАСХОДОВ/2021 ГОД/СМЕТА затрат на 2021 год.xls",
        "C:/Users/uglanova/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/NKWE17BP/Подразделения/КД/План 2020_КД.xlsx",
        "C:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2020 год/План_2020.xlsx",
        "C:/Users/uglanova/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/NKWE17BP/План 2021 корректировка КД Угланова (2).xlsx",
        "C:/Users/nenasheva/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/1TPMXTHA/Подразделения/Казначейство/План_2021_Казначейство_вариант_5.xls",
        "C:/Users/nenasheva/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/1TPMXTHA/Подразделения/КД/План 2021_КД_вариант_4.xlsx",
        "C:/Users/nenasheva/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/1TPMXTHA/Подразделения/ДБУиНО/План_2021_ДБУиНО_для варианта 6.xlsx",
        "C:/Users/nenasheva/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/1TPMXTHA/Подразделения/ДОК/План_2021_ДОК_вариант_5.xlsx",
        "C:/Users/nenasheva/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/1TPMXTHA/Подразделения/ДЭЛБ/План_2021_ДТТС_ДБО.xlsx",
        "C:/Users/nenasheva/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/1TPMXTHA/Подразделения/ДЭЛБ/План_2021_ДЭЛБ_19.02.xlsx",
        "C:/Аналитика/СМЕТА КАПИТАЛЬНЫХ ЗАТРАТ И РАСХОДОВ/2021 ГОД/Электронный бизнес/РАЗРАБОТОЧНАЯ ТАБЛИЦА ДЛЯ ФИНПЛАНА 2021 ЭКВАЙРИНГ ФИНИШ 15.02.2021.xlsx",
        "C:/Users/nenasheva/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/1TPMXTHA/Подразделения/Вал.отдел/План_2021_ОВК_вариант_5.xlsx",
        "C:/Users/nenasheva/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/1TPMXTHA/Подразделения/КД/План 2020_КД.xlsx",
        "C:/Users/nenasheva/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/1TPMXTHA/План_2021_2023_вариант_2.xlsx",
        "C:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2020/Финансовый результат на 01.01.2021/Исполнение Финансового плана помесячно 2020_декабрь.xlsm",
        "C:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2020/Финансовый результат на 01.01.2021/Исполнение Финансового плана помесячно 2020_декабрь_СПОД.xlsm",
        "C:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2020/Финансовый результат на 01.01.2021/Рабочие файлы/Факт_декабрь_Нарастающий.xlsx",
        "C:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/финансовые  результаты/2020/Финансовый результат на 01.01.2021/Рабочие файлы/Факт_СПОД.xlsx",
        "C:/Users/nenasheva/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/1TPMXTHA/Подразделения/Вал.отдел/План_2021_ОВК_вариант_1.xlsx",
        "C:/Users/uglanova/AppData/Local/Microsoft/Windows/Temporary Internet Files/Content.Outlook/NKWE17BP/Резервная копия План_2021_ДБУиНО_для бизнес плана.xlk",

        "C:/Users/venukov/AppData/Local/Temp/ФИНПЛАН на 2018 год - ФР ДО по балансу.xls",
        "C:/Documents and Settings/ezhkova/Local Settings/Temporary Internet Files/Content.Outlook/2U2272XS/Рабочие файлы/Расходы_август.xls",
        "C:/Аналитика/ПЛАНЫ И ОТЧЕТЫ/бизнес-план/2018 год/ФИНПЛАН на 2018 год - ФР ДО по балансу.xls",
        "C:/Documents and Settings/ezhkova/Local Settings/Temporary Internet Files/Content.Outlook/2U2272XS/Рабочие файлы/Доходы.xls",
        "C:/Documents and Settings/uglanova/Application Data/Microsoft/Excel/Доходы.xls",
        "C:/Documents and Settings/ezhkova/Local Settings/Temporary Internet Files/Content.Outlook/2U2272XS/Рабочие файлы/Расходы.xls")

    //@Test
    fun firstTest() {

        //val workbook: Workbook = WorkbookFactory.create(FileInputStream("C:/app/План_2021.xlsx"))

        val workbook: Workbook = WorkbookFactory.create(FileInputStream("C:/app/1/2/РАЗРАБОТОЧНАЯ ТАБЛИЦА ДЛЯ ФИНПЛАНА 2021 ЭКВАЙРИНГ ФИНИШ 19.02.2021.xlsx"))

        workbook.forceFormulaRecalculation = true

       // val formulaEvaluator = workbook.creationHelper.createFormulaEvaluator()

/*        val workbooks: HashMap<String, FormulaEvaluator> = HashMap()
       // formulaEvaluator.evaluateAll()
        //val refWorkbook = WorkbookFactory.create(FileInputStream("T:\\Аналитика\\ПЛАНЫ И ОТЧЕТЫ\\бизнес-план\\2021 год\\Подразделения\\Казначейство\\План_2021_Казначейство.xls"))
        //val evaluator1: FormulaEvaluator   = refWorkbook.creationHelper.createFormulaEvaluator()
       // workbooks["T:\\Аналитика\\ПЛАНЫ И ОТЧЕТЫ\\бизнес-план\\2021 год\\Подразделения\\Казначейство\\План_2021_Казначейство.xls"] = evaluator1

        val k2 = "T:\\Аналитика\\ПЛАНЫ И ОТЧЕТЫ\\бизнес-план\\2021 год\\Подразделения\\Казначейство\\План_2021_Казначейство_вариант_5.xls"
        //val k02 = "T:/Аналитика/ПЛАНЫ%20И%20ОТЧЕТЫ/бизнес-план/2021%20год/Подразделения/Казначейство/План_2021_Казначейство_вариант_5.xls"
        val refWorkbook2 = WorkbookFactory.create(FileInputStream(k2))
        val evaluator2: FormulaEvaluator   = refWorkbook2.creationHelper.createFormulaEvaluator()
        workbooks[k2] = evaluator2

        formulaEvaluator.setupReferencedWorkbooks(workbooks)

        try {
        evaluator2.evaluateAll()
        //formulaEvaluator.evaluateAll()
        } catch (e: Exception) {
            logger.error("evaluateAll", e)
        }
*/
        //evaluateAllFormulaCells(refWorkbook2, formulaEvaluator)

       val listWorkBooks = ArrayList<String>()

       val evalWorkbook = XSSFEvaluationWorkbook.create(workbook as XSSFWorkbook)
       for(sheetIndex in 0 until workbook.numberOfSheets) {
           val sheet: Sheet = workbook.getSheetAt(sheetIndex)
           val evalSheet = evalWorkbook.getSheet(sheetIndex)

           for (row in sheet) {

               for (cell in row.cellIterator()) {
                   if(cell?.cellType != CellType.FORMULA) continue

                   val evaluationCell = evalSheet.getCell(cell.rowIndex, cell.columnIndex)

                   val infoFormula = evalWorkbook.getFormulaTokens(evaluationCell)

                   val refExt: Ref3DPxg = infoFormula.firstOrNull {it is Ref3DPxg} as? Ref3DPxg ?: continue

                   val externalSheet = evalWorkbook.getExternalSheet(refExt.sheetName, refExt.sheetName, refExt.externalWorkbookNumber)

                   val linkedFileName = externalSheet.workbookName

                   /*if(linkedFileName == null) {
                       logger.error("sheet=${sheet.sheetName} row=${cell.rowIndex} col=${cell.columnIndex}")
                   }*/

                   /*if( (linkedFileName?.indexOf("МСФО") ?: -1) >= 0) {
                       logger.error("sheet=${sheet.sheetName} row=${cell.rowIndex} col=${cell.columnIndex}")
                   }*/

                   if(!listWorkBooks.contains(linkedFileName)) {
                       listWorkBooks+= linkedFileName
                   }
               }
           }
       }

        listWorkBooks.forEach { logger.error(it) }
    }

    private fun evaluateAllFormulaCells(wb: Workbook, evaluator: FormulaEvaluator) {
        //for (i in 0 until wb.numberOfSheets) {
            val sheet = wb.getSheetAt(0)
            for (r in sheet) {
                for (c in r) {
                    if (c.cellType == CellType.FORMULA) {
                        evaluator.evaluateFormulaCell(c)
                    }
                }
            }
        //}
    }
}



class AltBugFixKeyEventDispatcher : KeyEventDispatcher {

    override fun dispatchKeyEvent(e: KeyEvent?): Boolean {
       // logger.error("e.isAltDown=${e?.isAltDown}")

      //  logger.error("e.getID=${e?.id}")

     //   logger.error("e.getKeyChar=${e?.keyChar}")

        return true
    }
}