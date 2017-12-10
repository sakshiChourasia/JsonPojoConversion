package com.poc.jsonToPojo;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

public class PojoGenerator{

	public static Class<?> generate(String className, Map<String, Class<?>> properties) throws NotFoundException, CannotCompileException
	{
		ClassPool pool = ClassPool.getDefault();
		System.out.println("Creating class  ::  "+className);
		//System.out.println("Properties of class  ::  "+properties);
		CtClass cc = pool.makeClass(className);
		cc.addInterface(resolveCtClass(Serializable.class));
		for(Entry<String, Class<?>> entry : properties.entrySet()){
			cc.addField(new CtField(resolveCtClass(entry.getValue()), entry.getKey(), cc));
			cc.addMethod(generateGetter(cc, entry.getKey(), entry.getValue()));
			cc.addMethod(generateSetter(cc, entry.getKey(), entry.getValue()));
		}
		return cc.toClass();
	}

	public static void addFieldsToExistingClass(String className, Map<String, Class<?>> properties)throws NotFoundException, CannotCompileException
	{
		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.get(className);
		cc.defrost();
		for(Entry<String, Class<?>> entry : properties.entrySet()){
			cc.addField(new CtField(resolveCtClass(entry.getValue()), entry.getKey(), cc));
			cc.addMethod(generateGetter(cc, entry.getKey(), entry.getValue()));
			cc.addMethod(generateSetter(cc,  entry.getKey(), entry.getValue()));
		}
	}

	public static CtMethod generateGetter(CtClass declaringClass, String fieldName, Class<?> fieldClass) throws CannotCompileException
	{
		String getterName = "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
		StringBuffer sb = new StringBuffer();
		sb.append("public ").append(fieldClass.getName()).append(" ").append(getterName).append("(){").append("return this.").append(fieldName). append(";").append("}");
		return CtMethod.make(sb.toString(), declaringClass);
	}

	public static CtMethod generateSetter(CtClass declaringClass, String fieldName, Class<?> fieldClass) throws CannotCompileException
	{
		String setterName = "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
		StringBuffer sb = new StringBuffer();
		sb.append("public void ").append(setterName).append("(").append(fieldClass.getName()).append(" ").append(fieldName). append("){this.").append(fieldName). append("=").append(fieldName).append(";}");
		return CtMethod.make(sb.toString(), declaringClass);
	}

	public static CtClass resolveCtClass(Class<?> clazz) throws NotFoundException
	{
		ClassPool pool = ClassPool.getDefault();
		return pool.get(clazz.getName());
	}

	public static boolean checkClassExistance(String className)
	{
		boolean isExist = false;
		ClassPool pool = ClassPool.getDefault();
		
		if(pool.getOrNull(className)!=null)
			isExist = true;
		return isExist;
	}
}