/*
 * Copyright 2011-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.plugins.mybatis

import org.apache.ibatis.session.SqlSessionFactory

import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder
import static griffon.util.GriffonNameUtils.isBlank

/**
 * @author Andres Almiray
 */
class MybatisHolder {
    private static final String DEFAULT = 'default'
    private static final Object[] LOCK = new Object[0]
    private final Map<String, SqlSessionFactory> sessionFactories = [:]

    private static final MybatisHolder INSTANCE

    static {
        INSTANCE = new MybatisHolder()
    }

    static MybatisHolder getInstance() {
        INSTANCE
    }

    private MybatisHolder() {}

    String[] getSqlSessionFactoryNames() {
        List<String> sessionFactoryNames = new ArrayList().addAll(sessionFactories.keySet())
        sessionFactoryNames.toArray(new String[sessionFactoryNames.size()])
    }

    SqlSessionFactory getSqlSessionFactory(String sessionFactoryName = DEFAULT) {
        if (isBlank(sessionFactoryName)) sessionFactoryName = DEFAULT
        retrieveSqlSessionFactory(sessionFactoryName)
    }

    void setSqlSessionFactory(String sessionFactoryName = DEFAULT, SqlSessionFactory sf) {
        if (isBlank(sessionFactoryName)) sessionFactoryName = DEFAULT
        storeSqlSessionFactory(sessionFactoryName, sf)
    }

    boolean isSqlSessionFactoryAvailable(String sessionFactoryName) {
        if (isBlank(sessionFactoryName)) sessionFactoryName = DEFAULT
        retrieveSqlSessionFactory(sessionFactoryName) != null
    }

    void disconnectSqlSessionFactory(String sessionFactoryName) {
        if (isBlank(sessionFactoryName)) sessionFactoryName = DEFAULT
        storeSqlSessionFactory(sessionFactoryName, null) 
    }

    SqlSessionFactory fetchSqlSessionFactory(String sessionFactoryName) {
        if (isBlank(sessionFactoryName)) sessionFactoryName = DEFAULT
        SqlSessionFactory sf = retrieveSqlSessionFactory(sessionFactoryName)
        if (sf == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = MybatisConnector.instance.createConfig(app)
            sf = MybatisConnector.instance.connect(app, config, sessionFactoryName)
        }

        if (sf == null) {
            throw new IllegalArgumentException("No such SqlSessionFactory configuration for name $sessionFactoryName")
        }
        sf
    }

    private SqlSessionFactory retrieveSqlSessionFactory(String sessionFactoryName) {
        synchronized(LOCK) {
            sessionFactories[sessionFactoryName]
        }
    }

    private void storeSqlSessionFactory(String sessionFactoryName, SqlSessionFactory sf) {
        synchronized(LOCK) {
            sessionFactories[sessionFactoryName] = sf
        }
    }
}
