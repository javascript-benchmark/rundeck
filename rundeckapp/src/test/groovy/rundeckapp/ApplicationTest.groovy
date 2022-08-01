/*
 * Copyright 2018 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rundeckapp

import org.springframework.core.env.Environment
import org.springframework.core.env.MutablePropertySources
import org.springframework.core.env.StandardEnvironment
import rundeckapp.init.RundeckInitConfig
import rundeckapp.init.RundeckInitializer
import spock.lang.Specification


class ApplicationTest extends Specification {

    def "setEnvironment with both property file and groovy"() {
        when:
        File tmpCfgDir = File.createTempDir()
        File tmpGroovy = new File(tmpCfgDir, "rundeck-config.groovy")
        tmpGroovy << "grails { mail {} } "
        File tmpProp = File.createTempFile("app-test",".properties")
        tmpProp << "myprop=avalue"
        System.setProperty(RundeckInitConfig.SYS_PROP_RUNDECK_CONFIG_LOCATION,tmpProp.absolutePath)
        System.setProperty(RundeckInitConfig.SYS_PROP_RUNDECK_SERVER_CONFIG_DIR,tmpCfgDir.absolutePath)

        Application.rundeckConfig = new RundeckInitConfig()
        Properties runtimeProps = new Properties()
        runtimeProps.setProperty(RundeckInitializer.PROP_REALM_LOCATION,"fake")
        runtimeProps.setProperty(RundeckInitializer.PROP_LOGINMODULE_NAME,"fake")
        Application.rundeckConfig.runtimeConfiguration = runtimeProps
        Application app = new Application()
        app.metaClass.initialize = { -> }
        app.metaClass.loadJdbcDrivers = { -> }
        TestEnvironment env = new TestEnvironment()

        app.setEnvironment(env)
        List<String> propertiesLoaded = env.propertySources.iterator().collect { it.name }

        then:
        propertiesLoaded.size() == 2
        propertiesLoaded.contains("rundeck.config.location")
        propertiesLoaded.contains("rundeck-config-groovy")
    }

    def "load rundeck-config.properties if set in system property RDECK_CONFIG_LOCATION"() {

        when:
        File tmpProp = File.createTempFile("app-test",".properties")
        tmpProp << "myprop=avalue"
        Application app = new Application()
        System.setProperty(RundeckInitConfig.SYS_PROP_RUNDECK_CONFIG_LOCATION,tmpProp.absolutePath)
        Properties props = app.loadRundeckPropertyFile()

        then:
        props.get("myprop") == "avalue"

    }

    def "do not try to load rundeck-config.groovy if set in system property RDECK_CONFIG_LOCATION"() {

        when:
        File tmpGroovy = File.createTempFile("app-test",".groovy")
        tmpGroovy << "grails { mail {} } "
        Application app = new Application()
        System.setProperty(RundeckInitConfig.SYS_PROP_RUNDECK_CONFIG_LOCATION,tmpGroovy.absolutePath)
        Properties props = app.loadRundeckPropertyFile()

        then:
        props.isEmpty()

    }

    class TestEnvironment extends StandardEnvironment {
        MutablePropertySources propertySources = new MutablePropertySources()

        @Override
        public MutablePropertySources getPropertySources() {
            return this.propertySources;
        }

    }

}
