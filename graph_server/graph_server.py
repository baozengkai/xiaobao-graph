#!/usr/bin/env python
# -*- coding:utf-8 -*-

import os
import tornado.web
import tornado.httpserver
import tornado.ioloop
import router
import settings
from log.log_graph import My_Logger
from arpylibs import langlib


if __name__ == '__main__':

    application = tornado.web.Application(router.ROUTER, **settings.SETTINGS)
    server = tornado.httpserver.HTTPServer(application, xheaders=True)

    if settings.SETTINGS['debug'] or settings.SETTINGS['autoreload']:
        server.listen(11004)
        server.start(1)
    else:
        server.bind(11004)
        server.start(1)

    # 初始化国际化语言
    langlib.init_language(local_path=os.path.dirname(os.path.realpath(__file__)))

    tornado.ioloop.IOLoop.instance().start()
