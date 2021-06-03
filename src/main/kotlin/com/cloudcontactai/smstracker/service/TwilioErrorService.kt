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

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class TwilioErrorService {
    private var twilioErrors: Map<String, String>? = null

    companion object{
        private val logger = LoggerFactory.getLogger(TwilioErrorService::class.java)
    }

    init {
        twilioErrors = getTwilioErrors()
    }

    fun getError(errorCode: String?): String? {
        errorCode?.let { code ->
            return twilioErrors?.get(code)
        }
        return null
    }

    private fun getTwilioErrors(): HashMap<String, String>? {
        try {
            val errorFile = File("twilio", "errors.json")
            if (errorFile.exists()) {
                return ObjectMapper().readValue(errorFile, Map::class.java) as HashMap<String, String>?
            }
            logger.info("No errors file found")
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error("Error reading errors file: ${e.message}")
        }
        return null
    }
}