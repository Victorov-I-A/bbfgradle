package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bugfinder.executor.project.Project
import com.stepanov.bbf.bugfinder.generator.targetsgenerators.RandomInstancesGenerator
import com.stepanov.bbf.bugfinder.util.getType
import com.stepanov.bbf.bugfinder.util.getPsi
import com.stepanov.bbf.bugfinder.util.name
import com.stepanov.bbf.reduktor.parser.PSICreator
import org.jetbrains.kotlin.cfg.getDeclarationDescriptorIncludingConstructors
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTypeParameter
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.parentsWithSelf
import org.jetbrains.kotlin.types.KotlinType
import java.io.File
import kotlin.random.Random
import kotlin.reflect.KClass


object Utils {
    object ProjectTools {
        var project = Project.createFromCode(File("tmp/body_test_2.kt").readText())
        var ktFile = project.files.first().psiFile as KtFile

        var ctx: BindingContext = PSICreator.analyze(ktFile, project) ?: throw IllegalArgumentException()
        var rig: RandomInstancesGenerator = RandomInstancesGenerator(ktFile, ctx)
    }

    fun setProjectTools(ktFile: KtFile, project: Project) {
        ProjectTools.project = project
        ProjectTools.ktFile = ktFile

        ProjectTools.ctx = PSICreator.analyze(ProjectTools.ktFile, project)!!
        ProjectTools.rig = RandomInstancesGenerator(ProjectTools.ktFile, ProjectTools.ctx)
    }

    fun KtClass.generatePath(): String =
        ProjectTools.rig.classInstanceGenerator.generateRandomInstanceOfUserClass(
            (this.getDeclarationDescriptorIncludingConstructors(ProjectTools.ctx) as ClassDescriptor)
                .defaultType)?.first?.text ?: generatePath()

    fun generateUserDefinedTypePath(type: KotlinType): String =
        type.constructor.declarationDescriptor!!.parentsWithSelf.toList()
            .filterIsInstance<ClassDescriptor>()
            .reversed()
            .joinToString(".") {
                it.defaultType.let { stype ->
                    if (((type.constructor.declarationDescriptor?.source?.getPsi() as KtClass).isInner() ||
                                stype == type) && stype.typeParameters().isNotEmpty())
                        stype.withRandomParamTypes()
                    else
                        it.name.asString().trim()
                }
            }


    fun KtClass.isNotEnum(): Boolean = !this.isEnum()

    fun KClass<*>.withRandomParamTypes(): String {
        val parameters = List(this.typeParameters.size) {
            Config.PrimitiveTypes.randomContent().simpleName!!
        }
        return this.simpleName!! + parameters.joinToString(prefix = "<", separator = ",", postfix = ">")
    }

    fun KotlinType.withRandomParamTypes(): String {
        val parameters = List(this.typeParameters().size) {
            val type = this.typeParameters()[it].getType(ProjectTools.ctx)
            if (type != null)
                type.name
            else
                Config.PrimitiveTypes.randomContent().simpleName!!
        }
        return this.name + parameters.joinToString(prefix = "<", separator = ",", postfix = ">")
    }

    fun KotlinType.typeParameters(): List<KtTypeParameter> =
        (this.constructor.declarationDescriptor?.source?.getPsi() as KtClass).typeParameters

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