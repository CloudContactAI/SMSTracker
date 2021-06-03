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

package com.cloudcontactai.smstracker.exception

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.task.AsyncTaskExecutor

import java.util.concurrent.Callable
import java.util.concurrent.Future

class ExceptionHandlingAsyncTaskExecutor(private val executor: AsyncTaskExecutor) : AsyncTaskExecutor, InitializingBean, DisposableBean {
    private val log = LoggerFactory.getLogger(ExceptionHandlingAsyncTaskExecutor::class.java)

    override fun execute(task: Runnable) {
        this.executor.execute(this.createWrappedRunnable(task))
    }

    override fun execute(task: Runnable, startTimeout: Long) {
        this.executor.execute(this.createWrappedRunnable(task), startTimeout)
    }

    private fun <T> createCallable(task: Callable<T>): Callable<T> {
        return Callable<T>{
            try {
                task.call()
            } catch (var3: Exception) {
                this.handle(var3)
                throw var3
            }
        }
    }

    private fun createWrappedRunnable(task: Runnable): Runnable {
        return Runnable{
            try {
                task.run()
            } catch (var3: Exception) {
                this.handle(var3)
            }
        }
    }

    protected fun handle(e: Exception) {
        this.log.error("Caught async exception", e)
    }

    override fun submit(task: Runnable): Future<*> {
        return this.executor.submit(this.createWrappedRunnable(task))
    }

    override fun <T> submit(task: Callable<T>): Future<T> {
        return this.executor.submit(this.createCallable(task))
    }

    @Throws(Exception::class)
    override fun destroy() {
        if (this.executor is DisposableBean) {
            val bean = this.executor as DisposableBean
            bean.destroy()
        }

    }

    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        if (this.executor is InitializingBean) {
            val bean = this.executor as InitializingBean
            bean.afterPropertiesSet()
        }

    }

    companion object {
        internal val EXCEPTION_MESSAGE = "Caught async exception"
    }
}
