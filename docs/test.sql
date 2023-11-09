-- zzy_transaction.order_tab definition

CREATE TABLE `order_tab` (
                             `id` bigint DEFAULT NULL,
                             `order_key` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- zzy_transaction.point_tab definition

CREATE TABLE `point_tab` (
                             `uid` bigint NOT NULL,
                             `point` bigint DEFAULT NULL,
                             PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- zzy_transaction.product definition

CREATE TABLE `product` (
                           `id` bigint DEFAULT NULL,
                           `stock` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO zzy_transaction.product (id,stock) VALUES
(1,10000);


