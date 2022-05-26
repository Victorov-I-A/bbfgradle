package com.stepanov.bbf.bodygenerator

import com.stepanov.bbf.bodygenerator.Config.AnyTypeExpression
import com.stepanov.bbf.bodygenerator.Config.Statements
import com.stepanov.bbf.bodygenerator.Config.Declarations
import com.stepanov.bbf.bodygenerator.Config.MultiTypeExpression
import com.stepanov.bbf.bodygenerator.Config.SingleTypeExpressions
import com.stepanov.bbf.bodygenerator.TypeTable.getRandomType
import com.stepanov.bbf.bodygenerator.TypeTable.primitiveTypesTranslator
import com.stepanov.bbf.bodygenerator.Utils.randomContent
import com.stepanov.bbf.bodygenerator.contentgeneration.Declaration
import com.stepanov.bbf.bodygenerator.contentgeneration.Expression
import com.stepanov.bbf.bodygenerator.contentgeneration.Statement
import com.stepanov.bbf.bugfinder.util.name
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.types.KotlinType
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.functions


object Generation {

    fun generateContent(scope: BodyScope, depth: Int): String {
        (SingleTypeExpressions.map { it.second } + MultiTypeExpression + AnyTypeExpression + Statements + Declarations)
            .randomContent()
            .let { klass ->
                return when {
                    Declaration::class.sealedSubclasses.contains(klass) ->
                        klass.generate(scope, depth, getRandomType(), Random.nextBoolean())
                    Statement::class.sealedSubclasses.contains(klass) ->
                        klass.generate(scope, depth)
                    depth <= 0 ->
                        generateValue(scope, depth)
                    Expression.AnyTypeExpression::class.sealedSubclasses.contains(klass) ->
                        klass.generate(scope, depth, getRandomType())
                    Expression.MultiTypeExpression::class.sealedSubclasses.contains(klass) ->
                        klass.generate(
                            scope,
                            depth,
                            Expression.MultiTypeExpression.possibleTypes[klass]!!
                                .randomContent().let {
                                    if (it.typeParameters.isNotEmpty())
                                        TypeTable.generateCollectionTypeFromClass(it)
                                    else
                                        primitiveTypesTranslator[it.simpleName!!]!!
                                })
                    else ->
                        klass.generate(scope, depth)
                }
            }
    }

    fun generateExpression(scope: BodyScope, depth: Int, type: KotlinType = getRandomType()): String =
        if (depth > 0)
            (SingleTypeExpressions
                .filter { type.name == it.first.simpleName }.map { it.second }
                    + MultiTypeExpression
                .filter {
                    Expression.MultiTypeExpression.possibleTypes[it.first]!!
                        .map { klass -> klass.key.simpleName}.contains(type.name)
                }
                    + AnyTypeExpression)
                .randomContent()
                .let { klass ->
                    if (Expression.SingleTypeExpression::class.sealedSubclasses.contains(klass))
                        klass.generate(scope, depth)
                    else
                        klass.generate(scope, depth, type)
                }
        else
            generateValue(scope, depth, type)

    fun generateDeclaration(
        scope: BodyScope,
        depth: Int,
        type: KotlinType = getRandomType(),
        isVar: Boolean = Random.nextBoolean()
    ): String = Declarations.randomContent().generate(scope, depth, type, isVar)

    fun generateStatement(scope: BodyScope, depth: Int): String = Statements.randomContent().generate(scope, depth)

    fun generateValue(scope: BodyScope, depth: Int, type: KotlinType = getRandomType()): String =
        scope.getRandomPropertyByType(type) ?: Expression.AnyTypeExpression.Constant(scope, depth, type).generate()

    fun generateVariable(scope: BodyScope, depth: Int, type: KotlinType = getRandomType()): String {
        return scope.getRandomPropertyByType(type, onlyVar = true) ?:
        Declaration.PropertyDeclaration(scope, depth - 1, type, true).generate().let {
            return it + "\n" + (scope.scopeTable.innerScope.last().last().psiElement as KtProperty).identifyingElement!!.text
        }
    }

    fun KClass<*>.generate(scope: BodyScope, depth: Int): String =
        this.functions.first().call(this.constructors.first().call(scope, depth - 1)) as String

    fun KClass<*>.generate(scope: BodyScope, depth: Int, type: KotlinType): String =
        this.functions.first().call(this.constructors.first().call(scope, depth - 1, type)) as String

    fun KClass<*>.generate(scope: BodyScope, depth: Int, type: KotlinType, isVar: Boolean): String =
        this.functions.first().call(this.constructors.first().call(scope, depth - 1, type, isVar)) as String
}
