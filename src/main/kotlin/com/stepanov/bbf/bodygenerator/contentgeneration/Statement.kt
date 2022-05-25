package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.Config.BlockSize
import com.stepanov.bbf.bodygenerator.contentgeneration.Generation.generateConstant
import com.stepanov.bbf.bodygenerator.contentgeneration.Generation.generateContent


sealed class Statement(scope: BodyScope, depth: Int) : Content(scope, depth) {

    fun generateBlock(): String {
            val blockContent = mutableListOf<String>()
            if (scope.scopeTable.addLevel()) {
                for (i in 0 until BlockSize) {
                    blockContent += generateContent(scope, depth)
                }
                scope.scopeTable.deleteLevel()
            } else {
                for (i in 0 until BlockSize) {
                    blockContent += generateConstant(scope, depth)
                }
            }
            return blockContent.joinToString(separator = "\n", prefix = "\n", postfix = "\n")
        }

    class For(scope: BodyScope, depth: Int) : Statement(scope, depth) {
        override fun generate(): String {
            return String.format("for (i in 0..1) {%s}", generateBlock())
        }
    }

    class While(scope: BodyScope, depth: Int) : Statement(scope, depth) {
        override fun generate(): String {
            return String.format("while (%s) {%s}", boolExpr, generateBlock())
        }
    }

    class DoWhile(scope: BodyScope, depth: Int) : Statement(scope, depth) {
        override fun generate(): String {
            return String.format("do {%s} while (%s)", generateBlock(), boolExpr)
        }
    }
}