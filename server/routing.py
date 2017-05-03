"""
This module handles directory routing for sending messages and any other
message-related activities.
"""
import json
import MySQLdb as mariadb
import socket

from functools import wraps

from response import JSONResponse

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
def get_message_route(requester_user, target_user):
    """ Looks up routing information for the given user.

    On a successful lookup, the following info should be returned:
        public_key: public key of the targeted user
        device_ip: last known IP of the targeted user
    """
    # Lookup public key and device ip of target
    try:
        cursor.execute(("SELECT users.public_key, directory.device_ip FROM directory "
                        "INNER JOIN users ON users.id = directory.user_id "
                        "WHERE users.username=%s"),
                        (target_user,))
    except mariadb.Error as error:
        return JSONResponse(False, "Database error: {}".format(error))

    rows = cursor.fetchall()

    if len(rows) != 1:
        return JSONResponse(False, "No directory entry found for given username")

    target_key, target_ip = rows[0]

    # Lookup public key and device ip of requester
    try:
        cursor.execute(("SELECT users.public_key, directory.device_ip FROM directory "
                        "INNER JOIN users ON users.id = directory.user_id "
                        "WHERE users.username=%s"),
                        (requester_user,))
    except mariadb.Error as error:
        return JSONResponse(False, "Database error: {}".format(error))

    rows = cursor.fetchall()

    if len(rows) != 1:
        return JSONResponse(False, "No directory entry found for given username")

    requester_key, requester_ip = rows[0]

    # Send requester ip and key to target (via socket to target)
    message = {}
    message["sender_ip"] = requester_ip
    message["sender_public_key"] = requester_key
    push_message(target_ip, json.dumps(message))

    # Return target ip and key to requester (via original http response)
    response = JSONResponse(True)
    response.target_ip = target_ip
    response.target_public_key = target_key

@uses_db
def update_route():
    """ Stores an updated IP address for a client. """
    pass

def push_message(ip_address, message):
    """ Connects to a client app and sends a message request """

    message = message + "\n"

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # get local machine name
    host = ip_address
    port = 5677

    print("connecting to socket: {}:{}".format(host, port))

    # connect to client
    s.connect((host, port))

    # prepare message
    message = message.encode("utf-8")

    sent = s.send(message)

    response_code = s.recv(1)

    s.close()

    print("sent {} bytes. response: {}".format(sent, response_code))
