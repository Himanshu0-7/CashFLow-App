import subprocess
import time
import requests
import xml.etree.ElementTree as ET
from pywinauto import Application
import win32gui


# companies = {
#     "70648680-baa1-4aa0-aa68-9784b6043661": {
#         "company_name": "Rahul Dixena - (2025-26)",
#         "company_code": "010001",
#         "data_path": r"C:\Users\himan\Downloads\DATA_25-26",
#     }
# }

class CompanyManager:

    def __init__(
        self,
        companies,
        tally_exe,
        tally_ini,
        tally_url="http://localhost:9000"
    ):
        self.companies = companies
        self.TALLY_EXE = tally_exe
        self.TALLY_INI = tally_ini
        self.TALLY_URL = tally_url

    # ---------------------------------------------------
    # Public
    # ---------------------------------------------------

    def ensure_loaded(self, guid):

        company = self.get_company(guid)

        if company is None:
            print("Company not found.")
            return False

        company_name = company["company_name"]
        company_code = company["company_code"]
        data_path = company["data_path"]

        if self.is_company_loaded(company_name):
            print(f"✅ {company_name} already loaded.")
            return True

        if self.is_tally_running():

            print("Loading company...")

            self.load_company_via_ui(
                data_path,
                company_name
            )

            if self.wait_until_loaded(company_name):
                return True

            print("Restarting Tally...")
            self.close_tally()

        self.patch_tally_ini(
            data_path,
            company_code
        )

        self.open_tally(
            data_path,
            company_code
        )

        return self.wait_until_loaded(company_name)

    # ---------------------------------------------------
    # Company
    # ---------------------------------------------------

    def get_company(self, guid):
        return self.companies.get(guid)

    # ---------------------------------------------------
    # Tally
    # ---------------------------------------------------

    def is_tally_running(self):

        result = subprocess.run(
            ["tasklist"],
            capture_output=True,
            text=True
        )

        return "tally.exe" in result.stdout.lower()

    def open_tally(
        self,
        data_path,
        company_code
    ):

        subprocess.Popen([
            self.TALLY_EXE,
            f"/LOAD:{company_code}",
            f"/DATA:{data_path}"
        ])

    def close_tally(self):

        subprocess.call(
            [
                "taskkill",
                "/f",
                "/im",
                "Tally.exe"
            ],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL
        )

        time.sleep(1)

    # ---------------------------------------------------
    # Company Loading
    # ---------------------------------------------------

    def load_company_via_ui(
        self,
        data_path,
        company_name
    ):

        try:

            previous = win32gui.GetForegroundWindow()

            app = Application(
                backend="win32"
            ).connect(
                title="TallyPrime:9000",
                timeout=5
            )

            win = app.top_window()

            win.set_focus()

            time.sleep(.5)

            win.type_keys("%{F3}")

            time.sleep(2)

            win.type_keys(
                data_path,
                with_spaces=True
            )

            win.type_keys("{ENTER}")

            time.sleep(1)

            win.type_keys(
                company_name,
                with_spaces=True
            )

            win.type_keys("{ENTER}")

            if previous:
                win32gui.SetForegroundWindow(previous)

            return True

        except Exception as ex:

            print(ex)

            return False

    def wait_until_loaded(
        self,
        company_name,
        timeout=30
    ):

        start = time.time()

        while time.time() - start < timeout:

            if self.is_company_loaded(company_name):
                return True

            time.sleep(1)

        return False

    def is_company_loaded(
        self,
        company_name
    ):

        companies = self.get_loaded_companies()

        return any(
            c.lower() == company_name.lower()
            for c in companies
        )

    def get_loaded_companies(self):

        xml = """
<ENVELOPE>
    <HEADER>
        <VERSION>1</VERSION>
        <TALLYREQUEST>Export</TALLYREQUEST>
        <TYPE>Collection</TYPE>
        <ID>MyCompanyList</ID>
    </HEADER>

    <BODY>

        <DESC>

            <TDL>

                <TDLMESSAGE>

                    <COLLECTION NAME="MyCompanyList">

                        <TYPE>Company</TYPE>

                        <FETCH>Name</FETCH>

                    </COLLECTION>

                </TDLMESSAGE>

            </TDL>

        </DESC>

    </BODY>

</ENVELOPE>
"""

        try:

            r = requests.post(
                self.TALLY_URL,
                data=xml.encode("utf-8"),
                timeout=3
            )

            root = ET.fromstring(r.text)

            return [
                c.attrib["NAME"]
                for c in root.iter("COMPANY")
                if c.attrib.get("NAME")
            ]

        except:

            return []

    # ---------------------------------------------------
    # Tally.ini
    # ---------------------------------------------------

    def patch_tally_ini(
        self,
        data_path,
        company_code
    ):

        with open(
            self.TALLY_INI,
            "r"
        ) as f:

            lines = f.readlines()

        new_lines = []

        found_data = False
        found_load = False
        found_default = False

        for line in lines:

            s = line.strip().lower()

            if s.startswith("data="):

                new_lines.append(
                    f"Data={data_path}\n"
                )

                found_data = True

            elif s.startswith("load="):

                new_lines.append(
                    f"Load={company_code}\n"
                )

                found_load = True

            elif s.startswith("default companies="):

                new_lines.append(
                    "Default Companies=Yes\n"
                )

                found_default = True

            else:

                new_lines.append(line)

        if not found_data:
            new_lines.append(
                f"Data={data_path}\n"
            )

        if not found_load:
            new_lines.append(
                f"Load={company_code}\n"
            )

        if not found_default:
            new_lines.append(
                "Default Companies=Yes\n"
            )

        with open(
            self.TALLY_INI,
            "w"
        ) as f:

            f.writelines(new_lines)