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

import com.cloudcontactai.smstracker.model.message.UpdateSmsStatusMessage
import com.cloudcontactai.smstracker.model.types.MessageStatus
import com.cloudcontactai.smstracker.repository.CampaignMessageRepository
import com.cloudcontactai.smstracker.util.TwilioMessageStatus.DELIVERED
import com.cloudcontactai.smstracker.util.TwilioMessageStatus.FAILED
import com.cloudcontactai.smstracker.util.TwilioMessageStatus.UNDELIVERED
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CampaignMessageService {
    @Autowired
    lateinit var repository: CampaignMessageRepository

    @Autowired
    lateinit var twilioErrorService: TwilioErrorService

    companion object{
        private val logger = LoggerFactory.getLogger(CampaignMessageService::class.java)
    }

    fun updateMessageStatus(result: UpdateSmsStatusMessage) {
        logger.debug("Received request: ${result.errorCode} => ${result.errorMessage}")
        val message = if (result.id != null) {
            repository.findById(result.id!!).orElseThrow {
                Exception("Couldn't find message with ID ${result.id}")
            }
        } else{
            repository.findByTwilioId(result.sid!!).orElseThrow {
                Exception("Couldn't find message with SID ${result.sid}")
            }
        }
        if (result.status == DELIVERED) {
            message.status = MessageStatus.SENT
        } else if (result.status == UNDELIVERED || result.status == FAILED) {
            message.status = MessageStatus.ERROR
            message.twilioErrorCode = result.errorCode
            message.twilioErrorMessage = if (result.errorMessage.isNullOrEmpty()) {
                twilioErrorService.getError(result.errorCode)
            } else {
                result.errorMessage
            }
        } else {
            return
        }
        repository.save(message)
    }
}