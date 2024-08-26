/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fi.livi.digitraffic.common.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionMessage.Style;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link Condition} that checks if the specified property have non blank value.
 *
 * @see ConditionalOnPropertyNotBlank
 *
 * Copied partly from {@link org.springframework.boot.autoconfigure.condition.OnPropertyCondition}
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
class OnPropertyNotBlankCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
		final List<AnnotationAttributes> allAnnotationAttributes = metadata.getAnnotations()
			.stream(ConditionalOnPropertyNotBlank.class.getName())
			.filter(MergedAnnotationPredicates.unique(MergedAnnotation::getMetaTypes))
			.map(MergedAnnotation::asAnnotationAttributes)
			.toList();
		final List<ConditionMessage> noMatch = new ArrayList<>();
		final List<ConditionMessage> match = new ArrayList<>();
		for (final AnnotationAttributes annotationAttributes : allAnnotationAttributes) {
			final ConditionOutcome outcome = determineOutcome(annotationAttributes, context.getEnvironment());
			(outcome.isMatch() ? match : noMatch).add(outcome.getConditionMessage());
		}
		if (!noMatch.isEmpty()) {
			return ConditionOutcome.noMatch(ConditionMessage.of(noMatch));
		}
		return ConditionOutcome.match(ConditionMessage.of(match));
	}

	private ConditionOutcome determineOutcome(final AnnotationAttributes annotationAttributes, final PropertyResolver resolver) {
		final Spec spec = new Spec(annotationAttributes);
		final List<String> missingProperties = new ArrayList<>();
		final List<String> nonMatchingProperties = new ArrayList<>();
		spec.collectProperties(resolver, missingProperties, nonMatchingProperties);
		if (!missingProperties.isEmpty()) {
			return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnProperty.class, spec)
				.didNotFind("property", "properties")
				.items(Style.QUOTE, missingProperties));
		}
		if (!nonMatchingProperties.isEmpty()) {
			return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnProperty.class, spec)
				.found("different value in property", "different value in properties")
				.items(Style.QUOTE, nonMatchingProperties));
		}
		return ConditionOutcome
			.match(ConditionMessage.forCondition(ConditionalOnProperty.class, spec).because("matched"));
	}

	private static class Spec {

		private final String prefix;

		private final String[] names;

		private final boolean matchIfMissing;

		Spec(final AnnotationAttributes annotationAttributes) {
			String prefix = annotationAttributes.getString("prefix").trim();
			if (StringUtils.hasText(prefix) && !prefix.endsWith(".")) {
				prefix = prefix + ".";
			}
			this.prefix = prefix;
			this.names = getNames(annotationAttributes);
			this.matchIfMissing = annotationAttributes.getBoolean("matchIfMissing");
		}

		private String[] getNames(final Map<String, Object> annotationAttributes) {
			final String[] value = (String[]) annotationAttributes.get("value");
			final String[] name = (String[]) annotationAttributes.get("name");
			Assert.state(value.length > 0 || name.length > 0,
					"The name or value attribute of @ConditionalOnProperty must be specified");
			Assert.state(value.length == 0 || name.length == 0,
					"The name and value attributes of @ConditionalOnProperty are exclusive");
			return (value.length > 0) ? value : name;
		}

		private void collectProperties(final PropertyResolver resolver, final List<String> missing, final List<String> nonMatching) {
			for (final String name : this.names) {
				final String key = this.prefix + name;
				if (resolver.containsProperty(key)) {
					if (!isMatch(resolver.getProperty(key))) {
						nonMatching.add(name);
					}
				}
				else {
					if (!this.matchIfMissing) {
						missing.add(name);
					}
				}
			}
		}

		private boolean isMatch(final String value) {
			return StringUtils.hasText(value) && !"false".equalsIgnoreCase(value);
		}

		@Override
		public String toString() {
			final StringBuilder result = new StringBuilder();
			result.append("(");
			result.append(this.prefix);
			if (this.names.length == 1) {
				result.append(this.names[0]);
			}
			else {
				result.append("[");
				result.append(StringUtils.arrayToCommaDelimitedString(this.names));
				result.append("]");
			}
			result.append(")");
			return result.toString();
		}

	}

}
