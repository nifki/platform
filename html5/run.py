#!/usr/bin/python3

from http.server import HTTPServer, SimpleHTTPRequestHandler

httpd = HTTPServer(('localhost', 0), SimpleHTTPRequestHandler)
ip_address, port = httpd.server_address
print(f"Visit http://{ip_address}:{port}/Rocks/play/")
httpd.serve_forever()
