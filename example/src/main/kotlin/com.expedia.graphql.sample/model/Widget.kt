package com.expedia.graphql.sample.model

import com.expedia.graphql.annotations.GraphQLDefaultValue
import com.expedia.graphql.annotations.GraphQLDescription
import com.expedia.graphql.annotations.GraphQLIgnore

@GraphQLDescription("A useful widget")
data class Widget(
    @GraphQLDescription("The widget's value that can be null")
    var value: Int? = null,
    @Deprecated(message = "This field is deprecated", replaceWith = ReplaceWith("value"))
    @GraphQLDescription("The widget's deprecated value that shouldn't be used")
    val deprecatedValue: Int? = value,
    @GraphQLIgnore
    val ignoredField: String? = "ignored",
    private val hiddenField: String? = "hidden",
    @property:GraphQLDescription("The type of the widget with default value")
    @property:GraphQLDefaultValue("UNICORN")
    var type: WidgetType? = WidgetType.UNICORN
) {

    @GraphQLDescription("returns original value multiplied by target OR null if original value was null")
    fun multiplyValueBy(multiplier: Int) = value?.times(multiplier)
}

enum class WidgetType {
    UNICORN,
    DRAGON
}
