import html

class ReceiptService:

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
        VCHTYPE="Receipt"
        ACTION="Create"
        OBJVIEW="Accounting Voucher View">

      <VOUCHERTYPENAME>Receipt</VOUCHERTYPENAME>

      <DATE>{payload.date}</DATE>

      <NARRATION>
       {html.escape(payload.description)}
       {html.escape(payload.remark)}
      </NARRATION>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.bankName)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>

       <AMOUNT>-{payload.credit:.2f}</AMOUNT>

      </ALLLEDGERENTRIES.LIST>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.partyName)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>

       <AMOUNT>{payload.credit:.2f}</AMOUNT>

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
        VCHTYPE="Receipt"
        ACTION="Alter"
        OBJVIEW="Accounting Voucher View">

      <DATE>{payload.date}</DATE>

      <VOUCHERNUMBER>
       {payload.voucherNumber}
      </VOUCHERNUMBER>

      <NARRATION>
       {html.escape(payload.description)}
       {html.escape(payload.remark)}
      </NARRATION>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.bankName)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>

       <AMOUNT>-{payload.credit:.2f}</AMOUNT>

      </ALLLEDGERENTRIES.LIST>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.partyName)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>

       <AMOUNT>{payload.credit:.2f}</AMOUNT>

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
        VCHTYPE="Receipt"
        ACTION="Delete">

      <DATE>{payload.date}</DATE>

      <VOUCHERNUMBER>
       {payload.voucherNumber}
      </VOUCHERNUMBER>

     </VOUCHER>

    </TALLYMESSAGE>

   </REQUESTDATA>

  </IMPORTDATA>

 </BODY>

</ENVELOPE>
"""

    return xml
