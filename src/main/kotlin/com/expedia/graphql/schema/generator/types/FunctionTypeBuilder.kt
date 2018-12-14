package com.expedia.graphql.schema.generator.types

import com.expedia.graphql.schema.KotlinDataFetcher
import com.expedia.graphql.schema.Parameter
import com.expedia.graphql.schema.extensions.directives
import com.expedia.graphql.schema.extensions.getDefaultValueAsString
import com.expedia.graphql.schema.extensions.getDeprecationReason
import com.expedia.graphql.schema.extensions.getGraphQLDescription
import com.expedia.graphql.schema.extensions.hasDefaultValue
import com.expedia.graphql.schema.extensions.isGraphQLContext
import com.expedia.graphql.schema.extensions.throwIfUnathorizedInterface
import com.expedia.graphql.schema.generator.SchemaGenerator
import com.expedia.graphql.schema.generator.TypeBuilder
import graphql.schema.DataFetcher
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLOutputType
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType

@Suppress("Detekt.UnsafeCast")
internal class FunctionTypeBuilder(generator: SchemaGenerator) : TypeBuilder(generator) {

    internal fun function(fn: KFunction<*>, target: Any? = null, abstract: Boolean = false): GraphQLFieldDefinition {
        val builder = GraphQLFieldDefinition.newFieldDefinition()
        builder.name(fn.name)
        builder.description(fn.getGraphQLDescription())

        fn.getDeprecationReason()?.let {
            builder.deprecate(it)
        }

        fn.directives(generator).forEach {
            builder.withDirective(it)
            state.directives.add(it)
        }

        val args = mutableMapOf<String, Parameter>()
        fn.valueParameters.forEach {
            if (!it.isGraphQLContext()) {
                // deprecation of arguments is currently unsupported: https://github.com/facebook/graphql/issues/197
                builder.argument(argument(it))
            }

            val name = it.name
            if (name.isNullOrBlank()) {
                throw IllegalArgumentException("argument name is null or blank, $it")
            } else {
                // Kotlin 1.3 will support contracts, until then we need to force non-null
                @Suppress("Detekt.UnsafeCallOnNullableType")
                args[name] = Parameter(it.type.javaType as Class<*>, it.annotations)
            }
        }

        if (!abstract) {
            val dataFetcher: DataFetcher<*> = KotlinDataFetcher(target, fn, args, config.hooks.dataFetcherExecutionPredicate)
            val hookDataFetcher = config.hooks.didGenerateDataFetcher(fn, dataFetcher)
            builder.dataFetcher(hookDataFetcher)
        }

        val monadType = config.hooks.willResolveMonad(fn.returnType)
        builder.type(graphQLTypeOf(monadType) as GraphQLOutputType)
        val graphQLType = builder.build()
        return config.hooks.onRewireGraphQLType(monadType, graphQLType) as GraphQLFieldDefinition
    }

    private fun argument(parameter: KParameter): GraphQLArgument {
        parameter.throwIfUnathorizedInterface()
        val builder = GraphQLArgument.newArgument()
            .name(parameter.name)
            .description(parameter.getGraphQLDescription() ?: parameter.type.getGraphQLDescription())
            .type(graphQLTypeOf(parameter.type, true) as GraphQLInputType)

        parameter.directives(generator).forEach {
            builder.withDirective(it)
            state.directives.add(it)
        }

        if(parameter.hasDefaultValue()) {
            builder.defaultValue(parameter.getDefaultValueAsString())
        }

        return config.hooks.onRewireGraphQLType(parameter.type, builder.build()) as GraphQLArgument
    }
}
