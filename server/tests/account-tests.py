#!/usr/bin/env python3
import json, requests

url = "http://localhost:5678"

def dump_response(response):
    print("HTTP {}: {}".format(response.status_code, response.text))
    print(response.cookies)

def test_create_account():

    print(">>> Testing account creation")

    data = {}
    session = requests.Session()

    response = session.post(url + "/create-account", data)

def test_login():

    print(">>>>>> Testing login")

    data = {}
    session = requests.Session()

    print(">>> Testing with no request fields")

    response = session.post(url + "/login", data)
    dump_response(response)

    print("\n>>> Testing with username field but no password field")

    data["username"] = "TestUser"
    response = session.post(url + "/login", data)
    dump_response(response)

    print("\n>>> Testing with invalid username")

    data["username"] = "UnknownUser"
    data["password"] = "invalidpassword"
    response = session.post(url + "/login", data)
    dump_response(response)

    print("\n>>> Testing with invalid password")

    data["username"] = "TestUser"
    data["password"] = "invalidpassword"
    response = session.post(url + "/login", data)
    dump_response(response)

    print("\n>>> Testing successful login")

    data["username"] = "TestUser"
    data["password"] = "testpassword"
    response = session.post(url + "/login", data)
    dump_response(response)

    print("\n>>> Testing correct login when already logged in")

    data["username"] = "TestUser"
    data["password"] = "testpassword"
    response = session.post(url + "/login", data)
    dump_response(response)

    print("\n>>> Testing login as someone else when already logged in")

    data["username"] = "TestUser2"
    data["password"] = "testpassword"
    response = session.post(url + "/login", data)
    dump_response(response)

def main():
    test_login()

if __name__ == '__main__':
    main()
