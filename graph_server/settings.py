#!/usr/bin/env python
# -*- coding:utf-8 -*-
import os
import sys

BASE_DIR = os.path.dirname(__file__)
LIBS_DIR = os.path.join(BASE_DIR, 'libs')

sys.path.append(BASE_DIR)
sys.path.append(LIBS_DIR)


SETTINGS = {
    # debug配置
    'debug': False,
    'autoreload': False,
    'debug_host': '127.0.0.1',

    # template配置
    'template_path': os.path.join(BASE_DIR, 'templates')
}
