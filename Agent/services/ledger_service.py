import html

class LedgerService:

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

     <LEDGER NAME="{html.escape(payload["ledgerName"])}" ACTION="Create">

      <NAME>{html.escape(payload["ledgerName"])}</NAME>

      <PARENT>{html.escape(payload["underGroup"])}</PARENT>

      <ISBILLWISEON>No</ISBILLWISEON>

      <ISCOSTCENTRESON>No</ISCOSTCENTRESON>

     </LEDGER>

    </TALLYMESSAGE>

   </REQUESTDATA>

  </IMPORTDATA>
 </BODY>
</ENVELOPE>
"""
        return xml

    def alter(self, payload):

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

     <LEDGER NAME="{html.escape(payload["ledgerName"])}" ACTION="Alter">

      <NAME>{html.escape(payload["ledgerName"])}</NAME>

      <PARENT>{html.escape(payload["underGroup"])}</PARENT>

      <ISBILLWISEON>No</ISBILLWISEON>

      <ISCOSTCENTRESON>No</ISCOSTCENTRESON>

     </LEDGER>

    </TALLYMESSAGE>

   </REQUESTDATA>

  </IMPORTDATA>
 </BODY>
</ENVELOPE>
"""
        return xml

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

     <LEDGER NAME="{html.escape(payload["ledgerName"])}" ACTION="Delete">
     </LEDGER>

    </TALLYMESSAGE>

   </REQUESTDATA>

  </IMPORTDATA>
 </BODY>
</ENVELOPE>
"""
        return xml
