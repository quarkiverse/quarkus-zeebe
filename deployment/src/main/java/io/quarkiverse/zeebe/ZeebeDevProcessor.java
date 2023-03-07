package io.quarkiverse.zeebe;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;

import org.jboss.logging.Logger;

import io.quarkiverse.zeebe.runtime.ZeebeBuildTimeConfig;
import io.quarkiverse.zeebe.runtime.devmode.JobWorkerReplacementInterceptor;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.HotDeploymentWatchedFileBuildItem;

public class ZeebeDevProcessor {

    private static final Logger log = Logger.getLogger(ZeebeDevProcessor.class);

    @BuildStep(onlyIf = IsDevelopment.class)
    void hotReload(ZeebeDevServiceBuildTimeConfig buildTimeConfig, BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        if (buildTimeConfig.devMode.watchJobWorker) {
            additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(JobWorkerReplacementInterceptor.class));
        }
    }

    @BuildStep(onlyIfNot = IsNormal.class)
    void watchChanges(ZeebeBuildTimeConfig config, ZeebeResourcesBuildItem resources,
            ZeebeDevServiceBuildTimeConfig buildTimeConfig,
            BuildProducer<HotDeploymentWatchedFileBuildItem> watchedPaths) {

        if (!config.resources.enabled) {
            return;
        }

        // add all bpmn resources
        if (buildTimeConfig.devMode.watchBpmnFiles) {
            Collection<String> items = resources.getResources();
            if (items != null && !items.isEmpty()) {
                items.forEach(x -> watchedPaths.produce(new HotDeploymentWatchedFileBuildItem(x)));
            }
        }

        // watch directories for new files
        // add root directory and all subdirectories
        if (buildTimeConfig.devMode.watchBpmnDir) {
            watchedPaths.produce(new HotDeploymentWatchedFileBuildItem(config.resources.location));

            try {
                Enumeration<URL> location = Thread.currentThread().getContextClassLoader()
                        .getResources(config.resources.location);
                Files.walk(Path.of(location.nextElement().toURI()))
                        .filter(Files::isDirectory)
                        .map(Path::toString)
                        .map(dir -> dir.replace('\\', '/'))
                        .peek(dir -> log.infof("Watched bpmn sub-directory %s", dir))
                        .forEach(dir -> watchedPaths.produce(new HotDeploymentWatchedFileBuildItem(dir)));
            } catch (Exception ex) {
                throw new RuntimeException("Error find all sub-directories of " + config.resources.location, ex);
            }
        }
    }

}
