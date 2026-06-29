import html

class PurchaseMasterService:

    def create(self, payload):

        xml = f"""
<ENVELOPE>

 <HEADER>
  <TALLYREQUEST>Import Data</TALLYREQUEST>
 </HEADER>

 <BODY>

  <IMPORTDATA>

   <REQUESTDESC>
    <REPORTNAME>All Masters</REPORTNAME>
   </REQUESTDESC>

   <REQUESTDATA>

    <TALLYMESSAGE xmlns:UDF="TallyUDF">

     <LEDGER NAME="{html.escape(payload.partyName)}"
             ACTION="Create">

      <NAME>{html.escape(payload.partyName)}</NAME>

      <PARENT>Sundry Creditors</PARENT>

      <ISGSTAPPLICABLE>Yes</ISGSTAPPLICABLE>

     </LEDGER>

    </TALLYMESSAGE>

    <TALLYMESSAGE xmlns:UDF="TallyUDF">

     <LEDGER NAME="{html.escape(payload.purchaseLedger)}"
             ACTION="Create">

      <NAME>{html.escape(payload.purchaseLedger)}</NAME>

      <PARENT>Purchase Accounts</PARENT>

      <ISGSTAPPLICABLE>Yes</ISGSTAPPLICABLE>

     </LEDGER>

    </TALLYMESSAGE>

    <TALLYMESSAGE xmlns:UDF="TallyUDF">

     <LEDGER NAME="{html.escape(payload.cgstLedger)}"
             ACTION="Create">

      <NAME>{html.escape(payload.cgstLedger)}</NAME>

      <PARENT>Duties &amp; Taxes</PARENT>

      <ISGSTAPPLICABLE>Yes</ISGSTAPPLICABLE>

     </LEDGER>

    </TALLYMESSAGE>

    <TALLYMESSAGE xmlns:UDF="TallyUDF">

     <LEDGER NAME="{html.escape(payload.sgstLedger)}"
             ACTION="Create">

      <NAME>{html.escape(payload.sgstLedger)}</NAME>

      <PARENT>Duties &amp; Taxes</PARENT>

      <ISGSTAPPLICABLE>Yes</ISGSTAPPLICABLE>

     </LEDGER>

    </TALLYMESSAGE>

   </REQUESTDATA>

  </IMPORTDATA>

 </BODY>

</ENVELOPE>
"""

        return xml


    def alter(self, payload):

        xml = self.create(payload)

        return xml.replace(
            'ACTION="Create"',
            'ACTION="Alter"'
        )


    def delete(self, payload):

        xml = f"""
<ENVELOPE>

 <HEADER>
  <TALLYREQUEST>Import Data</TALLYREQUEST>
 </HEADER>

 <BODY>

  <IMPORTDATA>

   <REQUESTDESC>
    <REPORTNAME>All Masters</REPORTNAME>
   </REQUESTDESC>

   <REQUESTDATA>

    <TALLYMESSAGE xmlns:UDF="TallyUDF">

     <LEDGER NAME="{html.escape(payload.partyName)}"
             ACTION="Delete">
     </LEDGER>

    </TALLYMESSAGE>

    <TALLYMESSAGE xmlns:UDF="TallyUDF">

     <LEDGER NAME="{html.escape(payload.purchaseLedger)}"
             ACTION="Delete">
     </LEDGER>

    </TALLYMESSAGE>

    <TALLYMESSAGE xmlns:UDF="TallyUDF">

     <LEDGER NAME="{html.escape(payload.cgstLedger)}"
             ACTION="Delete">
     </LEDGER>

    </TALLYMESSAGE>

    <TALLYMESSAGE xmlns:UDF="TallyUDF">

     <LEDGER NAME="{html.escape(payload.sgstLedger)}"
             ACTION="Delete">
     </LEDGER>

    </TALLYMESSAGE>

   </REQUESTDATA>

  </IMPORTDATA>

 </BODY>

</ENVELOPE>
"""

        return xml