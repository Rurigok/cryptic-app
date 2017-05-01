import json

url = "http://localhost/cryptic"

def dump_response(response):
    print("HTTP {}: {}".format(response.status_code, response.text))
    #print(response.cookies)

def assert_result(response, expected_result, comment="", dump=True):
    if dump:
        dump_response(response)
    json_str = response.text
    json_obj = json.loads(json_str)
    assert expected_result == json_obj["success"], comment
    print("Test successful: " + comment)
