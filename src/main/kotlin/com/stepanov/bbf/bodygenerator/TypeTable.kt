package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bodygenerator.Config.CollectionTypes
import com.stepanov.bbf.bodygenerator.Config.PowerOfUserDefinedTypes
import com.stepanov.bbf.bodygenerator.Config.PrimitiveTypes
import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.ctx
import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.ktFile
import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.rig
import com.stepanov.bbf.bodygenerator.Utils.isNotEnum
import com.stepanov.bbf.bodygenerator.contentgeneration.Generation.randomContent
import com.stepanov.bbf.bugfinder.util.getAllPSIChildrenOfType
import com.stepanov.bbf.bugfinder.util.getFirstParentOfType
import org.jetbrains.kotlin.cfg.getDeclarationDescriptorIncludingConstructors
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.types.KotlinType
import kotlin.reflect.KClass


object TypeTable {
    val simpleTypes = PrimitiveTypes + (UserDefinedType::class to PowerOfUserDefinedTypes)

    val primitiveTypesTranslator = PrimitiveTypes.keys
        .associate { it.simpleName!! to generateType(it.simpleName!!) }

    val userDefinedTypes = ktFile
        .getAllPSIChildrenOfType<KtClass>()
        .filter { klass ->
            klass.getFirstParentOfType<KtClass>()?.isNotEnum() ?: true && klass.parentsWithSelf.toList()
                        .filterIsInstance<KtClass>()
                        .firstOrNull { it.isAbstract() || it.isInterface() } == null }
        .map { (it.getDeclarationDescriptorIncludingConstructors(ctx) as ClassDescriptor).defaultType }
        .filter { rig.classInstanceGenerator.generateRandomInstanceOfUserClass(it)?.first != null }

    val collectionTypesTranslator = mutableMapOf<String, KotlinType>()

    fun getRandomType(): KotlinType =
        (PrimitiveTypes + CollectionTypes + (UserDefinedType::class to PowerOfUserDefinedTypes))
            .randomContent().let { klass ->
                when {
                    klass == UserDefinedType::class -> userDefinedTypes.random()
                    klass.typeParameters.isNotEmpty() -> {
                        val type = klass.withRandomParamTypes()
                        if (collectionTypesTranslator.containsKey(type))
                            return collectionTypesTranslator[type]!!
                        else {
                            generateType(type).let {
                                collectionTypesTranslator[type]= it
                                return it
                            }
                        }
                    }
                    else -> primitiveTypesTranslator[klass.simpleName]!!
                }
            }

    fun getRandomPrimitiveType(): KotlinType = primitiveTypesTranslator[PrimitiveTypes.randomContent().simpleName]!!

    fun getRandomSimpleType(): KotlinType =
        simpleTypes.randomContent().let {
            if (it == UserDefinedType::class)
                userDefinedTypes.random()
            else
                primitiveTypesTranslator[it.simpleName]!!
        }

    fun KClass<*>.withRandomParamTypes(): String {
        val parameters = List(this.typeParameters.size) {
            PrimitiveTypes.randomContent().simpleName!!
        }
        return this.simpleName!! + parameters.joinToString(prefix = "<", separator = ",", postfix = ">")
    }

    fun generateType(type: String): KotlinType = rig.randomTypeGenerator.generateType(type)!!

    class UserDefinedType
}