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

package com.cloudcontactai.smstracker.controller

import com.cloudcontactai.smstracker.service.SmsTrackerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping("/api/v1/sms/tracker")
class SmsTrackerController {
    @Autowired
    lateinit var smsTrackerService: SmsTrackerService

    @PostMapping("", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = ["text/html"])
    fun trackSms(@RequestParam body: Map<String, String>): String {
        smsTrackerService.handleSmsTrack(body)
        return "<Response></Response>"
    }
}