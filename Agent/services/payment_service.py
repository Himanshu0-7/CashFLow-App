import html

class PaymentService:
    def create(self, payload):
        xml = f"""
             '<ENVELOPE>',
             '  <HEADER>',
             '    <TALLYREQUEST>Import Data</TALLYREQUEST>',
             '  </HEADER>',
             '  <BODY>',
             '    <IMPORTDATA>',
             '      <REQUESTDESC>',
             '        <REPORTNAME>Vouchers</REPORTNAME>',
             '        <STATICVARIABLES>',
             '          <SVCURRENTCOMPANY></SVCURRENTCOMPANY>',
             '        </STATICVARIABLES>',
             '      </REQUESTDESC>',
             '      <REQUESTDATA>'
             '        <TALLYMESSAGE xmlns:UDF="TallyUDF">',
             '          <VOUCHER VCHTYPE="Payment" ACTION="Create" OBJVIEW="Accounting Voucher View">',
             '          <VOUCHERTYPENAME>Payment</VOUCHERTYPENAME>',
             '            <DATE>{payload.date}</DATE>',
             '            <PARTYLEDGER>Party</PARTYLEDGER>',
             '            <Narration>{html.escape(payload.description)+html.escape(payload.remark)}</Narration>',


             '            <ALLLEDGERENTRIES.LIST>',
             '              <LEDGERNAME>{payload.bankName}</LEDGERNAME>',
             '              <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>',
             '              <AMOUNT>{payload.debit:.2f}</AMOUNT>',
             '            </ALLLEDGERENTRIES.LIST>',


             '            <ALLLEDGERENTRIES.LIST>',
             '              <LEDGERNAME>{html.escape(payload.partyName)}</LEDGERNAME>',
             '              <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>',
             '              <AMOUNT>-{payload.debi:.2f}</AMOUNT>',
             '            </ALLLEDGERENTRIES.LIST>',

             '          </VOUCHER>',
             '        </TALLYMESSAGE>'
             '      </REQUESTDATA>',
             '    </IMPORTDATA>',
             '  </BODY>',
             '</ENVELOPE>'
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
        VCHTYPE="Payment"
        ACTION="Alter"
        OBJVIEW="Accounting Voucher View">

      <DATE>{payload.date}</DATE>

      <VOUCHERNUMBER>{payload.voucherNumber}</VOUCHERNUMBER>

      <NARRATION>{payload.description}</NARRATION>

      ...

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
        VCHTYPE="Payment"
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
