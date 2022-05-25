package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bodygenerator.Config.BodySize
import com.stepanov.bbf.bodygenerator.Config.ExpressionDepth
import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.ktFile
import com.stepanov.bbf.bodygenerator.contentgeneration.Generation.generateContent
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import com.stepanov.bbf.bugfinder.mutator.transformations.util.ScopeCalculator
import com.stepanov.bbf.bugfinder.util.getAllPSIChildrenOfType
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedFunction


class BodyGenerator {
    private val scopeCalculator = ScopeCalculator(ktFile, Utils.ProjectTools.project)
    private val bodyList: List<KtExpression> = ktFile.getAllPSIChildrenOfType<KtNamedFunction>()
        .filter { it.hasBody() }
        .map { it.bodyExpression!! }

    fun generateAll() {
        bodyList.forEach {
            generateBody(it)
            println(1)
        }
    }

    private fun generateBody(body: KtExpression) {
        val scope = BodyScope(body, scopeCalculator)
        val bodyContent = mutableListOf<String>()
        for (i in 0 until BodySize) {
            bodyContent += generateContent(scope, ExpressionDepth)
        }
        bodyContent.add("TODO()")
        body.replace(
            Factory.psiFactory.createBlock(
                String.format("%s", bodyContent.joinToString(separator = "\n"))
            )
        )
    }
}