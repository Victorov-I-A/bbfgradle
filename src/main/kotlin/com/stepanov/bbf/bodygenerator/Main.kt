package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.ktFile
import com.stepanov.bbf.bugfinder.mutator.transformations.tce.StdLibraryGenerator
import com.stepanov.bbf.bugfinder.util.addImport


fun main() {
    val bodyGenerator = BodyGenerator()
    bodyGenerator.generateAll()
    val imports = StdLibraryGenerator.calcImports(ktFile)
    imports.forEach {
        ktFile.addImport(it, false)
    }
    println(ktFile.text)
}