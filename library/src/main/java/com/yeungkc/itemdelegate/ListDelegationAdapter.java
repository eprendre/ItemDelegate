/*
 * Copyright (c) 2015 Hannes Dorfmann.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.yeungkc.itemdelegate;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * An adapter implementation designed for dataSets organized in a {@link List}. This adapter
 * implementation is ready to go. All you have to do is to add {@link ItemDelegate}s to the
 * internal {@link ItemDelegatesManager} i.e.
 *
 * @param <T> The type of the dataSets. Must be something that extends from List like List<Foo>
 * @author Hannes Dorfmann
 */
public class ListDelegationAdapter<T> extends AbsDelegationAdapter<List<T>> {

    public ListDelegationAdapter() {
    }

    public ListDelegationAdapter(@NonNull ItemDelegatesManager<List<T>> delegatesManager) {
        super(delegatesManager);
    }

    @Override
    public int getItemCount() {
        return dataSets == null ? 0 : dataSets.size();
    }
}
