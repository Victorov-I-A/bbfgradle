package com.stepanov.bbf.bugfinder.mutator.transformations.tce

import com.intellij.psi.PsiElement
import com.stepanov.bbf.bugfinder.generator.targetsgenerators.RandomInstancesGenerator
import com.stepanov.bbf.bugfinder.mutator.transformations.Factory
import com.stepanov.bbf.bugfinder.generator.targetsgenerators.typeGenerators.RandomTypeGenerator
import com.stepanov.bbf.bugfinder.util.getTrue
import com.stepanov.bbf.bugfinder.util.isAbstractClass
import org.apache.log4j.Logger
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.types.typeUtil.isInterface
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import java.lang.StringBuilder
import kotlin.random.Random

class FillerGenerator(
    private val psi: KtFile,
    private val ctx: BindingContext,
    private val generatedUsages: MutableList<Triple<KtExpression, String, KotlinType?>>
) {

    private val randomTypeGenerator = RandomTypeGenerator
    private val blockListOfTypes = listOf("Unit", "Nothing", "Nothing?")
    private val log = Logger.getLogger("mutatorLogger")

    init {
        randomTypeGenerator.setFileAndContext(psi, ctx)
    }

    fun generateExpressionOfType(type: KotlinType, depth: Int = 0): KtExpression? {
        val randomUsage = generatedUsages.randomOrNull() ?: return null
        val isNullable = randomUsage.third!!.isNullable()
        return StdLibraryGenerator.generateForStandardType(randomUsage.third!!, type)
            .random()
            .let { handleCallSeq(it) }
            ?.let {
                val prefix = if (isNullable) "(${randomUsage.second})?." else "(${randomUsage.second})."
                Factory.psiFactory.createExpressionIfPossible("$prefix${it.text}")
            }
    }

    fun getFillExpressions(node: Pair<KtExpression, KotlinType?>, depth: Int = 0): List<KtExpression> {
        log.debug("replacing ${node.first.text} ${node.second}")
        //Nullable or most common types
        val res = mutableListOf<KtExpression>()
        val nodeType = node.second ?: return emptyList()
        log.debug("Getting value of type $nodeType")
//        val strNodeType = nodeType.toString().let { if (it.endsWith("?")) it.substring(0, it.length - 1) else it }
        val isNullable = nodeType.isNullable()
        val generated = RandomInstancesGenerator(psi).generateValueOfType(nodeType)
        log.debug("GENERATED VALUE OF TYPE $nodeType = $generated")
        if (generated.isNotEmpty()) {
            Factory.psiFactory.createExpressionIfPossible(generated)?.let {
                log.debug("GENERATED IS CALL =${it is KtCallExpression}")
                res.add(it)
            }
        }
        //Generate instance of random type and try to get needed type from it
        val randomType = randomTypeGenerator.generateRandomTypeWithCtx()!!
        log.debug("randomType = $randomType")
        if (!randomType.isAbstractClass() && !randomType.isInterface()) {
            val ins = RandomInstancesGenerator(psi).generateValueOfType(randomType, nullIsPossible = false)
            val variants = StdLibraryGenerator.generateForStandardType(randomType, nodeType.makeNotNullable())
            variants.randomOrNull()?.let { variant ->
                val prefix = if (randomType.isNullable())
                    "($ins)?."
                else "($ins)."
                val callSeq = handleCallSeq(variant)
                val postfix = if (variant.any { it.returnType?.isNullable() == true } && !isNullable) "!!" else ""
                Factory.psiFactory.createExpressionIfPossible("$prefix${callSeq?.text}$postfix")?.let {
                    log.debug("Generated call from random type = ${it.text}")
                    if (Random.getTrue(20)) return listOf(it)
                    res.add(it)
                }
            }
        }
        val localRes = mutableListOf<PsiElement>()
        val checkedTypes = mutableListOf<String>()

        for (el in generatedUsages.filter { it.first !is KtProperty }.shuffled()) {
            if (el.third.toString() in blockListOfTypes) continue
            val elCopy = el.first.copy()
            when {
                el.third?.toString() == "$nodeType" -> {
                    localRes.add(elCopy)
                }
                el.third?.toString() == "$nodeType?" -> {
                    localRes.add(elCopy)
                }
                StdLibraryGenerator.isImplementation(nodeType, el.third) -> {
                    localRes.add(elCopy)
                }
                //commonTypesMap[strNodeType]?.contains(el.third?.toString()) ?: false -> localRes.add(el.first)
            }
            if (nodeType.isNullable()) res.add(Factory.psiFactory.createExpression("null"))
            if (depth > 0) continue
            //val deeperCases = UsageSamplesGeneratorWithStLibrary.generateForStandardType(el.third!!, nodeType)
            log.debug("GETTING ${nodeType} from ${el.third.toString()}")
            if (checkedTypes.contains(el.third!!.toString())) continue
            checkedTypes.add(el.third!!.toString())
            StdLibraryGenerator.generateForStandardType(el.third!!, nodeType)
                .shuffled()
                .take(10)
                .forEach { list ->
                    log.debug("Case = ${list.map { it }}")
                    handleCallSeq(list)?.let {
                        val prefix = if (isNullable) "(${el.second})?." else "(${el.second})."
                        Factory.psiFactory.createExpressionIfPossible("$prefix${it.text}")?.let {
                            log.debug("GENERATED CALL = ${it.text}")
                            localRes.add(it)
                        }
                    }
                }
            if (localRes.isNotEmpty()) {
                localRes.forEach { res.add(it as KtExpression) }
                break
            }
        }
        return res
    }

    private fun handleCallSeq(postfix: List<CallableDescriptor>): KtExpression? {
        val res = StringBuilder()
        var prefix = ""
        postfix.map { desc ->
            val expr = when (desc) {
                is PropertyDescriptor -> desc.name.asString()
                is FunctionDescriptor -> generateCallExpr(desc)?.text
                else -> ""
            }
            expr ?: return null
            res.append(prefix)
            prefix = if (desc.returnType?.isNullable() == true) "?." else "."
            res.append(expr)
        }
        return Factory.psiFactory.createExpression(res.toString())
    }

    //We are not expecting typeParams
    private fun generateCallExpr(func: CallableDescriptor): KtExpression? {
        log.debug("GENERATING call of type $func")
        val name = func.name
        val valueParams = func.valueParameters.map { vp ->
            val fromUsages = generatedUsages.filter { usage -> "${vp.type}".trim() == "${usage.third}".trim() }
            if (fromUsages.isNotEmpty() && Random.getTrue(70)) fromUsages.random().second
            else RandomInstancesGenerator(psi).generateValueOfType(vp.type)
            //getInsertableExpressions(Pair(it, it.typeReference?.getAbbreviatedTypeOrType()), 1).randomOrNull()
        }
        if (valueParams.any { it.isEmpty() }) {
            log.debug("CANT GENERATE PARAMS FOR $func")
            return null
        }
        val inv = "$name(${valueParams.joinToString()})"
        return Factory.psiFactory.createExpressionIfPossible(inv)
    }

}