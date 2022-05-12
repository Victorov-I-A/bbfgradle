package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bugfinder.executor.project.Project
import com.stepanov.bbf.bugfinder.generator.targetsgenerators.typeGenerators.RandomTypeGenerator
import com.stepanov.bbf.bugfinder.mutator.transformations.tce.UsagesSamplesGenerator
import com.stepanov.bbf.bugfinder.mutator.transformations.util.ScopeCalculator
import com.stepanov.bbf.reduktor.parser.PSICreator
import com.stepanov.bbf.reduktor.util.getAllPSIChildrenOfType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.synthetics.findClassDescriptor
import java.io.File
import kotlin.system.exitProcess

fun main() {
    val project = Project.createFromCode(File("tmp/bodyTest.kt").readText())
    val ktFile = project.files.first().psiFile as KtFile
    //val ctx = PSICreator.analyze(ktFile, project)!!
    //val res = UsagesSamplesGenerator.generate(ktFile, ctx, project)
    val scopeCalculator = ScopeCalculator(ktFile, project)
    val bodyGenerator = BodyGenerator(ktFile, scopeCalculator)
    bodyGenerator.generateAll()
    println(ktFile.text)
//    val project = Project.createFromCode("""
//            class A(val a: Int, val b: String) {
//
//                fun lol(a: Int, b: String): Int = a + 123
//
//                fun test(c: Double): String = TODO()
//            }
//        """.trimIndent())
//    val ktFile = project.files.first().psiFile as KtFile
//    val ctx = PSICreator.analyze(ktFile, project)!!
//    val funToGenerate = ktFile.getAllPSIChildrenOfType<KtNamedFunction>().first()
//    val resultType = funToGenerate.typeReference!!.getAbbreviatedTypeOrType(ctx)
//    val funBody = funToGenerate.bodyExpression!!
//    val scope = ScopeCalculator(ktFile, project).calcScope(funBody)
//    val rig = RandomInstancesGenerator(ktFile, ctx)
//    val exprToInsert = scope[3].makeExpressionToInsertFromPsiElement(rig)
//    println(exprToInsert?.psiElement?.text)
}