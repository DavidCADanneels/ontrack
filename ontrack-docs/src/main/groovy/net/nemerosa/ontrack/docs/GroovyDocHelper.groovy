package net.nemerosa.ontrack.docs

import org.codehaus.groovy.groovydoc.GroovyClassDoc
import org.codehaus.groovy.groovydoc.GroovyMethodDoc
import org.codehaus.groovy.groovydoc.GroovyRootDoc
import org.codehaus.groovy.tools.groovydoc.ArrayClassDocWrapper
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool

import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class GroovyDocHelper {

    final GroovyRootDoc rootDoc

    GroovyDocHelper(String sourcePath) {
        rootDoc = createRootDoc(sourcePath)
    }

    private static createRootDoc(String sourcePath) {
        List filePaths = []
        File root = new File(sourcePath)
        root.eachFileRecurse { File file ->
            if (file.isFile()) {
                filePaths.add file.canonicalPath - root.canonicalPath
            }
        }
        GroovyDocTool tool = new GroovyDocTool([root] as String[])
        tool.add filePaths

        tool.rootDoc
    }

    private GroovyClassDoc getGroovyClassDoc(Class clazz) {
        String name = '/' + clazz.name.replaceAll('\\.', '/')
        rootDoc.classes().find { it.fullPathName == name }
    }

    static Method getMethodFromGroovyMethodDoc(GroovyMethodDoc methodDoc, Class clazz) {
        Method method = clazz.declaredMethods.findAll { it.name == methodDoc.name() }.find { Method method ->

            List docParamNames = methodDoc.parameters().collect {
                String name = it.type()?.qualifiedTypeName() ?: it.typeName()
                if (name.startsWith('.')) {
                    name = name[1..-1]
                }

                if (it.type() && it.type() instanceof ArrayClassDocWrapper) {
                    return "[L$name;"
                }

                Map primitiveToArrayName = [
                        'byte'   : '[B',
                        'short'  : '[S',
                        'int'    : '[I',
                        'long'   : '[J',
                        'float'  : '[F',
                        'double' : '[D',
                        'char'   : '[C',
                        'boolean': '[Z',
                ]

                if (it.vararg()) {
                    if (primitiveToArrayName[name]) {
                        return primitiveToArrayName[name]
                    }
                    return "[L$name;"
                } else if (name == 'def') {
                    return 'java.lang.Object'
                } else {
                    return name
                }
            }
            docParamNames == method.parameterTypes*.name ||
                    docParamNames == method.genericParameterTypes.collect { getGenericType(it) } ||
                    docParamNames == method.parameterTypes*.canonicalName ||
                    docParamNames == method.parameterTypes.collect { it.enum ? it.simpleName : it.name }
        }

        method
    }

    private static String getGenericType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = type as ParameterizedType
            return parameterizedType.rawType.simpleName +
                    "<" +
                    parameterizedType.actualTypeArguments*.simpleName.join(',') +
                    ">"
        } else {
            return type.typeName
        }
    }

    List<GroovyMethodDoc> getAllMethods(Class clazz) {
        GroovyClassDoc classDoc = getGroovyClassDoc(clazz)
        return (classDoc?.methods() ?: []) as List<GroovyMethodDoc>
    }

}