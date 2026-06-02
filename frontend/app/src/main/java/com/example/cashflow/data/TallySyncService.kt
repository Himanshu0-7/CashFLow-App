// data/TallySyncService.kt
package com.example.cashflow.data

import com.example.cashflow.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object TallySyncService {

    suspend fun isReachable(ip: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val conn = URL("http://$ip:$port").openConnection() as HttpURLConnection
            conn.connectTimeout = 2000
            conn.readTimeout = 2000
            conn.requestMethod = "GET"
            conn.connect()
            conn.disconnect()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun pushEntry(
        entry: TallyEntry,
        debitLedger: Ledger,
        creditLedger: Ledger,
        company: Company
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val xml = buildVoucherXml(entry, debitLedger, creditLedger, company.name)
            val conn = URL("http://${company.tallyIp}:${company.tallyPort}")
                .openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "text/xml")
            conn.outputStream.write(xml.toByteArray())
            conn.outputStream.flush()
            val code = conn.responseCode
            conn.disconnect()
            code == 200
        } catch (e: Exception) {
            false
        }
    }

    // ← now inside the object
    suspend fun fetchCompanies(ip: String, port: Int): List<String> = withContext(Dispatchers.IO) {
        try {
            val xml = """
                <ENVELOPE>
                  <HEADER><TALLYREQUEST>Export Data</TALLYREQUEST></HEADER>
                  <BODY>
                    <EXPORTDATA>
                      <REQUESTDESC>
                        <REPORTNAME>List of Companies</REPORTNAME>
                      </REQUESTDESC>
                    </EXPORTDATA>
                  </BODY>
                </ENVELOPE>""".trimIndent()

            val conn = URL("http://$ip:$port").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.setRequestProperty("Content-Type", "text/xml")
            conn.outputStream.write(xml.toByteArray())
            conn.outputStream.flush()

            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val regex = Regex("<COMPANY>(.*?)</COMPANY>")
            regex.findAll(response).map { it.groupValues[1].trim() }.toList()

        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun buildVoucherXml(
        entry: TallyEntry,
        debitLedger: Ledger,
        creditLedger: Ledger,
        companyName: String
    ): String {
        val date = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
            .format(java.util.Date(entry.date))
        return """
<ENVELOPE>
  <HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER>
  <BODY>
    <IMPORTDATA>
      <REQUESTDESC><REPORTNAME>Vouchers</REPORTNAME>
        <STATICVARIABLES><SVCURRENTCOMPANY>$companyName</SVCURRENTCOMPANY></STATICVARIABLES>
      </REQUESTDESC>
      <REQUESTDATA>
        <TALLYMESSAGE xmlns:UDF="TallyUDF">
          <VOUCHER VCHTYPE="${entry.voucherType}" ACTION="Create">
            <DATE>$date</DATE>
            <VOUCHERTYPENAME>${entry.voucherType}</VOUCHERTYPENAME>
            <NARRATION>${entry.narration}</NARRATION>
            <ALLLEDGERENTRIES.LIST>
              <LEDGERNAME>${debitLedger.name}</LEDGERNAME>
              <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>
              <AMOUNT>-${entry.amount}</AMOUNT>
            </ALLLEDGERENTRIES.LIST>
            <ALLLEDGERENTRIES.LIST>
              <LEDGERNAME>${creditLedger.name}</LEDGERNAME>
              <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>
              <AMOUNT>${entry.amount}</AMOUNT>
            </ALLLEDGERENTRIES.LIST>
          </VOUCHER>
        </TALLYMESSAGE>
      </REQUESTDATA>
    </IMPORTDATA>
  </BODY>
</ENVELOPE>""".trimIndent()
    }
}  // ← single closing brace for the object