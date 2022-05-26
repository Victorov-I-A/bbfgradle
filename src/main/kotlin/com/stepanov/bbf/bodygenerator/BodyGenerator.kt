package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bodygenerator.Config.BodySize
import com.stepanov.bbf.bodygenerator.Config.ExpressionDepth
import com.stepanov.bbf.bodygenerator.Generation.generateConstant
import com.stepanov.bbf.bodygenerator.Utils.ProjectTools.ktFile
import com.stepanov.bbf.bodygenerator.Generation.generateContent
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import com.stepanov.bbf.bugfinder.mutator.transformations.util.ScopeCalculator
import com.stepanov.bbf.bugfinder.util.getAllPSIChildrenOfType
import org.jetbrains.kotlin.cfg.getDeclarationDescriptorIncludingConstructors
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.psi.KtDeclaration
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
        //bodyContent.add(generateReturn(scope, body))
        bodyContent.add("TODO()")
        body.replace(
            Factory.psiFactory.createBlock(
                String.format("%s", bodyContent.joinToString(separator = "\n"))
            )
        )
    }

    fun generateReturn(scope: BodyScope, body: KtExpression): String {
        val returnType = ((body.parent as KtDeclaration)
            .getDeclarationDescriptorIncludingConstructors(Utils.ProjectTools.ctx) as CallableDescriptor).returnType!!
        return String.format("return %s", generateConstant(scope, ExpressionDepth, returnType))
    }
}