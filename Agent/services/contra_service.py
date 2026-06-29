import html

class ContraService:
    def create(self, payload):

        xml = f"""
<ENVELOPE>
 <HEADER>
  <TALLYREQUEST>Import Data</TALLYREQUEST>
 </HEADER>

 <BODY>
  <IMPORTDATA>

   <REQUESTDESC>
    <REPORTNAME>Vouchers</REPORTNAME>
   </REQUESTDESC>

   <REQUESTDATA>

    <TALLYMESSAGE xmlns:UDF="TallyUDF">

     <VOUCHER
        VCHTYPE="Contra"
        ACTION="Create"
        OBJVIEW="Accounting Voucher View">

      <VOUCHERTYPENAME>Contra</VOUCHERTYPENAME>

      <DATE>{payload.date}</DATE>

      <NARRATION>
       {html.escape(payload.description)}
       {html.escape(payload.ref)}
       {html.escape(payload.remark)}
      </NARRATION>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.bankName)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>

       <AMOUNT>{payload.debit:.2f}</AMOUNT>

      </ALLLEDGERENTRIES.LIST>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.partyName)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>

       <AMOUNT>-{payload.debit:.2f}</AMOUNT>

      </ALLLEDGERENTRIES.LIST>

     </VOUCHER>

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
    <REPORTNAME>Vouchers</REPORTNAME>
   </REQUESTDESC>

   <REQUESTDATA>

    <TALLYMESSAGE xmlns:UDF="TallyUDF">

     <VOUCHER
        VCHTYPE="Contra"
        ACTION="Alter"
        OBJVIEW="Accounting Voucher View">

      <VOUCHERNUMBER>{payload.voucherNumber}</VOUCHERNUMBER>

      <DATE>{payload.date}</DATE>

      <NARRATION>
       {html.escape(payload.description)}
       {html.escape(payload.ref)}
       {html.escape(payload.remark)}
      </NARRATION>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>{html.escape(payload.bankName)}</LEDGERNAME>

       <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>

       <AMOUNT>{payload.debit:.2f}</AMOUNT>

      </ALLLEDGERENTRIES.LIST>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>{html.escape(payload.partyName)}</LEDGERNAME>

       <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>

       <AMOUNT>-{payload.debit:.2f}</AMOUNT>

      </ALLLEDGERENTRIES.LIST>

     </VOUCHER>

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
    <REPORTNAME>Vouchers</REPORTNAME>
   </REQUESTDESC>

   <REQUESTDATA>

    <TALLYMESSAGE xmlns:UDF="TallyUDF">

     <VOUCHER
        VCHTYPE="Contra"
        ACTION="Delete">

      <VOUCHERNUMBER>
       {payload.voucherNumber}
      </VOUCHERNUMBER>

      <DATE>
       {payload.date}
      </DATE>

     </VOUCHER>

    </TALLYMESSAGE>

   </REQUESTDATA>

  </IMPORTDATA>

 </BODY>

</ENVELOPE>
"""

    return xml
