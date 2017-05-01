#!/usr/bin/env python3
from flask import Flask, redirect, render_template, request, session
from response import JSONResponse

import accounts

# App settings
app = Flask(__name__)
app.secret_key = "secret_key_here_in_deployment"

@app.route("/admin/login")
def admin_login():
    return render_template("login.html")

@app.route("/admin/console")
def admin_console():
    return render_template("console.html")

@app.route("/login", methods=["POST"])
def login_post():
    """ Handles login requests. """

    response = JSONResponse()

    if "login_error" in session:
        del session["login_error"]

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
	
	if "cookie" in request.form
		cookie = request.form["cookie"]
	
	# TODO: check if cookie is valid
	
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

    # Determine source of api request
    # If source is admin web panel, return admin web console.
    # Otherwise, return standard JSON response
    if "medium" in request.form:
        if request.form["medium"] == "admin_web":
            if response.success:
                return redirect("/admin/console")
            else:
                session["login_error"] = response.message
                return redirect("/admin/login")

    return response.to_json(), 200

@app.route("/create-account", methods=["POST"])
def create_account_post():
    """ Creates a new account. """

    response = JSONResponse()

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

    # TODO: implement account creation

    response = accounts.create_account(username)

    return response.to_json(), 200

@app.route("/send-message", methods=["POST"])
def message_route():
    """ Mediate peer-to-peer connections """
    
    response = JSONResponse()
    
    # Form validation
    if "username" not in request.form:
        response.success = False
        response.message = "No username provided for routing"
        return response.to_json(), 200

    if "target" not in request.form:
        response.success = False
        response.message = "No target provided for routing"
        return response.to_json(), 200

    username = request.form["username"]
    target = request.form["password"]
    
    if "target" in session
        print("target user online: ", session["target"]);
        
        # Find target in DB
        try:
            cursor.except("SELECT public_key FROM users WHERE username is=%s",
                            (target,))
        except mariadb.Error as error
            return JSONResponse(False, "Database error: ()".format(error))
        
        rows = cursor.fetchall()
        
        if len(rows) != 1:
            return JSONResponse(False, "Invalid Public Key")
        
        fetched_target_key = rows[0]
        
        # TODO: send target Public Key to username, wait for successful response
        
        # find user in DB
        try:
            cursor.except("SELECT id, public_key FROM users WHERE username is=%s",
                            (username,))
        except mariadb.Error as error
            return JSONResponse(False, "Database error: ()".format(error))
        
        rows = cursor.fetchall()
        
        if len(rows) != 1:
            return JSONResponse(False, "Invalid Public Key")
        
        uid, fetched_user_key = rows[0]
        
        try:
            cursor.except("SELECT device_ip FROM directory WHERE id is=%s",
                            (uid,))
        except mariadb.Error as error
            return JSONResponse(False, "Database error: ()".format(error))
        
        rows = cursor.fetchall()
        
        if len(rows) != 1:
            return JSONResponse(False, "Invalid IP")
        
        fetched_user_ip = rows[0]
        
        # TODO: send user IP and Public Key to target, wait for successful response
    
    # TODO: handle target offline, place message notification in database
    
    return response.to_json(), 200

if __name__ == '__main__':
    app.run(port=5678)
