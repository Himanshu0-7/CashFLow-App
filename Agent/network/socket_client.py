import socketio

sio = socketio.Client()

@sio.event
def connect():
    print("Connected to Nodejs")

@sio.event
def disconnect():
    print("Disconnected")

sio.connect("http://localhost:3000")

sio.wait()