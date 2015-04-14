import grails.util.GrailsNameUtils
import grails.util.Metadata

includeTargets << grailsScript("_GrailsInit")

includeTargets << new File("$springSecurityCorePluginDir/scripts/_S2Common.groovy")

USAGE = """
Usage: grails init-api-master <user-domain-class-package> <user-class-name> <role-class-name>

Takes 3 arguments of the spring-security USER package, USER classname, ROLE classname 
and then it creates webhook domain, controller, views and supporting services

Example: grails init-api-master com.yourapp User Role
"""

packageName = ''
userClassName = ''
roleClassName = ''
nosqlName = ''

templateDir = "$apiToolkitPluginDir/src/templates/webhook"
appDir = "$basedir/grails-app"

target(initApiMaster: 'Creates artifacts for webhooks') {
	if (!configure()) {
		return 1
	}

	createDomains()
	copyControllersAndViews()
	updateConfig()

	printMessage """
	*************************************************************
	* SUCCESS! Created domain classes, controllers, and GSPs.   *
	* Webhooks are now installed. Please see documentation *
	* page on implementation details.                           *
	*************************************************************
	"""
}

private boolean configure() {
	def argValues = parseArgs()
	if (!argValues) {
		return false
	}

	if (argValues.size() == 3) {
		(packageName, userClassName,roleClassName) = argValues
	}else {
		return false
	}

	templateAttributes = [packageName: packageName,userClassName: userClassName,roleClassName:roleClassName]

	true
}

target(createDomains:"Create API WebHook Domains") {
	String dir = packageToDir(packageName)
	printMessage "$templateDir"
	generateFile "$templateDir/Hook.groovy.template", "$appDir/domain/${dir}Hook.groovy"
	generateFile "$templateDir/HookRole.groovy.template", "$appDir/domain/${dir}HookRole.groovy"
	printMessage "Domains created..."
}

target(copyControllersAndViews:"Create API Controllers and Views for webhooks") {
	ant.mkdir dir: "$appDir/views/hook"
	// add default views for webhooks administration
	copyFile "$templateDir/create.gsp.template", "$appDir/views/hook/create.gsp"
	copyFile "$templateDir/edit.gsp.template", "$appDir/views/hook/edit.gsp"
	copyFile "$templateDir/list.gsp.template", "$appDir/views/hook/list.gsp"
	copyFile "$templateDir/show.gsp.template", "$appDir/views/hook/show.gsp"

	String dir2 = packageToDir(packageName)
	generateFile "$templateDir/HookController.groovy.template", "$appDir/controllers/${dir2}HookController.groovy"
	printMessage "Controller / Views created..."
}

target(updateConfig:"Update Config for API Master Setup") {
	ant.mkdir dir: "${userHome}/.iostate"
	def configFile = new File(appDir, 'conf/Config.groovy')
	if (configFile.exists()) {
		configFile.withWriterAppend {
			it.writeLine '\n// Added by the Api Toolkit plugin:'
			it.writeLine ' '
			it.writeLine "apitoolkit.webhook.domain = '${packageName}.Hook'"
			it.writeLine "apitoolkit.webhook.controller = '${packageName}.HookController'"
			it.writeLine " "
			it.writeLine "apitoolkit.iostate.preloadDir=[\"file:${userHome}/.iostate\"]"
			it.writeLine "apitoolkit.sharedCache.type='mongo'"
			it.writeLine "apitoolkit.sharedCache.url='127.0.0.1'"
			it.writeLine "apitoolkit.sharedCache.port=27017"
			it.writeLine "apitoolkit.sharedCache.user='changeUsername'"
			it.writeLine "apitoolkit.sharedCache.password='changePassword'"
		}
	}
}

private parseArgs() {
	def args = argsMap.params
	if (3 == args.size()) {
		printMessage "Creating classes in package ${args[0]}..."
		return args
	}

	errorMessage USAGE
	null
}

setDefaultTarget('initApiMaster')
