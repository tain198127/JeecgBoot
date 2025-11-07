package org.jeecg.modules.airag.docsearch.service;

import org.jeecg.modules.airag.docsearch.entity.GenerateRequest;
import org.jeecg.modules.airag.docsearch.entity.GenerateResult;

/**
 * @Description: 生成服务接口
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
public interface GenerateService {

    /**
     * 生成内容
     * @param generateRequest 生成请求
     * @return 生成结果
     */
    GenerateResult generate(GenerateRequest generateRequest);

    /**
     * 流式生成内容
     * @param generateRequest 生成请求
     * @return 生成结果流
     */
    void generateStream(GenerateRequest generateRequest, GenerateStreamHandler handler);

    /**
     * 事实性验证
     * @param content 生成内容
     * @param contextSlices 上下文切片
     * @return 事实性验证结果
     */
    GenerateResult.FactCheckResult factCheck(String content, java.util.List<org.jeecg.modules.airag.docsearch.entity.SearchResult.SearchHit> contextSlices);

    /**
     * 生成指令模板
     * @param promptOptimized 优化后的提示词
     * @param contextSlices 上下文切片
     * @return 指令模板
     */
    String generatePromptTemplate(String promptOptimized, java.util.List<org.jeecg.modules.airag.docsearch.entity.SearchResult.SearchHit> contextSlices);

    /**
     * 流式生成处理器接口
     */
    interface GenerateStreamHandler {
        /**
         * 处理生成的部分内容
         * @param partialResult 部分生成结果
         */
        void onPartialResult(GenerateResult partialResult);

        /**
         * 生成完成
         * @param finalResult 最终生成结果
         */
        void onComplete(GenerateResult finalResult);

        /**
         * 生成出错
         * @param error 错误信息
         */
        void onError(Exception error);
    }
}
