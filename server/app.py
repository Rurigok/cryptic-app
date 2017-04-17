from flask import Flask, request

import accounts

app = Flask(__name__)

@app.route("/login", methods=["POST"])
def login_post():

    username = request.form["username"]
    password = request.form["password"]

    # TODO: do some session nonsense...

    #return accounts.login(username, password)
    return "Login not yet implemented", 501

@app.route("/register", methods=["POST"])
def register_post():

    # TODO: implement account creation

    return "Account creation not yet implemented", 501

if __name__ == '__main__':
    app.run()
