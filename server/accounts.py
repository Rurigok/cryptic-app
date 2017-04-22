"""
This module handles all account state operations, such as:
    - account creation
    - login
    - logout
"""
import bcrypt
import MySQLdb as mariadb
import random
import string

from response import JSONResponse

# Database connection
mariadb_conn = mariadb.connect(host='localhost',
                               db='cryptic',
                               user='cryptic_user',
                               passwd='deployment_password')
# Database cursor
cursor = mariadb_conn.cursor()

# Random password generation alphabet
ALPHABET = string.punctuation + string.ascii_letters + string.digits

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

def logout(username):
    pass

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

    # Insert new account into database
    try:
        cursor.execute("INSERT INTO users (username, password) VALUES (%s, %s)",
                       (username, hashed_password))
    except mariadb.Error as error:
        return JSONResponse(False, "Database error: {}".format(error))

    # Commit transaction
    mariadb_conn.commit()

    # Successful insert. Prepare response for client
    response = JSONResponse(True)
    response.password = gen_password
    # TODO: modify JSONResponse to actually send this key ^^

    return response

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
