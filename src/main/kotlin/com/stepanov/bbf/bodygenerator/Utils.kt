package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bugfinder.executor.project.Project
import com.stepanov.bbf.bugfinder.generator.targetsgenerators.RandomInstancesGenerator
import com.stepanov.bbf.reduktor.parser.PSICreator
import org.jetbrains.kotlin.cfg.getDeclarationDescriptorIncludingConstructors
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.descriptorUtil.parentsWithSelf
import org.jetbrains.kotlin.types.KotlinType
import java.io.File

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
        type.constructor.declarationDescriptor!!.parentsWithSelf.toList()
            .filterIsInstance<ClassDescriptor>()
            .reversed()
            .joinToString(".") { it.name.asString().trim() }

    fun KtClass.isNotEnum(): Boolean = !this.isEnum()
}