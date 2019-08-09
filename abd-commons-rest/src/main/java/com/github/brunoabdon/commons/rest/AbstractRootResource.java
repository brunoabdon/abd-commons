package com.github.brunoabdon.commons.rest;

import com.github.brunoabdon.commons.util.modelo.Identifiable;

public abstract class AbstractRootResource <E extends Identifiable<Key>,Key>
        extends AbstractRestCrud<E, Key, Key>{

    @Override
    protected Key getFullId(final Key pathId) {
        return pathId;
    }
}
