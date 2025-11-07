package org.jeecg.modules.airag.docsearch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.airag.docsearch.entity.Slice;
import org.jeecg.modules.airag.docsearch.entity.SliceConfig;

import java.util.List;

/**
 * @Description: 文档切片服务接口
 * @Author: AIrag
 * @Date: 2024-05-20
 * @Version: V1.0
 */
public interface SliceService extends IService<Slice> {

    /**
     * 对文档进行切片
     * @param docId 文档唯一标识
     * @param sliceConfig 切片配置
     * @return 切片列表
     */
    List<Slice> sliceDocument(String docId, SliceConfig sliceConfig);

    /**
     * 根据文档ID获取所有切片
     * @param docId 文档唯一标识
     * @return 切片列表
     */
    List<Slice> getSlicesByDocId(String docId);

    /**
     * 根据切片ID获取切片信息
     * @param sliceId 切片唯一标识
     * @return 切片信息
     */
    Slice getSliceBySliceId(String sliceId);

    /**
     * 批量获取切片信息
     * @param sliceIds 切片唯一标识列表
     * @return 切片列表
     */
    List<Slice> getSlicesBySliceIds(List<String> sliceIds);

    /**
     * 删除文档的所有切片
     * @param docId 文档唯一标识
     * @return 删除成功的数量
     */
    int deleteSlicesByDocId(String docId);

    /**
     * 批量删除切片
     * @param sliceIds 切片唯一标识列表
     * @return 删除成功的数量
     */
    int batchDeleteSlices(List<String> sliceIds);

    /**
     * 验证切片质量
     * @param slices 切片列表
     * @param sliceConfig 切片配置
     * @return 验证结果
     */
    boolean validateSlices(List<Slice> slices, SliceConfig sliceConfig);
}
