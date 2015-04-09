/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.core.remote.metadata;

import com.google.common.base.Predicate;
import com.salesforce.ide.core.internal.utils.Constants;

public class CustomObjectNameResolver {
    private static final String KNOWLEDGE_ARTICLE_SUFFIX = "__kav";
    private final Predicate<Pair<String>>[] predicatesToCheck;

    public static CustomObjectNameResolver getCheckerForStandardObject() {
        return new CustomObjectNameResolver(isValidTypePredicate, validStandardObjectPredicate);
    }
    public static CustomObjectNameResolver getCheckerForCustomObject(){
        return new CustomObjectNameResolver(isValidTypePredicate, validCustomObjectPredicate);
    }
    
    private static final Predicate<Pair<String>> validCustomObjectPredicate = new Predicate<Pair<String>>() {
        @Override
        public boolean apply(Pair<String> arg0) {
            return !doesNotEndWithValidCustomObjectSuffix.apply(arg0) || isWildCard.apply(arg0);
        }
    };
    private static final Predicate<Pair<String>> validStandardObjectPredicate = new Predicate<Pair<String>>() {
        @Override
        public boolean apply(Pair<String> arg0) {
            return doesNotEndWithValidCustomObjectSuffix.apply(arg0) && !isWildCard.apply(arg0);
        }
    };
    
    private static final Predicate<Pair<String>> doesNotEndWithValidCustomObjectSuffix = new Predicate<Pair<String>>() {
        @Override
        public boolean apply(Pair<String> arg0) {
            return !(arg0.getName().endsWith(Constants.CUSTOM_OBJECT_SUFFIX) || arg0.getName().endsWith(KNOWLEDGE_ARTICLE_SUFFIX));
        }
    };
    
    private static final Predicate<Pair<String>> isWildCard = new Predicate<Pair<String>>() {
        @Override
        public boolean apply(Pair<String> arg0) {
            return arg0.getName().equals(Constants.SUBSCRIBE_TO_ALL);
        }
    };

    private static final Predicate<Pair<String>> isValidTypePredicate = new Predicate<Pair<String>>() {
        @Override
        public boolean apply(Pair<String> arg0) {
            return Constants.CUSTOM_OBJECT.equals(arg0.getType())
                    || Constants.STANDARD_OBJECT.equals(arg0.getType());
        }
    };
    
    @SafeVarargs
    private CustomObjectNameResolver(Predicate<Pair<String>>... predicates) {
        this.predicatesToCheck = predicates;
    }
    
    public boolean check(String name, String type){
        Pair<String> nameType = new Pair<>(name, type);
        for(Predicate<Pair<String>> p:predicatesToCheck){
            if(!p.apply(nameType)){
                return false;
            }
        }
        return true;
    }

    class Pair<T> {
        private T name;
        private T type;

        public Pair(T name, T type) {
        this.name = name;
        this.type = type;
        }
        
       public T getName(){
           return name;
       }
       public T getType(){
           return type;
       }
    }


}

