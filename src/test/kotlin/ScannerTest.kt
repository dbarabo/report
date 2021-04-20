import org.junit.Test
import org.slf4j.LoggerFactory
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import java.io.File
import java.net.InetAddress

private val logger = LoggerFactory.getLogger("ScannerTest")!!

class ScannerTest {

    //@Test
    fun compList() {

        val ip = byteArrayOf(192.toByte(), 168.toByte(), 0, 0)

        for (tail in 1..255) {

            ip[3] = tail.toByte()

            val address = InetAddress.getByAddress(ip)

            val path = "//${address.hostName}/C$/Users/boicova"

            val file = File(path)

            if(file.exists()) {
                logger.error(path)
            } else {
                logger.error("NOT FOUND $path")
            }
        }
    }


    //@Test
    fun checkCountTest() {
        val count = "QWE\n\r1234\n\r5".count { it == '\n' }
        logger.error("count=$count")
    }

    //@Test
    fun testFormat() {
        //logger.error("%010d".format(10957))

        logger.error(10957.toString().padStart(10, '0') )
    }
}

//@Test
fun firstTest() {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
        AltBugFixKeyEventDispatcher())
}

class AltBugFixKeyEventDispatcher : KeyEventDispatcher {

    override fun dispatchKeyEvent(e: KeyEvent?): Boolean {
       // logger.error("e.isAltDown=${e?.isAltDown}")

      //  logger.error("e.getID=${e?.id}")

     //   logger.error("e.getKeyChar=${e?.keyChar}")

        return true
    }


}