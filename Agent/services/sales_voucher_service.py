import html

class SalesVoucherService:

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
        VCHTYPE="Sales"
        ACTION="Create"
        OBJVIEW="Accounting Voucher View">

      <VOUCHERTYPENAME>Sales</VOUCHERTYPENAME>

      <DATE>{payload.date}</DATE>

      <VOUCHERNUMBER>
       {payload.voucherNumber}
      </VOUCHERNUMBER>

      <PARTYLEDGERNAME>
       {html.escape(payload.partyName)}
      </PARTYLEDGERNAME>

      <REFERENCE>
       {html.escape(payload.referenceNo)}
      </REFERENCE>

      <ISINVOICE>Yes</ISINVOICE>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.partyName)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>

       <AMOUNT>-{payload.totalAmount}</AMOUNT>

      </ALLLEDGERENTRIES.LIST>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.salesLedger)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>

       <AMOUNT>{payload.salesAmount}</AMOUNT>

      </ALLLEDGERENTRIES.LIST>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.cgstLedger)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>

       <AMOUNT>{payload.cgstAmount}</AMOUNT>

      </ALLLEDGERENTRIES.LIST>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.sgstLedger)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>

       <AMOUNT>{payload.sgstAmount}</AMOUNT>

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

    xml = self.create(payload)

    return xml.replace(
        'ACTION="Create"',
        'ACTION="Alter"',
        1
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
    <REPORTNAME>Vouchers</REPORTNAME>
   </REQUESTDESC>

   <REQUESTDATA>

    <TALLYMESSAGE xmlns:UDF="TallyUDF">

     <VOUCHER
        VCHTYPE="Sales"
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