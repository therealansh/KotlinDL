/*
 * Copyright 2020 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package org.jetbrains.kotlinx.dl.dataset.preprocessor

/**
 * The whole tensor preprocessing pipeline DSL.
 *
 * It supports operations that implement [Preprocessor], for example:
 * - [rescaling] See [Rescaling] preprocessor.
 * - [customPreprocessor] See [CustomPreprocessor] preprocessor.
 *
 * It's a part of the [org.jetbrains.kotlinx.dl.dataset.preprocessor.Preprocessing] pipeline DSL.
 */
public class TensorPreprocessing {
    internal val operations = mutableListOf<Preprocessor>()

    public fun addOperation(operation: Preprocessor) {
        operations.add(operation)
    }
}

/** */
public fun TensorPreprocessing.rescale(block: Rescaling.() -> Unit) {
    addOperation(Rescaling().apply(block))
}






