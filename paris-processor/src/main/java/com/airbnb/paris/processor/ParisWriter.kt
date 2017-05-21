package com.airbnb.paris.processor

import com.airbnb.paris.processor.utils.className
import com.squareup.javapoet.*
import java.io.IOException
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

internal object ParisWriter {

    @Throws(IOException::class)
    internal fun writeFrom(filer: Filer, styleableClassesInfo: List<StyleableInfo>) {
        val parisTypeBuilder = TypeSpec.classBuilder(ParisProcessor.PARIS_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        ParisProcessor.BUILT_IN_STYLE_APPLIERS.forEach { styleApplierQualifiedName, viewQualifiedName ->
            val styleApplierClassName = styleApplierQualifiedName.className()
            parisTypeBuilder.addMethod(buildStyleMethod(
                    styleApplierClassName.packageName(),
                    styleApplierClassName.simpleName(),
                    viewQualifiedName.className()))
        }

        for (styleableClassInfo in styleableClassesInfo) {
            parisTypeBuilder.addMethod(buildStyleMethod(styleableClassInfo))
        }

        JavaFile.builder(ParisProcessor.PARIS_CLASS_NAME.packageName(), parisTypeBuilder.build())
                .build()
                .writeTo(filer)
    }

    private fun buildStyleMethod(styleableClassInfo: StyleableInfo): MethodSpec {
        return buildStyleMethod(
                styleableClassInfo.elementPackageName,
                String.format(Locale.US, ParisProcessor.STYLE_APPLIER_CLASS_NAME_FORMAT, styleableClassInfo.elementName),
                TypeName.get(styleableClassInfo.elementType))
    }

    private fun buildStyleMethod(styleApplierPackageName: String, styleApplierSimpleName: String, viewParameterTypeName: TypeName): MethodSpec {
        val styleApplierClassName = ClassName.get(
                styleApplierPackageName,
                styleApplierSimpleName)
        return MethodSpec.methodBuilder("style")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(styleApplierClassName)
                .addParameter(ParameterSpec.builder(viewParameterTypeName, "view").build())
                .addStatement("return new \$T(view)", styleApplierClassName)
                .build()
    }
}