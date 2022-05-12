package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.contentgeneration.Generation.createConstant
import com.stepanov.bbf.bodygenerator.contentgeneration.Generation.createExpression

sealed class Statement(scope: BodyScope, depth: Int): ContentGeneration(scope, depth) {

    fun genStateBlock(): String {
            val blockContent = mutableListOf<String>()
            if (scope.scopeTable.addLevel()) {
                for (i in 0..0) {
                    blockContent += createExpression(scope, depth)
                }
                blockContent.addAll(0, scope.scopeTable.innerScope.last().map { it.psiElement.text })
                scope.scopeTable.deleteLevel()
            } else {
                for (i in 0..0) {
                    blockContent += createConstant(scope, depth)
                }
            }
            return blockContent.joinToString(separator = "\n", prefix = "\n", postfix = "\n")
        }

    class For(scope: BodyScope, depth: Int) : Statement(scope, depth) {
        override fun generate(): String {
            return "for (i in 0..1) ${genStateBlock()}"
        }
    }

    class While(scope: BodyScope, depth: Int) : Statement(scope, depth) {
        override fun generate(): String {
            return "while ($boolExpr) {${genStateBlock()}}\n"
        }
    }

    class DoWhile(scope: BodyScope, depth: Int) : Statement(scope, depth) {
        override fun generate(): String {
            return "do {${genStateBlock()}} while ($boolExpr)\n"
        }
    }
}