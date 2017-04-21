#!/usr/bin/env python3
from flask import Flask, request, session
from response import JSONResponse

import accounts

# App settings
app = Flask(__name__)
app.secret_key = "secret_key_here_in_deployment"

@app.route("/login", methods=["POST"])
def login_post():
    """ Handles login requests. """

    response = JSONResponse("login")

    print("Before:", session)

    # Form validation
    if "username" not in request.form:
        response.success = False
        response.message = "No username provided for login"
        return response.to_json(), 200

    if "password" not in request.form:
        response.success = False
        response.message = "No password provided for login"
        return response.to_json(), 200

    username = request.form["username"]
    password = request.form["password"]

    if len(username) > 255:
        response.success = False
        response.message = "Username field may not exceed 255 characters"
        return response.to_json(), 200

    if len(password) > 255:
        response.success = False
        response.message = "Password field may not exceed 255 characters"
        return response.to_json(), 200

    # Check for active sessions
    if "username" in session:
        print("username in session found: ", session["username"])
        # Client is already logged in as someone
        if session["username"] == username:
            # Already logged in as person who they are trying to login as
            response.success = True
            return response.to_json(), 200
        else:
            response.success = False
            response.message = "You are already logged in as someone else"
            return response.to_json(), 200

    # Perform login
    response.success, response.message = accounts.login(session, username, password)

    print("After:", session)

    return response.to_json(), 200

@app.route("/create-account", methods=["POST"])
def create_account_post():
    """ Creates a new account. """

    response = JSONResponse("create-account")

    # Form validation
    if "username" not in request.form:
        response.success = False
        response.message = "No username provided for login"
        return response.to_json(), 200

    username = request.form["username"]

    if len(username) > 255:
        response.success = False
        response.message = "Username field may not exceed 255 characters"
        return response.to_json(), 200


    # TODO: implement account creation

    response.success, response.message = accounts.create_account(session, username)

    return response.to_json(), 200

if __name__ == '__main__':
    app.run(port=5678)
