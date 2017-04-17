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

    if "username" not in request.form:
        response.success = False
        response.message = "no username provided for login"
        return response.to_json(), 200

    if "password" not in request.form:
        response.success = False
        response.message = "no password provided for login"
        return response.to_json(), 200

    username = request.form["username"]
    password = request.form["password"]

    if username in session:
        # Client is already logged in as someone
        if session["username"] == username:
            # Already logged in as person who they are trying to login as
            response.success = True
            return response.to_json(), 200
        else:
            response.success = False
            response.message = "you are already logged in as someone else"
            return response.to_json(), 200

    #accounts.login(username, password)
    response.success = False
    response.message = "login not yet implemented"

    return response.to_json(), 501

@app.route("/create-account", methods=["POST"])
def create_account_post():
    """ Creates a new account. """

    response = JSONResponse("create-account")

    # TODO: implement account creation

    response.success = False
    response.message = "account creation not yet implemented"

    return response.to_json(), 501

if __name__ == '__main__':
    app.run()
