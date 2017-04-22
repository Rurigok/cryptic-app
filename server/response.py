""" Basic template for JSON responses. """

import json

class JSONResponse:

    def __init__(self, success=False, message=""):
        self.success = success
        self.message = message

    def to_json(self):
        """ Returns this JSONResponse as a JSON string. """

        response = {}
        response["success"] = self.success
        if self.message: # if message field is not empty
            response["message"] = self.message

        # TODO: detect custom attributes and turn into json or make this
        # extend dict or something...

        return json.dumps(response)
