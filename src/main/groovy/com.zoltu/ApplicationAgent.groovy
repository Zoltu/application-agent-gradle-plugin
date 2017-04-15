package com.zoltu

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.tasks.testing.Test

class ApplicationAgentPlugin implements Plugin<Project> {
	void apply(Project project) {
		project.getPlugins().apply(ApplicationPlugin.class)

		project.extensions.create("applicationAgent", ApplicationAgentPluginExtension)

		project.configurations {
			agent
			runtime.extendsFrom(agent)
		}

		project.startScripts {
			doLast {
				if (!project.applicationAgent.applyToStartScripts)
					return

				project.configurations.agent.each { agent ->
					String agentFileName = agent.name

					String forwardSlash = "/"
					String unixRegex = $/exec "$$JAVACMD" /$
					String unixReplacement = $/exec "$$JAVACMD" -javaagent:"$$APP_HOME/lib${forwardSlash}${agentFileName}" /$
					unixScript.text = unixScript.text.replace(unixRegex, unixReplacement)

					String windowsRegex = $/"%JAVA_EXE%" %DEFAULT_JVM_OPTS%/$
					String windowsReplacement = $/"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -javaagent:"%APP_HOME%\lib\$agentFileName"/$
					windowsScript.text = windowsScript.text.replace(windowsRegex, windowsReplacement)
				}
			}
		}

		project.tasks.withType(Test) {
			doFirst {
				if (!project.applicationAgent.applyToTests)
					return

				project.configurations.agent.each { agent ->
					jvmArgs += [ "-javaagent:${agent.path}" ]
				}
			}
		}

		project.tasks.run {
			doFirst {
				if (!project.applicationAgent.applyToRun)
					return

				project.configurations.agent.each { agent ->
					project.applicationDefaultJvmArgs += [ "-javaagent:${agent.path}" ]
				}
			}
		}
	}
}

class ApplicationAgentPluginExtension {
	Boolean applyToTests = true
	Boolean applyToRun = true
	Boolean applyToStartScripts = true
}
