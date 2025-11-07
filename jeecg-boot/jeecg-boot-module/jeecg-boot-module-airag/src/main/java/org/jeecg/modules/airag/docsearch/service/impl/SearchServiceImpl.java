package org.jeecg.modules.airag.docsearch.service.impl;

import com.alibaba.fastjson.JSON;
import dev.langchain4j.model.chat.ChatMessage;
import dev.langchain4j.model.chat.HumanMessage;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.SearchParam;
import org.jeecg.modules.airag.llm.handler.EmbeddingHandler;","},{
import org.jeecg.modules.airag.docsearch.entity.SearchRequest;
import org.jeecg.modules.airag.docsearch.entity.SearchResult;
import org.jeecg.modules.airag.docsearch.service.SearchService;
import org.jeecg.modules.airag.llm.handler.AiChatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 搜索服务实现类
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Autowired
    private AiChatHandler aiChatHandler;

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    @Value("${elasticsearch.index.name:airag_doc_search}")
    private String esIndexName;
    
    @Autowired
    private MilvusServiceClient milvusClient;
    
    @Autowired
    private EmbeddingHandler embeddingHandler;
    
    @Value("${milvus.collection.name:airag_doc_search}")
    private String milvusCollectionName;
    
    @Value("${milvus.embedding.dimension:768}")
    private int embeddingDimension;

    /**
     * 执行搜索
     * @param searchRequest 搜索请求
     * @return 搜索结果
     */
    @Override
    public SearchResult search(SearchRequest searchRequest) {
        long startTime = System.currentTimeMillis();

        // 1. 优化查询词
        String optimizedQuery = searchRequest.getEnableQueryRewrite() ? optimizeQuery(searchRequest.getQuery()) : searchRequest.getQuery();

        // 2. 执行双路检索
        SearchResult esResult = esSearch(searchRequest);
        SearchResult milvusResult = milvusSearch(searchRequest);

        // 3. 融合搜索结果
        SearchResult fusedResult = fuseSearchResults(esResult, milvusResult, searchRequest.getFuseMethod());

        // 4. 设置优化后的查询词
        fusedResult.setOptimizedQuery(optimizedQuery);

        long totalTime = System.currentTimeMillis() - startTime;
        fusedResult.setSearchTime(totalTime);

        return fusedResult;
    }

    /**
     * 执行ES搜索
     * @param searchRequest 搜索请求
     * @return 搜索结果
     */
    @Override
    public SearchResult esSearch(SearchRequest searchRequest) {
        long startTime = System.currentTimeMillis();
        SearchResult result = new SearchResult();
        List<SearchResult.SearchHit> hits = new ArrayList<>();

        try {
            // 构建ES搜索请求
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            
            // 租户隔离
            boolQuery.filter(QueryBuilders.termQuery("tenantId", searchRequest.getTenantId()));
            
            // 关键字查询
            String query = searchRequest.getQuery();
            boolQuery.must(QueryBuilders.multiMatchQuery(query, "cleanedContent", "originalContent")
                    .fuzziness("AUTO")
                    .prefixLength(2)
                    .maxExpansions(10));
            
            // 过滤条件
            if (searchRequest.getFilters() != null && !searchRequest.getFilters().isEmpty()) {
                for (String filter : searchRequest.getFilters()) {
                    // 简单处理过滤条件，实际需要根据业务需求实现
                    boolQuery.filter(QueryBuilders.termQuery("metadata", filter));
                }
            }
            
            sourceBuilder.query(boolQuery);
            sourceBuilder.sort(SortBuilders.scoreSort().order(SortOrder.DESC));
            sourceBuilder.size(searchRequest.getTopN());
            
            org.elasticsearch.action.search.SearchRequest esSearchRequest = new org.elasticsearch.action.search.SearchRequest(esIndexName);
            esSearchRequest.source(sourceBuilder);
            
            // 执行搜索
            SearchResponse esResponse = elasticsearchClient.search(esSearchRequest, RequestOptions.DEFAULT);
            
            // 处理搜索结果
            for (SearchHit hit : esResponse.getHits().getHits()) {
                SearchResult.SearchHit searchHit = new SearchResult.SearchHit();
                searchHit.setDocId(hit.getSourceAsMap().get("docId").toString());
                searchHit.setSliceId(hit.getSourceAsMap().get("sliceId").toString());
                searchHit.setContent(hit.getSourceAsMap().get("cleanedContent").toString());
                searchHit.setScore(hit.getScore());
                searchHit.setSource("ES");
                searchHit.setChapter(hit.getSourceAsMap().get("chapterId") != null ? hit.getSourceAsMap().get("chapterId").toString() : "");
                searchHit.setParagraphIndex(hit.getSourceAsMap().get("paragraphId") != null ? Integer.parseInt(hit.getSourceAsMap().get("paragraphId").toString()) : 0);
                searchHit.setSimilarity(hit.getScore());
                hits.add(searchHit);
            }
            
            result.setHits(hits);
            result.setTotalHits(esResponse.getHits().getTotalHits().value);
            
        } catch (IOException e) {
            logger.error("ES search failed: {}", e.getMessage(), e);
            // 如果ES不可用，返回空结果
        }
        
        result.setSearchTime(System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 执行Milvus搜索
     * @param searchRequest 搜索请求
     * @return 搜索结果
     */
    @Override
    public SearchResult milvusSearch(SearchRequest searchRequest) {
        long startTime = System.currentTimeMillis();
        SearchResult result = new SearchResult();
        List<SearchResult.SearchHit> hits = new ArrayList<>();

        try {
            // 1. 生成查询向量
            String query = searchRequest.getQuery();
            
            // TODO: 需要实现一个方法来获取查询向量
            // 目前EmbeddingHandler没有直接返回float[]的方法，需要先获取Embedding对象再转换
            float[] queryEmbedding = new float[embeddingDimension]; // 临时占位符
            
            // 2. 构建Milvus搜索请求
            SearchParam.Builder builder = SearchParam.newBuilder()
                    .withCollectionName(milvusCollectionName)
                    .withMetricType(MetricType.IP)
                    .withTopK(searchRequest.getTopN())
                    .withVectorFieldName("embedding")
                    .withVectors(List.of(queryEmbedding))
                    .withOutputFields(List.of("docId", "sliceId", "cleanedContent", "chapterId", "paragraphId", "tenantId"))
                    .withExpr(String.format("tenantId == '%s'", searchRequest.getTenantId()));
            
            // 3. 执行搜索
            R<SearchResults> searchResponse = milvusClient.search(builder.build());
            
            // 4. 处理搜索结果
            if (searchResponse.getData() != null && searchResponse.getData().getResults() != null) {
                SearchResultData results = searchResponse.getData().getResults();
                for (int i = 0; i < results.getScoresCount(); i++) {
                    float score = results.getScores(i);
                    SearchResult.SearchHit searchHit = new SearchResult.SearchHit();
                    
                    // 提取字段
                    List<FieldData> fields = results.getFieldsDataList();
                    for (FieldData field : fields) {
                        if (field.getFieldName().equals("docId")) {
                            searchHit.setDocId(field.getScalars().getBinaryDataList().get(i).toStringUtf8());
                        } else if (field.getFieldName().equals("sliceId")) {
                            searchHit.setSliceId(field.getScalars().getBinaryDataList().get(i).toStringUtf8());
                        } else if (field.getFieldName().equals("cleanedContent")) {
                            searchHit.setContent(field.getScalars().getBinaryDataList().get(i).toStringUtf8());
                        } else if (field.getFieldName().equals("chapterId")) {
                            searchHit.setChapter(field.getScalars().getBinaryDataList().get(i).toStringUtf8());
                        } else if (field.getFieldName().equals("paragraphId")) {
                            searchHit.setParagraphIndex(Integer.parseInt(field.getScalars().getBinaryDataList().get(i).toStringUtf8()));
                        }
                    }
                    
                    searchHit.setScore(score);
                    searchHit.setSource("MILVUS");
                    searchHit.setSimilarity(score);
                    hits.add(searchHit);
                }
            }
            
            result.setHits(hits);
            result.setTotalHits((long) hits.size());
            
        } catch (Exception e) {
            logger.error("Milvus search failed: {}", e.getMessage(), e);
            // 如果Milvus不可用，返回空结果
        }
        
        result.setSearchTime(System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 融合搜索结果
     * @param esResult ES搜索结果
     * @param milvusResult Milvus搜索结果
     * @param fuseMethod 融合方法
     * @return 融合后的搜索结果
     */
    @Override
    public SearchResult fuseSearchResults(SearchResult esResult, SearchResult milvusResult, String fuseMethod) {
        long startTime = System.currentTimeMillis();
        SearchResult fusedResult = new SearchResult();
        List<SearchResult.SearchHit> fusedHits = new ArrayList<>();

        // TODO: 实现结果融合逻辑
        // 支持RRF、线性加权、学习排序等融合方法
        // 默认使用RRF（Reciprocal Rank Fusion）

        // 这里先简单合并结果，后续需要实现更复杂的融合算法
        if (esResult != null && esResult.getHits() != null) {
            fusedHits.addAll(esResult.getHits());
        }
        if (milvusResult != null && milvusResult.getHits() != null) {
            fusedHits.addAll(milvusResult.getHits());
        }

        // 模拟融合排序，这里需要实现真正的RRF或其他融合算法
        // 按相似度降序排序
        fusedHits.sort((a, b) -> Float.compare(b.getSimilarity(), a.getSimilarity()));

        // 取top5结果
        List<SearchResult.SearchHit> topHits = fusedHits.size() > 5 ? fusedHits.subList(0, 5) : fusedHits;
        for (int i = 0; i < topHits.size(); i++) {
            topHits.get(i).setRank(i + 1);
            topHits.get(i).setSource("FUSED");
        }

        fusedResult.setHits(topHits);
        fusedResult.setTotalHits((long) fusedHits.size());
        fusedResult.setFuseTime(System.currentTimeMillis() - startTime);

        return fusedResult;
    }

    /**
     * 优化查询词
     * @param originalQuery 原始查询词
     * @return 优化后的查询词
     */
    @Override
    public String optimizeQuery(String originalQuery) {
        SearchResult.QueryRewriteResult rewriteResult = optimizeQueryWithReason(originalQuery);
        return rewriteResult.getOptimizedQuery();
    }

    /**
     * 优化查询词并返回理由
     * @param originalQuery 原始查询词
     * @return 优化后的查询词和理由
     */
    @Override
    public SearchResult.QueryRewriteResult optimizeQueryWithReason(String originalQuery) {
        SearchResult.QueryRewriteResult result = new SearchResult.QueryRewriteResult();
        result.setOriginalQuery(originalQuery);

        try {
            // 构建提示词模板
            String prompt = buildQueryRewritePrompt(originalQuery);
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new HumanMessage(prompt));

            // 调用大模型进行查询重写
            String aiResponse = aiChatHandler.completions(DEFAULT_MODEL_ID, messages);
            logger.info("Query rewrite AI response: {}", aiResponse);

            // 解析AI响应
            parseQueryRewriteResponse(aiResponse, result);

            // 如果AI没有返回优化后的查询词，使用原始查询词
            if (result.getOptimizedQuery() == null || result.getOptimizedQuery().isEmpty()) {
                result.setOptimizedQuery(originalQuery);
                result.setReason("大模型未返回有效优化结果，使用原始查询词");
            }
        } catch (Exception e) {
            logger.error("Query rewrite failed: {}", e.getMessage(), e);
            result.setOptimizedQuery(originalQuery);
            result.setReason("查询词优化失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 构建查询重写提示词模板
     * @param originalQuery 原始查询词
     * @return 提示词
     */
    private String buildQueryRewritePrompt(String originalQuery) {
        return "你是一个专业的查询重写助手，请根据用户的原始查询，生成一个更适合检索的优化查询词。\n" +
                "要求：\n" +
                "1. 纠正错别字和语法错误\n" +
                "2. 扩展同义词和相关术语\n" +
                "3. 识别查询意图和关键约束\n" +
                "4. 处理否定词和歧义\n" +
                "5. 输出结构化的JSON格式，包含以下字段：\n" +
                "   - optimizedQuery: 优化后的查询词\n" +
                "   - reason: 优化理由\n" +
                "   - intent: 查询意图\n" +
                "   - mustInclude: 必须包含的关键词列表\n" +
                "   - mustExclude: 必须排除的关键词列表\n" +
                "原始查询：" + originalQuery;
    }

    /**
     * 解析查询重写响应
     * @param aiResponse AI响应
     * @param result 重写结果对象
     */
    private void parseQueryRewriteResponse(String aiResponse, SearchResult.QueryRewriteResult result) {
        try {
            // 提取JSON部分
            int jsonStart = aiResponse.indexOf("{");
            int jsonEnd = aiResponse.lastIndexOf("}") + 1;
            if (jsonStart != -1 && jsonEnd != -1) {
                String jsonStr = aiResponse.substring(jsonStart, jsonEnd);
                SearchResult.QueryRewriteResult parsedResult = JSON.parseObject(jsonStr, SearchResult.QueryRewriteResult.class);
                if (parsedResult != null) {
                    result.setOptimizedQuery(parsedResult.getOptimizedQuery());
                    result.setReason(parsedResult.getReason());
                    result.setIntent(parsedResult.getIntent());
                    result.setMustInclude(parsedResult.getMustInclude());
                    result.setMustExclude(parsedResult.getMustExclude());
                }
            } else {
                // 如果没有JSON格式，直接将响应作为优化后的查询词
                result.setOptimizedQuery(aiResponse.trim());
                result.setReason("大模型返回非JSON格式响应，直接使用作为优化查询词");
            }
        } catch (Exception e) {
            logger.error("Failed to parse query rewrite response: {}", e.getMessage(), e);
            // 如果解析失败，将原始响应作为优化后的查询词
            result.setOptimizedQuery(aiResponse.trim());
            result.setReason("解析大模型响应失败: " + e.getMessage());
        }
    }
}