import org.apache.log4j.Logger
import org.junit.Test
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent

private val logger = Logger.getLogger("ScannerTest")!!


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