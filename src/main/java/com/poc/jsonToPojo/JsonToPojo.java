package com.poc.jsonToPojo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonToPojo{
	private static Map<String, Class<?>> classBySimpleName = new HashMap<String, Class<?>>();
	private static Map<String, HashMap<String, Class<?>>> classPropsMap = new LinkedHashMap<String, HashMap<String, Class<?>>>();

	private static final String packageName = "com.practice.dynamic.";
	private static final String filePath = "Sample_JSON.txt";

	public static void main(String args[]) throws IOException{
		String input = null;
		try {
			getAllJavaClassMap();
			input = getFileData();
			JSONObject mainObj = new JSONObject(input);
			parseJSONObject(null, mainObj, classPropsMap);
			System.out.println(classPropsMap);
			generatePojo(classPropsMap);
		} catch (ClassNotFoundException e) {
			System.out.println(e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void parseJSONObject(String prevKey, JSONObject jsonObj, Map<String, HashMap<String, Class<?>>> classPropsMap){
		Iterator superKeySet = jsonObj.keys();
		HashMap<String, Class<?>> innerMap = null;
		String currentKey = null;
		while(superKeySet.hasNext()){
			currentKey = (String)superKeySet.next();
			if(null!=currentKey){
				if(classPropsMap.containsKey(prevKey)){
					if(jsonObj.get(currentKey).getClass().getName().contains("JSON")){
						classPropsMap.get(prevKey).put(currentKey, jsonObj.get(currentKey).getClass());
						if(classPropsMap.get(currentKey)==null){
							classPropsMap.put(currentKey, new HashMap<String, Class<?>>());
						}
						else{
							classPropsMap.put(currentKey, classPropsMap.get(currentKey));
						}
					}
					else{
						Class<?> javaClassType = classBySimpleName.get((String) jsonObj.get(currentKey));
						//if(javaClassType)
						if(null==javaClassType || "".equals(javaClassType)){
							javaClassType = classBySimpleName.get("String");
						}
						classPropsMap.get(prevKey).put(currentKey, javaClassType);
						continue;
					}
				}				
				else{
					innerMap= new HashMap<String, Class<?>>();
					classPropsMap.put(currentKey, innerMap);
				}
				checkValueTypeAndParse(currentKey, jsonObj, classPropsMap);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static void parseJSONArray(String prevKey, JSONArray jsonArray, Map<String, HashMap<String, Class<?>>> classPropsMap)
	{
		String currentKey = null;
		JSONObject jsonObj= null;
		Iterator keySet = null;
		HashMap<String, Class<?>> innerMap = null;

		for(int i=0; i<jsonArray.length() ; i++){
			jsonObj= (JSONObject) jsonArray.get(i);
			keySet = jsonObj.keys();
			while(keySet.hasNext()){
				currentKey = (String)keySet.next();
				if(null!=currentKey){
					if(classPropsMap.containsKey(prevKey)){
						if(jsonObj.get(currentKey).getClass().getName().contains("JSON")){
							classPropsMap.get(prevKey).put(currentKey, jsonObj.get(currentKey). getClass());
							if(classPropsMap.get(currentKey)==null){
								classPropsMap.put(currentKey, new HashMap<String, Class<?>>());
							}
							else{
								classPropsMap.put(currentKey, classPropsMap.get(currentKey));
							}
						}
						else{
							Class<?> javaClassType = classBySimpleName.get((String) jsonObj.get(currentKey));
							if(null==javaClassType || "".equals(javaClassType)){
								javaClassType = classBySimpleName.get("String");
							}
							classPropsMap.get(prevKey).put(currentKey, javaClassType);
							continue;
						}
					}				
					else{
						innerMap= new HashMap<String, Class<?>>();
						classPropsMap.put(currentKey, innerMap);
					}
					checkValueTypeAndParse(currentKey, jsonObj, classPropsMap);
				}
			}
		}
	}

	public static void checkValueTypeAndParse(String currentKey, JSONObject jsonObj,  Map<String, HashMap<String, Class<?>>> classPropsMap)
	{
		if(jsonObj.get(currentKey) instanceof JSONObject){
			parseJSONObject(currentKey, jsonObj.getJSONObject(currentKey), classPropsMap);
		}
		else if(jsonObj.get(currentKey) instanceof JSONArray){
			parseJSONArray(currentKey, jsonObj.getJSONArray(currentKey), classPropsMap);
		}
	}

	public static void getAllJavaClassMap() throws ClassNotFoundException{
		classBySimpleName.put("String", Class.forName("java.lang.String"));
		classBySimpleName.put("Number", Class.forName("java.lang.Number"));
		classBySimpleName.put("Integer", Class.forName("java.lang.Integer"));
		classBySimpleName.put("Short", Class.forName("java.lang.Short"));
		classBySimpleName.put("Long", Class.forName("java.lang.Long"));
		classBySimpleName.put("Double", Class.forName("java.lang.Double"));
		classBySimpleName.put("Float", Class.forName("java.lang.Float"));
		classBySimpleName.put("Boolean", Class.forName("java.lang.Boolean"));
		classBySimpleName.put("Date", Class.forName("java.util.Date"));
	}

	public static String getFileData() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader("src/main/resources/"+filePath));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}

	public static void generatePojo(Map<String, HashMap<String, Class<?>>> classPropsMap)
	{
		try{
			List<String> listOfClasses = null;
			Map<String, HashMap<String, Class<?>>> classRemaining = classPropsMap;
			Map<String, HashMap<String, Class<?>>> classesGenerated = new HashMap<String, HashMap<String, Class<?>>>();
			while(classRemaining.size() > 0){
				listOfClasses = (new ArrayList<String>(classRemaining.keySet()));
				for(String className : listOfClasses){
					boolean classExists = PojoGenerator.checkClassExistance(packageName+className);//doubt
					if(classExists){
						PojoGenerator.addFieldsToExistingClass(packageName+className, classRemaining.get(className));
					}
					else{
						PojoGenerator.generate(packageName+className, classRemaining.get(className));
					}
					classRemaining.remove(className);
					classesGenerated.put(className, classPropsMap.get(className));
				}
			}
		}
		catch(Exception e){
			System.out.println("Unhandled Exception  ::  "+e);
		}
	}
}