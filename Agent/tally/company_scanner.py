import os
import re

class CompanyScanner:

    def extract_company(self, company_file):

        with open(company_file, "rb") as f:
            data = f.read()

        text = data.decode("utf-16le", errors="ignore")

        matches = re.findall(
            r"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
            text
        )

        matches_name = re.findall(
            r"[A-Za-z][A-Za-z0-9 _.,&()\-]{5,}",
            text
        )

        guid = matches[0] if matches else None
        company_name = matches_name[0] if matches_name else None

        return guid, company_name

    def scan(self, root_folder):

        companies = {}

        for root, dirs, files in os.walk(root_folder):

            if "Company.1800" not in files:
                continue

            company_file = os.path.join(
                root,
                "Company.1800"
            )

            guid, company_name = self.extract_company(company_file)

            companies[guid] = {
                "company_name": company_name,
                "company_code": os.path.basename(root),
                "data_path": os.path.dirname(root)
            }

        return companies