package org.jeecg.modules.srs.inspector.parser;

import org.apache.maven.model.Model;

import java.nio.file.Path;

/**
 * POM 信息封装类
 *
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-25
 */
public class PomInfo {

    /**
     * Maven Model 对象
     */
    private Model model;

    /**
     * POM 文件所在目录
     */
    private Path directory;

    /**
     * POM 文件路径
     */
    private Path pomPath;

    public PomInfo(Model model, Path directory, Path pomPath) {
        this.model = model;
        this.directory = directory;
        this.pomPath = pomPath;
    }

    /**
     * 获取 GroupId（考虑继承）
     */
    public String getGroupId() {
        if (model.getGroupId() != null) {
            return model.getGroupId();
        }
        // 如果当前 pom 没有 groupId，尝试从 parent 获取
        if (model.getParent() != null) {
            return model.getParent().getGroupId();
        }
        return null;
    }

    /**
     * 获取 ArtifactId
     */
    public String getArtifactId() {
        return model.getArtifactId();
    }

    /**
     * 获取 Version（考虑继承）
     */
    public String getVersion() {
        if (model.getVersion() != null) {
            return model.getVersion();
        }
        // 如果当前 pom 没有 version，尝试从 parent 获取
        if (model.getParent() != null) {
            return model.getParent().getVersion();
        }
        return null;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Path getDirectory() {
        return directory;
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    public Path getPomPath() {
        return pomPath;
    }

    public void setPomPath(Path pomPath) {
        this.pomPath = pomPath;
    }

    @Override
    public String toString() {
        return "PomInfo{" +
                "groupId='" + getGroupId() + '\'' +
                ", artifactId='" + getArtifactId() + '\'' +
                ", version='" + getVersion() + '\'' +
                ", directory=" + directory +
                ", pomPath=" + pomPath +
                '}';
    }
}
