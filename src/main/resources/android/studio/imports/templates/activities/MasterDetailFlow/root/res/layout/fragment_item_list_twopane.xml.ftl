<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_marginLeft="16dp"
    android:layout_marginRight="16dp"
    android:baselineAligned="false"
    android:divider="?android:attr/dividerHorizontal"
    android:orientation="horizontal"
    android:showDividers="middle"
    tools:context="${relativePackage}.${CollectionName}Activity">

    <!--
    This layout is a two-pane layout for the ${objectKindPlural}
    master/detail flow.
    <#if minApiLevel lt 13>See res/values-large/refs.xml and
    res/values-w900dp/refs.xml for an example of layout aliases
    that replace the single-pane version of the layout with
    this two-pane version.

    For more on layout aliases, see:
    http://developer.android.com/training/multiscreen/screensizes.html#TaskUseAliasFilters</#if>
    -->

    <android.support.v7.widget.RecyclerView xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        android:id="@+id/${collection_name}"
        android:name="${packageName}.${CollectionName}Fragment"
        android:layout_width="@dimen/item_width"
        android:layout_height="match_parent"
        android:layout_marginLeft="16dp"
        android:layout_marginRight="16dp"
        app:layoutManager="LinearLayoutManager"
        tools:context="${relativePackage}.${CollectionName}Activity"
        tools:listitem="@layout/${item_list_content_layout}" />

    <FrameLayout
        android:id="@+id/${detail_name}_container"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="3" />

</LinearLayout>
