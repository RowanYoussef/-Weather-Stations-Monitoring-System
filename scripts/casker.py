import argparse
import requests
import threading
import time
import csv
import os

API_URL = "http://localhost:8080/bitcask"

def view_all():
    resp = requests.get(f"{API_URL}/")
    if resp.status_code == 200:
        data = resp.json()
        print(data)
        # Optionally, write to CSV with timestamp
        timestamp = int(time.time())
        filename = f"{timestamp}_all.csv"
        with open(filename, "w", newline="") as csvfile:
            if data:
                writer = csv.DictWriter(csvfile, fieldnames=data[0].keys())
                writer.writeheader()
                writer.writerows(data)
        print(f"Saved to {filename}")
    else:
        print("Failed to fetch all entries:", resp.text)

def view_key(key):
    resp = requests.get(f"{API_URL}/{key}")
    if resp.status_code == 200:
        data = resp.json()
        print(data)
    else:
        print(f"Failed to fetch key {key}:", resp.text)

def put_key(data):
    try:
        resp = requests.post(f"{API_URL}/put", json=data)
        if resp.status_code == 200:
            print(f"Successfully put key: {resp.json()}")
        else:
            print(f"Failed to put key:", resp.text)
    except Exception as e:
        print(f"Error during PUT request: {e}")

def perf(clients):
    timestamp = int(time.time())
    threads = []
    def worker(thread_num):
        resp = requests.get(f"{API_URL}/")
        if resp.status_code == 200:
            data = resp.json()
            filename = f"{timestamp}_thread_{thread_num}.csv"
            with open(filename, "w", newline="") as csvfile:
                if data:
                    writer = csv.DictWriter(csvfile, fieldnames=data[0].keys())
                    writer.writeheader()
                    writer.writerows(data)
            print(f"Thread {thread_num}: Saved to {filename}")
        else:
            print(f"Thread {thread_num}: Failed to fetch all entries:", resp.text)

    for i in range(1, clients + 1):
        t = threading.Thread(target=worker, args=(i,))
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Bitcask client script")
    parser.add_argument("--view-all", action="store_true", help="View all entries")
    parser.add_argument("--view", action="store_true", help="View a specific key")
    parser.add_argument("--key", type=str, help="Key to view or put (used with --view or --put)")
    parser.add_argument("--perf", action="store_true", help="Performance test")
    parser.add_argument("--clients", type=int, default=1, help="Number of clients for perf test")
    parser.add_argument("--put", action="store_true", help="Put a JSON message for a key")
    parser.add_argument("--data", type=str, help="JSON string to put (used with --put and --key)")

    args = parser.parse_args()

    if args.view_all:
        view_all()
    elif args.view and args.key:
        view_key(args.key)
    elif args.perf:
        perf(args.clients)
    elif args.put and args.data:
        import json
        try:
            json_data = json.loads(args.data)
        except Exception as e:
            print(f"Invalid JSON data: {e}")
        else:
            put_key(json_data)
    else:
        parser.print_help()
