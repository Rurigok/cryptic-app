"""
This module handles all account state operations, such as:
    - account creation
    - login
    - logout
"""
import bcrypt
import MySQLdb as mariadb
import random

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

    Returns: a tuple (success, message) detailing the result of the login
             attempt
    """
    # Find user in DB
    try:
        cursor.execute("SELECT username, password, is_admin FROM users WHERE username=%s",
                       (username,))
    except mariadb.Error as error:
        return (False, "Database error: {}".format(error))

    rows = cursor.fetchall()

    if len(rows) != 1:
        return (False, "Invalid username or password")

    fetched_username, hashed_password, is_admin = rows[0]

    # Encode passwords for use in bcrypt
    password = password.encode("UTF-8")
    hashed_password = hashed_password.encode("UTF-8")

    if bcrypt.checkpw(password, hashed_password):
        # Login was successful
        #print("Successful login.")
        session["username"] = fetched_username
        session["is_admin"] = is_admin
        #print("Updated session:", session)
        return (True, "")
    else:
        # Invalid password
        return (False, "Invalid username or password")

    return (False, "Login not yet implemented")

def logout(session, username):
    pass

def create_account(session, username, password):
    """ Attempts to create the given user account.

    The user calling this method must have admin privileges to create a new
    user account.

    Returns: a tuple (success, message) detailing the result of the login
             attempt
    """
    try:
        cursor.execute("SELECT username FROM users WHERE username=%s",
                       (username,))
    except mariadb.Error as error:
        return (False, "Database error: {}".format(error))

    rows = cursor.fetchall()

    if len(rows) > 0:
        return (False, "Username is already taken")

    return (session, False, "Account creation not yet implemented")

def generate_password(password):
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
