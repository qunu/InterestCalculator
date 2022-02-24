package com.qunu.interest.config;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Profile("!test")
public class AsyncConfig {
}
