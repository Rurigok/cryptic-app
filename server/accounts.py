"""
This module handles all account state operations, such as:
    - account creation
    - login
    - logout
"""
import bcrypt
import MySQLdb as mariadb
import os
import random
import string

from base64 import b64encode
from functools import wraps

from response import JSONResponse

# Random password generation alphabet
ALPHABET = string.punctuation + string.ascii_letters + string.digits

# Privilege levels
STANDARD_USER = 0
ADMIN = 10

def uses_db(func):
    """ Used to decorate any function that requires database access.

    This decorator opens a database connection and creates a cursor for the
    decorated function, and closes the connection when the decorated function
    is done.
    """
    @wraps(func)
    def func_wrapper(*args):
        # Connect to database
        global db_conn
        db_conn = mariadb.connect(host='localhost',
                                  db='cryptic',
                                  user='cryptic_user',
                                  passwd='deployment_password')
        # Database cursor
        global cursor
        cursor = db_conn.cursor()

        ret_val = func(*args)
        # Close connection
        db_conn.close()
        return ret_val
    return func_wrapper

@uses_db
def login(session, username, password):
    """ Attempts to login the given user with the given password.

    Returns: a JSONResponse object detailing the result of the login request
    """

    # Find user in DB
    try:
        cursor.execute("SELECT username, password, is_admin FROM users WHERE username=%s",
                       (username,))
    except mariadb.Error as error:
        return JSONResponse(False, "Database error: {}".format(error))

    rows = cursor.fetchall()

    if len(rows) != 1:
        return JSONResponse(False, "Invalid username or password")

    fetched_username, hashed_password, is_admin = rows[0]

    # Encode passwords for use in bcrypt
    password = password.encode("UTF-8")
    hashed_password = hashed_password.encode("UTF-8")

    if bcrypt.checkpw(password, hashed_password):
        # Login was successful
        session["username"] = fetched_username
        session["is_admin"] = is_admin
        return JSONResponse(True)
    else:
        # Invalid password
        return JSONResponse(False, "Invalid username or password")

    return JSONResponse(False, "Login not yet implemented")

def logout(session):
    """ Logs a user out by destroying all session info. """

    if "username" not in session:
        return JSONResponse(False, "Not logged in")

    for k in session:
        del session[k]

    return JSONResponse(True)

@uses_db
def create_account(username):
    """ Attempts to create the given user account.

    The user calling this method must have admin privileges to create a new
    user account.

    Returns: a tuple (success, message) detailing the result of the login
             attempt
    """

    # Ensure username is not taken
    try:
        cursor.execute("SELECT username FROM users WHERE username=%s",
                       (username,))
    except mariadb.Error as error:
        return JSONResponse(False, "Database error: {}".format(error))

    rows = cursor.fetchall()

    if len(rows) > 0:
        return JSONResponse(False, "Username is already taken")

    # Generate a random password. This must be sent to the account creator
    gen_password = generate_password()
    gen_password = gen_password.encode("UTF-8")
    hashed_password = bcrypt.hashpw(gen_password, bcrypt.gensalt())

    # Personal key generation for message storage
    personal_key = generate_personal_key()

    # Insert new account into database
    try:
        cursor.execute("INSERT INTO users (username, password, personal_key) VALUES (%s, %s, %s)",
                       (username, hashed_password, personal_key))
    except mariadb.Error as error:
        return JSONResponse(False, "Database error: {}".format(error))

    # Commit transaction
    db_conn.commit()

    # Successful insert. Prepare response for client
    response = JSONResponse(True)
    response.password = gen_password

    return response

@uses_db
def search_for_user(search_str):
    pass

def generate_password():
    """ Generates a random password constrained to strength requirements

    The password shall be:
        - 16-24 characters in length
        - a combination of lowercase, uppercase, numbers, and symbols

    Returns: the randomly generated password
    """
    # Uses SystemRandom() to generate cryptographically secure numbers
    length = random.SystemRandom().randint(16, 24)
    chars = [random.SystemRandom().choice(ALPHABET) for _ in range(length)]
    return ''.join(chars)

def generate_personal_key():
    """ Generates a random symmetric encryption key. """

    key = os.urandom(32)
    skey = b64encode(key).decode('utf-8')

    return skey
