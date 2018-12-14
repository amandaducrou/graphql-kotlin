package com.expedia.graphql.sample.query

import com.expedia.graphql.annotations.GraphQLDefaultValue
import com.expedia.graphql.annotations.GraphQLDescription
import com.expedia.graphql.sample.model.Widget
import com.expedia.graphql.sample.model.WidgetType
import org.springframework.stereotype.Component

/**
 * Simple widget query.
 */
@Component
class WidgetQuery: Query {

    @GraphQLDescription("creates new widget for given ID")
    fun widgetById(@GraphQLDescription("The special ingredient") id: Int, @GraphQLDefaultValue("UNICORN") type: WidgetType? = WidgetType.UNICORN): Widget? = Widget(value = id, type = type)
}