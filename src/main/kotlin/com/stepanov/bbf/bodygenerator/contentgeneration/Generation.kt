package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.Config
import org.jetbrains.kotlin.types.KotlinType
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.functions

object Generation {

    fun createExpression(scope: BodyScope, depth: Int, type: KotlinType = scope.getRandomType()): String =
        if (depth > 0)
            generateExpression(scope, depth, type)
        else
            createConstant(scope, depth, type)

    fun createConstant(scope: BodyScope, depth: Int, type: KotlinType = scope.getRandomType()): String =
        scope.getRandomPropertyByType(type) ?: Expression.Constant(scope, depth, type).generate()

    fun generateDeclaration(scope: BodyScope, depth: Int, type: KotlinType = scope.getRandomType(), isVar: Boolean = Random.nextBoolean()): String =
        Config.Declarations.randomContent().call(scope, depth, type, isVar)

    fun generateStatement(scope: BodyScope, depth: Int): String = Config.Statements.randomContent().call(scope, depth)

    fun generateExpression(scope: BodyScope, depth: Int, type: KotlinType = scope.getRandomType()): String =
            (Config.SingleTypeExpressions
                .filter { scope.typeTranslator[it.first] == type }
                .map { it.second } + Config.MultiTypeExpression)
                .randomContent()
                .let { klass ->
                    if (Config.SingleTypeExpressions.map { it.second.first }.contains(klass))
                        klass.call(scope, depth)
                    else
                        klass.call(scope, depth, type)
                }

    fun KClass<*>.call(scope: BodyScope, depth: Int): String =
        this.functions.first().call(this.constructors.first().call(scope, depth - 1)) as String

    fun KClass<*>.call(scope: BodyScope, depth: Int, type: KotlinType): String =
        this.functions.first().call(this.constructors.first().call(scope, depth - 1, type)) as String

    fun KClass<*>.call(scope: BodyScope, depth: Int, type: KotlinType, isVar: Boolean): String =
        this.functions.first().call(this.constructors.first().call(scope, depth - 1, type, isVar)) as String

    fun List<Pair<KClass<out ContentGeneration>, Int>>.randomContent(): KClass<*> {
        var curPower = Random.nextInt(this.sumOf { it.second })
        for (pair in this) {
            if (curPower < pair.second)
                return pair.first
            else
                curPower -= pair.second
        }
        throw IllegalStateException()
    }
}
