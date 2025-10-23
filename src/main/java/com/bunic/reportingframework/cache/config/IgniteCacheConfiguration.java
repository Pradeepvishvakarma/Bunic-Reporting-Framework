//package com.bunic.reportingframework.cache.config;
//
//import org.apache.ignite.Ignite;
//import org.apache.ignite.Ignition;
//import org.apache.ignite.configuration.IgniteConfiguration;
//import org.apache.ignite.cache.CacheMode;
//import org.apache.ignite.configuration.CacheConfiguration;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.cache.annotation.CachingConfigurerSupport;
//import org.springframework.cache.CacheManager;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.apache.ignite.springdata22.repository.config.EnableIgniteRepositories;
//import org.apache.ignite.spring.cache.IgniteCacheManager;
//
//@Configuration
//@EnableCaching
//@EnableIgniteRepositories
//public class IgniteCacheConfiguration extends CachingConfigurerSupport {
//
//    @Bean
//    public Ignite igniteInstance() {
//        IgniteConfiguration cfg = new IgniteConfiguration();
//        cfg.setIgniteInstanceName("springIgniteApp");
//        cfg.setPeerClassLoadingEnabled(true);
//
//        // Example cache configuration
//        CacheConfiguration<String, Object> cacheCfg = new CacheConfiguration<>("userCache");
//        cacheCfg.setCacheMode(CacheMode.PARTITIONED); // DISTRIBUTED
//        cacheCfg.setBackups(1); // 1 backup copy
//        cfg.setCacheConfiguration(cacheCfg);
//
//        return Ignition.start(cfg);
//    }
//
//    @Bean
//    @Override
//    public CacheManager cacheManager() {
//        return new IgniteCacheManager(igniteInstance());
//    }
//}
