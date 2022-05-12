package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bodygenerator.contentgeneration.*
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import com.stepanov.bbf.bugfinder.mutator.transformations.util.ScopeCalculator
import com.stepanov.bbf.bugfinder.util.getAllPSIChildrenOfType
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

class BodyGenerator(val ktFile: KtFile, private val ScopeCalculator: ScopeCalculator) {
    private val bodyList: List<KtExpression> = ktFile.getAllPSIChildrenOfType<KtNamedFunction>()
        //.filter { it.hasBody() }
        .map { it.bodyExpression!! }

    fun generateAll() {
        bodyList.forEach {
            generateBody(it)
        }
    }

    private fun generateBody(body: KtExpression) {
        val scope = BodyScope(body, ScopeCalculator)
        var bodyText = ""
        for (i in 0..3) {
            val randomProperty = Generation.generateDeclaration(scope, Config.ExpressionDepth)
            bodyText += randomProperty
            bodyText += "\n"
        }
        for (i in 0..3) {
            val randomExpression = Generation.generateStatement(scope, Config.ExpressionDepth)
            bodyText += randomExpression
            bodyText += "\n"
        }
        body.replace(Factory.psiFactory.createBlock("$bodyText\nTODO()\n}"))
    }
}