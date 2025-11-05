package org.jeecg.modules.contract.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.contract.domain.ContractFile;

/**
 * 合同文件Mapper接口
 * @author: jeecg-boot
 * @date: 2024-05-20
 */
@Mapper
public interface ContractFileMapper extends BaseMapper<ContractFile> {

}
