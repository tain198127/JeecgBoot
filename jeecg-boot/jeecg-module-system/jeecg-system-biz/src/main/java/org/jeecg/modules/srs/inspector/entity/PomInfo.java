package org.jeecg.modules.srs.inspector.entity;

import lombok.Data;
import org.apache.maven.model.Model;
import org.jeecg.modules.srs.inspector.parser.CallChainAnalyzer;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * POM 信息封装类
 *
 * @author jeecg-boot
 * @version V1.0
 * @since 2025-01-25
 */
@Data
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

    private Set<PackageInfo> packageInfoSet = new HashSet<>();

    private Set<ClassInfo> classInfoSet = new HashSet<>();

    private Set<CallChainAnalyzer.ClzAndMethod> methodInfoSet = new HashSet<>();

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
