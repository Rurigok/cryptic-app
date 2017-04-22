#!/usr/bin/env python3
import requests

from tests import dump_response, url, assert_result

def test_login():

    print(">>>>>> Testing login")

    data = {}
    session = requests.Session()

    print(">>> Testing with no request fields")

    response = session.post(url + "/login", data)
    assert_result(response, False, "login with no fields set should always fail")

    print("\n>>> Testing with username field but no password field")

    data["username"] = "TestUser"
    response = session.post(url + "/login", data)
    assert_result(response, False)

    print("\n>>> Testing with invalid username")

    data["username"] = "UnknownUser"
    data["password"] = "invalidpassword"
    response = session.post(url + "/login", data)
    assert_result(response, False)

    print("\n>>> Testing with invalid password")

    data["username"] = "TestUser"
    data["password"] = "invalidpassword"
    response = session.post(url + "/login", data)
    assert_result(response, False)

    print("\n>>> Testing successful login")

    data["username"] = "TestUser"
    data["password"] = "testpassword"
    response = session.post(url + "/login", data)
    assert_result(response, True)

    print("\n>>> Testing correct login when already logged in")

    data["username"] = "TestUser"
    data["password"] = "testpassword"
    response = session.post(url + "/login", data)
    assert_result(response, True)

    print("\n>>> Testing login as someone else when already logged in")

    data["username"] = "TestUser2"
    data["password"] = "testpassword"
    response = session.post(url + "/login", data)
    assert_result(response, False)

def main():
    test_login()

if __name__ == '__main__':
    main()
