package com.eisoo.rainbow.query;

import org.json.JSONObject;

import java.util.*;

/**
 * @author tong.sha@eisoo.com
 * @version 1.0 , Copyright (c) 2014 AnyRobot, EISOO
 * @date 2018.02.28
 */

public interface SourceProvider {
    void init(final JSONObject dataSetConf) throws Exception;
    List<Map<String, Object>> search(final SourceSearchHolder sourceSearchHolder);
    long searchCount(final SourceSearchHolder sourceSearchHolder);
    Map<Object, Long> searchGroupCount(final SourceSearchHolder searchHolder, final String byField);
}
