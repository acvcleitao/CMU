#!/usr/bin/env python3
# encoding: utf-8
import json
from flask import Flask, request, jsonify
from flask_mongoengine import MongoEngine
from mongoengine.queryset.visitor import Q
from flask_sock import Sock
import json

app = Flask(__name__)
app.config['MONGODB_SETTINGS'] = {
    'db': 'CMU',
    'host': 'localhost',
    'port': 27017
}
db = MongoEngine()
db.init_app(app)

sock = Sock(app)

websockets = {}

''''
Data Structures
'''

class Library(db.Document):
    name = db.StringField()
    books = db.ListField(db.ReferenceField('Book'))
    latitude = db.LongField()
    longitude = db.LongField()
    isFavorite = db.BooleanField()
    photoLocation = db.StringField()

    def to_json(self):
        return {
            'name': self.name,
            'books': [],
            'latitude': str(self.latitude),
            'longitude': str(self.longitude),
            'isFavorite': str(self.isFavorite),
            'photoLocation': self.photoLocation
        }

class Book(db.Document):
    title = db.StringField()
    barcode = db.StringField()
    cover = db.StringField()
    quantity = db.IntField()
    library = db.StringField()

    def to_json(self):
        return {
            'title': self.title,
            'barcode': self.barcode,
            'cover': self.cover,
            'quantity': str(self.quantity),
            'library': self.library
        }

class User(db.Document):
    username = db.StringField()
    password = db.StringField()

    def to_json(self):
        return {"username": self.username, "password": self.password}


'''
-> Connect/disconect from server

Adds user to connected users
'''


@sock.route('/connect')
def connect(socket):
    user = User.objects(username=request.form['user']).first()
    print(user, " connected")
    if user is None:
        return jsonify({"status": "error", "message": "user not found"})
    else:
        websockets[user.username] = socket
        socket.send("Connected")
        return jsonify({"status": "ok"})


# close connection
@sock.route('/disconnect')
def disconnect(socket):
    data = request.form['name']
    user = User.objects(username=data['user']).first()
    if user is None:
        return jsonify({"status": "error", "message": "user not found"})
    else:
        del websockets[user.username]
        return jsonify({"status": "ok"})



'''
Auxiliar functions to find if Book/Library exists
'''

def bookExists(barcode):
    book = Book.objects(barcode=barcode).first()
    if book is None:
        return False
    else:
        return True

def libraryExists(name):
    library = Library.objects(name=name).first()
    if library is None:
        return False
    else:
        return True


'''
Creates a Library
Library {
    'name': self.name,
    'books': [],
    'latitude': str(self.latitude),
    'longitude': str(self.longitude),
    'isFavorite': str(self.isFavorite),
    'photoLocation': self.photoLocation
    }
'''

@app.route('/library/create', methods=['POST'])
def createLibrary():
    print("Creating libray")
    name = request.form['name']

    if libraryExists(name):
        return jsonify({"status": "error", "message": "Library already exists"})

    books = []
    latitude = request.form['latitude']
    longitude = request.form['longitude']
    isFavorite = request.form['isFavorite']
    photoLocation = request.form['isFavorite']
    
    library = Library(name=name, books=books, latitude=latitude, longitude=longitude, isFavorite=isFavorite, photoLocation=photoLocation).save()
    return jsonify({"status": "success", "message": "Library created successfully"})


'''
Creates a Book
Book {
    'title': self.title,
    'barcode': self.barcode,
    'cover': self.cover,
    'quantity': str(self.quantity)
    'library': self.library
}


Request {
    'title'
    'barcode'
    'cover'
    'library'
}
'''

@app.route('/book/create', methods=['POST'])
def createBook():
    print("Creating book...")
    title = request.form['title']
    barcode = request.form['barcode']
    cover = request.form['cover']
    library = request.form['library']

    # If library associated with book doesn't exist, send an error
    if not libraryExists(library):
        return jsonify({"status": "error", "message": "Library doesn't exist"})

    # if the book already exists in the specific library mentioned then add 1 to it's quantity
    if bookExists(title):
        Book.objects(Q(barcode=barcode) & Q(library=library)).first().update_one(inc__quantity=1)
        return jsonify({"status": "success", "message": "Quantity increased"})
    
    # if the book doesn't exist in the specific library, create new book
    book = Book(title=title, barcode=barcode, cover=cover, quantity=1, library=library).save()
    Library.objects(libraryName=latest_library).first().update_one(push__books=book)
    print("Book Created")
    return jsonify({"status": "success", "message": "Book created successfully"})


'''
Increases quantity of a Book by 1
Book {
    'title': self.title,
    'barcode': self.barcode,
    'cover': self.cover,
    'quantity': str(self.quantity)
    'library': self.library
    }
'''

@app.route('/book/increase_quantity', methods=['POST'])
def addBookQuantity():
    barcode = request.form['barcode']
    library = request.form['library']

    # Add 1 to the book's quantity if it exists associated with the library
    if bookExists(barcode):
        Book.objects(Q(barcode=barcode) & Q(library=library)).first().update_one(inc__quantity=1)
        return jsonify({"status": "success", "message": "Quantity increased"})

    return jsonify({"status": "error", "message": "Book doesn't exist in this Library"})



'''
Mainpage / Debug
'''

# ping


@app.route('/ping', methods=['GET'])
def ping():
    return "pong"


@ app.route('/')
def hello_world():
    return "Hello World"

@sock.route('/ws')
def ws(ws):
  global websocket_connections, state
  websocket_connections.append(ws)

  while True:
    ws.send(json.dumps(state))
    ws.receive()


#to display the connection status
@app.route('/', methods=['GET'])
def handle_call():
    return "Successfully Connected"

#the get method. when we call this, it just return the text "Hey!! I'm the fact you got!!!"
@app.route('/getfact', methods=['GET'])
def get_fact():
    return "Hey!! I'm the fact you got!!!"

#the post method. when we call this with a string containing a name, it will return the name with the text "I got your name"
@app.route('/getname/<name>', methods=['POST'])
def extract_name(name):
    return "I got your name "+name;

#this commands the script to run in the given port
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=False)
