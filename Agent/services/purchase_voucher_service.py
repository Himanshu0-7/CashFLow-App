import html

class PurchaseVoucherService:

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
        VCHTYPE="Purchase"
        ACTION="Create"
        OBJVIEW="Accounting Voucher View">

      <VOUCHERTYPENAME>Purchase</VOUCHERTYPENAME>

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

       <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>

       <AMOUNT>{payload.totalAmount}</AMOUNT>

      </ALLLEDGERENTRIES.LIST>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.purchaseLedger)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>

       <AMOUNT>-{payload.purchaseAmount}</AMOUNT>

      </ALLLEDGERENTRIES.LIST>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.cgstLedger)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>

       <AMOUNT>-{payload.cgstAmount}</AMOUNT>

      </ALLLEDGERENTRIES.LIST>

      <ALLLEDGERENTRIES.LIST>

       <LEDGERNAME>
        {html.escape(payload.sgstLedger)}
       </LEDGERNAME>

       <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>

       <AMOUNT>-{payload.sgstAmount}</AMOUNT>

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
        VCHTYPE="Purchase"
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