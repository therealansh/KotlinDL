/*
 * Copyright 2020 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package examples.transferlearning.modelzoo.vgg16

import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.Adam
import org.jetbrains.kotlinx.dl.api.inference.keras.loaders.InputType
import org.jetbrains.kotlinx.dl.api.inference.keras.loaders.predictTop5Labels
import org.jetbrains.kotlinx.dl.api.inference.keras.loaders.prepareHumanReadableClassLabels
import org.jetbrains.kotlinx.dl.api.inference.keras.loaders.preprocessInput
import org.jetbrains.kotlinx.dl.dataset.image.ColorOrder
import org.jetbrains.kotlinx.dl.dataset.preprocessor.ImageShape
import org.jetbrains.kotlinx.dl.dataset.preprocessor.Preprocessing
import org.jetbrains.kotlinx.dl.dataset.preprocessor.image.load
import org.jetbrains.kotlinx.dl.dataset.preprocessor.preprocess
import org.jetbrains.kotlinx.dl.dataset.preprocessor.transformImage
import java.io.File
import java.io.FileReader
import java.util.*

/**
 * This examples demonstrates the inference concept on VGG'16 model and weights loading from KotlinDL txt format:
 * - Weights are loaded from txt files, configuration is loaded from .json file.
 * - Model predicts on a few images located in resources.
 * - Special preprocessing (used in VGG'16 during training on ImageNet dataset) is applied to images before prediction.
 * - No additional training.
 * - No new layers are added.
 *
 * @see <a href="https://drive.google.com/drive/folders/1TkshZWtMKP-_oeJPvCl5dZKqxHzxwfRg">
 *     VGG'16 weights in txt format and model could be loaded here.</a>
 * @see <a href="https://arxiv.org/abs/1409.1556">
 *     Very Deep Convolutional Networks for Large-Scale Image Recognition (ICLR 2015).</a>
 * @see <a href="https://keras.io/api/applications/vgg/#vgg16-function">
 *    Detailed description of VGG'16 model and an approach to build it in Keras.</a>
 */
fun main() {
    val jsonConfigFile = getVGG16JSONConfigFile()
    val model = Sequential.loadModelConfiguration(jsonConfigFile)

    val imageNetClassLabels = prepareHumanReadableClassLabels()

    model.use {
        it.compile(
            optimizer = Adam(),
            loss = Losses.MAE,
            metric = Metrics.ACCURACY
        )
        println(it.kGraph)

        it.summary()

        it.loadWeights(getVGG16WeightsFile())

        for (i in 1..8) {
            val preprocessing: Preprocessing = preprocess {
                transformImage {
                    load {
                        pathToData = getFileFromResource("datasets/vgg/image$i.jpg")
                        imageShape = ImageShape(224, 224, 3)
                        colorMode = ColorOrder.BGR
                    }
                }
            }

            val inputData = preprocessInput(preprocessing().first, model.inputDimensions, inputType = InputType.CAFFE)
            val res = it.predict(inputData, "Activation_predictions")
            println("Predicted object for image$i.jpg is ${imageNetClassLabels[res]}")

            val top5 = predictTop5Labels(it, inputData, imageNetClassLabels)

            println(top5.toString())
        }
    }
}


/** Returns JSON file with model configuration, saved from Keras 2.x. */
private fun getVGG16JSONConfigFile(): File {
    val properties = Properties()
    val reader = FileReader("data.properties")
    properties.load(reader)

    val vgg16JSONModelPath = properties["vgg16JSONModelPath"] as String

    return File(vgg16JSONModelPath)
}

/** Returns .h5 file with model weights, saved from Keras 2.x. */
private fun getVGG16WeightsFile(): File {
    val properties = Properties()
    val reader = FileReader("data.properties")
    properties.load(reader)

    val vgg16h5TxtWeightsPath = properties["vgg16h5TxtWeightsPath"] as String

    return File(vgg16h5TxtWeightsPath)
}




