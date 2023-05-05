import http.server
import socketserver
import os
import gzip

PORT = 8000
FOLDER_PATH = '/tmp/benchmark/2023-12-12/12/'


class FileHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-Type', 'application/octet-stream')
        self.send_header('Content-Disposition', 'attachment; filename="all.gz"')
        self.end_headers()

        folder = os.listdir(FOLDER_PATH)
        for file_name in folder:
            if not os.path.isdir(os.path.join(FOLDER_PATH, file_name)) and file_name.lower().endswith('.gz'):
                with open(os.path.join(FOLDER_PATH, file_name), 'rb') as f:
                    while True:
                        chunk = f.read(1048576)
                        if not chunk:
                            break
                        self.wfile.write(chunk)
        return


with socketserver.TCPServer(("", PORT), FileHandler) as httpd:
    print("File server listening on port", PORT)
    httpd.serve_forever()
