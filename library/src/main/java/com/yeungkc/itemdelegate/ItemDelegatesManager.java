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
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

/**
 * This class is the element that ties {@link RecyclerView.Adapter} together with {@link
 * ItemDelegate}.
 * <p>
 * So you have to add / register your {@link ItemDelegate}s to this manager by calling {@link
 * #addDelegate(ItemDelegate)}
 * </p>
 * <p>
 * <p>
 * Next you have to add this ItemDelegatesManager to the {@link RecyclerView.Adapter} by calling
 * corresponding methods:
 * <ul>
 * <li> {@link #getItemViewType(Object, int)}: Must be called from {@link
 * RecyclerView.Adapter#getItemViewType(int)}</li>
 * <li> {@link #onCreateViewHolder(ViewGroup, int)}: Must be called from {@link
 * RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)}</li>
 * <li> {@link #onBindViewHolder(Object, int, RecyclerView.ViewHolder)}: Must be called from {@link
 * RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)}</li>
 * </ul>
 * <p>
 * You can also set a fallback {@link ItemDelegate} by using {@link
 * #setFallbackDelegate(ItemDelegate)} that will be used if no {@link ItemDelegate} is
 * responsible to handle a certain view type. If no fallback is specified, an Exception will be
 * thrown if no {@link ItemDelegate} is responsible to handle a certain view type
 * </p>
 *
 * @param <T> The type of the datasource of the adapter
 * @author Hannes Dorfmann
 */
public class ItemDelegatesManager<T> {

    /**
     * This id is used internally to claim that the {@link}
     */
    static final int FALLBACK_DELEGATE_VIEW_TYPE = Integer.MAX_VALUE - 1;

    /**
     * Used internally for {@link #onBindViewHolder(Object, int, RecyclerView.ViewHolder)} as empty
     * payload parameter
     */
    private static final List PAYLOADS_EMPTY_LIST = Collections.emptyList();

    /**
     * Map for ViewType to ItemDelegate
     */
    protected SparseArrayCompat<ItemDelegate<T>> delegates = new SparseArrayCompat<>();
    protected ItemDelegate<T> fallbackDelegate;

    /**
     * Adds an {@link ItemDelegate}.
     * <b>This method automatically assign internally the view type integer by using the next
     * unused</b>
     * <p>
     * Internally calls {@link #addDelegate(int, boolean, ItemDelegate)} with
     * allowReplacingDelegate = false as parameter.
     *
     * @param delegate the delegate to add
     * @return self
     * @throws NullPointerException if passed delegate is null
     * @see #addDelegate(int, ItemDelegate)
     * @see #addDelegate(int, boolean, ItemDelegate)
     */
    public ItemDelegatesManager<T> addDelegate(@NonNull ItemDelegate<T> delegate) {
        // algorithm could be improved since there could be holes,
        // but it's very unlikely that we reach Integer.MAX_VALUE and run out of unused indexes
        int viewType = delegates.size();
        while (delegates.get(viewType) != null) {
            viewType++;
            if (viewType == FALLBACK_DELEGATE_VIEW_TYPE) {
                throw new IllegalArgumentException(
                        "Oops, we are very close to Integer.MAX_VALUE. It seems that there are no more free and unused view type integers left to add another ItemDelegate.");
            }
        }
        return addDelegate(viewType, false, delegate);
    }

    /**
     * Adds an {@link ItemDelegate}.
     *
     * @param viewType               The viewType id
     * @param allowReplacingDelegate if true, you allow to replacing the given delegate any previous
     *                               delegate for the same view type. if false, you disallow and a {@link IllegalArgumentException}
     *                               will be thrown if you try to replace an already registered {@link ItemDelegate} for the
     *                               same view type.
     * @param delegate               The delegate to add
     * @throws IllegalArgumentException if <b>allowReplacingDelegate</b>  is false and an {@link
     *                                  ItemDelegate} is already added (registered)
     *                                  with the same ViewType.
     * @throws IllegalArgumentException if viewType is {@link #FALLBACK_DELEGATE_VIEW_TYPE} which is
     *                                  reserved
     * @see #addDelegate(ItemDelegate)
     * @see #addDelegate(int, ItemDelegate)
     * @see #setFallbackDelegate(ItemDelegate)
     */
    public ItemDelegatesManager<T> addDelegate(int viewType, boolean allowReplacingDelegate,
                                               @NonNull ItemDelegate<T> delegate) {

        if (delegate == null) {
            throw new NullPointerException("ItemDelegate is null!");
        }

        if (viewType == FALLBACK_DELEGATE_VIEW_TYPE) {
            throw new IllegalArgumentException("The view type = "
                    + FALLBACK_DELEGATE_VIEW_TYPE
                    + " is reserved for fallback adapter delegate (see setFallbackDelegate() ). Please use another view type.");
        }

        if (!allowReplacingDelegate && delegates.get(viewType) != null) {
            throw new IllegalArgumentException(
                    "An ItemDelegate is already registered for the viewType = "
                            + viewType
                            + ". Already registered ItemDelegate is "
                            + delegates.get(viewType));
        }

        delegates.put(viewType, delegate);

        return this;
    }

    /**
     * Adds an {@link ItemDelegate} with the specified view type.
     * <p>
     * Internally calls {@link #addDelegate(int, boolean, ItemDelegate)} with
     * allowReplacingDelegate = false as parameter.
     *
     * @param viewType the view type integer if you want to assign manually the view type. Otherwise
     *                 use {@link #addDelegate(ItemDelegate)} where a viewtype will be assigned manually.
     * @param delegate the delegate to add
     * @return self
     * @throws NullPointerException if passed delegate is null
     * @see #addDelegate(ItemDelegate)
     * @see #addDelegate(int, boolean, ItemDelegate)
     */
    public ItemDelegatesManager<T> addDelegate(int viewType,
                                               @NonNull ItemDelegate<T> delegate) {
        return addDelegate(viewType, false, delegate);
    }

    /**
     * Removes a previously registered delegate if and only if the passed delegate is registered
     * (checks the reference of the object). This will not remove any other delegate for the same
     * viewType (if there is any).
     *
     * @param delegate The delegate to remove
     * @return self
     */
    public ItemDelegatesManager<T> removeDelegate(@NonNull ItemDelegate<T> delegate) {

        if (delegate == null) {
            throw new NullPointerException("ItemDelegate is null");
        }

        int indexToRemove = delegates.indexOfValue(delegate);

        if (indexToRemove >= 0) {
            delegates.removeAt(indexToRemove);
        }
        return this;
    }

    /**
     * Removes the ItemDelegate for the given view types.
     *
     * @param viewType The Viewtype
     * @return self
     */
    public ItemDelegatesManager<T> removeDelegate(int viewType) {
        delegates.remove(viewType);
        return this;
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#getItemViewType(int)}. Internally it scans all
     * the registered {@link ItemDelegate} and picks the right one to return the ViewType integer.
     *
     * @param items    Adapter's data source
     * @param position the position in adapters data source
     * @return the ViewType (integer). Returns {@link #FALLBACK_DELEGATE_VIEW_TYPE} in case that the
     * fallback adapter delegate should be used
     * @throws IllegalArgumentException if no {@link ItemDelegate} has been found that is
     *                                  responsible for the given data element in data set (No {@link ItemDelegate} for the given
     *                                  ViewType)
     * @throws NullPointerException     if items is null
     */
    public int getItemViewType(@NonNull T items, int position) {
        ItemDelegate<T> delegate = getDelegate(items, position);

        if (delegate == fallbackDelegate) {
            return FALLBACK_DELEGATE_VIEW_TYPE;
        }
        return delegates.indexOfValue(delegate);
    }

    public ItemDelegate<T> getDelegate(@NonNull T items, int position) {

        if (items == null) {
            throw new NullPointerException("Items datasource is null!");
        }

        int delegatesCount = delegates.size();
        for (int i = 0; i < delegatesCount; i++) {
            ItemDelegate<T> delegate = delegates.valueAt(i);
            if (delegate.isForViewType(items, position)) {
                return delegate;
            }
        }

        if (fallbackDelegate != null) {
            return fallbackDelegate;
        }

        throw new NullPointerException(
                "No ItemDelegate added that matches position=" + position + " in data source");
    }

    public long getItemId(@NonNull T items, int position) {
        ItemDelegate<T> delegate = getDelegate(items, position);
        return delegate.getItemId(items, position);
    }

    /**
     * This method must be called in {@link RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)}
     *
     * @param parent   the parent
     * @param viewType the view type
     * @return The new created ViewHolder
     * @throws NullPointerException if no ItemDelegate has been registered for ViewHolders
     *                              viewType
     */
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemDelegate<T> delegate = getDelegateForViewType(viewType);
        if (delegate == null) {
            throw new NullPointerException("No ItemDelegate added for ViewType " + viewType);
        }

        RecyclerView.ViewHolder vh = delegate.onCreateViewHolder(parent);
        if (vh == null) {
            throw new NullPointerException("ViewHolder returned from ItemDelegate "
                    + delegate
                    + " for ViewType ="
                    + viewType
                    + " is null!");
        }
        return vh;
    }

    /**
     * Get the {@link ItemDelegate} associated with the given view type integer
     *
     * @param viewType The view type integer we want to retrieve the associated
     *                 delegate for.
     * @return The {@link ItemDelegate} associated with the view type param if it exists,
     * the fallback delegate otherwise if it is set or returns <code>null</code> if no delegate is
     * associated to this viewType (and no fallback has been set).
     */
    @Nullable
    public ItemDelegate<T> getDelegateForViewType(int viewType) {
        ItemDelegate<T> delegate = delegates.get(viewType);
        if (delegate == null) {
            if (fallbackDelegate == null) {
                return null;
            } else {
                return fallbackDelegate;
            }
        }

        return delegate;
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int,
     * List)}
     *
     * @param items      Adapter's data source
     * @param position   the position in data source
     * @param viewHolder the ViewHolder to bind
     * @throws NullPointerException if no ItemDelegate has been registered for ViewHolders
     *                              viewType
     */
    public void onBindViewHolder(@NonNull T items, int position,
                                 @NonNull RecyclerView.ViewHolder viewHolder) {
        onBindViewHolder(items, position, viewHolder, PAYLOADS_EMPTY_LIST);
    }

    /**
     * Must be called from{@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int,
     * List)}
     *
     * @param items      Adapter's data source
     * @param position   the position in data source
     * @param viewHolder the ViewHolder to bind
     * @param payloads   A non-null list of merged payloads. Can be empty list if requires full update.
     * @throws NullPointerException if no ItemDelegate has been registered for ViewHolders
     *                              viewType
     */
    public void onBindViewHolder(@NonNull T items, int position,
                                 @NonNull RecyclerView.ViewHolder viewHolder, @NonNull List payloads) {

        ItemDelegate<T> delegate = getDelegateForViewType(viewHolder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for item at position = "
                    + position
                    + " for viewType = "
                    + viewHolder.getItemViewType());
        }
        delegate.onBindViewHolder(items, position, viewHolder, payloads);
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#onViewRecycled(RecyclerView.ViewHolder)}
     *
     * @param viewHolder The ViewHolder for the view being recycled
     */
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        ItemDelegate<T> delegate = getDelegateForViewType(viewHolder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for "
                    + viewHolder
                    + " for item at position = "
                    + viewHolder.getAdapterPosition()
                    + " for viewType = "
                    + viewHolder.getItemViewType());
        }
        delegate.onViewRecycled(viewHolder);
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#onFailedToRecycleView(RecyclerView.ViewHolder)}
     *
     * @param viewHolder The ViewHolder containing the View that could not be recycled due to its
     *                   transient state.
     * @return True if the View should be recycled, false otherwise. Note that if this method
     * returns <code>true</code>, RecyclerView <em>will ignore</em> the transient state of
     * the View and recycle it regardless. If this method returns <code>false</code>,
     * RecyclerView will check the View's transient state again before giving a final decision.
     * Default implementation returns false.
     */
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder viewHolder) {
        ItemDelegate<T> delegate = getDelegateForViewType(viewHolder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for "
                    + viewHolder
                    + " for item at position = "
                    + viewHolder.getAdapterPosition()
                    + " for viewType = "
                    + viewHolder.getItemViewType());
        }
        return delegate.onFailedToRecycleView(viewHolder);
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#onViewAttachedToWindow(RecyclerView.ViewHolder)}
     *
     * @param viewHolder Holder of the view being attached
     */
    public void onViewAttachedToWindow(RecyclerView.ViewHolder viewHolder) {
        ItemDelegate<T> delegate = getDelegateForViewType(viewHolder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for "
                    + viewHolder
                    + " for item at position = "
                    + viewHolder.getAdapterPosition()
                    + " for viewType = "
                    + viewHolder.getItemViewType());
        }
        delegate.onViewAttachedToWindow(viewHolder);
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#onViewDetachedFromWindow(RecyclerView.ViewHolder)}
     *
     * @param viewHolder Holder of the view being attached
     */
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder viewHolder) {
        ItemDelegate<T> delegate = getDelegateForViewType(viewHolder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for "
                    + viewHolder
                    + " for item at position = "
                    + viewHolder.getAdapterPosition()
                    + " for viewType = "
                    + viewHolder.getItemViewType());
        }
        delegate.onViewDetachedFromWindow(viewHolder);
    }

    /**
     * Get the view type integer for the given {@link ItemDelegate}
     *
     * @param delegate The delegate we want to know the view type for
     * @return -1 if passed delegate is unknown, otherwise the view type integer
     */
    public int getViewType(@NonNull ItemDelegate<T> delegate) {
        if (delegate == null) {
            throw new NullPointerException("Delegate is null");
        }

        int index = delegates.indexOfValue(delegate);
        if (index == -1) {
            return -1;
        }
        return delegates.keyAt(index);
    }

    /**
     * Get the fallback delegate
     *
     * @return The fallback delegate or <code>null</code> if no fallback delegate has been set
     * @see #setFallbackDelegate(ItemDelegate)
     */
    @Nullable
    public ItemDelegate<T> getFallbackDelegate() {
        return fallbackDelegate;
    }

    /**
     * Set a fallback delegate that should be used if no {@link ItemDelegate} has been found that
     * can handle a certain view type.
     *
     * @param fallbackDelegate The {@link ItemDelegate} that should be used as fallback if no
     *                         other ItemDelegate has handled a certain view type. <code>null</code> you can set this to
     *                         null if
     *                         you want to remove a previously set fallback ItemDelegate
     */
    public ItemDelegatesManager<T> setFallbackDelegate(
            @Nullable ItemDelegate<T> fallbackDelegate) {
        this.fallbackDelegate = fallbackDelegate;
        return this;
    }

    public int getSpanSize(@NonNull T items, int position, int spanCount) {
        return getDelegate(items, position).getSpanCount(items, position, spanCount);
    }
}