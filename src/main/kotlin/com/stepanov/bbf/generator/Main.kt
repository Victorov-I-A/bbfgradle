package com.stepanov.bbf.generator

import com.stepanov.bbf.bugfinder.executor.CompilerArgs
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.project.Project
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import com.stepanov.bbf.reduktor.parser.PSICreator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PropertyConfigurator
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    //Init log4j
    PropertyConfigurator.configure("src/main/resources/bbfLog4j.properties")
    if (!CompilerArgs.getPropAsBoolean("LOG")) {
        Logger.getRootLogger().level = Level.OFF
        Logger.getLogger("bugFinderLogger").level = Level.OFF
        Logger.getLogger("mutatorLogger").level = Level.OFF
        Logger.getLogger("reducerLogger").level = Level.OFF
        Logger.getLogger("transformationManagerLog").level = Level.OFF
    }

//    val p_ = Project.createFromCode(
//        "class A()  {\n" +
//                "class B<T>() {\n" +
//                "}\n" +
//                "}"
//    )
//    val f = p_.files.first().psiFile as KtFile
//    val cont = PSICreator.analyze(f)!!
//    RandomTypeGenerator.setFileAndContext(f, cont)
//    println(RandomTypeGenerator.generateType("A.B<Int>"))
//    exitProcess(0)

//    val p1 = Project.createFromCode(File("tmp/myTest.kt").readText())
//    val psiFile1 = p1.files.first().psiFile as KtFile
//    val ctx1 = PSICreator.analyze(psiFile1)!!
//    exitProcess(0)

    val p = Generator().generate().first
    val file = p.files.first().psiFile as KtFile
    println(file.text)
    val project = Project.createFromCode(ktFile.text)
    val checker = CompilationChecker(JVMCompiler())
    val res = checker.checkCompiling(project)
    println("res = $res")
    exitProcess(0)
    /*val compiler = JVMCompiler("")
    val compiled = compiler.tryToCompile(p)
    if (!compiled.isCompileSuccess) {
        println(compiled.combinedOutput)
    }
    exitProcess(0)*/


//    val project = Project.createFromCode(File("test.kt").readText())
//    val ktFile = project.files.first().psiFile as KtFile
//    val ctx = PSICreator.analyze(ktFile)!!
//    val klass = ktFile.getAllPSIChildrenOfType<KtClassOrObject>()[1]
//    val instance = RandomInstancesGenerator(ktFile).generateRandomInstanceOfClass(klass)
//    println(instance?.first?.text)
//    RandomTypeGenerator.setFileAndContext(ktFile, ctx)
//    repeat(100) {
//        println("GENERATED TYPE = ${RandomTypeGenerator.generateRandomTypeWithCtx()}")
//    }
//
//    exitProcess(0)

//    val project = Project.createFromCode("class A<T> { val a: T }")
//    val ktFile = project.files.first().psiFile as KtFile
//    val ctx = PSICreator.analyze(ktFile)!!
//    val klass = ktFile.getAllPSIChildrenOfType<KtClassOrObject>().first()
//    val classDescr = klass.getDeclarationDescriptorIncludingConstructors(ctx)
//    RandomTypeGenerator.setFileAndContext(ktFile, ctx)
//    val intType = RandomTypeGenerator.generateType("Int")!!
//    val replaced = (classDescr as LazyClassDescriptor).defaultType.replace(listOf(intType.asTypeProjection()))
//    println(replaced.memberScope.getDescriptorsFiltered { true })
//    exitProcess(0)
}