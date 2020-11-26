package com.sth4nothing.bluetoothhid.ui.dashboard

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.sth4nothing.bluetoothhid.*
import com.sth4nothing.bluetoothhid.reports.KeyboardReport
import com.sth4nothing.bluetoothhid.reports.ScrollableTrackpadMouseReport
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min


class DashboardFragment : Fragment() {

    lateinit var dashboardViewModel: DashboardViewModel
    lateinit var parent: MainActivity
    lateinit var sharedPref: SharedPreferences

    lateinit var leftClick: MaterialButton
    lateinit var middleClick: MaterialButton
    lateinit var rightClick: MaterialButton
    lateinit var clickInput: TextInputEditText

//    lateinit var execButton: FloatingActionButton
    lateinit var scriptInput: TextInputEditText

    var mouseReport = ScrollableTrackpadMouseReport()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parent = requireActivity() as MainActivity
        dashboardViewModel = ViewModelProvider(
            parent,
            ViewModelProvider.NewInstanceFactory()
        ).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
//        execButton = root.findViewById(R.id.execute_button)
        scriptInput = root.findViewById(R.id.script)
        dashboardViewModel.script.observe(viewLifecycleOwner, {
            if (scriptInput.text.toString() != it) {
                scriptInput.setText(it)
            }
        })
        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.dashboard_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when(item.itemId) {
            R.id.run_script -> {
                try {
                    execute(parseCommands(scriptInput.text.toString()))
                } catch (e: Exception) {
                    parent.toast(e.message ?: "error")
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onStart() {
        super.onStart()

//        execButton.setOnClickListener {
//            try {
//                execute(parseCommands(scriptInput.text.toString()))
//            } catch (e: Exception) {
//                parent.toast(e.message ?: "error")
//            }
//        }
        scriptInput.doAfterTextChanged {
            val script = it.toString()
            if (script != dashboardViewModel.script.value) {
                dashboardViewModel.script.postValue(script)
            }
        }
    }

    private fun execute(seq: Sequence<Pair<Int, ByteArray?>>, delay: Long = 50L) {
        if (parent.rKeyboardSender != null && parent.hidDevice != null && parent.host != null) {
            val hidDevice = parent.hidDevice!!
            val host = parent.host!!
            val thread = object : Thread() {
                override fun run() {
                    for (pair in seq) {
                        val (id, arr) = pair
                        if (arr == null) {
                            sleep(if (id != 0) id.toLong() else delay)
                        } else {
                            hidDevice.sendReport(host, id, arr)
                        }
                    }
                }
            }
            thread.start()
        }
    }

    private fun parseCommands(commands: String): Sequence<Pair<Int, ByteArray?>> =
        sequence {
            try {
                var sep = Separator.PLUS
                val separators = Separator.values().map { it.toChar() }
                val cmdReg = Regex(
                    "^\\s*(\\w+)[ \t]*(.*)[ \t]*$",
                    setOf(RegexOption.MULTILINE, RegexOption.UNIX_LINES)
                )
                val argsReg = Regex(
                    "\\s*(\".*?(?<!\\\\)\"|'(?:.|\\\\['\"\\\\])'|[^,\"']+)\\s*(?:(?<!\\\\),)?"
                )
                for (cmdMatch in cmdReg.findAll(commands)) {
                    val cmd = cmdMatch.groupValues[1]
                    val argsStr = cmdMatch.groupValues[2]
                    var args = argsReg.findAll(argsStr).map { it.groupValues[1] }.toList()
                    when (cmd) {
                        "click" -> {
                            args = args.take(2)
                            if (args.any { it.toIntOrNull() == null }) {
                                error(">>$commands<< 格式错误")
                            }
                            val nums = args.map { it.toInt() }
                            when (nums.size) {
                                0 -> yieldAll(click())
                                1 -> yieldAll(click(nums[0]))
                                else -> yieldAll(click(nums[0], nums[1]))
                            }
                        }

                        "move" -> {
                            args = args.take(2)
                            if (args.size < 2 || args.any { it.toIntOrNull() == null }) {
                                error(">>$commands<< 格式错误")
                            }

                            val nums = args.map { it.toInt() }
                            yieldAll(move(nums[0], nums[1]))
                        }
                        "scroll" -> {
                            args = args.take(2)
                            if (args.any { it.toIntOrNull() == null }) {
                                error(">>$commands<< 格式错误")
                            }

                            val nums = args.map { it.toInt() }
                            when (nums.size) {
                                0 -> yieldAll(scroll())
                                1 -> yieldAll(scroll(nums[0]))
                                else -> yieldAll(scroll(nums[0], nums[1]))
                            }
                        }
                        "input" -> {
                            if (args.isEmpty()) {
                                error(">>$commands<< 格式错误")
                            }
                            var chars = args.first()
                            if (chars.first() == '"' && chars.last() == '"' && chars.length >= 2) {
                                chars = Regex("\\\\(['\"\\\\])").replace(
                                    chars.substring(1, chars.lastIndex)
                                ) {
                                    it.groupValues[1]
                                }
                            }
                            if (chars.isNotEmpty()) {
                                yieldAll(input(chars))
                            }
                        }
                        "press" -> {
                            if (args.isEmpty()) {
                                error(">>$commands<< 格式错误")
                            }
                            var keyCombs = args.first()
                            if (keyCombs.first() == '"' && keyCombs.last() == '"' && keyCombs.length >= 2) {
                                keyCombs = Regex("\\\\(['\"\\\\])").replace(
                                    keyCombs.substring(1, keyCombs.lastIndex)
                                ) {
                                    it.groupValues[1]
                                }
                            }
                            if (keyCombs.isNotEmpty()) {
                                yieldAll(press(keyCombs, sep))
                            }
                        }
                        "sep" -> {
                            if (args.isEmpty()) {
                                error(">>$commands<< 格式错误")
                            }
                            val sepReg = Regex("^'?(\\\\['\"\\\\]|.)'?$")
                            val sepChar =
                                sepReg.matchEntire(args.first())?.groupValues?.get(1)?.first()
                            if (sepChar != null && sepChar in separators) {
                                sep = Separator.fromChar(sepChar)
                            } else {
                                error(">>$commands<< 分隔符错误")
                            }
                        }
                        "sleep" -> {
                            args = args.take(1)
                            if (args.any { it.toIntOrNull() == null }) {
                                error(">>$commands<< 格式错误")
                            }

                            val nums = args.map { it.toInt() }
                            when (nums.size) {
                                0 -> yield(Pair(0, null))
                                else -> yield(Pair(nums[0], null))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                parent.toast(e.message ?: "解析格式错误")
            }
        }

    private fun input(chars: String): Sequence<Pair<Int, ByteArray?>> =
        sequence {
            val id = KeyboardReport.ID
            val empty = Pair(id, ByteArray(KeyboardReport.size) { 0 })
            for (c in chars) {
                val key = mapKey(c)
                Log.i("key", c.toString())
                yield(Pair(id, key))
                yield(empty)
                yield(Pair(0, null))
            }
        }

    private fun click(
        times: Int = 1,
        button: Int = 0,
    ): Sequence<Pair<Int, ByteArray?>> =
        sequence {
            val id = ScrollableTrackpadMouseReport.ID
            val empty = Pair(id, ByteArray(mouseReport.bytes.size) { 0 })
            for (i in 1..times) {
                Log.i(
                    "click", when (button) {
                        0 -> "left"
                        1 -> "right"
                        2 -> "middle"
                        else -> "unknown: $button"
                    }
                )
                mouseReport.bytes[0] = when (button) {
                    0 -> 1
                    1 -> 2
                    2 -> 4
                    else -> 0
                }
                yield(Pair(id, mouseReport.bytes.clone()))
                mouseReport.bytes[0] = 0
                yield(empty)
                yield(Pair(0, null))
            }
        }

    private fun move(dx: Int, dy: Int): Sequence<Pair<Int, ByteArray?>> =
        sequence {
            val id = ScrollableTrackpadMouseReport.ID
            val dx_ = min(max(dx, -2047), 2047).toShort()
            val dy_ = min(max(dy, -2047), 2047).toShort()
            Log.i("move", "($dx_, $dy_)")

            val buff = ByteBuffer.wrap(ByteArray(4) { 0 })
            buff.putShort(dx_)
            buff.putShort(dy_)
            mouseReport.dxMsb = buff[0]
            mouseReport.dxLsb = buff[1]
            mouseReport.dyMsb = buff[2]
            mouseReport.dyLsb = buff[3]
            yield(Pair(id, mouseReport.bytes.clone()))
            mouseReport.dxLsb = 0
            mouseReport.dxMsb = 0
            mouseReport.dyLsb = 0
            mouseReport.dyMsb = 0
            yield(Pair(0, null))
        }

    private fun scroll(dy: Int = -5) = scroll(0, dy)

    private fun scroll(dx: Int, dy: Int): Sequence<Pair<Int, ByteArray?>> =
        sequence {
            val id = ScrollableTrackpadMouseReport.ID
            val dx_ = min(max(dx, -127), 127).toByte()
            val dy_ = min(max(dy, -127), 127).toByte()
            Log.i("scroll", "($dx_, $dy_)")
            mouseReport.hScroll = dx_
            mouseReport.vScroll = dy_
            yield(Pair(id, mouseReport.bytes.clone()))
            mouseReport.hScroll = 0
            mouseReport.vScroll = 0
            yield(Pair(0, null))
        }

    private fun press(
        keyCombs: String,
        sep: Separator = Separator.PLUS
    ): Sequence<Pair<Int, ByteArray?>> =
        sequence {
            val id = KeyboardReport.ID
            val empty = Pair(id, ByteArray(KeyboardReport.size) { 0 })
            val keyCombReg = Regex("(\\{.+?(?<!\\\\)\\}|\\\\.|[^{}\\\\+])")
            for (res in keyCombReg.findAll(keyCombs)) {
                Log.i("comb", res.value)
                yield(Pair(id, mapKey(res.value, sep)))
                yield(empty)
                yield(Pair(0, null))
            }
        }


    private fun mapKey(keyComb: String, sep: Separator = Separator.PLUS): ByteArray {
        assert(keyComb.length > 2 && keyComb.first() == '{' && keyComb.last() == '}')
        val report = KeyboardReport()
        val reg = Regex(
            "(?<!\\\\)" + when (sep) {
                Separator.PLUS -> "\\"
                else -> ""
            } + sep.value
        )
        val keys =
            reg.split(if (keyComb.length > 2) keyComb.substring(1, keyComb.length - 1) else keyComb)
        var key: Byte? = null
        keys.forEach {
            when (it) {
                "ctrl" -> report.leftControl = true
                "alt" -> report.leftAlt = true
                "shift" -> report.leftShift = true
                "win" -> report.leftGui = true
                else -> if (it.length == 1) {
                    key = KeyboardReport.KeyEventMap[it.first().toKeyCode()]?.toByte()
                } else if (it.length == 2 && it.first() == '\\') {
                    key = KeyboardReport.KeyEventMap[it.last().toKeyCode()]?.toByte()
                } else {
                    key = KeyboardReport.KeyEventMap[it.toKeyCode()]?.toByte()
                }
            }
        }
        report.key1 = key ?: 0
        return report.bytes
    }

    private fun mapKey(c: Char): ByteArray {
        val report = KeyboardReport()
        report.leftShift = c in 'A'..'Z' || c in "~!@#$%^&*()_+{}|:\"<>?"
        report.key1 = KeyboardReport.KeyEventMap[c.toKeyCode()]?.toByte() ?: 0

        return report.bytes
    }
}