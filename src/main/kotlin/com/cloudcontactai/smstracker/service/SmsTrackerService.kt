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

package com.cloudcontactai.smstracker.service

import com.cloudcontactai.smstracker.model.SmsTracking
import com.cloudcontactai.smstracker.model.message.UpdateSmsStatusMessage
import com.cloudcontactai.smstracker.repository.SmsTrackingRepository
import com.cloudcontactai.smstracker.util.TwilioMessageStatus
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.core.JmsTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class SmsTrackerService {
    @Autowired
    lateinit var repository: SmsTrackingRepository

    @Autowired
    lateinit var jmsTemplate: JmsTemplate

    @Value("\${cloudcontactai.queue.updateSmsStatus}")
    lateinit var updateSmsStatusQueue: String

    companion object{
        private val logger = LoggerFactory.getLogger(SmsTrackerService::class.java)
    }

    @Async("taskExecutor")
    fun handleSmsTrack(body: Map<String, String>) {
        logger.info("Receive message: $body")
        saveRecord(body)
        sendUpdateMessage(body)
    }

    fun sendUpdateMessage(body: Map<String, String>) {
        try {
            val status = body["MessageStatus"]!!.toLowerCase()
            if (TwilioMessageStatus.FINALS.contains(status)) {
                logger.info("Processing SMS status: $status")
                val message = UpdateSmsStatusMessage(
                    sid = body["SmsSid"]!!,
                    status = status,
                    errorCode = body["ErrorCode"],
                    errorMessage = body["ErrorMessage"]
                )
                jmsTemplate.convertAndSend(updateSmsStatusQueue, ObjectMapper().writeValueAsString(message))
            } else {
                logger.info("Ignoring SMS status: $status")
            }
        } catch (e: Exception) {
            logger.error("Error handling sms status update: ${e.message}")
            e.printStackTrace()
        }
    }

    fun saveRecord(body: Map<String, String>) {
        val trackRecord = SmsTracking().apply {
            bodyRequest = body
        }
        repository.save(trackRecord)
        logger.info("Record saved")
    }
}