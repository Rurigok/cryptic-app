#!/usr/bin/env python3
import requests

from tests import dump_response, url, assert_result

def test_create_account():

    print(">>> Testing account creation")

    data = {}
    session = requests.Session()

    response = session.post(url + "/create-account", data)
    assert_result(response, False)

    data["username"] = "NewUser"
    response = session.post(url + "/create-account", data)
    assert_result(response, True)

def main():
    test_create_account()

if __name__ == '__main__':
    main()
