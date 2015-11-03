package com.lg.base.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

public class GsonUtil {
    private static Gson GSON = null;
    static{
    	GSON = createGson();
    }
    public static Gson getGson(){
    	return GSON;
    }
    private static Gson createGson() {
		GsonBuilder builder = new GsonBuilder();
    	//排除不需要序列化及反序化的字段修饰符
    	builder.excludeFieldsWithModifiers(Modifier.FINAL|Modifier.STATIC|Modifier.TRANSIENT);
    	
    	//(1)支持Map的key为复杂对象的形式
    	builder.enableComplexMapKeySerialization();
    	
    	//(2)空的也序列化
    	builder.serializeNulls();
    	
    	//(3)日期格式
    	builder.setDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	//(4)会把大写字母变成下划线+小写字母(someFieldName->some_field_name ),注:对于实体上使用了@SerializedName注解的不会生效.
//    	builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    	
    	//(5)对json结果格式化
//    	builder.setPrettyPrinting();
    	/*
    	 (6) 有的字段不是一开始就有的,会随着版本的升级添加进来,那么在进行序列化和返序列化的时候就会根据版本号来选择是否要序列化.
    	   @Since(版本号)能完美地实现这个功能.还的字段可能,随着版本的升级而删除,那么
    	   @Until(版本号)也能实现这个功能,GsonBuilder.setVersion(double)方法需要调用.
    	 */
    	builder.setVersion(1.0);
    	Gson gson = builder.create();
    	return gson;
	}
    /**
GsonBuilder 	addDeserializationExclusionStrategy ( ExclusionStrategy strategy)
配置GSON到申请，在反序列化过程中通过排除策略。

GsonBuilder 	addSerializationExclusionStrategy ( ExclusionStrategy strategy)
配置GSON到申请的序列化过程中通过排除策略。

Gson 	create ()
基于当前配置创建一个Gson实例。

GsonBuilder 	disableHtmlEscaping ()
默认情况下，GSON转义HTML字符，如<>等

GsonBuilder 	disableInnerClassSerialization ()
配置GSON排除在序列化过程中的内部类。

GsonBuilder 	enableComplexMapKeySerialization ()
启用此功能只会更改地图关键是一个复杂的类型（即序列化形式

GsonBuilder 	excludeFieldsWithModifiers (int... modifiers)
配置GSON以排除所有指定修饰符的类字段。

GsonBuilder 	excludeFieldsWithoutExposeAnnotation ()
排除配置GSON到各个领域，从序列化或反序列化没有Expose注解的代价。

GsonBuilder 	generateNonExecutableJson ()
使输出的JSON在Javascript中的非可执行的前缀生成的JSON与一些特殊的文本。

GsonBuilder 	registerTypeAdapter ( Type type, Object typeAdapter)
配置GSON的自定义序列化或反序列化。

GsonBuilder 	registerTypeAdapterFactory ( TypeAdapterFactory factory)
注册类型的适配器的工厂。

GsonBuilder 	registerTypeHierarchyAdapter ( Class <?> baseType, Object typeAdapter)
配置GSON的自定义序列化或反序列化的继承类型层次结构。

GsonBuilder 	serializeNulls ()
配置GSON空字段序列。

GsonBuilder 	serializeSpecialFloatingPointValues ()
JSON规范不允许使用特殊的双值（NAN，无穷远，无穷远）第2.4节。

GsonBuilder 	setDateFormat (int style)
配置GSON的Date对象序列化，根据所提供的样式值。

GsonBuilder 	setDateFormat (int dateStyle, int timeStyle)
配置GSON的Date对象序列化，根据所提供的样式值。

GsonBuilder 	setDateFormat ( String pattern)
配置GSON到Date对象序列化，根据提供的模式。

GsonBuilder 	setExclusionStrategies ( ExclusionStrategy ... strategies)
配置GSON申请一组在序列化和反序列化的排斥策略。

GsonBuilder 	setFieldNamingPolicy ( FieldNamingPolicy namingConvention)
配置GSON到应用特定的命名政策在序列化和反序列化对象的字段。

GsonBuilder 	setFieldNamingStrategy ( FieldNamingStrategy fieldNamingStrategy)
配置GSON到申请对象的字段序列化和反序列化过程中特定的命名政策战略。

GsonBuilder 	setLongSerializationPolicy ( LongSerializationPolicy serializationPolicy)
配置GSON到申请一个特定的序列化政策Long和long对象。

GsonBuilder 	setPrettyPrinting ()
配置GSON到输出JSON漂亮的印花，适合在一个页面。

GsonBuilder 	setVersion (double ignoreVersionsAfter)
配置GSON到启用版本支持。
     */

}
