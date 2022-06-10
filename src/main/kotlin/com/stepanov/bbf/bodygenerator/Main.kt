package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.ktFile
import com.stepanov.bbf.bugfinder.executor.checkers.CompilationChecker
import com.stepanov.bbf.bugfinder.executor.compilers.JVMCompiler
import com.stepanov.bbf.bugfinder.executor.project.Project
import com.stepanov.bbf.bugfinder.mutator.transformations.tce.StdLibraryGenerator
import com.stepanov.bbf.bugfinder.util.addImport
import kotlin.system.exitProcess


fun main() {
    val bodyGenerator = BodyGenerator()
    bodyGenerator.generateAll()
    val imports = StdLibraryGenerator.calcImports(ktFile)
    imports.forEach {
        ktFile.addImport(it, false)
    }
    println(ktFile.text)
    val project = Project.createFromCode(ktFile.text)
    val checker = CompilationChecker(JVMCompiler())
    val res = checker.checkCompiling(project)
    println("res = $res")
    exitProcess(0)
}