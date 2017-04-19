"""
This module handles all account state operations, such as:
    - account creation
    - login
    - logout
"""
import MySQLdb as mariadb
import bcrypt

mariadb_conn = mariadb.connect(host='localhost',
                               database='cryptic',
                               user='cryptic_user',
                               password='deployment_password')
cursor = mariadb_conn.cursor()

def login(session, username, password):
    """ Attempts to login the given user with the given password.

    Returns: a tuple (success, message) detailing the result of the login
             attempt
    """
    # Find user in DB
    try:
        cursor.execute("SELECT username, password, is_admin FROM users WHERE username=%s", (username,))
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
        session["username"] = fetched_username
        session["admin"] = is_admin
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
    return (False, "Account creation not yet implemented")
