package edu.wpi.first.gradlerio.frc

import groovy.transform.CompileStatic
import jaci.gradle.deploy.artifact.FileCollectionArtifact
import jaci.gradle.deploy.artifact.TaskHungryArtifact
import jaci.gradle.deploy.context.DeployContext
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.util.PatternFilterable

import java.util.concurrent.Callable

@CompileStatic
class ConfigurationArtifact extends FileCollectionArtifact implements Callable<FileCollection> {

    Configuration configuration
    boolean zipped
    Action<PatternFilterable> filter

    ConfigurationArtifact(String name, Project project) {
        super(name, project)
        directory = '/usr/local/frc/lib'
        onlyIf = { DeployContext ctx ->
            files.isPresent() && !files.get().empty && !files.get().files.empty
        }

        postdeploy << { DeployContext ctx ->
            ctx.execute("ldconfig")
        }

        files.set(project.files(this as Callable<FileCollection>))
    }

    @Override
    FileCollection call() {
        def conf = configuration.resolvedConfiguration
        if (zipped) {
            conf.files.collect { File file ->
                project.zipTree(file).matching(filter)
            }.findAll { it != null }.inject { a, b -> a + b}
        } else {
            project.files(conf.files)
        }
    }
}
