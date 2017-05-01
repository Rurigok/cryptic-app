""" Basic template for JSON responses. """

import json

class JSONResponse:

    def __init__(self, success=False, message=""):
        self.success = success
        self.message = message

    def to_json(self):
        """ Returns this JSONResponse as a JSON string. """
        return json.dumps(self.__dict__)
