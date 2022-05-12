package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.contentgeneration.Generation.createExpression
import org.jetbrains.kotlin.types.KotlinType

sealed class Expression(scope: BodyScope, depth: Int): ContentGeneration(scope, depth) {

    fun genExprBlock(type: KotlinType): String {
        val blockContent = mutableListOf<String>()
        if (scope.scopeTable.addLevel()) {
            for (i in 0..0) {
                blockContent += createExpression(scope, depth, type)
            }
            blockContent.addAll(0, scope.scopeTable.innerScope.last().map { it.psiElement.text })
            scope.scopeTable.deleteLevel()
        } else {
            for (i in 0..0) {
                blockContent += Constant(scope, depth, type).generate()
            }
        }
        return blockContent.joinToString(separator = "\n", prefix = "\n", postfix = "\n")
    }

    class Constant(scope: BodyScope, depth: Int, val type: KotlinType) : Expression(scope, depth) {
        override fun generate(): String = scope.rig.generateValueOfType(type)
    }

    class If(scope: BodyScope, depth: Int, val type: KotlinType) : Expression(scope, depth) {
        override fun generate(): String {
            return "if ($boolExpr) {${genExprBlock(type)}} else {${genExprBlock(type)}}\n"
        }
    }

    class When(scope: BodyScope, depth: Int, val type: KotlinType) : Expression(scope, depth) {
        override fun generate(): String {
            val randomType = scope.getRandomType()
            return "when (${createExpression(scope, depth, randomType)}) {\n${createExpression(scope, depth, randomType)} -> ${createExpression(scope, depth, type)}\nelse -> ${createExpression(scope, depth, type)}\n}\n"
        }
    }

    class Try(scope: BodyScope, depth: Int, val type: KotlinType) : Expression(scope, depth) {
        override fun generate(): String {
            return "try {${genExprBlock(type)}} catch (e: Exception) {${genExprBlock(type)}}\n"
        }
    }

    class Equality(scope: BodyScope, depth: Int) : Expression(scope, depth) {
        private val operators = setOf("==", "!=", "===", "!==")

        override fun generate(): String = scope.getRandomType().let {
            "${createExpression(scope, depth, it)} ${operators.random()} ${createExpression(scope, depth, it)}"
        }
    }

    class Comparison(scope: BodyScope, depth: Int) : Expression(scope, depth) {
        private val operators = setOf("<", ">", "<=", ">=")

        override fun generate(): String = scope.getRandomType().let {
            "${createExpression(scope, depth, it)} ${operators.random()} ${createExpression(scope, depth, it)}"
        }
    }

    class Disjunction(scope: BodyScope, depth: Int) : Expression(scope, depth) {
        override fun generate(): String = "$boolExpr || $boolExpr"
    }

    class Conjunction(scope: BodyScope, depth: Int) : Expression(scope, depth) {
        override fun generate(): String = "$boolExpr && $boolExpr"
    }
}