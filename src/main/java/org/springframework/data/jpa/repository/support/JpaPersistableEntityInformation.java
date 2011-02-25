/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.jpa.repository.support;

import java.io.Serializable;

import javax.persistence.metamodel.Metamodel;

import org.springframework.data.domain.Persistable;


/**
 * Extension of {@link JpaMetamodelEntityInformation} that consideres methods of
 * {@link Persistable} to lookup the id.
 * 
 * @author Oliver Gierke
 */
public class JpaPersistableEntityInformation<T extends Persistable> extends
        JpaMetamodelEntityInformation<T> {

    /**
     * Creates a new {@link JpaPersistableEntityInformation} for the given
     * domain class and {@link Metamodel}.
     * 
     * @param domainClass
     * @param metamodel
     */
    public JpaPersistableEntityInformation(Class<T> domainClass,
            Metamodel metamodel) {

        super(domainClass, metamodel);
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.data.jpa.repository.support.JpaMetamodelEntityMetadata
     * #getId(java.lang.Object)
     */
    @Override
    public Serializable getId(T entity) {

        return entity.getId();
    }
}
