/*
 * Copyright 2008-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.data.jpa.repository.query;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.RepositoryQuery;


/**
 * Query lookup strategy to execute finders.
 * 
 * @author Oliver Gierke
 */
public class JpaQueryLookupStrategy {

    /**
     * Base class for {@link QueryLookupStrategy} implementations that need
     * access to an {@link EntityManager}.
     * 
     * @author Oliver Gierke
     */
    private static abstract class AbstractQueryLookupStrategy implements
            QueryLookupStrategy<JpaQueryMethod> {

        private EntityManager em;


        public AbstractQueryLookupStrategy(EntityManager em) {

            this.em = em;
        }


        /*
         * (non-Javadoc)
         * 
         * @see
         * org.springframework.data.jpa.repository.query.QueryLookupStrategy
         * #resolveQuery(org.springframework.data.repository.query.QueryMethod)
         */
        public final RepositoryQuery resolveQuery(JpaQueryMethod method) {

            return resolveQuery(method, em);
        }


        protected abstract RepositoryQuery resolveQuery(JpaQueryMethod method,
                EntityManager em);
    }

    /**
     * {@link QueryLookupStrategy} to create a query from the method name.
     * 
     * @author Oliver Gierke
     */
    private static class CreateQueryLookupStrategy extends
            AbstractQueryLookupStrategy {

        public CreateQueryLookupStrategy(EntityManager em) {

            super(em);
        }


        @Override
        protected RepositoryQuery resolveQuery(JpaQueryMethod method,
                EntityManager em) {

            return SimpleJpaQuery.construct(method, em);
        }
    }

    /**
     * {@link QueryLookupStrategy} that tries to detect a declared query
     * declared via {@link Query} annotation followed by a JPA named query
     * lookup.
     * 
     * @author Oliver Gierke
     */
    private static class DeclaredQueryLookupStrategy extends
            AbstractQueryLookupStrategy {

        public DeclaredQueryLookupStrategy(EntityManager em) {

            super(em);
        }


        @Override
        protected RepositoryQuery resolveQuery(JpaQueryMethod method,
                EntityManager em) {

            RepositoryQuery query =
                    SimpleJpaQuery.fromHadesAnnotation(method, em);

            if (null != query) {
                return query;
            }

            query = NamedQuery.lookupFrom(method, em);

            if (null != query) {
                return query;
            }

            throw new IllegalStateException(
                    String.format(
                            "Did neither find a NamedQuery nor a Hades Query for method %s!",
                            method));
        }

    }

    /**
     * {@link QueryLookupStrategy} to try to detect a declared query first (
     * {@link Query}, JPA named query). In case none is found we fall back on
     * query creation.
     * 
     * @author Oliver Gierke
     */
    private static class CreateIfNotFoundQueryLookupStrategy extends
            AbstractQueryLookupStrategy {

        private final DeclaredQueryLookupStrategy strategy;
        private final CreateQueryLookupStrategy createStrategy;


        public CreateIfNotFoundQueryLookupStrategy(EntityManager em) {

            super(em);
            this.strategy = new DeclaredQueryLookupStrategy(em);
            this.createStrategy = new CreateQueryLookupStrategy(em);
        }


        @Override
        protected RepositoryQuery resolveQuery(JpaQueryMethod method,
                EntityManager em) {

            try {
                return strategy.resolveQuery(method, em);
            } catch (IllegalStateException e) {
                return createStrategy.resolveQuery(method);
            }
        }
    }


    /**
     * Creates a {@link QueryLookupStrategy} for the given {@link EntityManager}
     * and {@link QueryLookupStrategy.Key}.
     * 
     * @param em
     * @param key
     * @return
     */
    public static QueryLookupStrategy<JpaQueryMethod> create(EntityManager em,
            Key key) {

        if (key == null) {
            return new CreateIfNotFoundQueryLookupStrategy(em);
        }

        switch (key) {
        case CREATE:
            return new CreateQueryLookupStrategy(em);
        case USE_DECLARED_QUERY:
            return new DeclaredQueryLookupStrategy(em);
        case CREATE_IF_NOT_FOUND:
            return new CreateIfNotFoundQueryLookupStrategy(em);
        default:
            throw new IllegalArgumentException(String.format(
                    "Unsupported query lookup strategy %!", key));
        }
    }
}