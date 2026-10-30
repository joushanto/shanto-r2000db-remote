package com.shanto.r2000dbremote

import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val ir = getSystemService(CONSUMER_IR_SERVICE) as? ConsumerIrManager
        val available = ir?.hasIrEmitter() == true
        val status = findViewById<TextView>(R.id.status)
        status.setText(if (available) R.string.ready else R.string.no_ir)
        // R2000DB / RC10D: NEC extended address 0xE710, command + complement.
        val buttons = mapOf(
            R.id.power to 0x00, R.id.mute to 0x01,
            R.id.volume_up to 0x09, R.id.volume_down to 0x0C,
            R.id.line1 to 0x0D, R.id.line2 to 0x16,
            R.id.opt to 0x0A, R.id.bluetooth to 0x15,
            R.id.eq_classic to 0x0E, R.id.eq_dynamic to 0x14
        )
        for ((id, command) in buttons) {
            findViewById<Button>(id).apply {
                isEnabled = available
                setOnClickListener {
                    try {
                        ir?.transmit(38028, InfraredProtocol.pattern(command))
                        status.text = getString(R.string.sent, text)
                    } catch (e: RuntimeException) {
                        status.setText(R.string.send_failed)
                        Toast.makeText(this@MainActivity, R.string.send_failed,
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
