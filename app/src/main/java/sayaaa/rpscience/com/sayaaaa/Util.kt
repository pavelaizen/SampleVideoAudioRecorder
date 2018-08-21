package sayaaa.rpscience.com.sayaaaa

import kotlin.experimental.and

private val PSEUDO = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F")

fun ByteArray.toHexString(): String {
    var ch: Byte
    var i = 0
    if (this.isEmpty()) {
        return ""
    }
    val out_str_buf = StringBuffer(size * 3)
    while (i < size) {
        ch = (this[i] and 0xF0.toByte()).toByte() // Strip off high nibble
        ch = ch.toInt().ushr(4).toByte()     // shift the bits down
        ch = (ch and 0x0F).toByte()    // must do this is high order bit is on!
        out_str_buf.append(PSEUDO[ch.toInt()]) // convert the nibble to a String Character
        ch = (this[i] and 0x0F).toByte() // Strip off low nibble
        out_str_buf.append(PSEUDO[ch.toInt()]) // convert the nibble to a String Character
        out_str_buf.append(" ")
        i++
    }
    return String(out_str_buf)
}