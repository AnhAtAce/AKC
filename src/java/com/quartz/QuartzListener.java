/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.quartz;

/**
 *
 * @author AN2
 */
import com.quartz.AmzReports.AmazonGetReportsList;
import com.quartz.AmzReports.AmazonRequestQtyReport;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzListener implements ServletContextListener {

    Scheduler scheduler = null, yStoreScheduler = null, AMZScheduler = null,
            AMZQtyReportScheduler = null, AmzReportListScheduler = null,
            amzQtyUpdateScheduler = null;
    JobDetail job = null, yJob = null, amzJob = null, amzQtyReportJob = null,
            amzReportsListJob = null, amzQtyUpdateJob;

    @Override
    public void contextInitialized(ServletContextEvent servletContext) {
        System.out.println("Context Initialized");

        try {
            // Setup the Job class and the Job group
            job = newJob(ListrakQuartzJob.class).withIdentity(
                    "CronQuartzJob", "Group").build();
            yJob = newJob(YahooStoreQuartzJob.class).withIdentity(
                    "YahooStoreQuartzJob", "Group").build();
            amzJob = newJob(AmazonQuartzJob.class).withIdentity(
                    "AmazonQuartzJob", "Group").build();

            // Create a Trigger that fires every 5 minutes.
            Trigger trigger = newTrigger()
                    .withIdentity("TriggerName", "Group")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * 1/1 * ? *"))
                    .build();
            Trigger yStoreTrigger = newTrigger()
                    .withIdentity("TriggerName1", "Group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * 1/1 * ? *"))
                    .build();
            Trigger amzShippingConfirmTrigger = newTrigger()
                    .withIdentity("TriggerName2", "Group2")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * 1/1 * ? *"))
                    .build();

            // Setup the Job and Trigger with Scheduler & schedule jobs
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);

            yStoreScheduler = new StdSchedulerFactory().getScheduler();
            yStoreScheduler.start();
            yStoreScheduler.scheduleJob(yJob, yStoreTrigger);

            AMZScheduler = new StdSchedulerFactory().getScheduler();
            AMZScheduler.start();
            AMZScheduler.scheduleJob(amzJob, amzShippingConfirmTrigger);
            
            
            amzQtyReportJob = newJob(AmazonRequestQtyReport.class).withIdentity(
                    "amzQtyReportJob", "Group2").build();
            Trigger amzQtyReportTrigger = newTrigger()
                    .withIdentity("TriggerName3", "Group3")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * 1/1 * ? *"))
                    //.withSchedule(CronScheduleBuilder.cronSchedule("0 0 8-20 ? * *"))
                    .build();
            AMZQtyReportScheduler = new StdSchedulerFactory().getScheduler();
            AMZQtyReportScheduler.start();
            AMZQtyReportScheduler.scheduleJob(amzQtyReportJob, amzQtyReportTrigger);

            amzReportsListJob = newJob(AmazonGetReportsList.class).withIdentity(
                    "amzReportsListJob", "Group2").build();
            Trigger amzReportsListTrigger = newTrigger()
                    .withIdentity("TriggerName4", "Group3")
                    //.withSchedule(CronScheduleBuilder.cronSchedule("0 1 8-20 1/1 * ? *"))
                    .withSchedule(CronScheduleBuilder.cronSchedule("10 0/5 * 1/1 * ? *"))
                    .build();
            AmzReportListScheduler = new StdSchedulerFactory().getScheduler();
            AmzReportListScheduler.start();
            AmzReportListScheduler.scheduleJob(amzReportsListJob, amzReportsListTrigger);

            amzQtyUpdateJob = newJob(AmazonUpdateQtyJob.class).withIdentity(
                    "amzQtyUpdateJob", "Group3").build();
            Trigger amzQtyUpdateTrigger = newTrigger()
                    .withIdentity("TriggerName5", "Group3")
                    //.withSchedule(CronScheduleBuilder.cronSchedule("30 0/1 * 1/1 * ? *"))
                    .withSchedule(CronScheduleBuilder.cronSchedule("20 0/5 8-20 ? * * *"))
                    .build();
            amzQtyUpdateScheduler = new StdSchedulerFactory().getScheduler();
            amzQtyUpdateScheduler.start();
            amzQtyUpdateScheduler.scheduleJob(amzQtyUpdateJob, amzQtyUpdateTrigger);

        } catch (SchedulerException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContext) {
        System.out.println("Context Destroyed");
        try {
            scheduler.shutdown(true);
            yStoreScheduler.shutdown(true);
            AMZScheduler.shutdown(true);
            AMZQtyReportScheduler.shutdown(true);
                    
            int ct = 0;

            // Try waiting for the scheduler to shutdown. Only wait max of 30 seconds.
            while (ct < 30) {
                ct++;
                try {
                    // Sleep for a second so the quartz worker threads die.  This
                    // suppresses a warning from Tomcat during shutdown.
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    System.out.println(ex.getMessage());
                }
                if (scheduler.isShutdown() && yStoreScheduler.isShutdown() &&
                        AMZScheduler.isShutdown() && AMZQtyReportScheduler.isShutdown() &&
                        AmzReportListScheduler.isShutdown()
                        ) {
                    break;
                }
            }
        } catch (SchedulerException e) {
            System.out.println(e.getMessage());
        }
    }
}
