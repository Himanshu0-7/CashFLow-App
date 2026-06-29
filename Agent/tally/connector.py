class Connector:

    def execute(self, company_manager, guid, action, payload):

        # Step 1
        company_manager.ensure_loaded(guid)

        # Step 2
        if action == "createLedger":
            xml = LedgerService().create(payload)

        # Step 3
        response = TallyClient().send(xml)

        return response