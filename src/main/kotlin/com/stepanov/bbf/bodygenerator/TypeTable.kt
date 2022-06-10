package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bodygenerator.Config.CollectionTypes
import com.stepanov.bbf.bodygenerator.Config.PowerOfUserDefinedTypes
import com.stepanov.bbf.bodygenerator.Config.PrimitiveTypes
import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.ctx
import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.ktFile
import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.rig
import com.stepanov.bbf.bodygenerator.Utils.generateUserDefinedTypePath
import com.stepanov.bbf.bodygenerator.Utils.isNotEnum
import com.stepanov.bbf.bodygenerator.Utils.randomContent
import com.stepanov.bbf.bodygenerator.Utils.withRandomParamTypes
import com.stepanov.bbf.bugfinder.util.getAllPSIChildrenOfType
import com.stepanov.bbf.bugfinder.util.getFirstParentOfType
import com.stepanov.bbf.bugfinder.util.hasTypeParam
import com.stepanov.bbf.bugfinder.util.name
import org.jetbrains.kotlin.cfg.getDeclarationDescriptorIncludingConstructors
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isPrimitiveNumberType
import kotlin.reflect.KClass


class UserDefinedType

object TypeTable {
    val primitiveTypesTranslator = PrimitiveTypes.keys
        .associate { it.simpleName!! to generateType(it.simpleName!!) }

    val numericTypes = PrimitiveTypes.filter { primitiveTypesTranslator[it.key.simpleName]!!.isPrimitiveNumberType()
            && it.key.simpleName != "Char" }

    val arithmeticTypes = PrimitiveTypes.filter { setOf(Int::class, Long::class, Float::class, Double::class).contains(it.key) }

    val userDefinedTypes = ktFile
        .getAllPSIChildrenOfType<KtClass>()
        .filter { klass ->
            klass.getFirstParentOfType<KtClass>()?.isNotEnum() ?: true && klass.parentsWithSelf.toList()
                .filterIsInstance<KtClass>()
                .firstOrNull { it.isAbstract() || it.isInterface() } == null }
        .associate { it to (it.getDeclarationDescriptorIncludingConstructors(ctx) as ClassDescriptor).defaultType }
        .filter {
            var i = 5
            do {
                if (rig.classInstanceGenerator.generateRandomInstanceOfUserClass(it.value) == null)
                    return@filter false
            } while (i-- != 0)
            return@filter true
        }

    val collectionTypesTranslator = mutableMapOf<String, KotlinType>()

    init {
        if (userDefinedTypes.isEmpty()) {
            PowerOfUserDefinedTypes = 0
        }
    }

    fun getRandomType(): KotlinType =
        (PrimitiveTypes + CollectionTypes + (UserDefinedType::class to PowerOfUserDefinedTypes))
            .randomContent().let { klass ->
                when {
                    klass == UserDefinedType::class -> userDefinedTypes.values.random().let {
                        if (it.hasTypeParam())
                            generateType(generateUserDefinedTypePath(it))
                        else
                            it
                    }
                    klass.typeParameters.isNotEmpty() -> generateCollectionTypeFromClass(klass)
                    else -> primitiveTypesTranslator[klass.simpleName]!!
                }
            }

    fun getRandomPrimitiveOrCollectionType(): KotlinType =
        (PrimitiveTypes + CollectionTypes)
            .randomContent().let { klass ->
                when {
                    klass.typeParameters.isNotEmpty() -> generateCollectionTypeFromClass(klass)
                    else -> primitiveTypesTranslator[klass.simpleName]!!
                }
            }

    fun KotlinType.getRandomNumericBeforeType(): KotlinType =
        numericTypes
            .filter { pair -> pair.key in numericTypes.keys.toList().dropLastWhile { it.simpleName != this.name }.dropLast(1) }
            .randomContent()
            .let {
                 primitiveTypesTranslator[it.simpleName]!!
            }

    fun KotlinType.getRandomNumericBeforeOrCollectionType(): KotlinType =
        (numericTypes
            .filter { pair -> pair.key in numericTypes.keys.toList().dropLastWhile { it.simpleName != this.name } }
                + CollectionTypes)
            .randomContent()
            .let { klass ->
                if (klass.typeParameters.isNotEmpty())
                    generateCollectionTypeFromClass(klass)
                else
                    primitiveTypesTranslator[klass.simpleName!!]!!
            }

    fun getRandomPrimitiveType(): KotlinType = primitiveTypesTranslator[PrimitiveTypes.randomContent().simpleName]!!

    fun getRandomSimpleType(): KotlinType =
        (PrimitiveTypes + (UserDefinedType::class to PowerOfUserDefinedTypes))
            .randomContent().let {
            if (it == UserDefinedType::class)
                userDefinedTypes.values.random()
            else
                primitiveTypesTranslator[it.simpleName]!!
        }

    fun generateCollectionTypeFromClass(klass: KClass<*>): KotlinType {
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

    fun generateType(type: String): KotlinType = rig.randomTypeGenerator.generateType(type)!!
}