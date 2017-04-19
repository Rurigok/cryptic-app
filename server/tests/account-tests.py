import json, requests

url = "http://localhost:5678"

def dump_response(response):
    print("HTTP {}: {}".format(response.status_code, response.text))

def test_create_account():

    print(">>> Testing account creation")

    data = {}
    response = requests.post(url + "/create-account", data)

def test_login():

    print(">>>>>> Testing login")

    data = {}

    print(">>> Testing with no request fields")

    response = requests.post(url + "/login", data)
    dump_response(response)

    print(">>> Testing with username field but no password field")

    data["username"] = "TestUser"
    response = requests.post(url + "/login", data)
    dump_response(response)

    print(">>> Testing with invalid username")

    data["username"] = "UnknownUser"
    data["password"] = "invalidpassword"
    response = requests.post(url + "/login", data)
    dump_response(response)

    print(">>> Testing with invalid password")

    data["username"] = "TestUser"
    data["password"] = "invalidpassword"
    response = requests.post(url + "/login", data)
    dump_response(response)

    print(">>> Testing successful login")

    data["username"] = "TestUser"
    data["password"] = "testpassword"
    response = requests.post(url + "/login", data)
    dump_response(response)

    print(">>> Testing correct login when already logged in")

    data["username"] = "TestUser"
    data["password"] = "testpassword"
    response = requests.post(url + "/login", data)
    dump_response(response)

    print(">>> Testing login as someone else when already logged in")

    data["username"] = "TestUser2"
    data["password"] = "testpassword"
    response = requests.post(url + "/login", data)
    dump_response(response)

def main():
    test_login()

if __name__ == '__main__':
    main()
