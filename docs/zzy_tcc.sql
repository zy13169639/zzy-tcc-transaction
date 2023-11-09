-- zzy_transaction.idempotent_log definition

CREATE TABLE `idempotent_log` (
                                  `id` bigint NOT NULL,
                                  `request_key` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                  `application` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                  `create_time` datetime DEFAULT NULL,
                                  `module` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属模块',
                                  `transaction_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分布式事务id',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `idempotent_log_un` (`module`,`request_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- zzy_transaction.recovery_log definition

CREATE TABLE `recovery_log` (
                                `log_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                `create_time` timestamp NULL DEFAULT NULL,
                                `application` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标识服务',
                                `status` tinyint DEFAULT NULL,
                                UNIQUE KEY `recovery_log_un` (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- zzy_transaction.transaction_log definition

CREATE TABLE `transaction_log` (
                                   `id` bigint NOT NULL,
                                   `root_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                   `type` tinyint DEFAULT NULL COMMENT '1：主事务（事务发起者），2：分支事务',
                                   `bus_complete_time` bigint DEFAULT NULL COMMENT '业务完成时间,用来回滚时，校验是否业务数据的修改时间在这个时间之后，之后的话就表示在这期间业务数据被更新',
                                   `status` tinyint DEFAULT NULL COMMENT '事务状态（1：创建成功，2：业务执行成功，3：已确认，4：已取消）',
                                   `branch_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                   `meta` text COLLATE utf8mb4_unicode_ci,
                                   `origin` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事务发起者的application_name',
                                   `global_status` tinyint DEFAULT NULL COMMENT '全局事务状态（0：已确认，1：已取消）',
                                   `application` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事务执行者的application_name',
                                   `create_time` bigint DEFAULT NULL COMMENT '创建时间',
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
