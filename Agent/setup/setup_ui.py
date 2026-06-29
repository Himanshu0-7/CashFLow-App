import json
import tkinter as tk
from tkinter import filedialog
from tally.company_scanner import CompanyScanner


def show_setup():

    root = tk.Tk()
    root.title("Tally Agent Setup")
    root.configure(bg="#1E1E2E")
    window_width=600
    window_height=250

    screen_height=root.winfo_screenheight()
    screen_width=root.winfo_screenwidth()

    x=(screen_width - window_width) // 2
    y=(screen_height - window_height) // 2
    root.geometry(f"{window_width}x{window_height}+{x}+{y}")

    def browse_tally():
        folder = filedialog.askdirectory(
            title="Select Tally Installation Folder"
        )
        if folder:
            tally_entry.delete(0,tk.END)
            tally_entry.insert(0,folder)

    def browse_data():
        tally_data = filedialog.askdirectory(
            title="Select Tally Data Folder"
        )
        if tally_data:
            tally_entry2.delete(0,tk.END)
            tally_entry2.insert(0,tally_data)

    def save_config():
        tally_folder = tally_entry.get()
        data_folder = tally_entry2.get()

        scanner = CompanyScanner()
        companies = scanner.scan(data_folder)
        config = {
            "tally_folder": tally_folder,
            "data_folder": data_folder
        }

        with open("config.json", "w") as f:
            json.dump(config, f, indent=4)

        with open("company.json", "w") as f:
            json.dump(companies, f, indent=4)

        print(companies)


    tk.Label(
        root,
        text="Tall Installation Folder"
    ).pack(pady=(20,5))

    frame = tk.Frame(root)
    frame.pack()

    tally_entry =tk.Entry(
        frame,
        width=55
    )
    tally_entry.pack(side="left", padx=5)

    browse_btn = tk.Button(
        frame,
        text="Browse",
        command=browse_tally
    )
    browse_btn.pack(side="left")
    tk.Label(
        root,
        text="Tall Data Folder"
    ).pack(pady=(40,5))

    frame2 = tk.Frame(root)
    frame2.pack()

    tally_entry2 = tk.Entry(
        frame2,
        width=55
    )
    tally_entry2.pack(side="left", padx=5)

    browse_btn2 = tk.Button(
        frame2,
        text="Browse",
        command=browse_data
    )
    browse_btn2.pack(side="left")

    save_btn = tk.Button(
        root,
        text="Save",
        command=save_config
    )
    save_btn.pack(pady=20)
    root.mainloop()
