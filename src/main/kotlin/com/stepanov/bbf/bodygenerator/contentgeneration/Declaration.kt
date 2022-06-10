package com.stepanov.bbf.bodygenerator.contentgeneration

import com.stepanov.bbf.bodygenerator.BodyScope
import com.stepanov.bbf.bodygenerator.TypeTable.userDefinedTypes
import com.stepanov.bbf.bodygenerator.Utils.generateUserDefinedTypePath
import com.stepanov.bbf.bodygenerator.Generation.generateExpression
import com.stepanov.bbf.bodygenerator.TypeTable.generateType
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import org.jetbrains.kotlin.types.KotlinType


sealed class Declaration(scope: BodyScope, depth: Int, type: KotlinType): Content(scope, depth) {

    class PropertyDeclaration(
        scope: BodyScope,
        depth: Int,
        type: KotlinType,
        private val isVar: Boolean
    ) : Declaration(scope, depth, type) {

        companion object {
            var propertyIndex: Int = 0
                get() {
                    field++
                    return field
                }
        }

        private val strType: String
        private val type: KotlinType

        init {
            strType =
                if (userDefinedTypes.values.contains(type))
                    generateUserDefinedTypePath(type)
                else
                    type.toString()
            this.type =
                if (userDefinedTypes.values.contains(type))
                    generateType(strType)
                else
                    type
        }

        override fun generate(): String =
            Factory.psiFactory.createProperty(
                "property$propertyIndex",
                strType,
                isVar,
                generateExpression(scope, depth, type)
            ).let {
                scope.scopeTable.putProperty(it, type)
                it.text
            }
    }
}
