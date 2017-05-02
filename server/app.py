#!/usr/bin/env python3
from flask import Blueprint, Flask, redirect, render_template, request, session

from response import JSONResponse

import accounts
import routing

# App settings
app = Flask(__name__)
app.secret_key = "secret_key_here_in_deployment"

bp = Blueprint('cryptic', __name__, template_folder='templates')

@bp.route("/admin/login")
def admin_login():
    return render_template("login.html")

@bp.route("/admin/console")
def admin_console():

    # Check login status and privileges
    if "username" not in session:
        session["login_error"] = "You must be logged in"
        return redirect("/cryptic/admin/login")

    if "is_admin" in session:
        if session["is_admin"] < accounts.ADMIN:
            return redirect("/cryptic/admin/login")
    else:
        return redirect("/cryptic/admin/login")

    return render_template("console.html")

@bp.route("/admin/search-user", methods=["GET"])
def search_user_get():
    pass

@bp.route("/login", methods=["POST"])
def login_post():
    """ Handles login requests. """

    if "login_error" in session:
        del session["login_error"]

    response = JSONResponse()

    if "medium" in request.form and request.form["medium"] == "admin_web":
        # request is from web
        return "Not yet implemented", 501

    else:
        # assume request is from app

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
        response = accounts.login(session, username, password)

        return response.to_json(), 200


@bp.route("/create-account", methods=["POST"])
def create_account_post():
    """ Creates a new account. """

    response = JSONResponse()

    # Check login status and privileges
    if "username" not in session:
        response.success = False
        response.message = "You must be logged in"
        return response.to_json(), 200

    if "is_admin" in session:
        if session["is_admin"] < accounts.ADMIN:
            response.success = False
            response.message = "Insufficient privileges for account creation"
            return response.to_json(), 200
    else:
        response.success = False
        response.message = "Insufficient privileges for account creation"
        return response.to_json(), 200

    # Form validation
    if "username" not in request.form:
        response.success = False
        response.message = "No username provided for account creation"
        return response.to_json(), 200

    username = request.form["username"]

    if len(username) > 255:
        response.success = False
        response.message = "Username field may not exceed 255 characters"
        return response.to_json(), 200

    response = accounts.create_account(username)

    return response.to_json(), 200

@bp.route("/send-message", methods=["POST"])
def message_route():
    """ Mediate peer-to-peer connections.

    Expected request parameters:
        target: the user that we are requesting a route to

    Returns:
        JSONResponse detailing the request result
    """

    response = JSONResponse()

    # Check login status
    if "username" not in session:
        response.success = False
        response.message = "You must be logged in to request a route"
        return response.to_json(), 200

    requester = session["username"]

    # Form validation
    if "target" not in request.form:
        response.success = False
        response.message = "No target provided for routing"
        return response.to_json(), 200

    target = request.form["target"]

    if len(target) > 255:
        response.success = False
        response.message = "Target user field may not exceed 255 characters"
        return response.to_json(), 200

    response = routing.get_message_route(requester, target)

    return response.to_json(), 200

if __name__ == '__main__':
    app.register_blueprint(bp, url_prefix="/cryptic")
    app.run(port=5678)
