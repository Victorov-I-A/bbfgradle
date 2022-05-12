package com.stepanov.bbf.bodygenerator

import com.intellij.psi.PsiElement
import com.stepanov.bbf.bugfinder.mutator.transformations.util.ScopeCalculator
import com.stepanov.bbf.bugfinder.util.flatten
import com.stepanov.bbf.bugfinder.util.getFirstParentOfType
import org.jetbrains.kotlin.cfg.getDeclarationDescriptorIncludingConstructors
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.psi.KtClass

typealias ScopeComponent = ScopeCalculator.ScopeComponent

class BodyScope(val body: KtExpression, scopeCalculator: ScopeCalculator) {
    val rig = scopeCalculator.rig!!
    val ctx = scopeCalculator.ctx

    val scopeTable = ScopeTable(scopeCalculator.calcScope(body))

    fun getRandomPropertyByType(type: KotlinType): String? {
        val component = scopeTable.getComponentByType(type) ?: return null
        val expression = component.makeExpressionToInsertFromPsiElement(rig)!!.psiElement.text
        return if (scopeTable.outerScope.contains(component) &&
            component.psiElement.getFirstParentOfType<KtClass>() != body.getFirstParentOfType<KtClass>())
                (component.psiElement.getFirstParentOfType<KtClass>()?.generatePath() ?: "") + expression
        else
            expression
    }

    private fun KtClass.generatePath(): String =
        rig.classInstanceGenerator.generateRandomInstanceOfUserClass(
            (this.getDeclarationDescriptorIncludingConstructors(ctx!!) as ClassDescriptor)
                .defaultType)!!.first!!.text + '.'

    val typeTranslator = Config.AllowedTypes.associateWith { rig.randomTypeGenerator.generateType(it)!! }

    fun getRandomType(): KotlinType = typeTranslator.values.random()

    data class ScopeTable(
        val outerScope: List<ScopeComponent>,
    ) {
        val innerScope: MutableList<MutableList<ScopeComponent>> = mutableListOf(mutableListOf())

        private val maxLevel: Int = Config.BlockDepth
        private var curLevel: Int = 0

        fun getComponentByType(type: KotlinType): ScopeComponent? = filterByType(type).ifEmpty { null }?.random()

        private fun filterByType(type: KotlinType): List<ScopeComponent> =
            (innerScope.flatten<ScopeComponent>() + outerScope).filter { it.type == type }


        fun putProperty(property: PsiElement, type: KotlinType) {
            innerScope.last().add(ScopeComponent(property, type.constructor.declarationDescriptor, type))
        }

        fun addLevel(): Boolean {
            return if (curLevel == maxLevel)
                false
            else {
                innerScope.add(mutableListOf())
                curLevel++
                true
            }
        }

        fun deleteLevel(): Boolean {
            return if (curLevel == 0)
                false
            else {
                innerScope.removeLast()
                curLevel--
                true
            }
        }
    }
}