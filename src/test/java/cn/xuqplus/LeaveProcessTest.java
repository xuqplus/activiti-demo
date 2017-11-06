package cn.xuqplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
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

  @Test
  public void testUserTask() throws Exception {
    //创建流程引擎(使用内存数据库)
    ProcessEngine processEngine = ProcessEngineConfiguration
        .createStandaloneInMemProcessEngineConfiguration()
        .buildProcessEngine();
    //部署流程定义文件
    RepositoryService repositoryService = processEngine.getRepositoryService();
    repositoryService
        .createDeployment()
        .addClasspathResource("leave1.bpmn")
        .deploy();
    //验证已部署的流程
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .singleResult();
    Assert.assertEquals("sayByeToLeave", processDefinition.getKey());

    //启动流程并返回流程实例
    RuntimeService runtimeService = processEngine.getRuntimeService();
    Map<String, Object> variables = new HashMap(8);
    variables.put("applyUser", "emp1");
    variables.put("days", "16");

    //创建实例
    ProcessInstance processInstance = runtimeService
        .startProcessInstanceByKey("sayByeToLeave", variables);
    Assert.assertNotNull(processInstance);

    TaskService taskService = processEngine.getTaskService();
    Task taskOfDeptLeader = taskService.createTaskQuery()
        .taskCandidateGroupIn(new ArrayList<String>() {{
          add("deptLeader");
        }}).singleResult();
    Assert.assertNotNull(taskOfDeptLeader);
    Assert.assertEquals("上级审批", taskOfDeptLeader.getName());

    //完成任务
    variables = new HashMap(8);
    variables.put("approved", true);
    taskService.complete(taskOfDeptLeader.getId(), variables);

    //查询任务
    taskOfDeptLeader = taskService.createTaskQuery()
        .taskCandidateGroupIn(new ArrayList<String>() {{
          add("deptLeader");
        }}).singleResult();
    Assert.assertNull(taskOfDeptLeader);

    //历史任务
    HistoryService historyService = processEngine.getHistoryService();
    long count = historyService.createHistoricProcessInstanceQuery().finished().count();
    Assert.assertEquals(1, count);
  }
}
