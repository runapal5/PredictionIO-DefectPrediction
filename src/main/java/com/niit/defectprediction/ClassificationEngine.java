package com.niit.defectprediction;

import org.apache.predictionio.controller.EmptyParams;
import org.apache.predictionio.controller.Engine;
import org.apache.predictionio.controller.EngineFactory;
import org.apache.predictionio.core.BaseAlgorithm;
import org.apache.predictionio.core.BaseEngine;

import java.util.Collections;
import java.util.Set;

public class ClassificationEngine extends EngineFactory {

    @Override
    public BaseEngine<EmptyParams, Query, PredictedResult, Set<String>> apply() {
        return new Engine<>(
                DataSource.class,
                Preparator.class,
                Collections.<String, Class<? extends BaseAlgorithm<PreparedData, ?, Query, PredictedResult>>>singletonMap("randomforest", RandomForestAlgorithm.class),
                Serving.class
        );
    }
}
