import os
import sys
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
UNIT_DIR = os.path.realpath(os.path.join(BASE_DIR, "../"))
PROJECT_DIR = os.path.realpath(os.path.join(BASE_DIR, "../../"))
sys.path.append(UNIT_DIR)
sys.path.append(PROJECT_DIR)