package cn.xuqplus;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.junit.Test;

public class LeaveProcessTest {

  @Test
  public void testStartProcess() throws Exception {
    //创建流程引擎(使用内存数据库)
    ProcessEngine processEngine = ProcessEngineConfiguration
        .createStandaloneInMemProcessEngineConfiguration()
        .buildProcessEngine();
    //部署流程定义文件
    RepositoryService repositoryService = processEngine.getRepositoryService();
    repositoryService
        .createDeployment()
        .addClasspathResource("leave.bpmn")
        .deploy();
    //验证已部署的流程
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .singleResult();
    Assert.assertEquals("hello-id", processDefinition.getKey());
    //启动流程并返回流程实例
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ProcessInstance processInstance = runtimeService
        .startProcessInstanceByKey("hello-id");
    Assert.assertNotNull(processInstance);
  }

}
