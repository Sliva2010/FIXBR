package com.sliva2010.fixbr

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.sliva2010.fixbr.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isOptimized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkRootStatus()
    }

    private fun setupUI() {
        binding.optimizeBtn.setOnClickListener {
            val gamePackage = binding.packageInput.text?.toString()?.trim() ?: ""
            if (gamePackage.isEmpty()) {
                showSnackbar("Please enter a game package name")
                return@setOnClickListener
            }
            applyOptimization(gamePackage)
        }

        binding.restoreBtn.setOnClickListener {
            val gamePackage = binding.packageInput.text?.toString()?.trim() ?: ""
            if (gamePackage.isEmpty()) {
                showSnackbar("Please enter a game package name")
                return@setOnClickListener
            }
            restoreOptimization(gamePackage)
        }
    }

    private fun checkRootStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            val hasRoot = RootShell.checkRootAccess()
            withContext(Dispatchers.Main) {
                if (hasRoot) {
                    binding.rootStatusText.text = getString(R.string.root_granted)
                    binding.rootStatusText.setTextColor(getColor(android.R.color.holo_green_dark))
                } else {
                    binding.rootStatusText.text = getString(R.string.root_denied)
                    binding.rootStatusText.setTextColor(getColor(android.R.color.holo_red_dark))
                }
            }
        }
    }

    private fun applyOptimization(gamePackage: String) {
        binding.optimizeBtn.isEnabled = false
        binding.restoreBtn.isEnabled = false
        binding.statusText.text = getString(R.string.applying)

        CoroutineScope(Dispatchers.IO).launch {
            val result = PerformanceTweaks.applyTweaks(gamePackage)
            withContext(Dispatchers.Main) {
                isOptimized = true
                binding.statusText.text = getString(R.string.status_optimized)
                binding.statusText.setTextColor(getColor(android.R.color.holo_green_dark))
                binding.optimizeBtn.isEnabled = true
                binding.restoreBtn.isEnabled = true
                showSnackbar("Optimization applied for: $gamePackage")
            }
        }
    }

    private fun restoreOptimization(gamePackage: String) {
        binding.optimizeBtn.isEnabled = false
        binding.restoreBtn.isEnabled = false
        binding.statusText.text = getString(R.string.restoring)

        CoroutineScope(Dispatchers.IO).launch {
            val result = PerformanceTweaks.restoreDefaults(gamePackage)
            withContext(Dispatchers.Main) {
                isOptimized = false
                binding.statusText.text = getString(R.string.status_default)
                binding.statusText.setTextColor(getColor(android.R.color.holo_red_dark))
                binding.optimizeBtn.isEnabled = true
                binding.restoreBtn.isEnabled = true
                showSnackbar("Settings restored for: $gamePackage")
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
