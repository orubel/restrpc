
apiName = grailsApplication.config.apitoolkit.apiName
apiVersion = grailsApplication.metadata['app.version']


log4j = {
    error 'grails.app.controllers.net.nosegrind',
			  'grails.app.domain.net.nosegrind',
			  'grails.app.services.net.nosegrind.apitoolkit',
			  'grails.app.taglib.net.nosegrind.apitoolkit',
			  'grails.app.conf.your.package',
			  'grails.app.filters.your.package'
}

environments {
	development {

	}
	production {

	}
	test {
		
	}
}
grails.converters.default.pretty.print = true
grails.cache.enabled = true
grails.cache.clearAtStartup	= true
grails.cache.config = {
	cache {
		name 'ApiCache'
		eternal true
		overflowToDisk true
		maxElementsInMemory 10000
		maxElementsOnDisk 10000000
	}
 }

apitoolkit.apiName = 'api'
apitoolkit.protocol='http'

apitoolkit.apiobject.type = [
	"PKEY":["type":"Long","references":"self","description":"Primary Key"],
	"FKEY":["type":"Long","description":""],
	"INDEX":["type":"String","references":"self","description":"Foreign Key"],
	"String":["type":"String","description":"String"],
	"Long":["type":"Long","description":"Long"],
	"Boolean":["type":"Boolean","description":"Boolean"],
	"Float":["type":"Float","description":"Floating Point"],
	"BigDecimal":["type":"BigDecimal","description":"Big Decimal"],
	"URL":["type":"URL","description":"URL"],
	"Email":["type":"Email","description":"Email"]
]

grails.plugin.springsecurity.auth.loginFormUrl = "/${apiName}_v${apiVersion}/login/auth"
grails.plugin.springsecurity.auth.ajaxLoginFormUrl = "/${apiName}_v${apiVersion}/login/authAjax"
grails.plugin.springsecurity.failureHandler.defaultFailureUrl = '/'
grails.plugin.springsecurity.failureHandler.ajaxAuthFailUrl = '/'

grails.plugin.springsecurity.filterChain.chainMap = [
	"/${apiName}_v${apiVersion}/**": 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-securityContextHolderAwareRequestFilter,-rememberMeAuthenticationFilter,-anonymousAuthenticationFilter,-exceptionTranslationFilter',
	"/v${apiVersion}/**": 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-securityContextHolderAwareRequestFilter,-rememberMeAuthenticationFilter,-anonymousAuthenticationFilter,-exceptionTranslationFilter',
]

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	"/${apiName}_v${apiVersion}/**" : ['IS_AUTHENTICATED_ANONYMOUSLY'],
	"/v${apiVersion}/**" : ['IS_AUTHENTICATED_ANONYMOUSLY']
]
