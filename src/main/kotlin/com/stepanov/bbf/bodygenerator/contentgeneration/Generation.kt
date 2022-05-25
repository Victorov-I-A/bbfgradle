package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.Config.MultiTypeExpression
import com.stepanov.bbf.bodygenerator.Config.Statements
import com.stepanov.bbf.bodygenerator.Config.Declarations
import com.stepanov.bbf.bodygenerator.Config.SingleTypeExpressions
import com.stepanov.bbf.bodygenerator.TypeTable.getRandomType
import com.stepanov.bbf.bugfinder.util.name
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.parentsWithSelf
import org.jetbrains.kotlin.types.KotlinType
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.functions


object Generation {

    fun generateContent(scope: BodyScope, depth: Int): String {
        (SingleTypeExpressions.map { it.second } + MultiTypeExpression + Statements + Declarations)
            .randomContent()
            .let { klass ->
                return when {
                    Declaration::class.sealedSubclasses.contains(klass) ->
                        klass.generate(scope, depth, getRandomType(), Random.nextBoolean())
                    Statement::class.sealedSubclasses.contains(klass) ->
                        klass.generate(scope, depth)
                    depth <= 0 ->
                        generateConstant(scope, depth)
                    Expression.MultiTypeExpression::class.sealedSubclasses.contains(klass) ->
                        klass.generate(scope, depth, getRandomType())
                    else ->
                        klass.generate(scope, depth)
                }
            }
    }

    fun generateExpression(scope: BodyScope, depth: Int, type: KotlinType = getRandomType()): String =
        if (depth > 0)
            (SingleTypeExpressions.filter { type.name == it.first.simpleName }.map { it.second }
                    + MultiTypeExpression)
                .randomContent()
                .let { klass ->
                    if (Expression.MultiTypeExpression::class.sealedSubclasses.contains(klass))
                        klass.generate(scope, depth, type)
                    else
                        klass.generate(scope, depth)
                }
        else
            generateConstant(scope, depth, type)

    fun generateDeclaration(
        scope: BodyScope,
        depth: Int,
        type: KotlinType = getRandomType(),
        isVar: Boolean = Random.nextBoolean()
    ): String = Declarations.randomContent().generate(scope, depth, type, isVar)

    fun generateStatement(scope: BodyScope, depth: Int): String = Statements.randomContent().generate(scope, depth)

    fun generateConstant(scope: BodyScope, depth: Int, type: KotlinType = getRandomType()): String =
        scope.getRandomPropertyByType(type) ?: Expression.MultiTypeExpression.Constant(scope, depth, type).generate()

    fun KClass<*>.generate(scope: BodyScope, depth: Int): String =
        this.functions.first().call(this.constructors.first().call(scope, depth - 1)) as String

    fun KClass<*>.generate(scope: BodyScope, depth: Int, type: KotlinType): String =
        this.functions.first().call(this.constructors.first().call(scope, depth - 1, type)) as String

    fun KClass<*>.generate(scope: BodyScope, depth: Int, type: KotlinType, isVar: Boolean): String =
        this.functions.first().call(this.constructors.first().call(scope, depth - 1, type, isVar)) as String

    fun <T>  List<Pair<T, Int>>.randomContent(): T {
        var curPower = Random.nextInt(this.sumOf { it.second })
        for (pair in this) {
            if (curPower < pair.second)
                return pair.first
            else
                curPower -= pair.second
        }
        throw IllegalStateException()
    }

    fun <T> Map<T, Int>.randomContent(): T {
        var curPower = Random.nextInt(this.values.sum())
        for (pair in this) {
            if (curPower < pair.value)
                return pair.key
            else
                curPower -= pair.value
        }
        throw IllegalStateException()
    }
}
