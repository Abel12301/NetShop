// QRScanActivity.kt
package com.cibertec.pe.netshop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class QRScanActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val integrator = IntentIntegrator(this)
        integrator.setPrompt("Escanea el código QR")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(true)
        integrator.setBarcodeImageEnabled(true)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val intent = Intent()
                intent.putExtra("qr_result", result.contents)
                setResult(Activity.RESULT_OK, intent)
            } else {
                setResult(Activity.RESULT_CANCELED) // 👈 importante
            }
            finish()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }}
