/* Copyright 2021 Cloud Contact AI, Inc. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.cloudcontactai.smstracker.config

import com.cloudcontactai.smstracker.exception.ExceptionHandlingAsyncTaskExecutor
import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Configuration
@EnableAsync
class AsyncConfiguration : AsyncConfigurer, SchedulingConfigurer {
    /**
     * gets log to manage error
     */
    private val log = LoggerFactory.getLogger(AsyncConfiguration::class.java)

    @Value("\${server.async.core-pool-size}")
    private val corePoolSize: Int? = null

    @Value("\${server.async.max-pool-size}")
    private val maxPoolSize: Int? = null

    @Value("\${server.async.queue-capacity}")
    private val queueCapacity: Int? = null

    /**
     *
     * @return
     */
    @Bean(name = ["taskExecutor"])
    override fun getAsyncExecutor(): Executor? {
        log.debug("Creating Async Task Executor")
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = corePoolSize!!
        executor.maxPoolSize = maxPoolSize!!
        executor.setQueueCapacity(queueCapacity!!)
        executor.setThreadNamePrefix("ccai-sms-tracker-")
        return ExceptionHandlingAsyncTaskExecutor(executor)
    }

    /**
     * get async uncaught exception handler
     * @return
     */
    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler? {
        return SimpleAsyncUncaughtExceptionHandler()
    }

    /**
     * configures the tasks
     * @param taskRegistrar schedule
     */
    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        taskRegistrar.setScheduler(scheduledTaskExecutor())
    }

    /**
     * gets scheduled task executor
     * @return
     */
    @Bean
    fun scheduledTaskExecutor(): Executor {
        return Executors.newScheduledThreadPool(corePoolSize!!)
    }
}