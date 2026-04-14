package com.sliva2010.fixbr

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

/**
 * Utility class for executing shell commands with root (su) access.
 */
object RootShell {

    private var hasRootAccess = false

    /**
     * Checks if root access is available.
     */
    fun checkRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c id")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine()
            reader.close()
            process.waitFor()
            hasRootAccess = output != null && output.contains("uid=0")
            hasRootAccess
        } catch (e: Exception) {
            hasRootAccess = false
            false
        }
    }

    /**
     * Executes a shell command with root access.
     */
    fun execCommand(command: String): String {
        var process: Process? = null
        var os: DataOutputStream? = null
        var reader: BufferedReader? = null
        var errorReader: BufferedReader? = null
        var output = ""

        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            reader = BufferedReader(InputStreamReader(process.inputStream))
            errorReader = BufferedReader(InputStreamReader(process.errorStream))

            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()

            process.waitFor()

            val outputLine = reader.readLine()
            if (outputLine != null) {
                output = outputLine
            }

            val errorLine = errorReader.readLine()
            if (errorLine != null && process.exitValue() != 0) {
                output = "Error: $errorLine"
            }
        } catch (e: IOException) {
            output = "IOException: ${e.message}"
        } catch (e: InterruptedException) {
            output = "InterruptedException: ${e.message}"
        } finally {
            try {
                os?.close()
                reader?.close()
                errorReader?.close()
            } catch (e: IOException) {
                // Ignore
            }
        }

        return output
    }

    /**
     * Executes multiple shell commands in a single root session.
     */
    fun execCommands(commands: List<String>): String {
        var process: Process? = null
        var os: DataOutputStream? = null
        var reader: BufferedReader? = null
        var errorReader: BufferedReader? = null
        val output = StringBuilder()

        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            reader = BufferedReader(InputStreamReader(process.inputStream))
            errorReader = BufferedReader(InputStreamReader(process.errorStream))

            for (command in commands) {
                os.writeBytes("$command\n")
                os.flush()
            }

            os.writeBytes("exit\n")
            os.flush()

            process.waitFor()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }

            var errorLine: String?
            while (errorReader.readLine().also { errorLine = it } != null) {
                output.append("Error: ").append(errorLine).append("\n")
            }
        } catch (e: IOException) {
            output.append("IOException: ${e.message}\n")
        } catch (e: InterruptedException) {
            output.append("InterruptedException: ${e.message}\n")
        } finally {
            try {
                os?.close()
                reader?.close()
                errorReader?.close()
            } catch (e: IOException) {
                // Ignore
            }
        }

        return output.toString()
    }

    fun hasRoot(): Boolean = hasRootAccess
}
