package com.example.lab06

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.lab06.ui.theme.Lab06Theme
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureTimeMillis

const val N = 100

class MainActivity : ComponentActivity() {

    class Account {
        private var amount: Double = 0.0
        private val mutex = Mutex()

        suspend fun deposit(amount: Double) {
            mutex.withLock {
                val x = this.amount
                delay(1) // simulates processing time
                this.amount = x + amount
            }
        }

        fun saldo(): Double = amount
    }

    fun withTimeMeasurement(title: String, isActive: Boolean = true, code: () -> Unit) {
        if (!isActive) return
        val time = measureTimeMillis { code() }
        Log.i("MSU", "operation in '$title' took ${time} ms")
    }

    data class Saldos(val saldo1: Double, val saldo2: Double)

    suspend fun bankProcess(account: Account): Saldos {
        var saldo1: Double = 0.0
        var saldo2: Double = 0.0

        withTimeMeasurement("Single coroutine deposit $N times") {
            runBlocking {
                launch {
                    for (i in 1..N) account.deposit(0.0)
                }.join()
            }
            saldo1 = account.saldo()
        }

        withTimeMeasurement("Two $N times deposit coroutines together") {
            runBlocking {
                val job1 = launch {
                    for (i in 1..N) account.deposit(1.0)
                }
                val job2 = launch {
                    for (i in 1..N) account.deposit(1.0)
                }
                job1.join()
                job2.join()
            }
            saldo2 = account.saldo()
        }

        return Saldos(saldo1, saldo2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val account = Account()

        setContent {
            Lab06Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    var saldoResult by remember { mutableStateOf(Saldos(0.0, 0.0)) }

                    LaunchedEffect(Unit) {
                        saldoResult = bankProcess(account)
                    }

                    ShowResults(saldo1 = saldoResult.saldo1, saldo2 = saldoResult.saldo2)
                }
            }
        }
    }

    @Composable
    fun ShowResults(saldo1: Double, saldo2: Double) {
        Column {
            Text(text = "Saldo1: $saldo1")
            Text(text = "Saldo2: $saldo2")
        }
    }
}