//import static org.junit.jupiter.api.Assertions.*;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//import org.jeecg.modules.srs.inspector.entity.Endpoint;
//import org.jeecg.modules.srs.inspector.service.impl.CallChainServiceImpl;
//import org.mockito.Mockito;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.List;
//
//class CallChainServiceImplTest {
//    @Test
//    void scanAll_ShouldGenerateCsvFile(@TempDir Path tempDir) throws IOException {
//        // 准备测试数据
//        List<Endpoint> mockEndpoints = new ArrayList<>();
//        mockEndpoints.add(new Endpoint(1, "/api/users", "GET", "UserController", "getUserList", "获取用户列表"));
//        mockEndpoints.add(new Endpoint(2, "/api/users/{id}", "GET", "UserController", "getUserById", "根据ID获取用户"));
//
//        // 模拟依赖
//        CallChainServiceImpl service = Mockito.spy(new CallChainServiceImpl());
//        Mockito.doReturn(mockEndpoints).when(service).scanAllEndpoint();
//
//        // 执行测试
//        service.scanAll();
//
//        // 验证CSV文件是否生成
//        Path csvFile = tempDir.resolve("controllers_").toAbsolutePath();
//        assertTrue(java.nio.file.Files.exists(csvFile), "CSV文件未生成");
//    }
//}