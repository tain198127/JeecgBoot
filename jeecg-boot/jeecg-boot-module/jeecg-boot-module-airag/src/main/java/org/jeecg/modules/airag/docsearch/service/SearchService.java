package org.jeecg.modules.airag.docsearch.service;

import org.jeecg.modules.airag.docsearch.entity.SearchRequest;
import org.jeecg.modules.airag.docsearch.entity.SearchResult;

/**
 * @Description: 搜索服务接口
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
public interface SearchService {

    /**
     * 执行搜索
     * @param searchRequest 搜索请求
     * @return 搜索结果
     */
    SearchResult search(SearchRequest searchRequest);

    /**
     * 执行ES搜索
     * @param searchRequest 搜索请求
     * @return 搜索结果
     */
    SearchResult esSearch(SearchRequest searchRequest);

    /**
     * 执行Milvus搜索
     * @param searchRequest 搜索请求
     * @return 搜索结果
     */
    SearchResult milvusSearch(SearchRequest searchRequest);

    /**
     * 融合搜索结果
     * @param esResult ES搜索结果
     * @param milvusResult Milvus搜索结果
     * @param fuseMethod 融合方法
     * @return 融合后的搜索结果
     */
    SearchResult fuseSearchResults(SearchResult esResult, SearchResult milvusResult, String fuseMethod);

    /**
     * 优化查询词
     * @param query 原始查询词
     * @return 优化后的查询词
     */
    String optimizeQuery(String query);

    /**
     * 优化查询词并返回优化理由
     * @param query 原始查询词
     * @return 优化后的查询词和理由
     */
    SearchResult.QueryRewriteResult optimizeQueryWithReason(String query);
}
