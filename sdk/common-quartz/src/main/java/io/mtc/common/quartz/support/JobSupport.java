package io.mtc.common.quartz.support;

import com.alibaba.druid.pool.DruidDataSource;
import io.mtc.common.constants.Constants;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

/**
 * 支持类
 *
 * @author Chinhin
 * 2018/6/23
 */
@Component
public class JobSupport {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment env;

    private Properties quartzProperties() {
        Properties properties = null;
        try {
            properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("quartz.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private DataSource getDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(env.getProperty("spring.job-datasource.url"));
        dataSource.setUsername(env.getProperty("spring.job-datasource.username"));
        dataSource.setPassword(env.getProperty("spring.job-datasource.password"));
        dataSource.setDriverClassName(env.getProperty("spring.job-datasource.driver-class-name"));
        return dataSource;
    }

    public SchedulerFactoryBean makeScheduler(String name, Class<? extends Job> jobClass, String cron) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext)applicationContext;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)context.getBeanFactory();

        // job
        String jobName = "job_" + name;

        BeanDefinitionBuilder jobDetailBuilder = BeanDefinitionBuilder.rootBeanDefinition(JobDetailFactoryBean.class);
        jobDetailBuilder.addPropertyValue("jobClass", jobClass);
        jobDetailBuilder.addPropertyValue("durability", true);
        jobDetailBuilder.addPropertyValue("description", name + "'s job");
        beanFactory.registerBeanDefinition(jobName, jobDetailBuilder.getBeanDefinition());

        // trigger
        String triggerName = "cronTrigger_" + name;
        JobDetailImpl jobDetailFactory = applicationContext.getBean(jobName, JobDetailImpl.class);

        BeanDefinitionBuilder triggerBuilder = BeanDefinitionBuilder.rootBeanDefinition(CronTriggerFactoryBean.class);
        triggerBuilder.addPropertyValue("jobDetail", jobDetailFactory);
        triggerBuilder.addPropertyValue("cronExpression", cron);
        triggerBuilder.addPropertyValue("description", name + "'s trigger");
        beanFactory.registerBeanDefinition(triggerName, triggerBuilder.getBeanDefinition());

        // Schedule
        CronTriggerImpl cronTriggerFactoryBean = applicationContext.getBean(triggerName, CronTriggerImpl.class);

        SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();
        quartzScheduler.setQuartzProperties(quartzProperties());
        quartzScheduler.setDataSource(getDataSource());
        quartzScheduler.setOverwriteExistingJobs(true);
        quartzScheduler.setApplicationContextSchedulerContextKey(Constants.SPRING_CONTEXT);
        quartzScheduler.setBeanName(name);

        quartzScheduler.setTriggers(cronTriggerFactoryBean);
        return quartzScheduler;
    }

    public static ApplicationContext getContext(JobExecutionContext jobExecutionContext) {
        try {
            return (ApplicationContext) jobExecutionContext.getScheduler().getContext()
                    .get(Constants.SPRING_CONTEXT);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return null;
        }
    }

}
