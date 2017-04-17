""" Basic template for JSON responses. """

import json

class JSONResponse:

    def __init__(self, action, success=False, message=""):
        self.action = action
        self.success = success
        self.message = message

    def to_json(self):
        """ Returns this JSONResponse as a JSON string. """

        response = {}
        response["action"] = self.action
        response["success"] = self.success
        if self.message: # if message field is not empty
            response["message"] = self.message

        return json.dumps(response)
