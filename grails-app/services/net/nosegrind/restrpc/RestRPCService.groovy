package net.nosegrind.restrpc

import grails.converters.JSON
import grails.converters.XML
import java.lang.reflect.Method
import java.util.HashSet;

import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import grails.plugin.cache.CachePut

import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.web.context.request.RequestContextHolder as RCH
import net.nosegrind.restrpc.Api
import net.nosegrind.restrpc.*


class RestRPCService{

	def grailsApplication
	def springSecurityService

	static transactional = false

	Long responseCode
	String responseMessage
	
	def getRequest(){
		return RCH.currentRequestAttributes().currentRequest
	}

	def getResponse(){
		return RCH.currentRequestAttributes().currentResponse
	}

	def getParams(){
		def params = RCH.currentRequestAttributes().params
		def request = getRequest()
		def json = request.JSON
		json.each() { key,value ->
			params[key] = value
		}
		return params
	}
	
	// api call now needs to detect request method and see if it matches anno request method
	boolean isApiCall(){
		def request = getRequest()
		def params = getParams()
		def queryString = request.'javax.servlet.forward.query_string'
		
		def uri
		if(request.isRedirected()){
			if(params.action=='index'){
				uri = (queryString)?request.forwardURI+'?'+queryString:request.forwardURI+'/'+params.action
			}else{
				uri = (queryString)?request.forwardURI+'?'+queryString:request.forwardURI
			}
		}else{
			uri = (queryString)?request.forwardURI+'?'+queryString:request.forwardURI
		}
		
		def api
		if(grailsApplication.config.grails.app.context=='/'){
			api = "/${grailsApplication.config.restrpc.apiName}/${grailsApplication.metadata['app.version']}/"
		}else if(grailsApplication.config?.grails?.app?.context){
			api = "${grailsApplication.config.grails.app.context}/${grailsApplication.config.restrpc.apiName}/${grailsApplication.metadata['app.version']}/"
		}else if(!grailsApplication.config?.grails?.app?.context){
			api = "/${grailsApplication.metadata['app.name']}/${grailsApplication.config.restrpc.apiName}/${grailsApplication.metadata['app.version']}/"
		}
		api += (params?.format)?"${params.format}/${params.controller}/${params.action}":"JSON/${params.controller}/${params.action}"
		api += (params.id)?"/${params.id}":""
		api += (queryString)?"?${queryString}":""

		println("${uri}==${api}")
		return uri==api
	}

	boolean isRequestMatch(String protocol){
		def request = getRequest()
		return request.method.toString()==protocol.toString()
	}
	
	// true=primary
	// false=foreign
	Integer getKey(String key){
		switch(key){
			case'FKey':
				return 2
				break
			case 'PKey':
				return 1
				break
			default:
				return 0
		}
	}
	
	/*
	 * Which annos declare this KEY as being 'received'.
	 * Check first in own controller then walk all others
	 */
	String createLinkRelationships(String paramType,String name,String controllername){
		def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllername)
		//def methods = controller?.getClazz().metaClass.methods*.name.sort().unique()
		for (Method method : controller.getClazz().getMethods()){
				if(method.isAnnotationPresent(Api)) {}
		}
	}
	
	Map formatModel(Object data){
		def newMap = [:]
		if(data && (!data?.response && !data?.metaClass && !data?.params)){
			data.each{ key, value ->
				if(value){
					if(grailsApplication.isDomainClass(value.getClass())){
						newMap[key]=value
					}else{
						if(value in java.util.Collection){
							if(value?.size()>0){
								if(grailsApplication.isDomainClass(value[0].getClass())){
									value.each{ k,v ->
										newMap[key][v.id]= v
									}
								}else{
									value = formatModel(value)
									newMap[key]= value
								}
							}
						}else{
							newMap[key]=value.toString()
						}
					}
				}
			}
		}
		return newMap
	}

	boolean validateUrl(String url){
		String[] schemes = ["http","https"]
		UrlValidator urlValidator = new UrlValidator(schemes)
		return urlValidator.isValid(url)
	}
	
	boolean checkAuthority(HashSet set){
		def roles = set as List
		if(roles.size()>0 && roles[0].trim()){
			def roles2 = grailsApplication.getDomainClass(grailsApplication.config.grails.plugins.springsecurity.authority.className).clazz.list().authority
			def finalRoles
			def userRoles
			if (springSecurityService.isLoggedIn()){
				userRoles = springSecurityService.getPrincipal().getAuthorities()
			}
			
			if(userRoles){
				def temp = roles2.intersect(roles as Set)
				finalRoles = temp.intersect(userRoles)
				if(finalRoles){
					return true
				}else{
					return false
				}
			}else{
				return false
			}
		}else{
			return true
		}
	}
	
	def flushAllApiCache(){
		grailsApplication.controllerClasses.each { controllerClass ->
			String controllername = controllerClass.logicalPropertyName
			if(controllername!='aclClass'){
				def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllername)
				flushApiCache(controllername)
			}
		}
	}
	
	@CacheEvict(value="ApiCache",key="#controllername")
	def flushApiCache(String controllername){
		// flush and reset
		// setApiCache()
		setApiCache(controllername)
	}
	
	//@CachePut(value="ApiCache",key="#controllername")
	def setApiCache(controllername){
		def apiOutput = []
		def inc = 0

		if(controllername!='aclClass'){
			
			def controller = grailsApplication.getArtefactByLogicalPropertyName('Controller', controllername)
			//def methods = controller?.getClazz().metaClass.methods*.name.sort().unique()
			for (Method method : controller.getClazz().getMethods()){
					if(method.isAnnotationPresent(Api)) {
						def temp = getApiCache(controllername,method)
					}
			}
		}
	}
	
	String getBelongsTo(String paramType, String controller, String belongsTo){
		return (paramType=='PKey')?controller:belongsTo
	}
	
	@Cacheable(value="ApiCache",key="#controllername")
	def getApiCache(String controllername, Method method) {
		def action = method.getName().toString()
		def api = method.getAnnotation(Api)

		def apiList = ["${controllername}":["${action}":["api":[
						"requestMethod":"${api.method()}",
						"description":"${api.description()}",
						"receives":[],
						"returns":[],
						"errors":[]
					]
				]
			]
		]
		
		// RECEIVES
		api.receives().each{ p ->
			if (p.paramType()) {
				def belongsTo = getBelongsTo(p.paramType().toString(), controllername, p.belongsTo().toString())
				def list = [type:"${p.paramType()}",name:"${p.name()}",description:"${p.description()}",mockData:"${p.mockData()}",belongsTo:"${belongsTo}",roles:[],required:"${p.required()}",values:[]]
				
				if(p?.values()){
					def params2 = p.values()
					def values = []
					params2.each{ p2 ->
						if (p2.paramType()) {
							def belongsTo2 = getBelongsTo(p2.paramType().toString(), controllername, p2.belongsTo().toString())
							def pm2 = [type:"${p2.paramType()}",name:"${p2.name()}",description:"${p2.description()}",mockData:"${p2.mockData()}",belongsTo:"${belongsTo2}",roles:[],required:"${p2.required()}"]
							values.add(pm2)
						}
					}
					list.values.add(values)
				}
				apiList.get("${controllername}").get("${action}")["api"]["receives"].push(list)
			}
		}

		
		// RETURNS
		api.returns().each{ p ->
			if (p.paramType()) {
				def belongsTo = getBelongsTo(p.paramType().toString(), controllername, p.belongsTo().toString())
				def list = [type:"${p.paramType()}",name:"${p.name()}",description:"${p.description()}",mockData:"${p.mockData()}",belongsTo:"${belongsTo}",roles:[],required:"${p.required()}",values:[]]
				
				if(p?.values()){
					def values = []
					p.values().each{ p2 ->
						if (p2.paramType()) {
							def belongsTo2 = getBelongsTo(p2.paramType().toString(), controllername, p2.belongsTo().toString())
							def pm2 = [type:"${p2.paramType()}",name:"${p2.name()}",description:"${p2.description()}",mockData:"${p2.mockData()}",belongsTo:"${belongsTo2}",roles:[],required:"${p2.required()}"]
							values.add(pm2)
						}
					}
					list.values.add(values)
				}
				apiList.get("${controllername}").get("${action}")["api"]["returns"].push(list)
			}
		}
		
		// ERRORS
		api.errors().each{ p ->
			if (p.code()) {
				def list = [code:"${p.code()}",description:"${p.description()}"]
				apiList.get("${controllername}").get("${action}")["api"]["errors"].push(list)
			}
		}

		//println("apioutput >> ${apiList}")
		return (apiList)?apiList:null
	}
}
