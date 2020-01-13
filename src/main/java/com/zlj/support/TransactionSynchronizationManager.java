/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zlj.support;

import org.springframework.core.NamedThreadLocal;
import org.springframework.transaction.support.ResourceHolder;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {


    private static final ThreadLocal<Map<Object, Object>> resources =
        new NamedThreadLocal<Map<Object, Object>>("Transactional resources");


    public static Object getResource(Object key) {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        // Transparently remove ResourceHolder that was marked as void...
        if (value instanceof ResourceHolder && ((ResourceHolder) value).isVoid()) {
            map.remove(key);
            // Remove entire ThreadLocal if empty...
            if (map.isEmpty()) {
                resources.remove();
            }
            value = null;
        }
        return value;
    }


    public static void bindResource(Object key, Object value) throws IllegalStateException {
        Assert.notNull(value, "Value must not be null");
        Map<Object, Object> map = resources.get();
        // set ThreadLocal Map if none found
        if (map == null) {
            map = new HashMap<Object, Object>();
            resources.set(map);
        }
        map.put(key, value);
    }

    public static Object unbindResource(Object key) throws IllegalStateException {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            return null;
        }
        Object value = map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }
        return value;
    }

}
