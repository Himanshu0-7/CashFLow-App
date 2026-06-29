package com.example.cashflow.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object TallySyncService {

    suspend fun fetchCompanyName(
        ip: String,
        port: Int
    ): String? = withContext(Dispatchers.IO) {

        try {

            val xml = """
                <ENVELOPE>
                    <HEADER>
                        <TALLYREQUEST>Export Data</TALLYREQUEST>
                    </HEADER>
                    <BODY>
                        <EXPORTDATA>
                            <REQUESTDESC>
                                <REPORTNAME>Day Book</REPORTNAME>
                            </REQUESTDESC>
                        </EXPORTDATA>
                    </BODY>
                </ENVELOPE>
            """.trimIndent()

            val conn = URL("http://$ip:$port")
                .openConnection() as HttpURLConnection

            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "text/xml")

            OutputStreamWriter(conn.outputStream).use {
                it.write(xml)
                it.flush()
            }

            val response = conn.inputStream
                .bufferedReader()
                .use { it.readText() }

            Regex("<REMOTECMPNAME>(.*?)</REMOTECMPNAME>")
                .find(response)
                ?.groupValues
                ?.get(1)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}