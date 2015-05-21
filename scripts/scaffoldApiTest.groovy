import grails.util.GrailsNameUtils
import grails.util.Metadata
import grails.util.Holders as HOLDER

/*
 * Get apicache names and create scaffolded tests for controllers
 * based on cache names and I/O data
 */

includeTargets << grailsScript("_GrailsRun")
includeTargets << grailsScript("_GrailsBootstrap")

USAGE = """
Usage: grails scaffold-api-test

Scaffolds API Tests based on I/O State

Example: grails scaffold-api-test
"""

packageName = 'net.nosegrind.apitoolkit'
className = ''
templateDir = "$apiToolkitPluginDir/src/templates/tests"
appDir = "$basedir/grails-app/test/functional"

target(default: 'Scaffolds API Objects based on Controllers') {
	depends(checkVersion, configureProxy, packageApp, parseArguments)
	if (argsMap.https) {
		runAppHttps()
	}
	else {
		runApp()
		def grailsApplication = HOLDER.getGrailsApplication()
		for(controller in grailsApplication.controllerClasses) {
			println("controller:"+controller)
			def cName = controller.logicalPropertyName
			def cacheName = cName.replaceAll('Controller','').toLowerCase()
			
			//def serviceClass = grailsApp.getClassForName('net.nosegrind.apitoolkit.ApiCacheService')
			//def serviceClassMethod = serviceClass.metaClass.getMetaMethod('getCacheNames')
	
			//def apiCacheService = appCtx.getBean('apiCacheService')
			//def cacheNames = serviceClassMethod.invoke(apiCacheService,[] as Object[])
			def appCtx = ctx = HOLDER.applicationContext
			def cacheNames = appCtx.getBean('apiCacheService').getCacheNames()
	
			//def cache = serviceClassMethod.invoke(apiCacheService, [cacheName] as Object)
			
			//println(cache)
			// needed to determine i/o values and methods for template tests
			def adminRoles = grailsApp.config.apitoolkit.admin.roles
			def input = [:]
			def output = [:]
			
	
			
			//templateAttributes = [className: cName]
		}
		//startPluginScanner()
		//watchContext()
	}

	println """
	*************************************************************
	* API Tests successfully scaffolded.                        *
	*************************************************************
	"""
}

target(scaffoldIoState:'Scaffolds Basic REST Test Templates based on Available IO States'){
	println("### scaffoldIoState")
	//loadApp()
	//configureApp()
	
	def grailsApplication = HOLDER.getGrailsApplication()
	for(controller in grailsApp.controllerClasses) {
		println("controller:"+controller)
		def cName = controller.logicalPropertyName
		def cacheName = cName.replaceAll('Controller','').toLowerCase()
		
		//def serviceClass = grailsApp.getClassForName('net.nosegrind.apitoolkit.ApiCacheService')
		//def serviceClassMethod = serviceClass.metaClass.getMetaMethod('getCacheNames')

		//def apiCacheService = appCtx.getBean('apiCacheService')
		//def cacheNames = serviceClassMethod.invoke(apiCacheService,[] as Object[])
		def appCtx = ctx = HOLDER.applicationContext
		def cacheNames = appCtx.getBean('apiCacheService').getCacheNames()

		//def cache = serviceClassMethod.invoke(apiCacheService, [cacheName] as Object)
		
		//println(cache)
		// needed to determine i/o values and methods for template tests
		def adminRoles = grailsApp.config.apitoolkit.admin.roles
		def input = [:]
		def output = [:]
		

		
		//templateAttributes = [className: cName]
	}
	/*
	ant.mkdir dir: "$appDir/views/hook"
	// add default views for hooks administration
	copyFile "$templateDir/hook/create.gsp.template", "$appDir/views/hook/create.gsp"
	copyFile "$templateDir/hook/edit.gsp.template", "$appDir/views/hook/edit.gsp"
	copyFile "$templateDir/hook/list.gsp.template", "$appDir/views/hook/list.gsp"
	copyFile "$templateDir/hook/show.gsp.template", "$appDir/views/hook/show.gsp"

	String dir2 = packageToDir(packageName)
	generateFile "$templateDir/hook/HookController.groovy.template", "$appDir/controllers/${dir2}HookController.groovy"
	printMessage "Controller / Views created..."
	*/
}



//setDefaultTarget('scaffoldApiTest')