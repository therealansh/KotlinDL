/*
 * Copyright 2020 JetBrains s.r.o. and Kotlin Deep Learning project contributors. All Rights Reserved.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package examples.inference.production

import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.Adam
import org.jetbrains.kotlinx.dl.datasets.Dataset
import org.jetbrains.kotlinx.dl.datasets.handlers.*
import javax.swing.JFrame

private const val EPOCHS = 1
private const val TRAINING_BATCH_SIZE = 500
private const val TEST_BATCH_SIZE = 1000

fun main() {
    val (train, test) = Dataset.createTrainAndTestDatasets(
        TRAIN_IMAGES_ARCHIVE,
        TRAIN_LABELS_ARCHIVE,
        TEST_IMAGES_ARCHIVE,
        TEST_LABELS_ARCHIVE,
        NUMBER_OF_CLASSES,
        ::extractImages,
        ::extractLabels
    )

    val imageId = 1

    lenet5.use {
        it.compile(
            optimizer = Adam(),
            loss = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metric = Metrics.ACCURACY
        )

        it.summary()

        it.fit(
            dataset = train,
            validationRate = 0.1,
            epochs = EPOCHS,
            trainBatchSize = TRAINING_BATCH_SIZE,
            validationBatchSize = TEST_BATCH_SIZE,
            verbose = true
        )

        val weights = it.layers[0].getWeights() // first conv2d layer

        drawFilters(weights[0], colorCoefficient = 10.0)

        val weights2 = it.layers[2].getWeights() // first conv2d layer

        drawFilters(weights2[0], colorCoefficient = 10.0)

        val accuracy = it.evaluate(dataset = test, batchSize = TEST_BATCH_SIZE).metrics[Metrics.ACCURACY]

        println("Accuracy $accuracy")

        val (prediction, activations) = it.predictAndGetActivations(train.getX(imageId))

        println("Prediction: $prediction")

        drawActivations(activations)

        val trainImageLabel = train.getY(imageId)

        val maxIdx = trainImageLabel.indexOfFirst { it == trainImageLabel.maxOrNull()!! }

        println("Ground Truth: $maxIdx")
    }
}

fun drawActivations(activations: List<*>) {
    val frame = JFrame("Visualise the matrix weights on Relu")
    frame.contentPane.add(ReluGraphics(activations[0] as Array<Array<Array<FloatArray>>>))
    frame.setSize(1500, 1500)
    frame.isVisible = true
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isResizable = false

    val frame2 = JFrame("Visualise the matrix weights on Relu_1")
    frame2.contentPane.add(ReluGraphics2(activations[1] as Array<Array<Array<FloatArray>>>))
    frame2.setSize(1500, 1500)
    frame2.isVisible = true
    frame2.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame2.isResizable = false
}

fun drawFilters(filters: Array<*>, colorCoefficient: Double = 2.0) {
    val frame = JFrame("Filters")
    frame.contentPane.add(Conv2dJPanel(filters as Array<Array<Array<FloatArray>>>, colorCoefficient))
    frame.setSize(1000, 1000)
    frame.isVisible = true
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isResizable = false
}