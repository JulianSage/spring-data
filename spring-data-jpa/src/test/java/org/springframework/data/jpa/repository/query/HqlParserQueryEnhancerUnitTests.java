/*
 * Copyright 2023 the original author or authors.
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
package org.springframework.data.jpa.repository.query;

import static org.assertj.core.api.Assumptions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * TCK Tests for {@link HqlQueryParser} mixed into {@link JpaQueryParsingEnhancer}.
 *
 * @author Greg Turnquist
 * @since 3.1
 */
public class HqlParserQueryEnhancerUnitTests extends QueryEnhancerTckTests {

	public static final String HQL_PARSER_DOES_NOT_SUPPORT_NATIVE_QUERIES = "HqlParser does not support native queries";

	@Override
	QueryEnhancer createQueryEnhancer(DeclaredQuery declaredQuery) {
		return new JpaQueryParsingEnhancer(new HqlQueryParser(declaredQuery));
	}

	@Override
	@ParameterizedTest // GH-2773
	@MethodSource("jpqlCountQueries")
	void shouldDeriveJpqlCountQuery(String query, String expected) {

		assumeThat(query).as("HqlParser replaces the column name with alias name for count queries") //
				.doesNotContain("SELECT name FROM table_name some_alias");

		assumeThat(expected).as("HqlParser does turn 'select a.b' into 'select count(a.b)'") //
				.doesNotContain("select count(a.b");

		super.shouldDeriveJpqlCountQuery(query, expected);
	}

	@Disabled(HQL_PARSER_DOES_NOT_SUPPORT_NATIVE_QUERIES)
	@Override
	void findProjectionClauseWithIncludedFrom() {}

	@Disabled(HQL_PARSER_DOES_NOT_SUPPORT_NATIVE_QUERIES)
	@Override
	void shouldDeriveNativeCountQuery(String query, String expected) {}

	@Disabled(HQL_PARSER_DOES_NOT_SUPPORT_NATIVE_QUERIES)
	@Override
	void shouldDeriveNativeCountQueryWithVariable(String query, String expected) {}

}
