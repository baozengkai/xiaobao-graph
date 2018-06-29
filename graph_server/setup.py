#!/usr/bin/env python
# -*- coding:utf-8 -*-

"""
    版权说明：Copyright (c) 2014 AnyRobot, EISOO
    文件作者: bao.zengkai@eisoo.com
    @file: setup.py
    @time: 2018/1/19 10:19
"""
import os
import sys
from cx_Freeze import setup, Executable

dirname = None
if getattr(sys, 'frozen', False):
    dirname = os.path.dirname(sys.executable)
else:
    dirname = os.path.dirname(os.path.realpath(__file__))
root_path = os.path.dirname(os.path.dirname(dirname))
sys.path.append(root_path + '/anyrobot-graph')
sys.path.append(root_path + '/anyrobot-graph/anyrobot-pylibs')
sys.path.append('.')


# Dependencies are automatically detected, but it might need fine tuning.
buildOptions = dict(packages=['modules'], includes=[], excludes=['collections.sys', 'collections._weakref'])

base = 'Console'
targetName = 'graph_server'

executables = [
    Executable(dirname + '/graph_server.py', base=base, targetName=targetName)
]

setup(name='graph server',
      version='0.1.0',
      description='graph server',
      options=dict(build_exe=buildOptions),
      executables=executables)
