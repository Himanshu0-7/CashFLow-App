import requests

TALLY_URL = "http://localhost:9000"

class TallyClient:
    def send(self, xml):
        return requests.post(
            TALLY_URL,
            data=xml.encode("utf-8"),
            timeout=10
        )