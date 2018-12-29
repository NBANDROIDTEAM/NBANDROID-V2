package org.nbandroid.netbeans.gradle.config;

import com.android.builder.model.ProductFlavorContainer;
import com.android.builder.model.SourceProviderContainer;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import javax.annotation.Nullable;

/**
 *
 * @author radim
 */
public class ProductFlavors {

    @Nullable
    public static ProductFlavorContainer findFlavorByName(Iterable<ProductFlavorContainer> flavors, final String name) {
        return flavors == null || name == null
                ? null
                : Iterables.find(
                        flavors,
                        new Predicate<ProductFlavorContainer>() {

                    @Override
                    public boolean apply(ProductFlavorContainer t) {
                        return name.equals(t.getProductFlavor().getName());
                    }
                },
                        null);
    }

    @Nullable
    public static SourceProviderContainer getSourceProviderContainer(ProductFlavorContainer pfc, final String name) {
        return pfc == null || name == null
                ? null
                : Iterables.find(
                        pfc.getExtraSourceProviders(),
                        new Predicate<SourceProviderContainer>() {
                    @Override
                    public boolean apply(SourceProviderContainer t) {
                        return name.equals(t.getArtifactName());
                    }
                },
                        null);
    }
}
