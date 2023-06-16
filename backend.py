#!/usr/bin/env python3

from flask import Flask, render_template, jsonify, json, request
from flask_sock import Sock
from simple_websocket.ws import ConnectionClosed

state = False
websocket_connections = []
LibraryList = []
BookList = []

class Library:
  def __init__(self,name,books,latitude,longitude,isFavorite,photoLocation):
    self.name = name
    self.books = books
    self.latitude = latitude
    self.longitude = longitude
    self.isFavorite = isFavorite
    self.photoLocation = photoLocation


  def to_json(self):
    allBooks = []
    for book in self.books:
      allBooks.append(book.to_json())
    
    return {
      'name': self.name,
      'books': allBooks,
      'latitude': str(self.latitude),
      'longitude': str(self.longitude),
      'isFavorite': str(self.isFavorite),
      'photoLocation': self.photoLocation
    }



class Book:
  def __init__(self, title, barcode, cover, quantity, library, notifications):
    self.title = title
    self.barcode = barcode
    self.cover = cover
    self.quantity = quantity
    self.library = library
    self.notifications = notifications

  def to_json(self):
    return {
      'barcode': self.barcode,
      'title': self.title,
      'cover': self.cover,
      'quantity': str(self.quantity),
      'library': self.library,
      'notificatons': self.notifications
    }

def bookExists(barcode):
  for book in BookList:
    if(book.barcode == barcode):
      return True
  return False

def libraryExists(name):
  for library in LibraryList:
    if(library.name == name):
      return library
  return False

def getLibraryInServer(name):
  for library in LibraryList:
    if(library.name == name):
      return library
  return False 

def getBookInServer(barcode, library):
  for book in BookList:
    if(book.barcode == barcode and book.library == library):
      return book
  return False

def change_state(new_state):
  global state
  state = new_state
  websocket_broadcast(state)


def websocket_broadcast(message):
  for ws in websocket_connections:
    try:
      ws.send(json.dumps(message))
    except ConnectionClosed:
      websocket_connections.remove(ws)


app = Flask(__name__)
sockets = Sock(app)

@app.route('/library/create', methods=['POST'])
def createLibrary():
    print("Creating library")
    name = request.form['name']

    if libraryExists(name):
        return jsonify({"status": "error", "message": "Library already exists"})

    books = []
    latitude = request.form['latitude']
    longitude = request.form['longitude']
    isFavorite = request.form['isFavorite']
    photoLocation = request.form['photoLocation']
    library = Library(name, books, latitude, longitude, isFavorite, photoLocation)
    LibraryList.append(library)
    print("new library created: " + library.name)
    return jsonify({"status": "success", "message": "Library created successfully"})


@app.route('/library/get', methods=['GET'])
def getLibrary():
  print(getLibraryInServer(request.form['name']).to_json())
  return getLibraryInServer(request.form['name']).to_json()


@app.route('/library/getAll', methods=['GET'])
def getAllLibraries():
  libraryDict = {}
  print("Transfering " + str(len(LibraryList)) +  " libraries to client")
  for library in LibraryList:
    print(library.to_json())
    libraryDict[library.name] = library.to_json()
  return libraryDict


@app.route('/book/create', methods=['POST'])
def createBook():
  print("Creating book...")
  title = request.form['title']
  barcode = request.form['barcode']
  cover = request.form['cover']
  library = request.form['library']
  notifications = request.form['notifications']
  libraryInServer = libraryExists(library)

  # If library associated with book doesn't exist, send an error
  if not libraryInServer:
    return jsonify({"status": "error", "message": "Library doesn't exist"})

  # if the book doesn't exist in the specific library, create new book
  book = Book(title, barcode, cover, 1, library, notifications)
  libraryInServer.books.append(book)
  BookList.append(book)
  print("created new book " + str(barcode))
  return jsonify({"status": "success", "message": "Book created successfully"})

@app.route('/book/retrieve', methods=['POST'])
def retrieveBook():
  getBookInServer(request.form['barcode'], request.form['library']).quantity -= 1
  return jsonify({"status": "success", "message": "Quantity decreased"})

@app.route('/book/donate', methods=['POST'])
def donateBook():
  getBookInServer(request.form['barcode'], request.form['library']).quantity += 1
  return jsonify({"status": "success", "message": "Quantity increased"})

@app.route('/book/get', methods=['GET'])
def getBook():
  print(getBookInServer(request.form['barcode'], request.form['library']).to_json())
  return getBookInServer(request.form['barcode'], request.form['library']).to_json()


@app.route('/book/getAll', methods=['GET'])
def getAllBooks():
  print(BookList)
  bookDict = {}
  print("Transfering " + str(len(BookList)) +  " books to client")
  for book in BookList:
    print(book.to_json())
    if(book.barcode in bookDict):
      bookDict[book.barcode].append(book.to_json())
    else:
      bookDict[book.barcode] = [book.to_json(),]
  return bookDict


@app.route('/', methods=['GET'])
def index():
  return render_template('index.html')


@app.route('/state', methods=['GET'])
def get_state():
  global state
  return jsonify(state)


@app.route('/state', methods=['PUT'])
def put_state():
  global state
  change_state(json.loads(request.data))
  return jsonify(state)


@sockets.route('/ws')
def ws(ws):
  global websocket_connections, state
  websocket_connections.append(ws)

  while True:
    ws.send(json.dumps(state))
    ws.receive()


from gevent import monkey
monkey.patch_all()
from gevent.pywsgi import WSGIServer
WSGIServer(('0.0.0.0', 5000), app).serve_forever()
