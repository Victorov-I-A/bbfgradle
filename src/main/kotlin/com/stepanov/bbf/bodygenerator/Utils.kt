package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bugfinder.executor.project.Project
import com.stepanov.bbf.bugfinder.generator.targetsgenerators.RandomInstancesGenerator
import com.stepanov.bbf.bugfinder.util.getAllTypeParams
import com.stepanov.bbf.bugfinder.util.hasTypeParam
import com.stepanov.bbf.bugfinder.util.name
import com.stepanov.bbf.reduktor.parser.PSICreator
import org.jetbrains.kotlin.cfg.getDeclarationDescriptorIncludingConstructors
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.descriptorUtil.parents
import org.jetbrains.kotlin.types.KotlinType
import java.io.File
import kotlin.random.Random
import kotlin.reflect.KClass


object Utils {
    object ProjectTools {
        val project = Project.createFromCode(File("tmp/body_test_2.kt").readText())
        val ktFile = project.files.first().psiFile as KtFile

        val ctx = PSICreator.analyze(ktFile, project) ?: throw IllegalArgumentException()
        val rig = RandomInstancesGenerator(ktFile, ctx)
    }

    fun KtClass.generatePath(): String =
        ProjectTools.rig.classInstanceGenerator.generateRandomInstanceOfUserClass(
            (this.getDeclarationDescriptorIncludingConstructors(ProjectTools.ctx) as ClassDescriptor)
                .defaultType)?.first?.text + '.'

    fun generateUserDefinedTypePath(type: KotlinType): String =
        type.constructor.declarationDescriptor!!.parents.toList()
            .filterIsInstance<ClassDescriptor>()
            .reversed()
            .joinToString(".") { it.name.asString().trim() } +
                if (type.hasTypeParam())
                    type.withRandomParamTypes()
                else
                    type.name

    fun KtClass.isNotEnum(): Boolean = !this.isEnum()

    fun KClass<*>.withRandomParamTypes(): String {
        val parameters = List(this.typeParameters.size) {
            Config.PrimitiveTypes.randomContent().simpleName!!
        }
        return this.simpleName!! + parameters.joinToString(prefix = "<", separator = ",", postfix = ">")
    }

    fun KotlinType.withRandomParamTypes(): String {
        val parameters = List(this.getAllTypeParams().size) {
            Config.PrimitiveTypes.randomContent().simpleName!!
        }
        return this.name + parameters.joinToString(prefix = "<", separator = ",", postfix = ">")
    }

    fun <T> Map<T, Int>.randomContent(): T {
        var curPower = Random.nextInt(this.values.sum())
        for (pair in this) {
            if (curPower < pair.value)
                return pair.key
            else
                curPower -= pair.value
        }
        throw IllegalStateException()
    }

    fun <T>  List<Pair<T, Int>>.randomContent(): T {
        var curPower = Random.nextInt(this.sumOf { it.second })
        for (pair in this) {
            if (curPower < pair.second)
                return pair.first
            else
                curPower -= pair.second
        }
        throw IllegalStateException()
    }
}