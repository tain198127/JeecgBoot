package org.jeecg.modules.srs.inspector.entity;

import lombok.Data;
import org.jeecg.modules.srs.inspector.parser.CallChainAnalyzer;

import java.util.HashSet;
import java.util.Set;

@Data
public class ClassInfo {
    private String name;
    private Set<CallChainAnalyzer.ClzAndMethod> methodInfoSet = new HashSet<>();
}
