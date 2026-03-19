import json
from enum import Enum
import os

class UniquenessTypes(Enum):
    MUST_BE_UNIQUE = 1
    MUST_NOT_BE_UNIQUE = 2
    ANY_VALUE = 3

class Expected:
    #if count is -1 then any count is allowed
    def __init__(self, prop, count = 1, how_unique = UniquenessTypes.ANY_VALUE, type_of_value="str", optional=True, clear_outside_scope=True):
        self.prop = prop
        self.count = count
        self.how_unique = how_unique
        self.type_of_value = type_of_value
        self.optional = optional
        self.clear_outside_scope = clear_outside_scope
        self.children = []
        self.is_child = False
        self.usage_count = 0
        self.values = {}


    def add_child_element(self, expected_child):
        self.children.append(expected_child)
        expected_child.is_child = True

    def check_uniqueness(self, value):
        value = str(value)

        if(self.how_unique == UniquenessTypes.ANY_VALUE):
            return True

        if(self.how_unique == UniquenessTypes.MUST_BE_UNIQUE):
            if(value in self.values):
                return False
            else:
                self.values[value] = 1
                return True

        if(self.how_unique == UniquenessTypes.MUST_NOT_BE_UNIQUE):
            if(value in self.values):
                return True
            elif(len(self.values.keys) == 0):
                self.values[value] = 1
                return True
            else:
                return False

        return True

    def can_be_used_more(self):
        if(self.count == -1):
            return True
        elif(self.usage_count + 1 > self.count):
            return False
        return True

    def is_type(self, string):
        if(self.type_of_value == "bool" and "str" in string):
            return True

        return self.type_of_value in string

expected_list = {
    "start": Expected(""),
    "enabled": Expected("enabled", type_of_value="bool"),
    "neededInstalledMods": Expected("neededInstalledMods", type_of_value="list"),
    "neededUninstalledMods": Expected("neededUninstalledMods", type_of_value="list"),
    "spawns": Expected("spawns", type_of_value="list"),
    "id": Expected("id", count=-1, how_unique=UniquenessTypes.MUST_BE_UNIQUE, optional = False, clear_outside_scope = False),
    "pokemon": Expected("pokemon"),
    "presets": Expected("presets", type_of_value="list"),
    "type": Expected("type"),
    "context": Expected("context"),
    "bucket": Expected("bucket"),
    "level": Expected("level"),
    "weight": Expected("weight", type_of_value="float"),
    "weightMultiplier": Expected("weightMultiplier", type_of_value="dict"),
    "weightMultipliers": Expected("weightMultipliers", type_of_value="list"),
    "condition": Expected("condition", type_of_value="dict"),
    "anticondition": Expected("anticondition", type_of_value="dict"),
    "biomes": Expected("biomes", type_of_value="list"),
    "drops": Expected("drops", type_of_value="dict")
}

start = expected_list["start"]
start.add_child_element(expected_list["enabled"])
start.add_child_element(expected_list["neededInstalledMods"])
start.add_child_element(expected_list["neededUninstalledMods"])
start.add_child_element(expected_list["spawns"])

spawns = expected_list["spawns"]
spawns.add_child_element(expected_list["id"])
spawns.add_child_element(expected_list["pokemon"])
spawns.add_child_element(expected_list["presets"])
spawns.add_child_element(expected_list["type"])
spawns.add_child_element(expected_list["context"])
spawns.add_child_element(expected_list["bucket"])
spawns.add_child_element(expected_list["level"])
spawns.add_child_element(expected_list["weight"])
spawns.add_child_element(expected_list["weightMultiplier"])
spawns.add_child_element(expected_list["weightMultipliers"])
spawns.add_child_element(expected_list["condition"])
spawns.add_child_element(expected_list["anticondition"])
spawns.add_child_element(expected_list["biomes"])
spawns.add_child_element(expected_list["drops"])

def iterate_over_object(obj, start, filename):
    checked_list = []
    for child in start.children:
        checked_list.append(child.prop)
        if(child.prop in obj):
            if(not child.can_be_used_more):
                print(filename + ": Too many '" + child.prop + "'")
            child.usage_count += 1

            if(child.check_uniqueness(obj[child.prop])):
                child.values[str(obj[child.prop])] = 1
            else:
                print(filename + ": Value '" + obj[child.prop] + "' invalidated by uniqeness type of '" + child.prop + "'")


            if(not child.is_type(str(type(obj[child.prop])))):
                print(filename + ": Property '" + child.prop + "' is the wrong type. Expected '" + child.type_of_value + "', got '" + str(type(obj[child.prop])))

            if(len(child.children) > 0):
                if("list" in str(type(obj[child.prop]))):
                    for obj_child in obj[child.prop]:
                        iterate_over_object(obj_child, child, filename)
                else:
                    iterate_over_object(child, obj[child.prop], filename)
        elif(not child.optional):
            print(filename + ": MISSING: " + child.prop)

    for child in start.children:
        if(child.clear_outside_scope):
            child.values = {}
            child.usage_count = 0

    for key in obj.keys():
        if(not key in checked_list):
            print(filename + ": Extra property: '" + key + "'")

folder_path = "../common/src/main/resources/data/cobblemon/spawn_pool_world/"

files = os.listdir(folder_path)

for f in files:
    file = open(folder_path + f, "r")
    json_as_string = file.read()
    json_object = json.loads(json_as_string)
    iterate_over_object(json_object, start, f)


