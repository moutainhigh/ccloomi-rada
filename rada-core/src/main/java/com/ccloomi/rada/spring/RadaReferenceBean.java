package com.ccloomi.rada.spring;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.ccloomi.rada.annotation.RadaReference;
import com.ccloomi.rada.endpoint.RadaProxyServer;
import com.ccloomi.rada.handler.MQProxyHandler;
import com.ccloomi.rada.handler.ProxyInvoationHandler;
import com.ccloomi.rada.util.BytesUtil;
import com.ccloomi.rada.util.MethodUtil;
import com.ccloomi.rada.util.digest.DigestUtils;
import com.ccloomi.rada.util.digest.MessageDigestAlgorithmsEnum;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;

/**© 2015-2019 Chenxj Copyright
 * 类    名：RadaReferenceBean
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2019年1月25日-下午11:23:17
 */
public class RadaReferenceBean<T> {
	private Logger log=LoggerFactory.getLogger(getClass());
    private static final Class<?>[] constructorParams ={ ProxyInvoationHandler.class };
	private ProxyClassLoad proxyClassLoad;
	private RadaReference reference;
	private Class<?> referenceClass;
	private ClassLoader classLoader;
	private ApplicationContext applicationContext;
	private Set<String>asyncSet;
	public RadaReferenceBean() {
		asyncSet=new HashSet<>();
		asyncSet.add(Object.class.getName());
		asyncSet.add(CompletableFuture.class.getName());
	}
	@SuppressWarnings("unchecked")
	public T getObject() throws Exception {
		String server=referenceClass.getName();
		long timeout=reference.timeout();
		RadaReference rr=referenceClass.getAnnotation(RadaReference.class);
		if(timeout<1) {
			if(rr!=null) {
				timeout=rr.timeout();
			}
			if(timeout<1) {
				timeout=5000;
			}
		}
		//不同的配置需要注入不同的代理类
		String newClassName=genericProxyClassName(reference,referenceClass);
		Class<?>nc=null;
		try {nc=proxyClassLoad.loadClass(newClassName);}catch (Exception e) {}
		if(nc==null) {
			try {
				ClassPool cp=ClassPool.getDefault();
				//解决项目打成一个jar包时javassist.NotFoundException:xxx...错误 
				cp.insertClassPath(new ClassClassPath(ProxyInvoationHandler.class));
				CtClass newClass=cp.makeClass(newClassName);
				//生成Field
				CtClass fieldType=cp.getCtClass(ProxyInvoationHandler.class.getName());
				newClass.addField(new CtField(fieldType, "handler", newClass));
				//生成构造方法
				CtConstructor constructor=new CtConstructor(new CtClass[]{fieldType}, newClass);
				constructor.setBody("{this.handler=$1;}");
				newClass.addConstructor(constructor);
				//实现接口
				CtClass ctc_interfac=cp.get(referenceClass.getName());
				newClass.setInterfaces(new CtClass[]{ctc_interfac});
				//实现接口方法
				CtMethod[]ms=ctc_interfac.getMethods();
				for(int i=0;i<ms.length;i++){
					String methodName=ms[i].getLongName();
					if(!MethodUtil.isObjMethod(methodName)) {
						CtClass returnType=ms[i].getReturnType();
						String method=DigestUtils.MD5Hex(methodName);
						log.info("mapping [{}] to [{}]",method,methodName);
						CtMethod m=new CtMethod(returnType, ms[i].getName(), ms[i].getParameterTypes(), newClass);
						if(returnType==CtClass.booleanType||
								returnType==CtClass.byteType||
								returnType==CtClass.charType||
								returnType==CtClass.doubleType||
								returnType==CtClass.floatType||
								returnType==CtClass.intType||
								returnType==CtClass.longType||
								returnType==CtClass.shortType) {
							m.setBody(String.format("{return (%s)handler.invoke_%s(\"%s\",%dl,true,\"%s\",$args);}",
									returnType.getName(),
									returnType.getName(),
									server,timeout,method));
						}else if(returnType==CtClass.voidType) {
							m.setBody(String.format("{handler.invoke_void(\"%s\",%dl,false,\"%s\",$args);}",
									server,timeout,method));
						}else {
							if(returnType.isArray()) {
								m.setBody(String.format("{return (%s)handler.invoke(\"%s\",%dl,true,\"%s\",$args);}",
										returnType.getName(),
										server,timeout,method));
							}else {
								m.setBody(String.format("{return (%s)handler.invoke(\"%s\",%dl,%s,\"%s\",$args);}",
										returnType.getName(),
										server,timeout,
										!asyncSet.contains(returnType.getName()),
										method));
							}
						}
						newClass.addMethod(m);
					}
				}
				//加载新类
				nc=proxyClassLoad.loadClass(newClass.toBytecode(),
						newClassName,
						classLoader);
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		try {
			//生成实体类
			Constructor<?> cons=nc.getConstructor(constructorParams);
	        if (!Modifier.isPublic(nc.getModifiers())) {
	            AccessController.doPrivileged(new PrivilegedAction<Void>() {
	                public Void run() {
	                    cons.setAccessible(true);
	                    return null;
	                }
	            });
	        }
	        RadaProxyServer proxyServer=applicationContext.getBean(RadaProxyServer.class);
	        ProxyInvoationHandler handler=new MQProxyHandler(proxyServer);
			return (T) cons.newInstance(new Object[]{handler});
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String genericProxyClassName(RadaReference reference, Class<?> referenceClass) {
		return genericReferenceBeanName(reference, referenceClass);
	}
	private static String genericReferenceBeanName(RadaReference reference, Class<?> referenceClass) {
		MessageDigest md=MessageDigestAlgorithmsEnum.MD5.digest();
		md.update(referenceClass.getName().getBytes());
		md.update(reference.toString().getBytes());
		byte[]bs=md.digest();
		bs[0]=(byte) (bs[0]|0xC0);
		return BytesUtil.bytesToHEXString(bs);
	}
	public static void main(String[] args) throws Exception {

		MessageDigest md=MessageDigestAlgorithmsEnum.MD5.digest();
		for(int i=0;i<10;i++) {
			md.update(("a").getBytes());
			md.update(("b").getBytes());
			MessageDigest c1=((MessageDigest)md.clone());
			byte[]bs=c1.digest();
			bs[0]=(byte) (bs[0]|0xC0);
			System.out.println(BytesUtil.bytesToHexString(bs));
		}
	}
	/**设置 reference*/
	public RadaReferenceBean<T> setReference(RadaReference reference) {
		this.reference = reference;
		return this;
	}
	/**设置 referenceClass*/
	public RadaReferenceBean<T> setReferenceClass(Class<?> referenceClass) {
		this.referenceClass = referenceClass;
		return this;
	}
	/**设置 classLoader*/
	public RadaReferenceBean<T> setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
		return this;
	}
	/**设置 applicationContext*/
	public RadaReferenceBean<T> setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.proxyClassLoad=new ProxyClassLoad(applicationContext.getClassLoader());
		return this;
	}
	
	private static class ProxyClassLoad extends ClassLoader{
		public ProxyClassLoad(ClassLoader pcl) {
			super(pcl);
		}
		@SuppressWarnings("unchecked")
		public <T>Class<T> loadClass(byte[]b,String className,ClassLoader cl) throws Exception{
			return (Class<T>)defineClass(className, b, 0, b.length);
		}
	}
}
